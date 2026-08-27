package ca.tangerine.pce.rest.validation.validators;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static ca.tangerine.pce.model.domain.enumeration.CalculationMetric.ANNUAL_RETURNS;
import static ca.tangerine.pce.model.domain.enumeration.CalculationMetric.GROWTH_OF_10K;
import static ca.tangerine.pce.model.domain.enumeration.CalculationMetric.TRAILING_TOTAL_RETURNS;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.GicHolding;
import ca.tangerine.pce.model.error.ErrorCode;

@Component
@Order(421)
public class NotEmptyGicInterestRateReqValidator extends AbstractGicFieldReqValidator<BigDecimal> {

  public NotEmptyGicInterestRateReqValidator() {
    super(GicHolding::getClientIntRate, ErrorCode.GIC_HOLDING_MISSING_INTEREST_RATE);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(TRAILING_TOTAL_RETURNS, ANNUAL_RETURNS, GROWTH_OF_10K);
  }
}
