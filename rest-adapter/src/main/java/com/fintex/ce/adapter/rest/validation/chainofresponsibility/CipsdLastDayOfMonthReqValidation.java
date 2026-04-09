package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.enumeration.ExceptionCode;

import java.time.LocalDate;
import lombok.EqualsAndHashCode;

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
