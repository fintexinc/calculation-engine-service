package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.mapper.EquityMarketCapitalizationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.HoldingEquityMarketCap;
import com.fintex.sm.model.domain.datapoint.EquityMarketCapitalization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class EquityMarketCapitalizationFetcher
    extends AbstractSecurityMasterFetcher<HoldingEquityMarketCap, EquityMarketCapitalization> {

  private final String endpointPath;
  private final EquityMarketCapitalizationMapper mapper;

  public EquityMarketCapitalizationFetcher(
      SecurityMasterWebClient client,
      EquityMarketCapitalizationMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.equity-market-cap}") String endpointPath) {
    super(client);
    this.mapper = mapper;
    this.endpointPath = endpointPath;
  }

  @Override
  protected String endpointPath() {
    return endpointPath;
  }

  @Override
  protected ParameterizedTypeReference<List<SecurityAttributeResult<EquityMarketCapitalization>>> responseType() {
    return new ParameterizedTypeReference<>() {};
  }

  @Override
  protected SecurityMasterResponseMapper<HoldingEquityMarketCap, EquityMarketCapitalization> responseMapper() {
    return mapper;
  }
}
