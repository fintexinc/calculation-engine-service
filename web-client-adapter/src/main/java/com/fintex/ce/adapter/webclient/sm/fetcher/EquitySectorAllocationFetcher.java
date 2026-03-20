package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.mapper.EquitySectorAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.sm.model.domain.allocation.EquitySectorAllocation;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

/**
 * Fetcher for Equity Sector allocation data from Security Master API.
 * Activated when external-services.security-master.api-type=rest (default).
 */
@Component
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class EquitySectorAllocationFetcher
    extends AbstractSecurityMasterFetcher<EquitySector, EquitySectorAllocation> {

  private final String endpointPath;
  private final EquitySectorAllocationMapper mapper;

  public EquitySectorAllocationFetcher(
      SecurityMasterWebClient client,
      EquitySectorAllocationMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.equity-sector}") String endpointPath) {
    super(client);
    this.mapper = mapper;
    this.endpointPath = endpointPath;
  }

  @Override
  protected String endpointPath() {
    return endpointPath;
  }

  @Override
  protected ParameterizedTypeReference<List<SecurityAttributeResult<EquitySectorAllocation>>> responseType() {
    return new ParameterizedTypeReference<>() {};
  }

  @Override
  protected SecurityMasterResponseMapper<EquitySector, EquitySectorAllocation> responseMapper() {
    return mapper;
  }
}