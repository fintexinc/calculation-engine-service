package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.holding.HoldingIdentifiers;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.springframework.core.ParameterizedTypeReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class HoldingIdentifiersFetcherTest
    extends
      AbstractSecurityMasterFetcherTest<HoldingIdentifiers, HoldingIdentifiers> {

  private static final String ENDPOINT_PATH = "/api/v1/wealth/securities/holdings/identifiers";

  @Mock
  private SecurityMasterResponseMapper<HoldingIdentifiers, HoldingIdentifiers> mapper;

  private AbstractSecurityMasterFetcher<HoldingIdentifiers, HoldingIdentifiers> fetcher;

  @BeforeEach
  void setUp() {
    fetcher = new AbstractSecurityMasterFetcher<>(securityMasterWebClient, ENDPOINT_PATH, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<HoldingIdentifiers>>>() {}) {};
  }

  @Override
  protected AbstractSecurityMasterFetcher<HoldingIdentifiers, HoldingIdentifiers> fetcher() {
    return fetcher;
  }

  @Override
  protected String expectedEndpointPath() {
    return ENDPOINT_PATH;
  }

  @Override
  protected HoldingIdentifiers createSmsResponse() {
    var id = new SecurityIdentifier();
    id.setId("AAPL");
    id.setIdType(FiIdentifierType.MORNINGSTAR_ID);

    var response = new HoldingIdentifiers();
    response.setHoldingIds(List.of(id));
    return response;
  }

  @Override
  protected HoldingIdentifiers createExpectedDomainModel(String holdingId) {
    return createSmsResponse();
  }

  @Override
  protected SecurityMasterResponseMapper<HoldingIdentifiers, HoldingIdentifiers> mapper() {
    return mapper;
  }
}
