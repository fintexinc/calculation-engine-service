package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.holding.Holding;

/**
 * Interface for mapping Security Master API responses to domain models.
 *
 * @param <DomainModel> the domain model type (e.g., AssetAllocation)
 * @param <SecurityMasterResponse> the Security Master API response type
 */
@FunctionalInterface
public interface SecurityMasterResponseMapper<DomainModel, SecurityMasterResponse> {

  /**
   * Maps a Security Master API response to a domain model.
   *
   * @param response the response from Security Master API
   * @param holding the holding context for the mapping
   * @return the mapped domain model
   */
  DomainModel map(SecurityMasterResponse response, Holding holding);
}
