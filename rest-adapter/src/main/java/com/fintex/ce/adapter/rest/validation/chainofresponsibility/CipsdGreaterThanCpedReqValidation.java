package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.enumeration.ExceptionCode;

import java.time.LocalDate;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class CipsdGreaterThanCpedReqValidation extends DateGreaterThanDateAbstractReqValidation {

  public CipsdGreaterThanCpedReqValidation(final LocalDate firstDate, final LocalDate secondDate) {
    super(firstDate, secondDate);
  }

  @Override
  protected void throwException() {
    throw ExceptionCode.ERR_RRC_CIPSD_002.reqValidationError();
  }
}
