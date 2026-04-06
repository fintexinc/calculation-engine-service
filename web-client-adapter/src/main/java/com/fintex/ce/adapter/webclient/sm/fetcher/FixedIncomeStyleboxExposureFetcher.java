package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.mapper.FixedIncomeStyleboxExposureMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.FixedIncomeStyleboxExposure;
import com.fintex.sm.model.domain.rating.FixedIncomeStyleBoxes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fetcher for Fixed Income Stylebox exposure data from Security Master API.
 * Activated when external-services.security-master.api-type=rest (default).
 */
@Component
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class FixedIncomeStyleboxExposureFetcher
    extends AbstractSecurityMasterFetcher<FixedIncomeStyleboxExposure, FixedIncomeStyleBoxes> {

  private final String endpointPath;
  private final FixedIncomeStyleboxExposureMapper mapper;

  public FixedIncomeStyleboxExposureFetcher(
      SecurityMasterWebClient client,
      FixedIncomeStyleboxExposureMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.fixed-income-stylebox}") String endpointPath) {
    super(client);
    this.mapper = mapper;
    this.endpointPath = endpointPath;
  }

  @Override
  protected String endpointPath() {
    return endpointPath;
  }

  @Override
  protected ParameterizedTypeReference<List<SecurityAttributeResult<FixedIncomeStyleBoxes>>> responseType() {
    return new ParameterizedTypeReference<>() {};
  }

  @Override
  protected SecurityMasterResponseMapper<FixedIncomeStyleboxExposure, FixedIncomeStyleBoxes> responseMapper() {
    return mapper;
  }
}
