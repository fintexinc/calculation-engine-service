package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.config.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
public class CpsdLastDayOfMonthReqValidation extends LastDayOfMonthAbstractReqValidator {

    public CpsdLastDayOfMonthReqValidation(final LocalDate date) {
        super(date);
    }

    @Override
    protected void throwException() {
        throw ExceptionCode.ERR_RRC_CPSD_001.reqValidationError();
    }
}
