package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.fintex.ce.model.domain.enumeration.CalculationMetric.*;

@Component
@Order(421)
public class NotEmptyGicInterestRateReqValidator extends AbstractGicFieldReqValidator<BigDecimal> {

  public NotEmptyGicInterestRateReqValidator() {
    super(GicHolding::getClientIntRate, ErrorCode.ERR_GIC_MC_001);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(TRAILING_TOTAL_RETURNS);
  }
}
