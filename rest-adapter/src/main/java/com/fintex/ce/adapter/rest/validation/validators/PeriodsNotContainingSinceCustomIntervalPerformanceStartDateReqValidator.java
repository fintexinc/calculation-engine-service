package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.exception.code.ErrorCode;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.enumeration.Period;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fintex.ce.domain.model.enumeration.CalculationMetric.*;

@Component
@Order(322)
public class PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidator
    extends
      AbstractPeriodsNotContainingReqValidator {

  public PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidator() {
    super(Period.SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE, ErrorCode.ERR_RRC_TIP_008);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return CalculationMetric.ROLLING_AND_LEADING_METRICS;
  }
}
