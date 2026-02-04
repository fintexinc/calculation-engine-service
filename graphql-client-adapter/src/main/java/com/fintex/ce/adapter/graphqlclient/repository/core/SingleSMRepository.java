package com.fintex.ce.adapter.graphqlclient.repository.core;

/**
 * Adapter-specific extension of the port interface. This interface extends the port interface from the api module.
 *
 * @param <F>
 *          Fund Canada domain model type
 * @param <C>
 *          ETF Canada domain model type
 * @param <U>
 *          ETF US domain model type
 */
public interface SingleSMRepository<F, C, U>
    extends
      com.fintex.ce.port.output.graphql.SingleSMRepository<F, C, U> {

  // Inherits all methods from the port interface
  // Add adapter-specific methods here if needed

}
