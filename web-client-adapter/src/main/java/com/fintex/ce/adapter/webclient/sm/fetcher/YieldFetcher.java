package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.YieldMapper;
import com.fintex.ce.domain.model.Yield;
import com.fintex.sm.model.domain.datapoint.Income;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

/**
 * Fetcher for Yield data from Security Master API. Uses the /income endpoint which returns dividend yield information.
 * Activated when external-services.security-master.api-type=rest (default).
 */
@Component
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class YieldFetcher extends AbstractSecurityMasterFetcher<Yield, Income> {

  private final String endpointPath;
  private final YieldMapper mapper;

  public YieldFetcher(
          SecurityMasterWebClient client,
          YieldMapper mapper,
          @Value("${external-services.security-master.rest.endpoints.income}") String endpointPath) {
    super(client);
    this.mapper = mapper;
    this.endpointPath = endpointPath;
  }

  @Override
  protected String endpointPath() {
    return endpointPath;
  }

  @Override
  protected ParameterizedTypeReference<List<SecurityAttributeResult<Income>>> responseType() {
    return new ParameterizedTypeReference<>() {
    };
  }

  @Override
  protected SecurityMasterResponseMapper<Yield, Income> responseMapper() {
    return mapper;
  }
}
