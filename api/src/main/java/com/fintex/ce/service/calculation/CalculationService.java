package com.fintex.ce.service.calculation;

/**
 * Base interface for simple calculation services. Used for services that follow the pattern: perform(request) ->
 * response
 *
 * @param <E>
 *          The response type
 * @param <R>
 *          The request type
 */
public interface CalculationService<E, R> {

  /**
   * Performs the calculation.
   *
   * @param command
   *          the command containing input parameters
   * @return the calculation result
   */
  E perform(R command);

}
