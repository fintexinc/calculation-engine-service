package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.mapper.FeesMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.FeeData;
import com.fintex.sm.model.domain.datapoint.Fees;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

/**
 * Fetcher for Fees data from Security Master API. Returns combined fee data including management fee, MER, and expense
 * ratios. Activated when external-services.security-master.api-type=rest (default).
 */
@Component
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class FeesFetcher extends AbstractSecurityMasterFetcher<FeeData, Fees> {

  private final String endpointPath;
  private final FeesMapper mapper;

  public FeesFetcher(
          SecurityMasterWebClient client,
          FeesMapper mapper,
          @Value("${external-services.security-master.rest.endpoints.fees}") String endpointPath) {
    super(client);
    this.mapper = mapper;
    this.endpointPath = endpointPath;
  }

  @Override
  protected String endpointPath() {
    return endpointPath;
  }

  @Override
  protected ParameterizedTypeReference<List<SecurityAttributeResult<Fees>>> responseType() {
    return new ParameterizedTypeReference<>() {
    };
  }

  @Override
  protected SecurityMasterResponseMapper<FeeData, Fees> responseMapper() {
    return mapper;
  }
}
