package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.TopHoldingsMapper;
import com.fintex.ce.model.domain.calculation.holding.CommonTopHoldings;
import com.fintex.ce.model.domain.calculation.holding.CommonTopHoldings.CommonTopHolding;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.holding.TopHolding;
import com.fintex.wm.commons.domain.holding.TopHoldings;

import org.springframework.core.ParameterizedTypeReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class HoldingsFetcherTest extends AbstractSecurityMasterFetcherTest<CommonTopHoldings, TopHoldings> {

  private static final String ENDPOINT_PATH = "/api/v1/wealth/securities/top-holdings";

  @Mock
  private TopHoldingsMapper mapper;

  private AbstractSecurityMasterFetcher<CommonTopHoldings, TopHoldings> fetcher;

  @BeforeEach
  void setUp() {
    fetcher = new AbstractSecurityMasterFetcher<>(securityMasterWebClient, ENDPOINT_PATH, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<TopHoldings>>>() {}) {};
  }

  @Override
  protected AbstractSecurityMasterFetcher<CommonTopHoldings, TopHoldings> fetcher() {
    return fetcher;
  }

  @Override
  protected String expectedEndpointPath() {
    return ENDPOINT_PATH;
  }

  @Override
  protected TopHoldings createSmsResponse() {
    var topHolding = new TopHolding();
    topHolding.setCompanyName("NVIDIA Corp");
    topHolding.setType("E");
    topHolding.setWeighting(new BigDecimal("10.5"));
    topHolding.setMarketValue(new BigDecimal("1000000"));

    var response = new TopHoldings();
    response.setAllocation(List.of(topHolding));
    return response;
  }

  @Override
  protected CommonTopHoldings createExpectedDomainModel() {
    var ch = new CommonTopHolding();
    ch.setCompanyName("NVIDIA Corp");
    ch.setType("E");
    ch.setWeight(new BigDecimal("10.5"));
    ch.setValue(new BigDecimal("1000000"));

    return CommonTopHoldings.builder()
        .holdings(List.of(ch))
        .build();
  }

  @Override
  protected SecurityMasterResponseMapper<CommonTopHoldings, TopHoldings> mapper() {
    return mapper;
  }
}
