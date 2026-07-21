package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.dto.command.contract.BenchmarkHoldingsProvider;

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
        HoldingsValidationHelper::validate, "benchmarkHoldings", true);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return CalculationMetric.BENCHMARK_METRICS;
  }
}
