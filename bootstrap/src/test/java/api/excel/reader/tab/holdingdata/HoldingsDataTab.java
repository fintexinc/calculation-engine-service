package api.excel.reader.tab.holdingdata;

import api.config.constant.HoldingGroups;
import api.excel.reader.HoldingReaderTab;
import api.model.HoldingDataDTO;
import api.model.HoldingsDataColumnType;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;
import com.google.common.base.Strings;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static api.model.HoldingsDataColumnType.*;
import static api.util.excel.ExcelUtils.readAsString;
import static org.apache.poi.ss.util.CellReference.convertColStringToIndex;

public class HoldingsDataTab implements HoldingReaderTab<HoldingDataDTO> {
  private static final String TAB_NAME = "Holdings_Data";

  private static Map<String, HoldingDataDTO> readHoldingsData(final Sheet sheet,
      Map<HoldingsDataColumnType, String> indexMap) {
    final Map<String, HoldingDataDTO> map = new LinkedHashMap<>();
    for (int i = 1; i < 15; i++) {
      final Row row = sheet.getRow(i);
      final String holdingCode = readAsString(row, convertColStringToIndex(indexMap.get(HOLDING_CODE)));
      if (Strings.isNullOrEmpty(holdingCode)) {
        break;
      }

      final String trimmedHoldingCode = holdingCode.trim();
      final String holdingTypeStr = readAsString(row, convertColStringToIndex(indexMap.get(HOLDING_TYPE)));
      final HoldingType holdingType = HoldingType.valueOf(holdingTypeStr);
      final int exchangedId = convertColStringToIndex(indexMap.get(EXCHANGE_ID));

      final HoldingDataDTO dto = new HoldingDataDTO(
          trimmedHoldingCode,
          holdingType,
          readAsString(row, exchangedId));

      if (!HoldingGroups.CASH.contains(holdingType) && !HoldingGroups.GIC.contains(holdingType)) {
        final String identifier = readAsString(row, convertColStringToIndex(indexMap.get(HOLDING_IDENTIFIER)));
        final FiIdentifierType fiIdentifierType = FiIdentifierType.valueOf(identifier);
        dto.setSecurityIdentifier(new SecurityIdentifier(trimmedHoldingCode, fiIdentifierType));
      }

      map.put(trimmedHoldingCode, dto);
    }
    return map;
  }

  private Map<HoldingsDataColumnType, String> getDefaultIndexes() {
    return Map.of(
        HOLDING_CODE, "A",
        HOLDING_TYPE, "B",
        EXCHANGE_ID, "C",
        HOLDING_IDENTIFIER, "D");
  }

  public Map<String, HoldingDataDTO> read(final Workbook workbook) {
    final Sheet sheet = Objects.requireNonNull(workbook.getSheet(TAB_NAME));
    return readHoldingsData(sheet, getDefaultIndexes());
  }

}
