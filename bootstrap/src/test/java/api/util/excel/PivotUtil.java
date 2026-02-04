package api.util.excel;

import com.google.common.base.Strings;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.fintex.ce.util.CollectorUtils.toLinkedHashMap;
import static com.fintex.ce.util.DecimalUtils.toUserScale;

public class PivotUtil {

  private PivotUtil() {
  }

  public static LinkedHashMap<String, BigDecimal> calculateTop10Holdings(final Sheet sheet) {
    int nameCellIndex = CellReference.convertColStringToIndex("C");
    int weightCellIndex = CellReference.convertColStringToIndex("F");

    final FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();

    final Map<String, BigDecimal> weights = new HashMap<>();
    for (int i = 2; i <= sheet.getLastRowNum(); i++) {
      final Row row = sheet.getRow(i);
      final Cell cell = row.getCell(nameCellIndex);
      if (cell == null) {
        continue;
      }
      CellValue evaluate = evaluator.evaluate(cell);
      if (evaluate == null) {
        continue;
      }
      final String name = evaluate.getStringValue();
      if (Strings.isNullOrEmpty(name) || "0.0".equalsIgnoreCase(name)) {
        continue;
      }

      final CellValue evaluatedValue = evaluator.evaluate(row.getCell(weightCellIndex));
      BigDecimal weight = toUserScale(BigDecimal.valueOf(evaluatedValue.getNumberValue()));
      weights.computeIfPresent(name, (s, value) -> value.add(weight));
      weights.putIfAbsent(name.trim(), weight);
    }

    return weights.entrySet().stream()
        .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
        .limit(10)
        .collect(toLinkedHashMap());
  }
}