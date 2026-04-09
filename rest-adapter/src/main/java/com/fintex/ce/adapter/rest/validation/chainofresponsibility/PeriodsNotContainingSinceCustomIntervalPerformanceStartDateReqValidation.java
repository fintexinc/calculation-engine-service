package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import com.fintex.ce.domain.model.enumeration.Period;

import java.util.Set;
import lombok.EqualsAndHashCode;

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
