package com.fintex.ce.adapter.webclient.mic.mapper;

import com.fintex.ce.model.domain.holding.PortfolioHolding;

/**
 * Interface for mapping Market Investment Catalogue API responses to domain models.
 *
 * @param <D>
 *          the domain model type (e.g., AssetAllocation)
 * @param <R>
 *          the Market Investment Catalogue API response type
 */
@FunctionalInterface
public interface MarketInvestmentCatalogueResponseMapper<D, R> {

  /**
   * Maps a Market Investment Catalogue API response to a domain model.
   *
   * @param response
   *          the response from Market Investment Catalogue API
   * @param holding
   *          the holding context for the mapping
   * @return the mapped domain model
   */
  D map(R response, PortfolioHolding holding);
}