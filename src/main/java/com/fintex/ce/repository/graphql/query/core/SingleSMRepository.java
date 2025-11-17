package com.fintex.ce.repository.graphql.query.core;

import com.fintex.ce.config.enumeration.HoldingIdentifierType;
import com.fintex.ce.dto.holding.EtfHolding;

/**
 * @param <F> Fund Canada
 * @param <C> ETF Canada
 * @param <U> ETF US
 */
public interface SingleSMRepository<F, C, U> {

    /**
     * For only for CANADA_MUTUAL_FUND
     *
     * @return mapped object
     */
    default F queryFundCanada(final HoldingIdentifierType identifier, final String id) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * Could work for both Canada ETF & US ETF
     *
     * @return mapped object
     */
    default C queryEtf(final EtfHolding holding) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    default C queryEtfCanada(final EtfHolding holding) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    default U queryEtfUs(final EtfHolding holding) {
        throw new UnsupportedOperationException("Method not implemented");
    }

}

