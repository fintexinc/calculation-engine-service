package ca.tangerine.pce.rest.validation.validators;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static ca.tangerine.pce.model.domain.enumeration.CalculationMetric.ANNUAL_RETURNS;
import static ca.tangerine.pce.model.domain.enumeration.CalculationMetric.GROWTH_OF_10K;
import static ca.tangerine.pce.model.domain.enumeration.CalculationMetric.TRAILING_TOTAL_RETURNS;
import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.model.domain.holding.GicHolding;
import ca.tangerine.pce.rest.validation.RequestValidator;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;

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
    return "The gic holding GIC-CAD-10 is missing interest rate";
  }
}
