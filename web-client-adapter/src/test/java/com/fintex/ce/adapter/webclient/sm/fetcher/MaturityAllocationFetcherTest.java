package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.mapper.MaturityAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.MaturityAllocation;
import com.fintex.sm.model.domain.datapoint.Maturities;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MaturityAllocationFetcherTest {

  private static final String ENDPOINT_PATH = "/api/v1/wealth/securities/allocations/maturities";

  private final SecurityMasterWebClient client = mock(SecurityMasterWebClient.class);
  private final MaturityAllocationMapper mapper = mock(MaturityAllocationMapper.class);
  private final MaturityAllocationFetcher fetcher = new MaturityAllocationFetcher(client, mapper, ENDPOINT_PATH);

  @Test
  void shouldReturnCorrectResponseType() {
    ParameterizedTypeReference<List<SecurityAttributeResult<Maturities>>> responseType = fetcher.responseType();
    assertThat(responseType).isNotNull();
  }

  @Test
  void shouldReturnInjectedMapper() {
    SecurityMasterResponseMapper<MaturityAllocation, Maturities> responseMapper = fetcher.responseMapper();
    assertThat(responseMapper).isSameAs(mapper);
  }
}
