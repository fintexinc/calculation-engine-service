package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorFixedIncomeEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.FixedIncomeSectorGraphqlDataFetcher;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.FixedIncomeBondSecurities;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.smclient.service.GraphqlTransportComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class FixedIncomeSectorGraphqlDataFetcherTest {

  @Test
  void queryBenchOfFundCanada_verifyDoQuery() {
    // SETUP
    final var graphqlTransport = mock(GraphqlTransportComponent.class);
    final FixedIncomeSectorGraphqlDataFetcher fixedIncomeBondSectorAbstractGraphqlDataFetcher = mock(
        FixedIncomeSectorGraphqlDataFetcher.class,
        withSettings().useConstructor(graphqlTransport));
    final List<FundSeriesHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(fixedIncomeBondSectorAbstractGraphqlDataFetcher).queryBenchOfFundCanada(any(), anyList());
    // ACT
    fixedIncomeBondSectorAbstractGraphqlDataFetcher.queryBenchOfFundCanada(holdings, providers);

    // VERIFY
    verify(fixedIncomeBondSectorAbstractGraphqlDataFetcher).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == FixedIncomeBondSectorFundCanadaEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfFundCanada_checkResult() {
    // SETUP
    final FixedIncomeSectorGraphqlDataFetcher fixedIncomeBondSectorAbstractGraphqlDataFetcher = mock(
        FixedIncomeSectorGraphqlDataFetcher.class);
    final List<FundSeriesHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(fixedIncomeBondSectorAbstractGraphqlDataFetcher.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);
    doCallRealMethod().when(fixedIncomeBondSectorAbstractGraphqlDataFetcher).queryBenchOfFundCanada(any(), anyList());
    // ACT
    final Map<FundSeriesHolding, FixedIncomeBondSecurities> actual = fixedIncomeBondSectorAbstractGraphqlDataFetcher
        .queryBenchOfFundCanada(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfOfEtfUs_verifyDoQuery() {
    // SETUP
    final FixedIncomeSectorGraphqlDataFetcher fixedIncomeBondSectorAbstractGraphqlDataFetcher = mock(
        FixedIncomeSectorGraphqlDataFetcher.class);
    final List<EtfHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(fixedIncomeBondSectorAbstractGraphqlDataFetcher).queryBenchOfOfEtfUs(any(), anyList());
    // ACT
    fixedIncomeBondSectorAbstractGraphqlDataFetcher.queryBenchOfOfEtfUs(holdings, providers);

    // VERIFY
    verify(fixedIncomeBondSectorAbstractGraphqlDataFetcher).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == FixedIncomeBondSectorEtfUsEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfOfEtfUs_checkResult() {
    // SETUP
    final FixedIncomeSectorGraphqlDataFetcher fixedIncomeBondSectorAbstractGraphqlDataFetcher = mock(
        FixedIncomeSectorGraphqlDataFetcher.class);
    final List<EtfHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(fixedIncomeBondSectorAbstractGraphqlDataFetcher.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);
    doCallRealMethod().when(fixedIncomeBondSectorAbstractGraphqlDataFetcher).queryBenchOfOfEtfUs(any(), anyList());
    // ACT
    final Map<EtfHolding, FixedIncomeBondSecurities> actual = fixedIncomeBondSectorAbstractGraphqlDataFetcher.queryBenchOfOfEtfUs(
        holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfEtfCanada_verifyDoQuery() {
    // SETUP
    final FixedIncomeSectorGraphqlDataFetcher fixedIncomeBondSectorAbstractGraphqlDataFetcher = mock(
        FixedIncomeSectorGraphqlDataFetcher.class);
    final List<EtfHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(fixedIncomeBondSectorAbstractGraphqlDataFetcher).queryBenchOfEtfCanada(any(), anyList());
    // ACT
    fixedIncomeBondSectorAbstractGraphqlDataFetcher.queryBenchOfEtfCanada(holdings, providers);

    // VERIFY
    verify(fixedIncomeBondSectorAbstractGraphqlDataFetcher).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == FixedIncomeBondSectorEtfCanadaEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfEtfCanada_checkResult() {
    // SETUP
    final FixedIncomeSectorGraphqlDataFetcher fixedIncomeBondSectorAbstractGraphqlDataFetcher = mock(
        FixedIncomeSectorGraphqlDataFetcher.class);
    final List<EtfHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(fixedIncomeBondSectorAbstractGraphqlDataFetcher.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(fixedIncomeBondSectorAbstractGraphqlDataFetcher).queryBenchOfEtfCanada(any(), anyList());
    // ACT
    final Map<EtfHolding, FixedIncomeBondSecurities> actual = fixedIncomeBondSectorAbstractGraphqlDataFetcher.queryBenchOfEtfCanada(
        holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfBenchmarks_verifyDoQuery() {
    // SETUP
    final FixedIncomeSectorGraphqlDataFetcher fixedIncomeBondSectorAbstractGraphqlDataFetcher = mock(
        FixedIncomeSectorGraphqlDataFetcher.class);
    final List<BenchmarkIndexHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(fixedIncomeBondSectorAbstractGraphqlDataFetcher).queryBenchOfBenchmarks(any(), anyList());
    // ACT
    fixedIncomeBondSectorAbstractGraphqlDataFetcher.queryBenchOfBenchmarks(holdings, providers);

    // VERIFY
    verify(fixedIncomeBondSectorAbstractGraphqlDataFetcher).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == FixedIncomeBondSectorBenchmarkEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfBenchmarks_checkResult() {
    // SETUP
    final FixedIncomeSectorGraphqlDataFetcher fixedIncomeBondSectorAbstractGraphqlDataFetcher = mock(
        FixedIncomeSectorGraphqlDataFetcher.class);
    final List<BenchmarkIndexHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(fixedIncomeBondSectorAbstractGraphqlDataFetcher.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(fixedIncomeBondSectorAbstractGraphqlDataFetcher).queryBenchOfBenchmarks(any(), anyList());
    // ACT
    final Map<BenchmarkIndexHolding, FixedIncomeBondSecurities> actual = fixedIncomeBondSectorAbstractGraphqlDataFetcher
        .queryBenchOfBenchmarks(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfFixedIncomes_verifyDoQuery() {
    // SETUP
    final FixedIncomeSectorGraphqlDataFetcher fixedIncomeBondSectorAbstractGraphqlDataFetcher = mock(
        FixedIncomeSectorGraphqlDataFetcher.class);
    final List<FixedIncomeHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(fixedIncomeBondSectorAbstractGraphqlDataFetcher).queryBenchOfFixedIncomes(any(), anyList());
    // ACT
    fixedIncomeBondSectorAbstractGraphqlDataFetcher.queryBenchOfFixedIncomes(holdings, providers);

    // VERIFY
    verify(fixedIncomeBondSectorAbstractGraphqlDataFetcher).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == FixedIncomeBondSectorFixedIncomeEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfFixedIncomes_checkResult() {
    // SETUP
    final FixedIncomeSectorGraphqlDataFetcher fixedIncomeBondSectorAbstractGraphqlDataFetcher = mock(
        FixedIncomeSectorGraphqlDataFetcher.class);
    final List<FixedIncomeHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(fixedIncomeBondSectorAbstractGraphqlDataFetcher.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(fixedIncomeBondSectorAbstractGraphqlDataFetcher).queryBenchOfFixedIncomes(any(), anyList());
    // ACT
    final Map<FixedIncomeHolding, FixedIncomeBondSecurities> actual = fixedIncomeBondSectorAbstractGraphqlDataFetcher
        .queryBenchOfFixedIncomes(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryUsMutualFunds_verifyDoQuery() {
    // SETUP
    final FixedIncomeSectorGraphqlDataFetcher m = mock(FixedIncomeSectorGraphqlDataFetcher.class);
    final List<UsMutualFundHolding> holdings = List.of(mock(UsMutualFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryUsMutualFunds(any(), anyList());
    // ACT
    m.queryUsMutualFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == FixedIncomeBondSectorUsMutualFundEndpoint.class),
        eq(providers));
  }

  @Test
  void queryCanadaHedgeFunds_verifyDoQuery() {
    // SETUP
    final FixedIncomeSectorGraphqlDataFetcher m = mock(FixedIncomeSectorGraphqlDataFetcher.class);
    final List<CanadaHedgeFundHolding> holdings = List.of(mock(CanadaHedgeFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
    // ACT
    m.queryCanadaHedgeFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == FixedIncomeBondSectorCanadaHedgeFundEndpoint.class),
        eq(providers));
  }

  @Test
  void queryCanadaHedgeFunds_checkResult() {
    // SETUP
    final FixedIncomeSectorGraphqlDataFetcher m = mock(FixedIncomeSectorGraphqlDataFetcher.class);
    final List<CanadaHedgeFundHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
    // ACT
    final Map<CanadaHedgeFundHolding, FixedIncomeBondSecurities> actual = m.queryCanadaHedgeFunds(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryCanadaPooledFunds_verifyDoQuery() {
    // SETUP
    final FixedIncomeSectorGraphqlDataFetcher m = mock(FixedIncomeSectorGraphqlDataFetcher.class);
    final List<CanadaPooledFundHolding> holdings = List.of(mock(CanadaPooledFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
    // ACT
    m.queryCanadaPooledFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == FixedIncomeBondSectorCanadaPooledFundEndpoint.class),
        eq(providers));
  }

  @Test
  void queryCanadaPooledFunds_checkResult() {
    // SETUP
    final FixedIncomeSectorGraphqlDataFetcher m = mock(FixedIncomeSectorGraphqlDataFetcher.class);
    final List<CanadaPooledFundHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
    // ACT
    final Map<CanadaPooledFundHolding, FixedIncomeBondSecurities> actual = m.queryCanadaPooledFunds(holdings,
        providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

}
