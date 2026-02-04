package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.enumeration.Period;
import com.fintex.ce.domain.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
public class PeriodsNotContainingSincePerformanceStartDateReqValidation
    extends
      PeriodsNotContainingAbstractReqValidation {

  public PeriodsNotContainingSincePerformanceStartDateReqValidation(Set<String> periods) {
    super(periods);
  }

  @Override
  public Period getNotAllowedPeriod() {
    return Period.SINCE_PERFORMANCE_START_DATE;
  }

  @Override
  public void throwException() {
    throw ExceptionCode.ERR_RRC_TIP_007.reqValidationError();
  }
}
