package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class NotIncludeCpedReqValidation extends NotIncludePropertyAbstractReqValidation {
  public NotIncludeCpedReqValidation(Object property) {
    super(property);
  }

  @Override
  public void throwException() {
    throw ExceptionCode.ERR_RRC_TIP_006.reqValidationError();
  }
}
