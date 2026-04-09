package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.mapper.FeesMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SalesChargeMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.YieldMapper;
import com.fintex.ce.domain.model.FeeData;
import com.fintex.ce.domain.model.SalesCharge;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.sm.model.domain.datapoint.Fees;
import com.fintex.sm.model.domain.datapoint.Income;
import com.fintex.sm.model.domain.datapoint.SalesChargeData;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

/**
 * REST fetchers for Security Master financial data endpoints (fees, sales-charge, income).
 */
@Configuration
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class FinancialDataFetchers {

  @Bean
  SecurityDataFetcher<FeeData> feesFetcher(
      SecurityMasterWebClient client, FeesMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.fees}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<Fees>>>() {}) {};
  }

  @Bean
  SecurityDataFetcher<SalesCharge> salesChargeFetcher(
      SecurityMasterWebClient client, SalesChargeMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.sales-charge}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<SalesChargeData>>>() {}) {};
  }

  @Bean
  SecurityDataFetcher<Yield> yieldFetcher(
      SecurityMasterWebClient client, YieldMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.income}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<Income>>>() {}) {};
  }
}