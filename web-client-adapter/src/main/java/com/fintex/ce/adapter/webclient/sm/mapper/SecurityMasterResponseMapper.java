package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.holding.Holding;

/**
 * Interface for mapping Security Master API responses to domain models.
 *
 * @param <D> the domain model type (e.g., AssetAllocation)
 * @param <R> the Security Master API response type
 */
@FunctionalInterface
public interface SecurityMasterResponseMapper<D, R> {

  /**
   * Maps a Security Master API response to a domain model.
   *
   * @param response the response from Security Master API
   * @param holding the holding context for the mapping
   * @return the mapped domain model
   */
  D map(R response, Holding holding);
}