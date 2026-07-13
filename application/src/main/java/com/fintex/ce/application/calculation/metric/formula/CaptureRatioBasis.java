package com.fintex.ce.application.calculation.metric.formula;

import java.math.BigDecimal;
import java.util.List;

import static com.fintex.ce.application.util.DecimalUtils.annualizedReturn;
import static com.fintex.ce.application.util.DecimalUtils.divide;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

/**
 * Capture ratios use geometric monthly returns over benchmark-qualified months: {@code product(1 + r_i)^(1/n) - 1}.
 */
public enum CaptureRatioBasis {

  GEOMETRIC_MONTHLY {
    @Override
    public BigDecimal calculate(List<BigDecimal> returnFactors) {
      if (returnFactors.isEmpty()) {
        return ZERO;
      }
      BigDecimal product = returnFactors.stream().reduce(ONE, BigDecimal::multiply);
      return annualizedReturn(product, divide(ONE, BigDecimal.valueOf(returnFactors.size())));
    }
  };

  public abstract BigDecimal calculate(List<BigDecimal> returnFactors);
}
