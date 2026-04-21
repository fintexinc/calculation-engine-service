package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.TopHoldingsMapper;
import com.fintex.ce.model.domain.calculation.holding.CommonTopHoldings;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.holding.HoldingIdentifiers;
import com.fintex.wm.commons.domain.holding.TopHoldings;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

/**
 * REST fetchers for Security Master holdings endpoints.
 */
@Configuration
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class HoldingsFetchers {

  @Bean
  SecurityDataFetcher<CommonTopHoldings> topHoldingsFetcher(
      SecurityMasterWebClient client, TopHoldingsMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.holdings.top}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<TopHoldings>>>() {}) {};
  }

  @Bean
  SecurityDataFetcher<HoldingIdentifiers> holdingIdentifiersFetcher(
      SecurityMasterWebClient client,
      @Value("${external-services.security-master.rest.endpoints.holdings.identifiers}") String endpointPath) {
    SecurityMasterResponseMapper<HoldingIdentifiers, HoldingIdentifiers> mapper = (response, holding) -> response;
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<HoldingIdentifiers>>>() {}) {};
  }
}
