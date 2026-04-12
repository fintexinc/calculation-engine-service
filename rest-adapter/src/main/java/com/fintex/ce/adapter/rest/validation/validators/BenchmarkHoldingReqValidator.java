package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.dto.command.BenchmarkHoldingsProvider;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(450)
public class BenchmarkHoldingReqValidator
    extends
      AbstractHoldingsValidationReqValidator<BenchmarkHoldingsProvider> {

  public BenchmarkHoldingReqValidator() {
    super(BenchmarkHoldingsProvider.class, BenchmarkHoldingsProvider::getBenchmarkHoldings,
        HoldingsValidationHelper::validate);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return CalculationMetric.BENCHMARK_METRICS;
  }
}
