package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.mapper.EquityStyleboxExposureMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.sm.model.domain.rating.StyleBoxes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fetcher for Equity Stylebox Exposure data from Security Master REST API.
 * Activated when external-services.security-master.api-type=rest (default).
 */
@Component
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class EquityStyleboxExposureFetcher
    extends AbstractSecurityMasterFetcher<EquityStyleboxExposure, StyleBoxes> {

  private final String endpointPath;
  private final EquityStyleboxExposureMapper mapper;

  public EquityStyleboxExposureFetcher(
      SecurityMasterWebClient client,
      EquityStyleboxExposureMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.equity-stylebox}") String endpointPath) {
    super(client);
    this.mapper = mapper;
    this.endpointPath = endpointPath;
  }

  @Override
  protected String endpointPath() {
    return endpointPath;
  }

  @Override
  protected ParameterizedTypeReference<List<SecurityAttributeResult<StyleBoxes>>> responseType() {
    return new ParameterizedTypeReference<>() {};
  }

  @Override
  protected SecurityMasterResponseMapper<EquityStyleboxExposure, StyleBoxes> responseMapper() {
    return mapper;
  }
}
