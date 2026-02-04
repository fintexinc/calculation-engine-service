package api.excel.writer;

import api.util.IdentifierUtils;
import api.util.excel.ExcelUtils;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.Holding;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class SMDataWriter<H extends Holding, K, V> {

  public void populateTab(final Map<H, Pair<DataProvider, Map<String, V>>> rawData, final Sheet sheet) {
    final CellAddress fundNameAddress = new CellAddress("D1");
    final CellAddress dataProviderAddress = new CellAddress("D2");
    final CellAddress valueAddress = new CellAddress("A3");

    ExcelUtils.clearSheet(sheet, new CellAddress("D3"));
    ExcelUtils.reevaluateSheet(CellRangeAddress.valueOf("D1:X1"), sheet);

    for (Map.Entry<H, Pair<DataProvider, Map<String, V>>> entry : rawData.entrySet()) {
      final String fundCode = IdentifierUtils.cutUserIdentifier(entry.getKey().generateUserIdentifier());
      for (int colIndex = fundNameAddress.getColumn(); colIndex < 50; colIndex++) {
        final Cell fundNameCell = sheet.getRow(fundNameAddress.getRow()).getCell(colIndex);
        if (fundNameCell == null) {
          break;
        }
        if (!fundCode.equalsIgnoreCase(fundNameCell.getStringCellValue().trim())) {
          continue;
        }
        final DataProvider dataProvider = entry.getValue().getKey();
        if (dataProvider != null) {
          final Cell dataProviderCell = CellUtil.getCell(sheet.getRow(dataProviderAddress.getRow()), colIndex);
          dataProviderCell.setCellValue(dataProvider.name());
        }
        fillValues(sheet, fundNameCell, valueAddress, entry);
      }
    }
  }

  private void fillValues(Sheet sheet, Cell fundNameAddress, CellAddress valueAddress,
      Map.Entry<H, Pair<DataProvider, Map<String, V>>> entry) {
    for (int rowIndex = valueAddress.getRow(); rowIndex < 1_000; rowIndex++) {
      final Row row = sheet.getRow(rowIndex);
      if (row == null) {
        break;
      }
      final Cell cell = row.getCell(valueAddress.getColumn());
      if (cell == null) {
        break;
      }
      final String valueStr = cell.getStringCellValue();
      final Optional<V> first = entry.getValue().getValue().entrySet().stream().filter(e -> e.getKey()
          .equalsIgnoreCase(valueStr)).map(Map.Entry::getValue).findFirst();
      if (first.isPresent()) {
        final Cell valueCell = CellUtil.getCell(row, fundNameAddress.getColumnIndex());
        if (first.get() instanceof String) {
          valueCell.setCellValue((String) first.get());
        } else if (first.get() instanceof BigDecimal) {
          valueCell.setCellValue(((BigDecimal) first.get()).doubleValue());
        }
      }
    }
  }

  public void fillSpreadSheet(final Map<H, Pair<DataProvider, Map<K, V>>> rawData,
      final List<DataProvider> dataProviders, final Sheet sheet) {
    // read static data
    Set<String> holdingsCode = readHoldingsCodes(sheet);
    Set<String> keyData = readKeyData(sheet);
    ExcelUtils.clearSheet(sheet);

    Row row0 = sheet.createRow(0);
    Row row1 = sheet.createRow(1);
    row1.createCell(0).setCellValue("dataProvider");

    // fill static data
    Map<Integer, Integer> rowsCols = fillStaticData(holdingsCode, keyData, sheet);

    int rows = rowsCols.keySet().stream().findFirst().orElseThrow();
    int cols = rowsCols.values().stream().findFirst().orElseThrow();

    for (Map.Entry<H, Pair<DataProvider, Map<K, V>>> entry : rawData.entrySet()) {
      Set<K> keySet = entry.getValue().getValue().keySet();
      // getting through the rows and cols ang filling the data
      for (int rowIndex = 2; rowIndex < rows; rowIndex++) {
        for (K key : keySet) {
          if (key.toString().equalsIgnoreCase(sheet.getRow(rowIndex).getCell(0).getStringCellValue())) {
            for (int colIndex = 3; colIndex < cols; colIndex++) {
              if (row0.getCell(colIndex).getStringCellValue().equals(IdentifierUtils.cutUserIdentifier(entry.getKey()
                  .generateUserIdentifier()))) {
                V v = entry.getValue().getValue().get(key);
                if (v instanceof BigDecimal) {
                  sheet.getRow(rowIndex).createCell(colIndex).setCellValue(((BigDecimal) v).doubleValue());
                }
                if (v instanceof String) {
                  sheet.getRow(rowIndex).createCell(colIndex).setCellValue(v.toString());
                }
                row1.createCell(colIndex).setCellValue(Optional.ofNullable(entry.getValue().getKey()).map(Enum::name)
                    .orElse(""));
                break;
              }
            }
            break;
          }
        }
      }
    }
  }

  private Set<String> readKeyData(final Sheet sheet) {
    Set<String> keyData = new LinkedHashSet<>();
    for (int rowIndex = 2; rowIndex < 1_000; rowIndex++) {
      if (sheet.getRow(rowIndex) == null)
        break;
      if (sheet.getRow(rowIndex).getCell(0) == null) {
        break;
      }
      keyData.add(sheet.getRow(rowIndex).getCell(0).getStringCellValue());
    }
    return keyData;
  }

  private Set<String> readHoldingsCodes(final Sheet sheet) {
    Set<String> holdingsCode = new LinkedHashSet<>();
    Row row0 = sheet.getRow(0);
    for (int colIndex = 3; colIndex < 1_000; colIndex++) {
      if (row0.getCell(colIndex) == null) {
        break;
      }
      holdingsCode.add(row0.getCell(colIndex).getStringCellValue());
    }
    return holdingsCode;
  }

  private Map<Integer, Integer> fillStaticData(final Set<String> holdingsCode, final Set<String> keyData,
      final Sheet sheet) {
    int rows = 2;
    int cols = 3;

    for (String holdingCode : holdingsCode) {
      sheet.getRow(0).createCell(cols++).setCellValue(holdingCode);
    }
    for (String key : keyData) {
      sheet.createRow(rows++).createCell(0).setCellValue(key);
    }
    return Map.of(rows, cols);
  }

}
