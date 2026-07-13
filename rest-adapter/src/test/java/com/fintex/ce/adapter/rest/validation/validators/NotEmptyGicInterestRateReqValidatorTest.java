package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.fintex.ce.model.domain.enumeration.CalculationMetric.ANNUAL_RETURNS;
import static com.fintex.ce.model.domain.enumeration.CalculationMetric.GROWTH_OF_10K;
import static com.fintex.ce.model.domain.enumeration.CalculationMetric.TRAILING_TOTAL_RETURNS;
import static org.assertj.core.api.Assertions.assertThat;

class NotEmptyGicInterestRateReqValidatorTest extends AbstractGicFieldReqValidatorTest {

  @Override
  RequestValidator createValidator() {
    return new NotEmptyGicInterestRateReqValidator();
  }

  @Test
  void shouldValidateGicInterestRateForAllReturnMetrics_whenReportingSupportedMetrics() {
    assertThat(new NotEmptyGicInterestRateReqValidator().supportedMetrics())
        .containsExactlyInAnyOrder(TRAILING_TOTAL_RETURNS, ANNUAL_RETURNS, GROWTH_OF_10K);
  }

  @Override
  GicHolding createInvalidGicHolding() {
    return GicHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.GIC)
        .clientIntRate(null)
        .build();
  }

  @Override
  GicHolding createValidGicHolding() {
    return GicHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.GIC)
        .clientIntRate(BigDecimal.valueOf(0.05))
        .build();
  }

  @Override
  String expectedErrorCode() {
    return "GIC_HOLDING_MISSING_INTEREST_RATE";
  }

  @Override
  String expectedMessage() {
    return "The gic holding is missing interest rate";
  }
}
