package api.excel.writer.tab;

import api.dto.RequestParamsSupplier;
import api.excel.writer.SMDataWriter;
import api.excel.writer.WritableSpreadsheet;
import api.util.CommonTools;
import api.util.excel.ExcelUtils;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.repository.graphql.query.ManagementFeeSMRepository;
import com.fintex.ce.util.CollectorUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fintex.ce.util.FilterUtils.*;

public class ManagementFeeFDSTab extends SMDataWriter<Holding, String, BigDecimal> implements WritableSpreadsheet {
	private static final ManagementFeeSMRepository ASSET_ALLOCATION_FDS = initAssetAllocationsFDS();
	private static final String TAB_NAME = "Master_Sheet";

	private static ManagementFeeSMRepository initAssetAllocationsFDS() {
		return new ManagementFeeSMRepository(CommonTools.GRAPHQL_TRANSPORT_COMPONENT);
	}

	@Override
	public void write(List<Holding> holdings, RequestParamsSupplier params, Workbook workbook) {
		final Sheet sheet = Objects.requireNonNull(Objects.requireNonNull(workbook.getSheet(TAB_NAME)));

		final Map<Holding, AverageManagementExpenseCalculationDTO> assetAllocationsMer = callFds(holdings,
				params.getDataProviders());

		fillSpreadSheetForMer(assetAllocationsMer, sheet);
	}

	public void fillSpreadSheetForMer(final Map<Holding, AverageManagementExpenseCalculationDTO> map,
			final Sheet sheet) {
		ExcelUtils.clearSheet(sheet, CellRangeAddress.valueOf("B15:J15"));

		var rbf605 = map.entrySet().stream()
				.filter(e -> e.getKey() instanceof FundSeriesHolding
						&& ((FundSeriesHolding) e.getKey()).getFundServCode().equalsIgnoreCase("RBF605"))
				.findFirst().orElseThrow().getValue();
		sheet.getRow(14).createCell(1).setCellValue(rbf605.getActualManagementFee().doubleValue());

		var xbal = map.entrySet().stream()
				.filter(e -> e.getKey() instanceof EtfHolding
						&& ((EtfHolding) e.getKey()).getTicker().equalsIgnoreCase("XBAL"))
				.findFirst().orElseThrow().getValue();
		sheet.getRow(14).createCell(2).setCellValue(xbal.getActualManagementFee().doubleValue());

		var aom = map.entrySet().stream()
				.filter(e -> e.getKey() instanceof EtfHolding
						&& ((EtfHolding) e.getKey()).getTicker().equalsIgnoreCase("AOM"))
				.findFirst().orElseThrow().getValue();
		sheet.getRow(14).createCell(3).setCellValue(aom.getActualManagementFee().doubleValue());

		var fhusa04gs5 = map.entrySet().stream()
				.filter(e -> e.getKey() instanceof CanadaHedgeFundHolding
						&& ((CanadaHedgeFundHolding) e.getKey()).getMorningstarId().equalsIgnoreCase("FHUSA04GS5"))
				.findFirst().orElseThrow().getValue();
		sheet.getRow(14).createCell(8).setCellValue(fhusa04gs5.getActualManagementFee().doubleValue());

		var vcfax = map.entrySet().stream()
				.filter(e -> e.getKey() instanceof UsMutualFundHolding
						&& ((UsMutualFundHolding) e.getKey()).getTicker().equalsIgnoreCase("VCFAX"))
				.findFirst().orElseThrow().getValue();
		sheet.getRow(14).createCell(9).setCellValue(vcfax.getActualManagementFee().doubleValue());
	}

	private Map<Holding, AverageManagementExpenseCalculationDTO> callFds(final List<Holding> holdings,
			final List<DataProvider> dataProviders) {
		final Map<Holding, AverageManagementExpenseCalculationDTO> map = new LinkedHashMap<>();

		map.putAll(ASSET_ALLOCATION_FDS.queryBenchOfOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), dataProviders)
				.entrySet().stream()
				.collect(CollectorUtils.toMap(e -> e.getKey(), e -> new AverageManagementExpenseCalculationDTO()
						.setActualManagementFee(e.getValue().getManagementFee()))));

		map.putAll(ASSET_ALLOCATION_FDS
				.queryBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), dataProviders).entrySet()
				.stream()
				.collect(CollectorUtils.toMap(e -> e.getKey(), e -> new AverageManagementExpenseCalculationDTO()
						.setActualManagementFee(e.getValue().getManagementFee()))));

		map.putAll(ASSET_ALLOCATION_FDS
				.queryBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), dataProviders).entrySet()
				.stream()
				.collect(CollectorUtils.toMap(e -> e.getKey(), e -> new AverageManagementExpenseCalculationDTO()
						.setActualManagementFee(e.getValue().getManagementFee()))));

		map.putAll(ASSET_ALLOCATION_FDS.queryCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), dataProviders)
				.entrySet().stream()
				.collect(CollectorUtils.toMap(e -> e.getKey(), e -> new AverageManagementExpenseCalculationDTO()
						.setActualManagementFee(e.getValue().getManagementFee()))));

		map.putAll(ASSET_ALLOCATION_FDS.queryUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), dataProviders)
				.entrySet().stream()
				.collect(CollectorUtils.toMap(e -> e.getKey(), e -> new AverageManagementExpenseCalculationDTO()
						.setActualManagementFee(e.getValue().getManagementFee()))));
		return map;
	}

}