package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.mapper.FeesMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.FeeData;
import com.fintex.sm.model.domain.datapoint.Fees;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FeesFetcherTest {

  private static final String ENDPOINT_PATH = "/api/v1/wealth/securities/fees";

  private final SecurityMasterWebClient client = mock(SecurityMasterWebClient.class);
  private final FeesMapper mapper = mock(FeesMapper.class);
  private final FeesFetcher sut = new FeesFetcher(client, mapper, ENDPOINT_PATH);

  @Test
  void shouldReturnCorrectResponseType() {
    ParameterizedTypeReference<List<SecurityAttributeResult<Fees>>> responseType = sut.responseType();
    assertThat(responseType).isNotNull();
  }

  @Test
  void shouldReturnInjectedMapper() {
    SecurityMasterResponseMapper<FeeData, Fees> responseMapper = sut.responseMapper();
    assertThat(responseMapper).isSameAs(mapper);
  }
}
