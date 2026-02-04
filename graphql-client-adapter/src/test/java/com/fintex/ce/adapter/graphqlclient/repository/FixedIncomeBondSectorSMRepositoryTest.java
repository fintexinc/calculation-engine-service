package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorFixedIncomeEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.FixedIncomeBondSectorSMRepository;
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

class FixedIncomeBondSectorSMRepositoryTest {

  @Test
  void queryBenchOfFundCanada_verifyDoQuery() {
    // SETUP
    final var graphqlTransport = mock(GraphqlTransportComponent.class);
    final FixedIncomeBondSectorSMRepository fixedIncomeBondSectorSMRepository = mock(
        FixedIncomeBondSectorSMRepository.class,
        withSettings().useConstructor(graphqlTransport));
    final List<FundSeriesHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(fixedIncomeBondSectorSMRepository).queryBenchOfFundCanada(any(), anyList());
    // ACT
    fixedIncomeBondSectorSMRepository.queryBenchOfFundCanada(holdings, providers);

    // VERIFY
    verify(fixedIncomeBondSectorSMRepository).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == FixedIncomeBondSectorFundCanadaEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfFundCanada_checkResult() {
    // SETUP
    final FixedIncomeBondSectorSMRepository fixedIncomeBondSectorSMRepository = mock(
        FixedIncomeBondSectorSMRepository.class);
    final List<FundSeriesHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(fixedIncomeBondSectorSMRepository.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);
    doCallRealMethod().when(fixedIncomeBondSectorSMRepository).queryBenchOfFundCanada(any(), anyList());
    // ACT
    final Map<FundSeriesHolding, FixedIncomeBondSecurities> actual = fixedIncomeBondSectorSMRepository
        .queryBenchOfFundCanada(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfOfEtfUs_verifyDoQuery() {
    // SETUP
    final FixedIncomeBondSectorSMRepository fixedIncomeBondSectorSMRepository = mock(
        FixedIncomeBondSectorSMRepository.class);
    final List<EtfHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(fixedIncomeBondSectorSMRepository).queryBenchOfOfEtfUs(any(), anyList());
    // ACT
    fixedIncomeBondSectorSMRepository.queryBenchOfOfEtfUs(holdings, providers);

    // VERIFY
    verify(fixedIncomeBondSectorSMRepository).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == FixedIncomeBondSectorEtfUsEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfOfEtfUs_checkResult() {
    // SETUP
    final FixedIncomeBondSectorSMRepository fixedIncomeBondSectorSMRepository = mock(
        FixedIncomeBondSectorSMRepository.class);
    final List<EtfHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(fixedIncomeBondSectorSMRepository.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);
    doCallRealMethod().when(fixedIncomeBondSectorSMRepository).queryBenchOfOfEtfUs(any(), anyList());
    // ACT
    final Map<EtfHolding, FixedIncomeBondSecurities> actual = fixedIncomeBondSectorSMRepository.queryBenchOfOfEtfUs(
        holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfEtfCanada_verifyDoQuery() {
    // SETUP
    final FixedIncomeBondSectorSMRepository fixedIncomeBondSectorSMRepository = mock(
        FixedIncomeBondSectorSMRepository.class);
    final List<EtfHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(fixedIncomeBondSectorSMRepository).queryBenchOfEtfCanada(any(), anyList());
    // ACT
    fixedIncomeBondSectorSMRepository.queryBenchOfEtfCanada(holdings, providers);

    // VERIFY
    verify(fixedIncomeBondSectorSMRepository).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == FixedIncomeBondSectorEtfCanadaEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfEtfCanada_checkResult() {
    // SETUP
    final FixedIncomeBondSectorSMRepository fixedIncomeBondSectorSMRepository = mock(
        FixedIncomeBondSectorSMRepository.class);
    final List<EtfHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(fixedIncomeBondSectorSMRepository.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(fixedIncomeBondSectorSMRepository).queryBenchOfEtfCanada(any(), anyList());
    // ACT
    final Map<EtfHolding, FixedIncomeBondSecurities> actual = fixedIncomeBondSectorSMRepository.queryBenchOfEtfCanada(
        holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfBenchmarks_verifyDoQuery() {
    // SETUP
    final FixedIncomeBondSectorSMRepository fixedIncomeBondSectorSMRepository = mock(
        FixedIncomeBondSectorSMRepository.class);
    final List<BenchmarkIndexHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(fixedIncomeBondSectorSMRepository).queryBenchOfBenchmarks(any(), anyList());
    // ACT
    fixedIncomeBondSectorSMRepository.queryBenchOfBenchmarks(holdings, providers);

    // VERIFY
    verify(fixedIncomeBondSectorSMRepository).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == FixedIncomeBondSectorBenchmarkEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfBenchmarks_checkResult() {
    // SETUP
    final FixedIncomeBondSectorSMRepository fixedIncomeBondSectorSMRepository = mock(
        FixedIncomeBondSectorSMRepository.class);
    final List<BenchmarkIndexHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(fixedIncomeBondSectorSMRepository.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(fixedIncomeBondSectorSMRepository).queryBenchOfBenchmarks(any(), anyList());
    // ACT
    final Map<BenchmarkIndexHolding, FixedIncomeBondSecurities> actual = fixedIncomeBondSectorSMRepository
        .queryBenchOfBenchmarks(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfFixedIncomes_verifyDoQuery() {
    // SETUP
    final FixedIncomeBondSectorSMRepository fixedIncomeBondSectorSMRepository = mock(
        FixedIncomeBondSectorSMRepository.class);
    final List<FixedIncomeHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(fixedIncomeBondSectorSMRepository).queryBenchOfFixedIncomes(any(), anyList());
    // ACT
    fixedIncomeBondSectorSMRepository.queryBenchOfFixedIncomes(holdings, providers);

    // VERIFY
    verify(fixedIncomeBondSectorSMRepository).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == FixedIncomeBondSectorFixedIncomeEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfFixedIncomes_checkResult() {
    // SETUP
    final FixedIncomeBondSectorSMRepository fixedIncomeBondSectorSMRepository = mock(
        FixedIncomeBondSectorSMRepository.class);
    final List<FixedIncomeHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(fixedIncomeBondSectorSMRepository.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(fixedIncomeBondSectorSMRepository).queryBenchOfFixedIncomes(any(), anyList());
    // ACT
    final Map<FixedIncomeHolding, FixedIncomeBondSecurities> actual = fixedIncomeBondSectorSMRepository
        .queryBenchOfFixedIncomes(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryUsMutualFunds_verifyDoQuery() {
    // SETUP
    final FixedIncomeBondSectorSMRepository m = mock(FixedIncomeBondSectorSMRepository.class);
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
    final FixedIncomeBondSectorSMRepository m = mock(FixedIncomeBondSectorSMRepository.class);
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
    final FixedIncomeBondSectorSMRepository m = mock(FixedIncomeBondSectorSMRepository.class);
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
    final FixedIncomeBondSectorSMRepository m = mock(FixedIncomeBondSectorSMRepository.class);
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
    final FixedIncomeBondSectorSMRepository m = mock(FixedIncomeBondSectorSMRepository.class);
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
