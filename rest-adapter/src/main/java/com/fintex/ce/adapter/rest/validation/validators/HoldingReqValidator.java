package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.dto.command.contract.HoldingsProvider;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Order(400)
public class HoldingReqValidator
    extends
      AbstractHoldingsValidationReqValidator<HoldingsProvider> {

  public HoldingReqValidator(HoldingsValidator holdingsValidator) {
    super(HoldingsProvider.class, HoldingsProvider::getHoldings, holdingsValidator::validate);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return Arrays.stream(CalculationMetric.values())
        .filter(metric -> metric != CalculationMetric.COMMON_PERFORMANCE_DATES)
        .toList();
  }
}
