package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.mapper.MaturityAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.MaturityAllocation;
import com.fintex.sm.model.domain.datapoint.Maturities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fetcher for Maturity Allocation data from Security Master REST API.
 * Activated when external-services.security-master.api-type=rest (default).
 */
@Component
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class MaturityAllocationFetcher
    extends AbstractSecurityMasterFetcher<MaturityAllocation, Maturities> {

  private final String endpointPath;
  private final MaturityAllocationMapper mapper;

  public MaturityAllocationFetcher(
      SecurityMasterWebClient client,
      MaturityAllocationMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.maturities}") String endpointPath) {
    super(client);
    this.mapper = mapper;
    this.endpointPath = endpointPath;
  }

  @Override
  protected String endpointPath() {
    return endpointPath;
  }

  @Override
  protected ParameterizedTypeReference<List<SecurityAttributeResult<Maturities>>> responseType() {
    return new ParameterizedTypeReference<>() {};
  }

  @Override
  protected SecurityMasterResponseMapper<MaturityAllocation, Maturities> responseMapper() {
    return mapper;
  }
}
