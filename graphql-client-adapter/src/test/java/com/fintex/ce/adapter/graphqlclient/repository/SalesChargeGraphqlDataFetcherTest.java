package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.ce.adapter.graphqlclient.endpoint.SalesChargeEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.SalesChargeGraphqlDataFetcher;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.SalesCharge;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.smclient.service.GraphqlTransportComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SalesChargeGraphqlDataFetcherTest {

  @Test
  void queryBenchOfFundCanada_verifyDoQuery() {
    // SETUP
    final var graphqlTransport = mock(GraphqlTransportComponent.class);
    final SalesChargeGraphqlDataFetcher sut = mock(SalesChargeGraphqlDataFetcher.class, withSettings().useConstructor(
        graphqlTransport));
    final List<FundSeriesHolding> holdings = List.of(mock(FundSeriesHolding.class));
    final List<DataProvider> providers = mock(List.class);

    doCallRealMethod().when(sut).queryBenchOfFundCanada(any(), anyList());

    // ACT
    sut.queryBenchOfFundCanada(holdings, providers);

    // VERIFY
    verify(sut).doQuery(eq(holdings), argThat(argument -> argument.getClass() == SalesChargeEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfStock_checkResult() {
    // SETUP
    final SalesChargeGraphqlDataFetcher sut = mock(SalesChargeGraphqlDataFetcher.class);
    final List<FundSeriesHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(sut.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = mock(List.class);

    doCallRealMethod().when(sut).queryBenchOfFundCanada(any(), anyList());
    // ACT
    final Map<FundSeriesHolding, SalesCharge> actual = sut.queryBenchOfFundCanada(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

}
