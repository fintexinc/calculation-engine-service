package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.config.enumeration.ExceptionCode;
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
