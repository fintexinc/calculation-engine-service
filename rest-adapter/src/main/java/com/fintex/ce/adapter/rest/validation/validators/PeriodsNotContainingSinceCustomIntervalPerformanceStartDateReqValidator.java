package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.Period;
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(322)
public class PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidator
    extends
      AbstractPeriodsNotContainingReqValidator {

  public PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidator() {
    super(Period.SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE, ErrorCode.TIME_INTERVAL_PERIOD_CONTAINS_SINCE_CIPSD);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return CalculationMetric.ROLLING_AND_LEADING_METRICS;
  }
}
