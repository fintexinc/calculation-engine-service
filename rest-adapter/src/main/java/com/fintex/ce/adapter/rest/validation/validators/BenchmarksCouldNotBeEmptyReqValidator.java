package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.dto.command.BenchmarkHoldingsProvider;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fintex.ce.domain.model.enumeration.CalculationMetric.*;

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
