package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.config.enumeration.ExceptionCode;
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
