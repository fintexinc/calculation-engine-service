package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.config.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;
import org.springframework.util.CollectionUtils;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
public class RollingPeriodsLessThan12ReqValidation extends ReqValidation {

    private final Set<String> periods;

    public RollingPeriodsLessThan12ReqValidation(final Set<String> periods) {
        this.periods = periods;
    }

    @Override
    public void check() {
        if (CollectionUtils.isEmpty(periods)) {
            return;
        }
        for (final var period : periods) {
            if (Long.parseLong(period) < 12) {
                throw ExceptionCode.ERR_RRC_RTIP_001.reqValidationError();
            }
        }
    }
}
