package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.mapper.EquityCountryAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.sm.model.domain.allocation.CountryAllocation;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EquityCountryAllocationFetcherTest {

  private static final String ENDPOINT_PATH = "/api/v1/wealth/securities/allocations/equity-country";

  private final SecurityMasterWebClient client = mock(SecurityMasterWebClient.class);
  private final EquityCountryAllocationMapper mapper = mock(EquityCountryAllocationMapper.class);
  private final EquityCountryAllocationFetcher sut =
      new EquityCountryAllocationFetcher(client, mapper, ENDPOINT_PATH);

  @Test
  void shouldReturnConfiguredEndpointPath() {
    assertThat(sut.endpointPath()).isEqualTo(ENDPOINT_PATH);
  }

  @Test
  void shouldReturnCorrectResponseType() {
    ParameterizedTypeReference<List<SecurityAttributeResult<CountryAllocation>>> responseType = sut.responseType();
    assertThat(responseType).isNotNull();
  }

  @Test
  void shouldReturnInjectedMapper() {
    SecurityMasterResponseMapper<EquityCountryAllocation, CountryAllocation> responseMapper = sut.responseMapper();
    assertThat(responseMapper).isSameAs(mapper);
  }
}