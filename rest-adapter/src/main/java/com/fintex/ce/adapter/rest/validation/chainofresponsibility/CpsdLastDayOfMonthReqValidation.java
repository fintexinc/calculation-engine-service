package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.enumeration.ExceptionCode;

import java.time.LocalDate;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class CpsdLastDayOfMonthReqValidation extends LastDayOfMonthAbstractReqValidator {

  public CpsdLastDayOfMonthReqValidation(final LocalDate date) {
    super(date);
  }

  @Override
  protected void throwException() {
    throw ExceptionCode.ERR_RRC_CPSD_001.reqValidationError();
  }
}
