package api.excel.writer.tab;

import api.dto.RequestParamsSupplier;
import api.excel.writer.SMDataWriter;
import api.excel.writer.WritableSpreadsheet;
import api.util.CommonTools;
import api.util.excel.ExcelUtils;
import com.fintex.ce.adapter.graphqlclient.repository.AverageMerGraphqlDataFetcher;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
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

public class MerFDSTab extends SMDataWriter<Holding, String, BigDecimal> implements WritableSpreadsheet {
  private static final AverageMerGraphqlDataFetcher ASSET_ALLOCATION_FDS = initAssetAllocationsFDS();
  private static final String TAB_NAME = "Master_Sheet";

  private static AverageMerGraphqlDataFetcher initAssetAllocationsFDS() {
    return new AverageMerGraphqlDataFetcher(CommonTools.GRAPHQL_TRANSPORT_COMPONENT);
  }

  @Override
  public void write(List<Holding> holdings, RequestParamsSupplier params, Workbook workbook) {
    final Sheet sheet = Objects.requireNonNull(Objects.requireNonNull(workbook.getSheet(TAB_NAME)));

    final Map<Holding, AverageManagementExpenseCalculationDTO> assetAllocationsMer = callFds(holdings, params
        .getDataProviders());

    fillSpreadSheetForMer(assetAllocationsMer, sheet);
  }

  public void fillSpreadSheetForMer(final Map<Holding, AverageManagementExpenseCalculationDTO> map, final Sheet sheet) {
    ExcelUtils.clearSheet(sheet, CellRangeAddress.valueOf("B16:J20"));

    var rbf605 = map.entrySet().stream()
        .filter(e -> e.getKey() instanceof FundSeriesHolding && ((FundSeriesHolding) e.getKey()).getFundServCode()
            .equalsIgnoreCase("RBF605")).findFirst().orElseThrow().getValue();
    sheet.getRow(15).createCell(1).setCellValue(rbf605.getManagementExpenseRatio().doubleValue());
    sheet.getRow(18).createCell(1).setCellValue(rbf605.getActualManagementFee().doubleValue());

    var xbal = map.entrySet().stream()
        .filter(e -> e.getKey() instanceof EtfHolding && ((EtfHolding) e.getKey()).getTicker().equalsIgnoreCase("XBAL"))
        .findFirst().orElseThrow().getValue();
    sheet.getRow(15).createCell(2).setCellValue(xbal.getManagementExpenseRatio().doubleValue());
    sheet.getRow(18).createCell(2).setCellValue(xbal.getActualManagementFee().doubleValue());

    var aom = map.entrySet().stream()
        .filter(e -> e.getKey() instanceof EtfHolding && ((EtfHolding) e.getKey()).getTicker().equalsIgnoreCase("AOM"))
        .findFirst().orElseThrow().getValue();
    sheet.getRow(16).createCell(3).setCellValue(aom.getNetExpenseRatio().doubleValue());
    sheet.getRow(17).createCell(3).setCellValue(aom.getGrossExpenseRatio().doubleValue());

    var fhusa04gs55 = map.entrySet().stream()
        .filter(e -> e.getKey() instanceof CanadaHedgeFundHolding && ((CanadaHedgeFundHolding) e.getKey())
            .getMorningstarId().equalsIgnoreCase("FHUSA04GS5")).findFirst().orElseThrow().getValue();
    sheet.getRow(15).createCell(8).setCellValue(fhusa04gs55.getManagementExpenseRatio().doubleValue());
    sheet.getRow(18).createCell(8).setCellValue(fhusa04gs55.getActualManagementFee().doubleValue());

    var tgdfx = map.entrySet().stream()
        .filter(e -> e.getKey() instanceof UsMutualFundHolding && ((UsMutualFundHolding) e.getKey()).getTicker()
            .equalsIgnoreCase("TGDFX")).findFirst().orElseThrow().getValue();
    sheet.getRow(16).createCell(9).setCellValue(tgdfx.getNetExpenseRatio().doubleValue());
    sheet.getRow(17).createCell(9).setCellValue(tgdfx.getGrossExpenseRatio().doubleValue());
  }

  private Map<Holding, AverageManagementExpenseCalculationDTO> callFds(final List<Holding> holdings,
      final List<DataProvider> dataProviders) {
    final Map<Holding, AverageManagementExpenseCalculationDTO> map = new LinkedHashMap<>();

    map.putAll(ASSET_ALLOCATION_FDS.queryBenchOfOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), dataProviders)
        .entrySet().stream()
        .collect(CollectorUtils.toMap(e -> e.getKey(),
            e -> new AverageManagementExpenseCalculationDTO().setGrossExpenseRatio(e.getValue().getGrossExpenseRatio())
                .setNetExpenseRatio(e.getValue().getNetExpenseRatio()))));

    map.putAll(ASSET_ALLOCATION_FDS
        .queryBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), dataProviders).entrySet()
        .stream()
        .collect(CollectorUtils.toMap(e -> e.getKey(),
            e -> new AverageManagementExpenseCalculationDTO().setManagementExpenseRatio(e.getValue().getMer())
                .setActualManagementFee(e.getValue().getActualManagementFee()))));

    map.putAll(ASSET_ALLOCATION_FDS
        .queryBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), dataProviders).entrySet()
        .stream()
        .collect(CollectorUtils.toMap(e -> e.getKey(),
            e -> new AverageManagementExpenseCalculationDTO().setManagementExpenseRatio(e.getValue().getMer())
                .setActualManagementFee(e.getValue().getActualManagementFee()))));

    map.putAll(ASSET_ALLOCATION_FDS
        .queryCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), dataProviders).entrySet()
        .stream()
        .collect(CollectorUtils.toMap(e -> e.getKey(),
            e -> new AverageManagementExpenseCalculationDTO().setManagementExpenseRatio(e.getValue().getMer())
                .setActualManagementFee(e.getValue().getActualManagementFee()))));

    map.putAll(ASSET_ALLOCATION_FDS
        .queryUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), dataProviders).entrySet()
        .stream()
        .collect(CollectorUtils.toMap(e -> e.getKey(),
            e -> new AverageManagementExpenseCalculationDTO().setGrossExpenseRatio(e.getValue().getGrossExpenseRatio())
                .setNetExpenseRatio(e.getValue().getNetExpenseRatio()))));

    return map;
  }
}