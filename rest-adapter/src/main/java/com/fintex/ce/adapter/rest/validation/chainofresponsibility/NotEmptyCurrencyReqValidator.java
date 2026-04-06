package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import lombok.EqualsAndHashCode;

import static java.util.Objects.isNull;

@EqualsAndHashCode(callSuper = true)
public class NotEmptyCurrencyReqValidator extends ReqValidation {

  private final CurrencyType currency;

  public NotEmptyCurrencyReqValidator(final CurrencyType currency) {
    this.currency = currency;
  }

  @Override
  protected void check() {
    if (isNull(currency)) {
      throw ExceptionCode.ERR_RRC_MC_001.reqValidationError();
    }
  }
}
