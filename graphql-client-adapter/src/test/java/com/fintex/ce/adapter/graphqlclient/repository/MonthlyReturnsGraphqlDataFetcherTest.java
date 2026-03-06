package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsPagGuidedPortfolioEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsStockEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.MonthlyReturnsGraphqlDataFetcher;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.MonthlyReturns;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.PagHolding;
import com.fintex.ce.domain.model.holding.SmaHolding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.smclient.service.GraphqlTransportComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class MonthlyReturnsGraphqlDataFetcherTest {

  @Test
  void queryBenchOfFundCanada_verifyDoQuery() {
    // SETUP
    final var graphqlTransport = mock(GraphqlTransportComponent.class);
    final MonthlyReturnsGraphqlDataFetcher m = mock(MonthlyReturnsGraphqlDataFetcher.class,
        withSettings().useConstructor(graphqlTransport));
    final List<FundSeriesHolding> holdings = List.of();

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfFundCanada(any(), anyList());
    // ACT
    m.queryBenchOfFundCanada(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == MonthlyReturnsFundCanadaEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfFundCanada_checkResult() {
    // SETUP
    final MonthlyReturnsGraphqlDataFetcher m = mock(MonthlyReturnsGraphqlDataFetcher.class);
    final List<FundSeriesHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfFundCanada(any(), anyList());
    // ACT
    final Map<FundSeriesHolding, MonthlyReturns> actual = m.queryBenchOfFundCanada(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfOfEtfUs_verifyDoQuery() {
    // SETUP
    final MonthlyReturnsGraphqlDataFetcher m = mock(MonthlyReturnsGraphqlDataFetcher.class);
    final List<EtfHolding> holdings = List.of();

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);
    doCallRealMethod().when(m).queryBenchOfOfEtfUs(any(), anyList());
    // ACT
    m.queryBenchOfOfEtfUs(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == MonthlyReturnsEtfUsEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfOfEtfUs_checkResult() {
    // SETUP
    final MonthlyReturnsGraphqlDataFetcher m = mock(MonthlyReturnsGraphqlDataFetcher.class);
    final List<EtfHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfOfEtfUs(any(), anyList());
    // ACT
    final Map<EtfHolding, MonthlyReturns> actual = m.queryBenchOfOfEtfUs(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfEtfCanada_verifyDoQuery() {
    // SETUP
    final MonthlyReturnsGraphqlDataFetcher m = mock(MonthlyReturnsGraphqlDataFetcher.class);
    final List<EtfHolding> holdings = List.of();

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfEtfCanada(any(), anyList());
    // ACT
    m.queryBenchOfEtfCanada(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == MonthlyReturnsEtfCanadaEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfEtfCanada_checkResult() {
    // SETUP
    final MonthlyReturnsGraphqlDataFetcher m = mock(MonthlyReturnsGraphqlDataFetcher.class);
    final List<EtfHolding> holdings = List.of();

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(m).queryBenchOfEtfCanada(any(), anyList());
    // ACT
    final Map<EtfHolding, MonthlyReturns> actual = m.queryBenchOfEtfCanada(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfStock_verifyDoQuery() {
    // SETUP
    final MonthlyReturnsGraphqlDataFetcher m = mock(MonthlyReturnsGraphqlDataFetcher.class);
    final List<StockHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfStock(any(), anyList());
    // ACT
    m.queryBenchOfStock(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == MonthlyReturnsStockEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfStock_checkResult() {
    // SETUP
    final MonthlyReturnsGraphqlDataFetcher m = mock(MonthlyReturnsGraphqlDataFetcher.class);
    final List<StockHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);
    doCallRealMethod().when(m).queryBenchOfStock(any(), anyList());
    // ACT
    final Map<StockHolding, MonthlyReturns> actual = m.queryBenchOfStock(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfBenchmarks_verifyDoQuery() {
    // SETUP
    final MonthlyReturnsGraphqlDataFetcher m = mock(MonthlyReturnsGraphqlDataFetcher.class);
    final List<BenchmarkIndexHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfBenchmarks(any(), anyList());
    // ACT
    m.queryBenchOfBenchmarks(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == MonthlyReturnsBenchmarkEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfBenchmarks_checkResult() {
    // SETUP
    final MonthlyReturnsGraphqlDataFetcher m = mock(MonthlyReturnsGraphqlDataFetcher.class);
    final List<BenchmarkIndexHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);
    doCallRealMethod().when(m).queryBenchOfBenchmarks(any(), anyList());
    // ACT
    final Map<BenchmarkIndexHolding, MonthlyReturns> actual = m.queryBenchOfBenchmarks(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfFixedIncome_checkResult() {
    // SETUP
    final MonthlyReturnsGraphqlDataFetcher m = mock(MonthlyReturnsGraphqlDataFetcher.class);
    final List<FixedIncomeHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(m).queryBenchOfFixedIncomes(any(), anyList());
    // ACT
    final Map<FixedIncomeHolding, MonthlyReturns> actual = m.queryBenchOfFixedIncomes(holdings, List.of());

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfSeparatelyManagedAccount_checkResult() {
    // SETUP
    final MonthlyReturnsGraphqlDataFetcher m = mock(MonthlyReturnsGraphqlDataFetcher.class);
    final List<SmaHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(m).queryBenchOfSeparatelyManagedAccounts(any(), anyList());
    // ACT
    final Map<SmaHolding, MonthlyReturns> actual = m.queryBenchOfSeparatelyManagedAccounts(holdings, List.of());

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryUsMutualFunds_verifyDoQuery() {
    // SETUP
    final MonthlyReturnsGraphqlDataFetcher m = mock(MonthlyReturnsGraphqlDataFetcher.class);
    final List<UsMutualFundHolding> holdings = List.of(mock(UsMutualFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryUsMutualFunds(any(), anyList());
    // ACT
    m.queryUsMutualFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == MonthlyReturnsUsMutualFundEndpoint.class),
        eq(providers));
  }

  @Test
  void queryCanadaHedgeFunds_verifyDoQuery() {
    // SETUP
    final MonthlyReturnsGraphqlDataFetcher m = mock(MonthlyReturnsGraphqlDataFetcher.class);
    final List<CanadaHedgeFundHolding> holdings = List.of(mock(CanadaHedgeFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
    // ACT
    m.queryCanadaHedgeFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == MonthlyReturnsCanadaHedgeFundEndpoint.class),
        eq(providers));
  }

  @Test
  void queryCanadaHedgeFunds_checkResult() {
    // SETUP
    final MonthlyReturnsGraphqlDataFetcher m = mock(MonthlyReturnsGraphqlDataFetcher.class);
    final List<CanadaHedgeFundHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
    // ACT
    final Map<CanadaHedgeFundHolding, MonthlyReturns> actual = m.queryCanadaHedgeFunds(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryCanadaPooledFunds_verifyDoQuery() {
    // SETUP
    final MonthlyReturnsGraphqlDataFetcher m = mock(MonthlyReturnsGraphqlDataFetcher.class);
    final List<CanadaPooledFundHolding> holdings = List.of(mock(CanadaPooledFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
    // ACT
    m.queryCanadaPooledFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == MonthlyReturnsCanadaPooledFundEndpoint.class),
        eq(providers));
  }

  @Test
  void queryCanadaPooledFunds_checkResult() {
    // SETUP
    final MonthlyReturnsGraphqlDataFetcher m = mock(MonthlyReturnsGraphqlDataFetcher.class);
    final List<CanadaPooledFundHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
    // ACT
    final Map<CanadaPooledFundHolding, MonthlyReturns> actual = m.queryCanadaPooledFunds(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfPagGuidedPortfolio_verifyDoQuery() {
    // SETUP
    final MonthlyReturnsGraphqlDataFetcher m = mock(MonthlyReturnsGraphqlDataFetcher.class);
    final List<PagHolding> holdings = List.of(mock(PagHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.PAG);

    doCallRealMethod().when(m).queryBenchOfPagGuidedPortfolios(any(), anyList());
    // ACT
    m.queryBenchOfPagGuidedPortfolios(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == MonthlyReturnsPagGuidedPortfolioEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfPagGuidedPortfolio_checkResult() {
    // SETUP
    final MonthlyReturnsGraphqlDataFetcher m = mock(MonthlyReturnsGraphqlDataFetcher.class);
    final List<PagHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.PAG);

    doCallRealMethod().when(m).queryBenchOfPagGuidedPortfolios(any(), anyList());
    // ACT
    final Map<PagHolding, MonthlyReturns> actual = m.queryBenchOfPagGuidedPortfolios(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

}