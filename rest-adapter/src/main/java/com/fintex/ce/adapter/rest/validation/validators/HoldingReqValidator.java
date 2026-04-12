package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.dto.command.HoldingsProvider;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Order(400)
public class HoldingReqValidator
    extends
      AbstractHoldingsValidationReqValidator<HoldingsProvider> {

  public HoldingReqValidator() {
    super(HoldingsProvider.class, HoldingsProvider::getHoldings, HoldingsValidationHelper::validate);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return Arrays.stream(CalculationMetric.values())
        .filter(metric -> metric != CalculationMetric.COMMON_PERFORMANCE_DATES)
        .toList();
  }
}
