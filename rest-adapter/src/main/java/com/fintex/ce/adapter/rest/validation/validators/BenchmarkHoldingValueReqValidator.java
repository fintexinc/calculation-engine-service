package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.dto.command.BenchmarkHoldingsProvider;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fintex.ce.domain.model.enumeration.CalculationMetric.*;

@Component
@Order(460)
public class BenchmarkHoldingValueReqValidator
    extends
      AbstractHoldingsValidationReqValidator<BenchmarkHoldingsProvider> {

  public BenchmarkHoldingValueReqValidator() {
    super(BenchmarkHoldingsProvider.class, BenchmarkHoldingsProvider::getBenchmarkHoldings,
        HoldingsValidationHelper::validateHoldingValues);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(
        EXCESS_RETURNS, TREYNOR_RATIO, INFORMATION_RATIO, TRACKING_ERROR, ALPHA,
        BETA, R_SQUARED, UPSIDE_CAPTURE, DOWNSIDE_CAPTURE);
  }
}
