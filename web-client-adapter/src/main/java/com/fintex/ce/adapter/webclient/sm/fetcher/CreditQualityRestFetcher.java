package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.mapper.CreditQualityMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.sm.model.domain.rating.CreditQualityRatings;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class CreditQualityRestFetcher
    extends AbstractSecurityMasterFetcher<CreditQuality, CreditQualityRatings> {

  private final String endpointPath;
  private final CreditQualityMapper mapper;

  public CreditQualityRestFetcher(
      SecurityMasterWebClient client,
      CreditQualityMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.credit-quality}") String endpointPath) {
    super(client);
    this.mapper = mapper;
    this.endpointPath = endpointPath;
  }

  @Override
  protected String endpointPath() {
    return endpointPath;
  }

  @Override
  protected ParameterizedTypeReference<List<SecurityAttributeResult<CreditQualityRatings>>> responseType() {
    return new ParameterizedTypeReference<>() {};
  }

  @Override
  protected SecurityMasterResponseMapper<CreditQuality, CreditQualityRatings> responseMapper() {
    return mapper;
  }
}