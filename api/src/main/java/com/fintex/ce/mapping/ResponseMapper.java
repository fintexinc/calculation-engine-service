package com.fintex.ce.mapping;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.error.Notification;

import java.util.List;
import java.util.Map;

/**
 * Generic mapper interface for converting domain models to results. Used in hexagonal architecture to separate domain
 * logic from REST representation.
 *
 * @param <D>
 *          Domain model type
 * @param <R>
 *          Result type
 */
public interface ResponseMapper<D, R> {

  /**
   * Converts a single domain model to result.
   *
   * @param domain
   *          the domain model
   * @return the result
   */
  R toResponse(D domain);

  /**
   * Converts a map of holdings to domain models into a result. Typically performs aggregation/calculation across all
   * holdings.
   *
   * @param domainMap
   *          map of holdings to domain models
   * @param warnings
   *          list of warnings to include in response
   * @return the aggregated result
   */
  R toResponse(Map<PortfolioHolding, D> domainMap, List<Notification> warnings);

}
