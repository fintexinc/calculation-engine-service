package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.mapper.FixedIncomeSectorAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.FixedIncomeBondSecurities;
import com.fintex.sm.model.domain.allocation.FixedIncomeSectorAllocation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fetcher for Fixed Income Sector allocation data from Security Master API.
 * Activated when external-services.security-master.api-type=rest (default).
 */
@Component
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class FixedIncomeBondSecuritiesFetcher
    extends AbstractSecurityMasterFetcher<FixedIncomeBondSecurities, FixedIncomeSectorAllocation> {

  private final String endpointPath;
  private final FixedIncomeSectorAllocationMapper mapper;

  public FixedIncomeBondSecuritiesFetcher(
      SecurityMasterWebClient client,
      FixedIncomeSectorAllocationMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.fixed-income-sector}") String endpointPath) {
    super(client);
    this.mapper = mapper;
    this.endpointPath = endpointPath;
  }

  @Override
  protected String endpointPath() {
    return endpointPath;
  }

  @Override
  protected ParameterizedTypeReference<List<SecurityAttributeResult<FixedIncomeSectorAllocation>>> responseType() {
    return new ParameterizedTypeReference<>() {};
  }

  @Override
  protected SecurityMasterResponseMapper<FixedIncomeBondSecurities, FixedIncomeSectorAllocation> responseMapper() {
    return mapper;
  }
}