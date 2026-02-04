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
 * @param <S>
 *          Stock domain model type
 */
public interface MultipleSMRepository<F, C, U, S>
    extends
      com.fintex.ce.port.output.graphql.MultipleSMRepository<F, C, U, S> {

  // Inherits all methods from the port interface
  // Add adapter-specific methods here if needed

}
