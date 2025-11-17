package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.dto.holding.Holding;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
public class HoldingsCouldNotBeEmptyReqValidation extends HoldingsCouldNotBeEmptyAbstractReqValidation {
    public HoldingsCouldNotBeEmptyReqValidation(final List<Holding> holdings) {
        super(holdings);
    }

    @Override
    protected String getMessage() {
        return "Holdings could not be empty";
    }
}
