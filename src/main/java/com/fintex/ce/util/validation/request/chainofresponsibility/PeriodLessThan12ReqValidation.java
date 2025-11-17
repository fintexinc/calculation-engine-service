package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.config.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
public class PeriodLessThan12ReqValidation extends ReqValidation {

    private final Set<String> periods;

    public PeriodLessThan12ReqValidation(final Set<String> periods) {
        this.periods = periods;
    }

    @Override
    public void check() {
        if (CollectionUtils.isEmpty(periods)) {
            return;
        }
        for (final String period : periods) {
            if (StringUtils.isNumeric(period) && Integer.parseInt(period) < 12) {
                throw ExceptionCode.ERR_RRC_TIP_001.reqValidationError();
            }
        }
    }
}
