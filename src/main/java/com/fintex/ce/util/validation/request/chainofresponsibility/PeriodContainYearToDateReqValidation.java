package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.config.enumeration.ExceptionCode;
import com.fintex.ce.config.enumeration.Period;
import lombok.EqualsAndHashCode;
import org.springframework.util.CollectionUtils;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
public class PeriodContainYearToDateReqValidation extends ReqValidation {

    private final Set<String> periods;

    public PeriodContainYearToDateReqValidation(final Set<String> periods) {
        this.periods = periods;
    }

    @Override
    public void check() {
        if (CollectionUtils.isEmpty(periods)) {
            return;
        }
        for (final String period : periods) {
            if (Period.YEAR_TO_DATE.name().equalsIgnoreCase(period)) {
                throw ExceptionCode.ERR_RRC_TIP_002.reqValidationError();
            }
        }
    }
}
