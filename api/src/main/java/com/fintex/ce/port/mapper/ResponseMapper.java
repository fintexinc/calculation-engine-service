package com.fintex.ce.port.mapper;

import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.core.Warning;

import java.util.List;
import java.util.Map;

/**
 * Generic mapper interface for converting domain models to response DTOs. Used in hexagonal architecture to separate
 * domain logic from REST representation.
 *
 * @param <D>
 *          Domain model type
 * @param <R>
 *          Response DTO type
 */
public interface ResponseMapper<D, R> {

  /**
   * Converts a single domain model to response DTO.
   *
   * @param domain
   *          the domain model
   * @return the response DTO
   */
  R toResponse(D domain);

  /**
   * Converts a map of holdings to domain models into a response DTO. Typically performs aggregation/calculation across
   * all holdings.
   *
   * @param domainMap
   *          map of holdings to domain models
   * @param warnings
   *          list of warnings to include in response
   * @return the aggregated response DTO
   */
  R toResponse(Map<Holding, D> domainMap, List<Warning> warnings);

}
