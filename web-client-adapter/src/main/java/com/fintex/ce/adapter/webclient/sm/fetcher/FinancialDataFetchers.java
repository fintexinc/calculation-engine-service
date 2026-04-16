package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.mapper.FeesMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SalesChargeMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.YieldMapper;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.calculation.fee.SalesCharge;
import com.fintex.ce.model.domain.calculation.yield.Yield;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.financial.Fees;
import com.fintex.wm.commons.domain.financial.Income;
import com.fintex.wm.commons.domain.sales.SalesChargeData;

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