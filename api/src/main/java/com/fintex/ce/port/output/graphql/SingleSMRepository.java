package com.fintex.ce.port.output.graphql;

import com.fintex.ce.domain.enumeration.HoldingIdentifierType;
import com.fintex.ce.domain.model.holding.EtfHolding;

/**
 * Port interface for single SM (Morningstar) entity queries.
 *
 * @param <F>
 *          Fund Canada domain model type
 * @param <C>
 *          ETF Canada domain model type
 * @param <U>
 *          ETF US domain model type
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
