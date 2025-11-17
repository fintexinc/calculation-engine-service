package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.config.enumeration.Period;
import lombok.EqualsAndHashCode;
import org.springframework.util.CollectionUtils;

import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNumeric;

@EqualsAndHashCode(callSuper = true)
public abstract class PeriodsNotContainingAbstractReqValidation extends ReqValidation {

    private final Set<String> periods;

    public PeriodsNotContainingAbstractReqValidation(final Set<String> periods) {
        this.periods = periods;
    }

    @Override
    public void check() {
        if (CollectionUtils.isEmpty(periods)) {
            return;
        }
        for (final String period : periods) {
            if (!isNumeric(period) && period.equals(getNotAllowedPeriod().name())) {
                throwException();
            }
        }
    }

    public abstract Period getNotAllowedPeriod();

    public abstract void throwException();
}
