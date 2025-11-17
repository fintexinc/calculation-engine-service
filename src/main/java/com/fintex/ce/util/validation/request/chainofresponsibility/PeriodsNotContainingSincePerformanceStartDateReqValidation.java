package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.config.enumeration.Period;
import com.fintex.ce.config.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
public class PeriodsNotContainingSincePerformanceStartDateReqValidation extends PeriodsNotContainingAbstractReqValidation {

    public PeriodsNotContainingSincePerformanceStartDateReqValidation(Set<String> periods) {
        super(periods);
    }

    @Override
    public Period getNotAllowedPeriod() {
        return Period.SINCE_PERFORMANCE_START_DATE;
    }

    @Override
    public void throwException() {
        throw ExceptionCode.ERR_RRC_TIP_007.reqValidationError();
    }
}
