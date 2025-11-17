package api.util.excel;

import api.util.DecimalUtils;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.AreaPtgBase;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtgBase;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ExcelFormulas {

    private ExcelFormulas() {
    }

    public static BigDecimal sumProduct(String ref1Str, String ref2Str, Sheet sheet) {
        final CellRangeAddress ref1 = CellRangeAddress.valueOf(ref1Str);
        final CellRangeAddress ref2 = CellRangeAddress.valueOf(ref2Str);

        return sumProduct(ref1, ref2, sheet);
    }

    public static BigDecimal sumProduct(CellRangeAddress ref1, CellRangeAddress ref2, Sheet sheet) {
        BigDecimal sum = BigDecimal.ZERO;
        int index = -1;

        final Row row1 = sheet.getRow(ref1.getFirstRow());
        final Row row2 = sheet.getRow(ref2.getFirstRow());

        for (CellAddress cellAddress : ref1) {
            index++;

            final Cell cell1 = row1.getCell(cellAddress.getColumn());
            if (cell1 == null) {
                continue;
            }
            final double value1 = cell1.getNumericCellValue();

            final Cell cell2 = row2.getCell(ref2.getFirstColumn() + index);
            if (cell2 == null) {
                continue;
            }

            final double value2 = cell2.getNumericCellValue();

            sum = sum.add(
                    BigDecimal.valueOf(value1).multiply(BigDecimal.valueOf(value2))
            )
                    .setScale(DecimalUtils.DEFAULT_SCALE, RoundingMode.HALF_UP);
        }

        return sum.setScale(DecimalUtils.DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    public static void populateFormulas(int rowIndex, int startFromCell, int finishAtCell, Sheet sheet) {
        final Row fromRow = sheet.getRow(rowIndex - 1);
        if (fromRow == null) {
            return;
        }
        final Row toRow = sheet.getRow(rowIndex);
        for (int i = startFromCell; i <= finishAtCell; i++) {
            final Cell cell = fromRow.getCell(i);
            if (cell == null) {
                continue;
            }
            final String cellFormula = cell.getCellFormula();
            toRow.createCell(i).setCellFormula(copyFormula(sheet, cellFormula, 0, 1));
        }
    }

    private static String copyFormula(Sheet sheet, String formula, int coldiff, int rowdiff) {

        XSSFEvaluationWorkbook workbookWrapper =
                XSSFEvaluationWorkbook.create((XSSFWorkbook) sheet.getWorkbook());
        Ptg[] ptgs = FormulaParser.parse(formula, workbookWrapper, FormulaType.CELL
                , sheet.getWorkbook().getSheetIndex(sheet));

        for (int i = 0; i < ptgs.length; i++) {
            if (ptgs[i] instanceof RefPtgBase) { // base class for cell references
                RefPtgBase ref = (RefPtgBase) ptgs[i];
                if (ref.isColRelative())
                    ref.setColumn(ref.getColumn() + coldiff);
                if (ref.isRowRelative())
                    ref.setRow(ref.getRow() + rowdiff);
            } else if (ptgs[i] instanceof AreaPtgBase) { // base class for range references
                AreaPtgBase ref = (AreaPtgBase) ptgs[i];
                if (ref.isFirstColRelative())
                    ref.setFirstColumn(ref.getFirstColumn() + coldiff);
                if (ref.isLastColRelative())
                    ref.setLastColumn(ref.getLastColumn() + coldiff);
                if (ref.isFirstRowRelative())
                    ref.setFirstRow(ref.getFirstRow() + rowdiff);
                if (ref.isLastRowRelative())
                    ref.setLastRow(ref.getLastRow() + rowdiff);
            }
        }

        formula = FormulaRenderer.toFormulaString(workbookWrapper, ptgs);
        return formula;
    }

}
