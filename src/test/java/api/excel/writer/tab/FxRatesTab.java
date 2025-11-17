package api.excel.writer.tab;

import api.dto.RequestParamsSupplier;
import api.excel.writer.WritableSpreadsheet;
import api.exception.TestException;
import api.util.CommonTools;
import api.util.excel.ExcelFormatUtils;
import api.util.excel.ExcelUtils;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.smclient.dto.FxRatesDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class FxRatesTab implements WritableSpreadsheet {
    private static final String TAB_NAME = "FX_Rates";

    public static void setDate(final Cell cell, final CellStyle dateCellStyle, final LocalDate localDate) throws RuntimeException {
        cell.setCellStyle(dateCellStyle);
        final String effectiveDate = localDate.format(ExcelFormatUtils.DATA_TIME_FORMATTER);
        try {
            cell.setCellValue(ExcelFormatUtils.DATE_FORMAT_MM_DD_YYYY.parse(effectiveDate));
        } catch (ParseException e) {
            throw new TestException("While parsing date for FXRates", e);
        }
    }

    @Override
    public void write(final List<Holding> holdings, final RequestParamsSupplier params, final Workbook workbook) {
        final Map<LocalDate, FxRatesDTO> fxRatesMap = CommonTools.CURRENCY_TRADING_PROVIDER.loadFxRates();
        if (CollectionUtils.isEmpty(fxRatesMap)) {
            throw new TestException("FX Rates could not be empty");
        }
        final Sheet sheet = Objects.requireNonNull(Objects.requireNonNull(workbook.getSheet(TAB_NAME)));
        fillSpreadsheet(fxRatesMap, sheet);
    }

    private void fillSpreadsheet(final Map<LocalDate, FxRatesDTO> fxRatesMap, final Sheet sheet) {
        ExcelUtils.clearSheet(sheet, new CellAddress("A2"));

        final CellStyle style = ExcelFormatUtils.createDateFormat(sheet.getWorkbook(), ExcelFormatUtils.DATA_FORMAT_2);

        final TreeMap<LocalDate, FxRatesDTO> treeMap = new TreeMap<>(fxRatesMap);

        int i = 0;
        for (Map.Entry<LocalDate, FxRatesDTO> entry : treeMap.entrySet()) {
            final Row row = sheet.createRow(i + 1);

            setDate(row.createCell(0), style, entry.getKey());

            final Cell usdCad = row.createCell(1);
            usdCad.setCellValue(entry.getValue().getUsdCad().doubleValue());

            final Cell cadCad = row.createCell(2);
            cadCad.setCellValue(1);

            final Cell cadUsd = row.createCell(3);
            cadUsd.setCellValue(entry.getValue().getCadUsd().doubleValue());

            i++;
        }
    }

}
