package api.excel.writer.tab;

import api.dto.RequestParamsSupplier;
import api.excel.writer.WritableSpreadsheet;
import api.exception.TestException;
import api.util.CommonTools;
import api.util.excel.ExcelFormatUtils;
import api.util.excel.ExcelUtils;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.model.redis.RHistoricalDistributions;
import com.fintex.ce.repository.graphql.query.HistoricalDistributionsFDSRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;

public class CapitalGainTab implements WritableSpreadsheet {
    private static final HistoricalDistributionsFDSRepository CapitalGain_FDS = initHistoricalDistributionPricesFDS();
    private static final String Capital_Gain_FundServ_Code_1 = "Capital gain FundServ Code 1";
    private static final String Capital_Gain_FundServ_Code_2 = "Capital gain FundServ Code 2";
    private static final String Capital_Gain_FundServ_Code_3 = "Capital gain FundServ Code 3";

    private static HistoricalDistributionsFDSRepository initHistoricalDistributionPricesFDS() {
        return new HistoricalDistributionsFDSRepository(CommonTools.GRAPHQL_TRANSPORT_COMPONENT);
    }

    @Override
    public void write(List<Holding> holdings, RequestParamsSupplier params, Workbook workbook) {
        final Map<FundSeriesHolding, RHistoricalDistributions> capitalGain = callFds(holdings, params.getDataProviders());
        fillSpreadSheet(capitalGain, workbook);
    }

    private Map<FundSeriesHolding, RHistoricalDistributions> callFds(final List<Holding> holdings, final List<DataProvider> dataProviders) {
        final Map<FundSeriesHolding, RHistoricalDistributions> map = new LinkedHashMap<>();
        List<FundSeriesHolding> filteredHoldings = filterHoldings(holdings, CANADA_MUTUAL_PREDICATE);
        if (!filteredHoldings.isEmpty()) {
            map.putAll(CapitalGain_FDS.queryBenchOfFundCanada(filteredHoldings, dataProviders));
        }
        return map;
    }

    private void fillSpreadSheet(final Map<FundSeriesHolding, RHistoricalDistributions> capitalGain, final Workbook workbook) {
        final CellStyle style = ExcelFormatUtils.createDateFormat(workbook, ExcelFormatUtils.DATA_FORMAT_2);
        String tabName;
        for (Map.Entry<FundSeriesHolding, RHistoricalDistributions> entry : capitalGain.entrySet()) {
            if (entry.getValue().getHoldingId().equals("RBF269"))
                tabName = Capital_Gain_FundServ_Code_1;
            else if (entry.getValue().getHoldingId().equals("RBF605"))
                tabName = Capital_Gain_FundServ_Code_2;
            else
                tabName = Capital_Gain_FundServ_Code_3;
            int i = 0;
            final Sheet sheet = Objects.requireNonNull(Objects.requireNonNull(workbook.getSheet(tabName)));
            ExcelUtils.clearSheet(sheet, new CellAddress("A2"));
            for (Map.Entry<LocalDate, RHistoricalDistributions.CapitalGainsDto> ret : entry.getValue().getCapitalGains().entrySet()) {
                final Row row = sheet.createRow(i + 1);
                setDate(row.createCell(0), style, ret.getKey());

                final Cell domesticDividend = row.createCell(1);
                if ((ret.getValue().getCapitalGains() != null))
                    domesticDividend.setCellValue(ret.getValue().getCapitalGains().doubleValue());

                final Cell foreignDividend = row.createCell(2);
                if ((ret.getValue().getReturnOfCapital() != null))
                    foreignDividend.setCellValue(ret.getValue().getReturnOfCapital().doubleValue());
                i++;
            }
        }
    }

    public static void setDate(final Cell cell, final CellStyle dateCellStyle, final LocalDate localDate) throws RuntimeException {
        cell.setCellStyle(dateCellStyle);
        final String effectiveDate = localDate.format(ExcelFormatUtils.DATA_TIME_FORMATTER);
        try {
            cell.setCellValue(ExcelFormatUtils.DATE_FORMAT_MM_DD_YYYY.parse(effectiveDate));
        } catch (ParseException e) {
            throw new TestException("While parsing date for Capital Gains", e);
        }
    }
}
