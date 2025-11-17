package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.config.enumeration.ExceptionCode;
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
