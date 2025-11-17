package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.config.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

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
