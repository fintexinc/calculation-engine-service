package com.fintex.ce.application.calculation.metric.formula;

import java.math.BigDecimal;
import java.util.List;

import static java.math.BigDecimal.ONE;

/**
 * Capture ratios use cumulative compounded returns over benchmark-qualified months: {@code product(1 + r_i) - 1}.
 */
public enum CaptureRatioBasis {

  CUMULATIVE_COMPOUNDED;

  public BigDecimal calculate(List<BigDecimal> returnFactors) {
    return returnFactors.stream().reduce(ONE, BigDecimal::multiply).subtract(ONE);
  }
}
