package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.dto.command.contract.HoldingsProvider;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.fintex.ce.model.domain.enumeration.CalculationMetric.*;

@Component
@Order(410)
public class HoldingValueReqValidator
    extends
      AbstractHoldingsValidationReqValidator<HoldingsProvider> {

  private static final Set<CalculationMetric> METRICS_WITHOUT_HOLDING_VALUE_VALIDATION = Set.of(
      COMMON_PERFORMANCE_DATES, NUMBER_OF_UNIQUE_HOLDINGS);

  public HoldingValueReqValidator(HoldingsValidator holdingsValidator) {
    super(HoldingsProvider.class, HoldingsProvider::getHoldings, holdingsValidator::validateHoldingValues);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return Arrays.stream(CalculationMetric.values())
        .filter(m -> !METRICS_WITHOUT_HOLDING_VALUE_VALIDATION.contains(m))
        .toList();
  }
}
