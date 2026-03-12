package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.ce.adapter.graphqlclient.endpoint.averagemer.AverageMERCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.averagemer.AverageMEREtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.averagemer.AverageMEREtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.averagemer.AverageMERFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.averagemer.AverageMERUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.AverageMerGraphqlDataFetcher;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.AverageMer;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.smclient.service.GraphqlTransportComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class AverageMerGraphqlDataFetcherTest {

  @Test
  void queryBenchOfFundCanada_verifyDoQuery() {
    // SETUP
    final var graphqlTransport = mock(GraphqlTransportComponent.class);
    final AverageMerGraphqlDataFetcher a = mock(AverageMerGraphqlDataFetcher.class, withSettings().useConstructor(
        graphqlTransport));
    final List<FundSeriesHolding> holdings = List.of();

    doCallRealMethod().when(a).queryBenchOfFundCanada(any(), any());
    // ACT
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);
    a.queryBenchOfFundCanada(holdings, providers);

    // VERIFY
    verify(a).doQuery(
        eq(holdings),
        argThat(argument -> argument.getClass() == AverageMERFundCanadaEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfFundCanada_checkResult() {
    // SETUP
    final AverageMerGraphqlDataFetcher a = mock(AverageMerGraphqlDataFetcher.class);
    final List<FundSeriesHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(a.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(a).queryBenchOfFundCanada(any(), any());
    // ACT
    final Map<FundSeriesHolding, AverageMer> actual = a.queryBenchOfFundCanada(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfEtfCanada_verifyDoQuery() {
    // SETUP
    final AverageMerGraphqlDataFetcher a = mock(AverageMerGraphqlDataFetcher.class);
    final List<EtfHolding> holdings = List.of();

    doCallRealMethod().when(a).queryBenchOfEtfCanada(any(), any());
    // ACT
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);
    a.queryBenchOfEtfCanada(holdings, providers);

    // VERIFY
    verify(a).doQuery(eq(holdings), argThat(argument -> argument.getClass() == AverageMEREtfCanadaEndpoint.class), eq(
        providers));
  }

  @Test
  void queryBenchOfEtfCanada_checkResult() {
    // SETUP
    final AverageMerGraphqlDataFetcher a = mock(AverageMerGraphqlDataFetcher.class);
    final List<EtfHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(a.doQuery(any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(a).queryBenchOfEtfCanada(any(), any());
    // ACT
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);
    final Map<EtfHolding, AverageMer> actual = a.queryBenchOfEtfCanada(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfOfEtfUs_verifyDoQuery() {
    // SETUP
    final AverageMerGraphqlDataFetcher a = mock(AverageMerGraphqlDataFetcher.class);
    final List<EtfHolding> holdings = List.of();

    doCallRealMethod().when(a).queryBenchOfOfEtfUs(any(), any());
    // ACT
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);
    a.queryBenchOfOfEtfUs(holdings, providers);

    // VERIFY
    verify(a).doQuery(eq(holdings), argThat(argument -> argument.getClass() == AverageMEREtfUsEndpoint.class), eq(
        providers));
  }

  @Test
  void queryBenchOfOfEtfUs_checkResult() {
    // SETUP
    final AverageMerGraphqlDataFetcher a = mock(AverageMerGraphqlDataFetcher.class);
    final List<EtfHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(a.doQuery(any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(a).queryBenchOfOfEtfUs(any(), any());
    // ACT
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);
    final Map<EtfHolding, AverageMer> actual = a.queryBenchOfOfEtfUs(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryCanadaHedgeFunds_verifyDoQuery() {
    // SETUP
    final AverageMerGraphqlDataFetcher m = mock(AverageMerGraphqlDataFetcher.class);
    final List<CanadaHedgeFundHolding> holdings = List.of(mock(CanadaHedgeFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
    // ACT
    m.queryCanadaHedgeFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == AverageMERCanadaHedgeFundEndpoint.class),
        eq(providers));
  }

  @Test
  void queryCanadaHedgeFunds_checkResult() {
    // SETUP
    final AverageMerGraphqlDataFetcher m = mock(AverageMerGraphqlDataFetcher.class);
    final List<CanadaHedgeFundHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
    // ACT
    final Map<CanadaHedgeFundHolding, AverageMer> actual = m.queryCanadaHedgeFunds(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryUsMutualFunds_verifyDoQuery() {
    // SETUP
    final AverageMerGraphqlDataFetcher m = mock(AverageMerGraphqlDataFetcher.class);
    final List<UsMutualFundHolding> holdings = List.of(mock(UsMutualFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryUsMutualFunds(any(), anyList());
    // ACT
    m.queryUsMutualFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == AverageMERUsMutualFundEndpoint.class),
        eq(providers));
  }

}