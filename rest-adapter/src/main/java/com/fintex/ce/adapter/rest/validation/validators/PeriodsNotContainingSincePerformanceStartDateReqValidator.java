package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.exception.code.ErrorCode;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.enumeration.Period;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fintex.ce.domain.model.enumeration.CalculationMetric.*;

@Component
@Order(321)
public class PeriodsNotContainingSincePerformanceStartDateReqValidator
    extends
      AbstractPeriodsNotContainingReqValidator {

  public PeriodsNotContainingSincePerformanceStartDateReqValidator() {
    super(Period.SINCE_PERFORMANCE_START_DATE, ErrorCode.ERR_RRC_TIP_007);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return CalculationMetric.ROLLING_AND_LEADING_METRICS;
  }
}
