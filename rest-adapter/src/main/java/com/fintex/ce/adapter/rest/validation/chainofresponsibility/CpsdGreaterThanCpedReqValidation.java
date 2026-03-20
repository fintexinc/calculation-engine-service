package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
public class CpsdGreaterThanCpedReqValidation extends DateGreaterThanDateAbstractReqValidation {

  public CpsdGreaterThanCpedReqValidation(final LocalDate firstDate, final LocalDate secondDate) {
    super(firstDate, secondDate);
  }

  @Override
  protected void throwException() {
    throw ExceptionCode.ERR_RRC_CPSD_004.reqValidationError();
  }
}
