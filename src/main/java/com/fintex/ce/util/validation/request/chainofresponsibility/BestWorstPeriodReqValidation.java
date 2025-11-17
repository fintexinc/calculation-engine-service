package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.config.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;
import org.springframework.util.CollectionUtils;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
public class BestWorstPeriodReqValidation extends ReqValidation {

    private final Set<Long> periods;

    public BestWorstPeriodReqValidation(final Set<Long> periods) {
        this.periods = periods;
    }

    @Override
    public void check() {
        if (CollectionUtils.isEmpty(periods)) {
            return;
        }
        for (Long period : periods) {
            if (period <= 0) {
                throw ExceptionCode.ERR_BWP_BWPTIP_001.reqValidationError();
            }
            if (period > 300) {
                throw ExceptionCode.ERR_BWP_BWPTIP_002.reqValidationError();
            }
        }
    }
}
