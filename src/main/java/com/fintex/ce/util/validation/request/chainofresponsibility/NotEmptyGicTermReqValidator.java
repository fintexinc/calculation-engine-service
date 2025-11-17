package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.dto.holding.GicHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.config.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import java.util.List;

import static java.util.Objects.isNull;

@EqualsAndHashCode(callSuper = true)
public class NotEmptyGicTermReqValidator extends ReqValidation {

    private final List<Holding> holdings;

    public NotEmptyGicTermReqValidator(final List<Holding> holdings) {
        this.holdings = holdings;
    }

    @Override
    protected void check() {
        for (final Holding holding : holdings) {
            if (holding instanceof GicHolding) {
                final var gic = (GicHolding) holding;
                if (isNull(gic.getTerm())) {
                    throw ExceptionCode.ERR_GIC_MC_002.reqValidationError();
                }
            }
        }
    }
}
