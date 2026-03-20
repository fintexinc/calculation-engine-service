package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.mapper.EquityCountryAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.sm.model.domain.allocation.CountryAllocation;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class EquityCountryAllocationFetcher
    extends AbstractSecurityMasterFetcher<EquityCountryAllocation, CountryAllocation> {

  private final String endpointPath;
  private final EquityCountryAllocationMapper mapper;

  public EquityCountryAllocationFetcher(
      SecurityMasterWebClient client,
      EquityCountryAllocationMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.equity-country}") String endpointPath) {
    super(client);
    this.mapper = mapper;
    this.endpointPath = endpointPath;
  }

  @Override
  protected String endpointPath() {
    return endpointPath;
  }

  @Override
  protected ParameterizedTypeReference<List<SecurityAttributeResult<CountryAllocation>>> responseType() {
    return new ParameterizedTypeReference<>() {};
  }

  @Override
  protected SecurityMasterResponseMapper<EquityCountryAllocation, CountryAllocation> responseMapper() {
    return mapper;
  }
}
