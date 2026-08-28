package ca.tangerine.pce.rest.validation.validators;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.dto.command.contract.BenchmarkHoldingsProvider;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Requires and validates {@code benchmarkHoldings} for every metric that compares a portfolio against a benchmark — the
 * returns-based performance metrics and {@code mer-benchmark-comparison} alike. Without this guard a missing benchmark
 * reaches the calculation and fails with an unhandled server error instead of a validation response.
 */
@Component
@Order(450)
public class BenchmarkHoldingReqValidator
    extends
      AbstractHoldingsValidationReqValidator<BenchmarkHoldingsProvider> {

  static final String BENCHMARK_HOLDINGS_FIELD = "benchmarkHoldings";

  public BenchmarkHoldingReqValidator(HoldingsValidator holdingsValidator) {
    super(BenchmarkHoldingsProvider.class, BenchmarkHoldingsProvider::getBenchmarkHoldings,
        holdingsValidator::validate, BENCHMARK_HOLDINGS_FIELD, true);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return CalculationMetric.BENCHMARK_METRICS;
  }
}
