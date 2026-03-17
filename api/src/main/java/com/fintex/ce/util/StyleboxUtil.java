package com.fintex.ce.util;

import com.fintex.sm.model.domain.value.StyleBoxValue;
import com.fintex.sm.model.domain.rating.StyleBoxes;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@UtilityClass
public class StyleboxUtil {

  public static Map<String, BigDecimal> getBoxValues(final StyleBoxes styleBoxes) {
    return styleBoxes.getBoxValues().stream()
        .filter(boxValue -> Objects.nonNull(boxValue) && Objects.nonNull(boxValue.getStyleBoxType()))
        .collect(Collectors.toMap(
            boxStyleType -> boxStyleType.getStyleBoxType().name(),
            StyleBoxValue::getValue));
  }

}
