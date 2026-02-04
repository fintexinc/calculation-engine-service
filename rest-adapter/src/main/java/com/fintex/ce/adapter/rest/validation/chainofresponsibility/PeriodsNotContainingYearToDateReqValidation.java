package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.enumeration.Period;
import com.fintex.ce.domain.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
public class PeriodsNotContainingYearToDateReqValidation extends PeriodsNotContainingAbstractReqValidation {

  public PeriodsNotContainingYearToDateReqValidation(Set<String> periods) {
    super(periods);
  }

  @Override
  public Period getNotAllowedPeriod() {
    return Period.YEAR_TO_DATE;
  }

  @Override
  public void throwException() {
    throw ExceptionCode.ERR_RRC_TIP_002.reqValidationError();
  }
}
