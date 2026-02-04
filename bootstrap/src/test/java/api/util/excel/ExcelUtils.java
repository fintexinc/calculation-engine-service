package api.util.excel;

import api.exception.TestException;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static api.util.excel.ExcelFormatUtils.convertToLocalDateViaInstant;
import static com.fintex.ce.domain.constant.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.util.Objects.nonNull;
import static org.apache.poi.ss.usermodel.CellType.NUMERIC;

@Log4j2
public class ExcelUtils {

  private ExcelUtils() {
  }

  public static Workbook openWorkbook(final File file) {
    try {
      return new XSSFWorkbook(file);
    } catch (IOException | InvalidFormatException e) {
      throw new TestException("General exception, see the description", e);
    }
  }

  public static void workbookProvider(String fileLocation, Consumer<Workbook> supplier) {
    try (Workbook workbook = new XSSFWorkbook(new File(ExcelUtils.class.getResource(fileLocation).getFile()))) {
      supplier.accept(workbook);
    } catch (InvalidFormatException | IOException e) {
      throw new TestException("General exception, see the description", e);
    }
  }

  public static void saveWorkbook(final String workbookName, final Workbook workbook) {
    try {
      final String dirStr = String.format("%s/%s/", System.getProperty("user.dir"), "tmp");
      final File dir = new File(dirStr);
      if (!dir.exists()) {
        dir.mkdir();
      }
      final String path = String.format(dirStr + "%s.xlsx", workbookName);
      final FileOutputStream fileOut = new FileOutputStream(path);
      workbook.write(fileOut);
      fileOut.close();
    } catch (IOException e) {
      log.error(e);
    }
  }

  public static void clearSheet(Sheet sheet) {
    for (int i = 0; i <= sheet.getLastRowNum(); i++) {
      final Row row = sheet.getRow(i);
      if (row == null) {
        continue;
      }
      sheet.removeRow(row);
    }
  }

  public static void clearSheet(Sheet sheet, CellAddress address) {
    for (int i = address.getRow(); i <= sheet.getLastRowNum(); i++) {
      final Row row = sheet.getRow(i);
      if (row == null) {
        continue;
      }
      for (int j = address.getColumn(); j < row.getLastCellNum(); j++) {
        final Cell cell = row.getCell(j);
        if (cell == null) {
          continue;
        }
        row.removeCell(cell);
      }
    }
  }

  public static void clearSheet(Sheet sheet, CellRangeAddress rangeAddress) {
    for (CellAddress address : rangeAddress) {
      final Row row = sheet.getRow(address.getRow());
      if (row == null) {
        continue;
      }
      final Cell cell = row.getCell(address.getColumn());
      if (cell == null) {
        continue;
      }
      row.removeCell(cell);
    }
  }

  public static void reevaluateSheet(Sheet sheet) {
    FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
    for (Row r : sheet) {
      for (Cell c : r) {
        if (c.getCellType() == CellType.FORMULA) {
          try {
            evaluator.evaluateFormulaCell(c);
          } catch (Throwable e) {
            log.error("Error occurred while evaluating formulas + " + c.getRowIndex() + ":"
                + c.getColumnIndex(), e.getMessage());
          }
        }
      }
    }
  }

  public static void reevaluateSheet(CellRangeAddress range, Sheet sheet) {
    FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
    for (CellAddress cellAddress : range) {
      final Row row = sheet.getRow(cellAddress.getRow());
      if (row == null) {
        continue;
      }
      final Cell cell = row.getCell(cellAddress.getColumn());
      if (cell != null && cell.getCellType() == CellType.FORMULA) {
        try {
          evaluator.evaluateFormulaCell(cell);
        } catch (Throwable e) {
          log.error("Error occurred while evaluating formulas + " + cell.getRowIndex() + ":"
              + cell.getColumnIndex(), e.getMessage());
        }
      }
    }
  }

  public static Integer readAsInteger(Row row, int cellIndex) {
    if (row == null) {
      return null;
    }
    Cell cell = row.getCell(cellIndex);
    if (cell == null) {
      return null;
    }
    return (int) cell.getNumericCellValue();
  }

  public static String readAsString(Row row, int cellIndex) {
    if (row == null) {
      return null;
    }
    Cell cell = row.getCell(cellIndex);
    if (cell == null) {
      return null;
    }
    return cell.getStringCellValue();
  }

  public static BigDecimal readAsBigDecimal(Row row, int cellIndex) {
    if (row == null) {
      return null;
    }
    Cell cell = row.getCell(cellIndex);
    if (cell == null) {
      return null;
    }
    return BigDecimal.valueOf(cell.getNumericCellValue());
  }

  public static BigDecimal readAsBigDecimalAndMultiplyBy100(Row row, int cellIndex) {
    if (row == null) {
      return null;
    }
    Cell cell = row.getCell(cellIndex);
    if (cell == null) {
      return null;
    }
    return BigDecimal.valueOf(cell.getNumericCellValue()).multiply(HUNDRED);
  }

  public static Boolean readAsBoolean(Row row, int cellIndex) {
    if (row == null) {
      return null;
    }
    Cell cell = row.getCell(cellIndex);
    if (cell == null) {
      return null;
    }
    return cell.getBooleanCellValue();
  }

  public static LocalDate readAsLocalDate(Row row, int cellIndex) {
    if (row == null) {
      return null;
    }
    Cell cell = row.getCell(cellIndex);
    if (cell == null) {
      return null;
    }
    return cell.getCachedFormulaResultType().equals(NUMERIC)
        ? convertToLocalDateViaInstant(cell.getDateCellValue())
        : null;
  }

  public static BigDecimal readBigDecimalFromPercentageCell(String valueStr) {
    return new BigDecimal(valueStr.replace('%', ' ').trim());
  }

  public static Map<LocalDate, BigDecimal> readRowData(final Sheet sheet, final int rowNumber,
      final int dateRowNum, final int cellToStart) {
    final Map<Integer, LocalDate> dates = readDates(sheet, dateRowNum, cellToStart);
    final Map<Integer, BigDecimal> returns = new HashMap<>();

    final Row row = sheet.getRow(rowNumber);
    for (int i = cellToStart; i < row.getLastCellNum(); i++) {
      final var cell = row.getCell(i);
      if (cell == null) {
        continue;
      }
      if (cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.NUMERIC) {
        final var returnValue = toUserScale(BigDecimal.valueOf(cell.getNumericCellValue()));
        returns.put(cell.getColumnIndex(), returnValue);
      }
    }
    return returns.entrySet()
        .stream()
        .collect(Collectors.toMap(e -> dates.get(e.getKey()), Map.Entry::getValue));
  }

  public static Map<LocalDate, String> readStringRowData(final Sheet sheet, final int rowNumber,
      final int dateRowNum, final int cellToStart) {
    final Map<Integer, LocalDate> dates = readDates(sheet, dateRowNum, cellToStart);
    final Map<Integer, String> returns = new HashMap<>();

    final Row row = sheet.getRow(rowNumber);
    for (int i = cellToStart; i < row.getLastCellNum(); i++) {
      final var cell = row.getCell(i);
      if (cell == null) {
        continue;
      }
      if (cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.NUMERIC) {
        final var returnValue = toUserScale(BigDecimal.valueOf(cell.getNumericCellValue()));
        returns.put(cell.getColumnIndex(), returnValue.toPlainString().trim());
      } else if (cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.STRING) {
        returns.put(cell.getColumnIndex(), cell.getStringCellValue().trim());
      }
    }
    return returns.entrySet()
        .stream()
        .collect(Collectors.toMap(e -> dates.get(e.getKey()), Map.Entry::getValue));
  }

  public static Map<Integer, LocalDate> readDates(final Sheet sheet, final int dateRowNum, final int cellToStart) {
    final Row row = sheet.getRow(dateRowNum);
    final Map<Integer, LocalDate> dates = new HashMap<>();
    for (int i = cellToStart; i < row.getLastCellNum(); i++) {
      final var cell = row.getCell(i);
      if (cell.getCellType() == CellType.FORMULA) {
        final var date = ExcelFormatUtils.convertToLocalDateViaInstant(cell.getDateCellValue());
        dates.put(cell.getAddress().getColumn(), date);
      }
    }
    return dates;
  }

  public static LocalDate getLocalDate(final Sheet sheet, final Integer rowNumber, final Integer cellNumber) {
    final Row row = sheet.getRow(rowNumber);
    final var cell = row.getCell(cellNumber);
    return nonNull(cell) && cell.getCachedFormulaResultType() == NUMERIC
        ? ExcelFormatUtils.convertToLocalDateViaInstant(cell.getDateCellValue())
        : null;
  }

  public static BigDecimal getBigDecimalValue(final Sheet sheet, final Integer rowNumber, final Integer cellNumber) {
    final var cell = sheet.getRow(rowNumber).getCell(cellNumber);
    if (cell == null) {
      return null;
    }
    if (cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.NUMERIC) {
      final var returnValue = toUserScale(BigDecimal.valueOf(cell.getNumericCellValue()));
      return returnValue;
    }
    return null;
  }

  public static String getStringValue(final Sheet sheet, final Integer rowNumber, final Integer cellNumber) {
    final var cell = sheet.getRow(rowNumber).getCell(cellNumber);
    if (cell == null) {
      return null;
    }
    return cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : null;
  }

  public static int getIntegerValue(final Sheet master, final Integer rowNumber, final Integer cellNumber) {
    return (int) master.getRow(1).getCell(1).getNumericCellValue();
  }

}
