package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldFixedIncomeEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldSeparatelyManagedAccountEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldStockEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.YieldSMRepository;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class YieldSMRepositoryTest {

  @Test
  void queryBenchOfFundCanada_verifyDoQuery() {
    // SETUP
    final var graphqlTransport = mock(GraphqlTransportComponent.class);
    final YieldSMRepository m = mock(YieldSMRepository.class,
        withSettings().useConstructor(graphqlTransport));
    final List<FundSeriesHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfFundCanada(any(), anyList());
    // ACT
    m.queryBenchOfFundCanada(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == YieldFundCanadaEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfFundCanada_checkResult() {
    // SETUP
    final YieldSMRepository m = mock(YieldSMRepository.class);
    final List<FundSeriesHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);
    doCallRealMethod().when(m).queryBenchOfFundCanada(any(), anyList());
    // ACT
    final Map<FundSeriesHolding, Yield> actual = m.queryBenchOfFundCanada(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfOfEtfUs_verifyDoQuery() {
    // SETUP
    final YieldSMRepository m = mock(YieldSMRepository.class);
    final List<EtfHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfOfEtfUs(any(), anyList());
    // ACT
    m.queryBenchOfOfEtfUs(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == YieldEtfUsEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfOfEtfUs_checkResult() {
    // SETUP
    final YieldSMRepository m = mock(YieldSMRepository.class);
    final List<EtfHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);
    doCallRealMethod().when(m).queryBenchOfOfEtfUs(any(), anyList());
    // ACT
    final Map<EtfHolding, Yield> actual = m.queryBenchOfOfEtfUs(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfEtfCanada_verifyDoQuery() {
    // SETUP
    final YieldSMRepository m = mock(YieldSMRepository.class);
    final List<EtfHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfEtfCanada(any(), anyList());
    // ACT
    m.queryBenchOfEtfCanada(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == YieldEtfCanadaEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfEtfCanada_checkResult() {
    // SETUP
    final YieldSMRepository m = mock(YieldSMRepository.class);
    final List<EtfHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfEtfCanada(any(), anyList());
    // ACT
    final Map<EtfHolding, Yield> actual = m.queryBenchOfEtfCanada(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryUsMutualFunds_verifyDoQuery() {
    // SETUP
    final YieldSMRepository m = mock(YieldSMRepository.class);
    final List<UsMutualFundHolding> holdings = List.of(mock(UsMutualFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryUsMutualFunds(any(), anyList());
    // ACT
    m.queryUsMutualFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == YieldUsMutualFundEndpoint.class),
        eq(providers));
  }

  @Test
  void queryCanadaHedgeFunds_verifyDoQuery() {
    // SETUP
    final YieldSMRepository m = mock(YieldSMRepository.class);
    final List<CanadaHedgeFundHolding> holdings = List.of(mock(CanadaHedgeFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
    // ACT
    m.queryCanadaHedgeFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == YieldCanadaHedgeFundEndpoint.class),
        eq(providers));
  }

  @Test
  void queryCanadaHedgeFunds_checkResult() {
    // SETUP
    final YieldSMRepository m = mock(YieldSMRepository.class);
    final List<CanadaHedgeFundHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
    // ACT
    final Map<CanadaHedgeFundHolding, Yield> actual = m.queryCanadaHedgeFunds(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryCanadaPooledFunds_verifyDoQuery() {
    // SETUP
    final YieldSMRepository m = mock(YieldSMRepository.class);
    final List<CanadaPooledFundHolding> holdings = List.of(mock(CanadaPooledFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
    // ACT
    m.queryCanadaPooledFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == YieldPooledFundEndpoint.class),
        eq(providers));
  }

  @Test
  void queryCanadaPooledFunds_checkResult() {
    // SETUP
    final YieldSMRepository m = mock(YieldSMRepository.class);
    final List<CanadaPooledFundHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
    // ACT
    final Map<CanadaPooledFundHolding, Yield> actual = m.queryCanadaPooledFunds(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryFixedIncomes_verifyDoQuery() {
    // SETUP
    final YieldSMRepository m = mock(YieldSMRepository.class);
    final List<FixedIncomeHolding> holdings = List.of(mock(FixedIncomeHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.BROADRIDGE);

    doCallRealMethod().when(m).queryBenchOfFixedIncomes(any(), anyList());
    // ACT
    m.queryBenchOfFixedIncomes(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == YieldFixedIncomeEndpoint.class),
        eq(providers));
  }

  @Test
  void queryFixedIncomes_checkResult() {
    // SETUP
    final YieldSMRepository m = mock(YieldSMRepository.class);
    final List<FixedIncomeHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.BROADRIDGE);

    doCallRealMethod().when(m).queryBenchOfFixedIncomes(any(), anyList());
    // ACT
    final Map<FixedIncomeHolding, Yield> actual = m.queryBenchOfFixedIncomes(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfSeparatelyManagedAccount_verifyDoQuery() {
    // SETUP
    final YieldSMRepository m = mock(YieldSMRepository.class);
    final List<SmaHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.ENVESTNET);

    doCallRealMethod().when(m).queryBenchOfSeparatelyManagedAccounts(any(), anyList());
    // ACT
    m.queryBenchOfSeparatelyManagedAccounts(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == YieldSeparatelyManagedAccountEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfSeparatelyManagedAccount_checkResult() {
    // SETUP
    final YieldSMRepository m = mock(YieldSMRepository.class);
    final List<SmaHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.ENVESTNET);

    doCallRealMethod().when(m).queryBenchOfSeparatelyManagedAccounts(any(), anyList());
    // ACT
    final Map<SmaHolding, Yield> actual = m.queryBenchOfSeparatelyManagedAccounts(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfStock_verifyDoQuery() {
    // SETUP
    final YieldSMRepository m = mock(YieldSMRepository.class);
    final List<StockHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);

    doCallRealMethod().when(m).queryBenchOfStock(any(), anyList());
    // ACT
    m.queryBenchOfStock(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == YieldStockEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfStock_checkResult() {
    // SETUP
    final YieldSMRepository m = mock(YieldSMRepository.class);
    final List<StockHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);
    doCallRealMethod().when(m).queryBenchOfStock(any(), anyList());
    // ACT
    final Map<StockHolding, Yield> actual = m.queryBenchOfStock(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

}
