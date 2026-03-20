package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import static java.util.Objects.isNull;

@EqualsAndHashCode(callSuper = true)
public class NotEmptyCurrencyReqValidator extends ReqValidation {

  private final Currency currency;

  public NotEmptyCurrencyReqValidator(final Currency currency) {
    this.currency = currency;
  }

  @Override
  protected void check() {
    if (isNull(currency)) {
      throw ExceptionCode.ERR_RRC_MC_001.reqValidationError();
    }
  }
}
