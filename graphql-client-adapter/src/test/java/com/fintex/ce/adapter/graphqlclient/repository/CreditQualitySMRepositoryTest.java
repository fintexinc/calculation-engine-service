package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityFixedIncomeEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.CreditQualitySMRepository;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.CreditQuality;
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

class CreditQualitySMRepositoryTest {

  @Test
  void queryBenchOfFundCanada_verifyDoQuery() {
    // SETUP
    final var graphqlTransport = mock(GraphqlTransportComponent.class);
    final CreditQualitySMRepository m = mock(CreditQualitySMRepository.class,
        withSettings().useConstructor(graphqlTransport));
    final List<FundSeriesHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfFundCanada(any(), anyList());
    // ACT
    m.queryBenchOfFundCanada(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == CreditQualityFundCanadaEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfFundCanada_checkResult() {
    // SETUP
    final CreditQualitySMRepository m = mock(CreditQualitySMRepository.class);
    final List<FundSeriesHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);
    doCallRealMethod().when(m).queryBenchOfFundCanada(any(), anyList());
    // ACT
    final Map<FundSeriesHolding, CreditQuality> actual = m.queryBenchOfFundCanada(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfOfEtfUs_verifyDoQuery() {
    // SETUP
    final CreditQualitySMRepository m = mock(CreditQualitySMRepository.class);
    final List<EtfHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfOfEtfUs(any(), anyList());
    // ACT
    m.queryBenchOfOfEtfUs(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == CreditQualityEtfUsEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfOfEtfUs_checkResult() {
    // SETUP
    final CreditQualitySMRepository m = mock(CreditQualitySMRepository.class);
    final List<EtfHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);
    doCallRealMethod().when(m).queryBenchOfOfEtfUs(any(), anyList());
    // ACT
    final Map<EtfHolding, CreditQuality> actual = m.queryBenchOfOfEtfUs(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfEtfCanada_verifyDoQuery() {
    // SETUP
    final CreditQualitySMRepository m = mock(CreditQualitySMRepository.class);
    final List<EtfHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfEtfCanada(any(), anyList());
    // ACT
    m.queryBenchOfEtfCanada(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == CreditQualityEtfCanadaEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfEtfCanada_checkResult() {
    // SETUP
    final CreditQualitySMRepository m = mock(CreditQualitySMRepository.class);
    final List<EtfHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfEtfCanada(any(), anyList());
    // ACT
    final Map<EtfHolding, CreditQuality> actual = m.queryBenchOfEtfCanada(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfBenchmarks_verifyDoQuery() {
    // SETUP
    final CreditQualitySMRepository m = mock(CreditQualitySMRepository.class);
    final List<BenchmarkIndexHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfBenchmarks(any(), anyList());
    // ACT
    m.queryBenchOfBenchmarks(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == CreditQualityBenchmarkEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfBenchmarks_checkResult() {
    // SETUP
    final CreditQualitySMRepository m = mock(CreditQualitySMRepository.class);
    final List<BenchmarkIndexHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfBenchmarks(any(), anyList());
    // ACT
    final Map<BenchmarkIndexHolding, CreditQuality> actual = m.queryBenchOfBenchmarks(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfFixedIncomes_verifyDoQuery() {
    // SETUP
    final CreditQualitySMRepository m = mock(CreditQualitySMRepository.class);
    final List<FixedIncomeHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfFixedIncomes(any(), anyList());
    // ACT
    m.queryBenchOfFixedIncomes(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == CreditQualityFixedIncomeEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfFixedIncomes_checkResult() {
    // SETUP
    final CreditQualitySMRepository m = mock(CreditQualitySMRepository.class);
    final List<FixedIncomeHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfFixedIncomes(any(), anyList());
    // ACT
    final Map<FixedIncomeHolding, CreditQuality> actual = m.queryBenchOfFixedIncomes(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryUsMutualFunds_verifyDoQuery() {
    // SETUP
    final CreditQualitySMRepository m = mock(CreditQualitySMRepository.class);
    final List<UsMutualFundHolding> holdings = List.of(mock(UsMutualFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryUsMutualFunds(any(), anyList());
    // ACT
    m.queryUsMutualFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == CreditQualityUsMutualFundEndpoint.class),
        eq(providers));
  }

  @Test
  void queryCanadaHedgeFunds_verifyDoQuery() {
    // SETUP
    final CreditQualitySMRepository m = mock(CreditQualitySMRepository.class);
    final List<CanadaHedgeFundHolding> holdings = List.of(mock(CanadaHedgeFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
    // ACT
    m.queryCanadaHedgeFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == CreditQualityCanadaHedgeFundEndpoint.class),
        eq(providers));
  }

  @Test
  void queryCanadaHedgeFunds_checkResult() {
    // SETUP
    final CreditQualitySMRepository m = mock(CreditQualitySMRepository.class);
    final List<CanadaHedgeFundHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
    // ACT
    final Map<CanadaHedgeFundHolding, CreditQuality> actual = m.queryCanadaHedgeFunds(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryCanadaPooledFunds_verifyDoQuery() {
    // SETUP
    final CreditQualitySMRepository m = mock(CreditQualitySMRepository.class);
    final List<CanadaPooledFundHolding> holdings = List.of(mock(CanadaPooledFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
    // ACT
    m.queryCanadaPooledFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == CreditQualityCanadaPooledFundEndpoint.class),
        eq(providers));
  }

  @Test
  void queryCanadaPooledFunds_checkResult() {
    // SETUP
    final CreditQualitySMRepository m = mock(CreditQualitySMRepository.class);
    final List<CanadaPooledFundHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
    // ACT
    final Map<CanadaPooledFundHolding, CreditQuality> actual = m.queryCanadaPooledFunds(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

}