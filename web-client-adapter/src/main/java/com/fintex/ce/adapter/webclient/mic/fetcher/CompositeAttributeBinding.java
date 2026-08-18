package com.fintex.ce.adapter.webclient.mic.fetcher;

import com.fintex.ce.adapter.webclient.mic.mapper.MarketInvestmentCatalogueResponseMapper;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Associates a {@link CompositeSecurityAttribute} with the MIC response type it deserializes into, the CE domain type
 * calculation services consume it as, and the {@link MarketInvestmentCatalogueResponseMapper} that performs the
 * conversion. The full set of bindings forms the attribute registry backing the generic security-attributes fetcher.
 *
 * @param <D>
 *          CE domain model type
 * @param <R>
 *          MIC API response type for the attribute
 */
public record CompositeAttributeBinding<D, R>(
    CompositeSecurityAttribute attribute,
    Class<R> responseType,
    Class<D> domainType,
    MarketInvestmentCatalogueResponseMapper<D, R> mapper) {

  public Map<PortfolioHolding, Object> mapResults(List<SecurityAttributeResult<JsonNode>> results,
      HoldingIdentifierIndex index, ObjectMapper objectMapper) {
    Map<PortfolioHolding, D> mapped = index.mapResponses(results,
        (data, holding) -> mapper.map(objectMapper.convertValue(data, responseType), holding));
    return new HashMap<>(mapped);
  }

}
