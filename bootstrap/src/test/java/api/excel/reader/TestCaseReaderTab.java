package api.excel.reader;

import api.exception.TestException;
import api.util.excel.ExcelFormatUtils;
import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.Frequency;
import com.fintex.ce.domain.enumeration.InterestFreq;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static api.util.excel.ExcelUtils.*;
import static org.apache.poi.ss.util.CellReference.convertColStringToIndex;

public interface TestCaseReaderTab<T> extends TabReader<Map<Integer, T>> {
  String TAB_NAME = "Test_Cases";

  default List<DataProvider> toDataProviders(String dataProvider) {
    switch (dataProvider.trim().toUpperCase()) {
      case "" :
      case "DEFAULT" :
      case "ANY" :
      case "SOFTAPPOINT" :
      case "HARDAPPOINT" :
        return List.of();
      case "EAGLE" :
        return List.of(DataProvider.EAGLE);
      case "MORNINGSTAR" :
        return List.of(DataProvider.MORNINGSTAR);
      case "EAGLE, MORNINGSTAR" :
        return List.of(DataProvider.EAGLE, DataProvider.MORNINGSTAR);
      case "MORNINGSTAR, EAGLE" :
        return List.of(DataProvider.MORNINGSTAR, DataProvider.EAGLE);
    }
    throw new TestException("There is no such data provider");
  }

  default Currency readCurrency(final Sheet sheet, final int rowIndex, final String currencyCell) {
    final Cell currency = sheet.getRow(rowIndex).getCell(convertColStringToIndex(currencyCell));
    return Objects.nonNull(currency) ? Currency.of(currency.getStringCellValue()) : null;
  }

  default Map<String, BigDecimal> readHoldings(final Sheet sheet, final int rowIndex, final String startAt,
      final String endAt) {
    final Map<String, BigDecimal> map = new HashMap<>();
    for (int colIndex = convertColStringToIndex(startAt); colIndex <= convertColStringToIndex(endAt); colIndex++) {
      final BigDecimal weight = readAsBigDecimal(sheet.getRow(rowIndex), colIndex);
      if (weight == null || weight.compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      final String holdingName = readAsString(sheet.getRow(0), colIndex);

      map.put(holdingName, weight);
    }
    return map;
  }

  default LocalDate readLocalDate(final Row row, final int cellIndex) {
    final Cell cell = row.getCell(cellIndex);
    if (!DateUtil.isCellDateFormatted(cell) || cell.getDateCellValue() == null) {
      return null;
    }
    final String date = ExcelFormatUtils.DATE_FORMAT_YYYY_MM_DD.format(cell.getDateCellValue());
    return LocalDate.parse(date);
  }

  default InterestFreq readInterestFreq(final Row row, final int cellIndex) {
    final BigDecimal weight = readAsBigDecimal(row, cellIndex);
    if (Objects.isNull(weight)) {
      return null;
    }
    return InterestFreq.of(weight);
  }

  default Frequency readFrequency(final Row row, final int cellIndex) {
    final int weight = readAsInteger(row, cellIndex);
    return Optional.ofNullable(weight).map(Frequency::of).orElse(null);
  }
}
