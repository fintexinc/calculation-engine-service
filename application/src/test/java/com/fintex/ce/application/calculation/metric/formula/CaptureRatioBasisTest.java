package com.fintex.ce.application.calculation.metric.formula;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.fintex.ce.application.calculation.metric.formula.CaptureRatioBasis.GEOMETRIC_MONTHLY;
import static com.fintex.ce.application.util.DecimalUtils.pow;
import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;

class CaptureRatioBasisTest {

  @Test
  void shouldCalculateGeometricMonthlyReturn_whenReturnFactorsAreProvided() {
    BigDecimal actual = GEOMETRIC_MONTHLY.calculate(List.of(
        new BigDecimal("1.02"),
        new BigDecimal("1.03")));

    assertThat(actual).isEqualByComparingTo(pow(new BigDecimal("1.0506"), new BigDecimal("0.5")).subtract(ONE));
  }

  @Test
  void shouldReturnZero_whenReturnFactorsAreEmpty() {
    BigDecimal actual = GEOMETRIC_MONTHLY.calculate(List.of());

    assertThat(actual).isEqualByComparingTo(BigDecimal.ZERO);
  }
}