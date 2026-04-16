package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.dto.command.BenchmarkHoldingsProvider;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(115)
public class BenchmarksCouldNotBeEmptyReqValidator
    extends
      AbstractHoldingsNotEmptyReqValidator<BenchmarkHoldingsProvider> {

  public BenchmarksCouldNotBeEmptyReqValidator() {
    super(BenchmarkHoldingsProvider.class, BenchmarkHoldingsProvider::getBenchmarkHoldings,
        "Benchmarks should not be empty");
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return CalculationMetric.BENCHMARK_METRICS;
  }
}
