package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.config.enumeration.ExceptionCode;
import com.fintex.ce.dto.holding.CashHolding;
import com.fintex.ce.dto.holding.Holding;

import java.util.List;
import java.util.Objects;

public class NotNullCashCurrencyValidation extends ReqValidation {

    private final List<Holding> holdings;

    public NotNullCashCurrencyValidation(final List<Holding> holdings) {
        this.holdings = holdings;
    }

    /**
     * Validates if currency value for Cash Holding type is not null.
     * Note: this validation will be applied only if there is only one Cash Holding in request payload,
     * otherwise currencies can be validated using {@link HoldingReqValidation} class.
     */
    @Override
    protected void check() {
        final List<CashHolding> cashHoldings = getCashHoldings();

        if (cashHoldings.size() == 1 && containsNullCurrency(cashHoldings)) {
            throw ExceptionCode.ERR_RRC_MC_002.reqValidationError();
        }
    }

    private List<CashHolding> getCashHoldings() {
        return this.holdings
                .stream()
                .filter(h -> h instanceof CashHolding)
                .map(h -> (CashHolding) h)
                .toList();
    }

    private boolean containsNullCurrency(final List<CashHolding> cashHoldings) {
        return cashHoldings.stream().anyMatch(holding -> Objects.isNull(holding.getCurrency()));
    }

}
