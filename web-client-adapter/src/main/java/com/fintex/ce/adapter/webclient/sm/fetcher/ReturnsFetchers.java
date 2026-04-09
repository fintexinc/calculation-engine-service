package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.mapper.MonthlyReturnsMapper;
import com.fintex.ce.domain.model.HoldingMonthlyReturns;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.sm.model.domain.performance.MonthlyReturns;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

/**
 * REST fetchers for Security Master returns endpoints (/returns/*).
 */
@Configuration
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class ReturnsFetchers {

  @Bean
  SecurityDataFetcher<HoldingMonthlyReturns> monthlyReturnsFetcher(
      SecurityMasterWebClient client, MonthlyReturnsMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.returns.monthly}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<MonthlyReturns>>>() {}) {};
  }
}