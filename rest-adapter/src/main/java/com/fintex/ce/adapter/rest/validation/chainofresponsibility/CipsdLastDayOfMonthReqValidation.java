package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
public class CipsdLastDayOfMonthReqValidation extends LastDayOfMonthAbstractReqValidator {
  public CipsdLastDayOfMonthReqValidation(LocalDate date) {
    super(date);
  }

  @Override
  protected void throwException() {
    throw ExceptionCode.ERR_RRC_CIPSD_001.reqValidationError();
  }
}
