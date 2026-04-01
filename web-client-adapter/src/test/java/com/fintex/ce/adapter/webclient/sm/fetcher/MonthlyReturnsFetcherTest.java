package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.mapper.MonthlyReturnsMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.HoldingMonthlyReturns;
import com.fintex.sm.model.domain.performance.MonthlyReturns;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MonthlyReturnsFetcherTest {

  private static final String ENDPOINT_PATH = "/api/v1/wealth/securities/returns/monthly";

  private final SecurityMasterWebClient client = mock(SecurityMasterWebClient.class);
  private final MonthlyReturnsMapper mapper = mock(MonthlyReturnsMapper.class);
  private final MonthlyReturnsFetcher fetcher = new MonthlyReturnsFetcher(client, mapper, ENDPOINT_PATH);

  @Test
  void shouldReturnCorrectResponseType() {
    ParameterizedTypeReference<List<SecurityAttributeResult<MonthlyReturns>>> responseType = fetcher.responseType();
    assertThat(responseType).isNotNull();
  }

  @Test
  void shouldReturnInjectedMapper() {
    SecurityMasterResponseMapper<HoldingMonthlyReturns, MonthlyReturns> responseMapper = fetcher.responseMapper();
    assertThat(responseMapper).isSameAs(mapper);
  }
}