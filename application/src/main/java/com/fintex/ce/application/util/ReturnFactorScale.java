package com.fintex.ce.application.util;

import com.fintex.ce.model.util.BigDecimalConstants;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.fintex.ce.application.util.DecimalUtils.divide;

@Getter
@AllArgsConstructor
public enum ReturnFactorScale {

  SCALE_OF_ONE(entry -> divide(entry.getValue(), BigDecimalConstants.HUNDRED)), // e.g. 20% would be converted to 0.2
  SCALE_OF_TWO(entry -> divide(entry.getValue().add(BigDecimalConstants.HUNDRED), BigDecimalConstants.HUNDRED)), // e.g.
  // 1.2
  AS_IS(Map.Entry::getValue); // e.g. 20% would be converted to 20

  private final Function<Map.Entry<LocalDate, BigDecimal>, BigDecimal> formula;

}
