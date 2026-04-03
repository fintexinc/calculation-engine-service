package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.mapper.AssetAllocationSecurityMasterMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.HoldingAssetAllocation;
import com.fintex.sm.model.domain.allocation.AssetAllocation;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

/**
 * Fetcher for Asset Allocation data from Security Master API.
 * Activated when external-services.security-master.api-type=rest (default).
 */
@Component
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class AssetAllocationSecurityMasterFetcher
    extends AbstractSecurityMasterFetcher<HoldingAssetAllocation, AssetAllocation> {

  private final String endpointPath;
  private final AssetAllocationSecurityMasterMapper mapper;

  public AssetAllocationSecurityMasterFetcher(
      SecurityMasterWebClient client,
      AssetAllocationSecurityMasterMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.asset}") String endpointPath) {
    super(client);
    this.mapper = mapper;
    this.endpointPath = endpointPath;
  }

  @Override
  protected String endpointPath() {
    return endpointPath;
  }

  @Override
  protected ParameterizedTypeReference<List<SecurityAttributeResult<AssetAllocation>>> responseType() {
    return new ParameterizedTypeReference<>() {};
  }

  @Override
  protected SecurityMasterResponseMapper<HoldingAssetAllocation, AssetAllocation> responseMapper() {
    return mapper;
  }
}
