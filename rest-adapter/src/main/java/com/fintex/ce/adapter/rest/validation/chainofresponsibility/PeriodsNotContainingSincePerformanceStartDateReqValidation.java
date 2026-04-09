package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import com.fintex.ce.domain.model.enumeration.Period;

import java.util.Set;
import lombok.EqualsAndHashCode;

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
