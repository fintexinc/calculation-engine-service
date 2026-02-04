package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.enumeration.Period;
import com.fintex.ce.domain.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
public class PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidation
    extends
      PeriodsNotContainingAbstractReqValidation {

  public PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidation(Set<String> periods) {
    super(periods);
  }

  @Override
  public Period getNotAllowedPeriod() {
    return Period.SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE;
  }

  @Override
  public void throwException() {
    throw ExceptionCode.ERR_RRC_TIP_008.reqValidationError();
  }
}
