package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
public class CpedLastDayOfMonthReqValidation extends LastDayOfMonthAbstractReqValidator {

  public CpedLastDayOfMonthReqValidation(final LocalDate date) {
    super(date);
  }

  @Override
  protected void throwException() {
    throw ExceptionCode.ERR_RRC_CPED_001.reqValidationError();
  }
}
