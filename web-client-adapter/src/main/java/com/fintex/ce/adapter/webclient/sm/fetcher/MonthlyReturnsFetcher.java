package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.mapper.MonthlyReturnsMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.HoldingMonthlyReturns;
import com.fintex.sm.model.domain.performance.MonthlyReturns;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fetcher for Monthly Returns data from Security Master REST API.
 * Activated when external-services.security-master.api-type=rest (default).
 */
@Component
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class MonthlyReturnsFetcher
    extends AbstractSecurityMasterFetcher<HoldingMonthlyReturns, MonthlyReturns> {

  private final String endpointPath;
  private final MonthlyReturnsMapper mapper;

  public MonthlyReturnsFetcher(
      SecurityMasterWebClient client,
      MonthlyReturnsMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.returns.monthly}") String endpointPath) {
    super(client);
    this.mapper = mapper;
    this.endpointPath = endpointPath;
  }

  @Override
  protected String endpointPath() {
    return endpointPath;
  }

  @Override
  protected ParameterizedTypeReference<List<SecurityAttributeResult<MonthlyReturns>>> responseType() {
    return new ParameterizedTypeReference<>() {};
  }

  @Override
  protected SecurityMasterResponseMapper<HoldingMonthlyReturns, MonthlyReturns> responseMapper() {
    return mapper;
  }
}