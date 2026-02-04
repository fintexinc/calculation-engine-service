package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsStockEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.CommonHoldingsSMRepository;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.CommonHoldings;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.smclient.service.GraphqlTransportComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class CommonHoldingsSMRepositoryTest {

  @Test
  void queryBenchOfEtfCanada_verifyDoQuery() {
    // SETUP
    final var graphqlTransport = mock(GraphqlTransportComponent.class);
    final var smRepo = mock(CommonHoldingsSMRepository.class, withSettings().useConstructor(graphqlTransport));
    final var holdings = new ArrayList<EtfHolding>();
    final var providers = new ArrayList<DataProvider>();

    doCallRealMethod().when(smRepo).queryBenchOfEtfCanada(any(), anyList());
    // ACT
    smRepo.queryBenchOfEtfCanada(holdings, providers);

    // VERIFY
    verify(smRepo).doQuery(
        eq(holdings),
        argThat(argument -> argument.getClass() == CommonHoldingsEtfCanadaEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfEtfCanada_checkResult() {
    // SETUP
    final var smRepo = mock(CommonHoldingsSMRepository.class);
    final var holdings = new ArrayList<EtfHolding>();
    final var providers = new ArrayList<DataProvider>();
    final var expected = new HashMap<>();

    when(smRepo.doQuery(anyList(), any(), any())).thenReturn(expected);

    doCallRealMethod().when(smRepo).queryBenchOfEtfCanada(any(), anyList());
    // ACT
    final var actual = smRepo.queryBenchOfEtfCanada(holdings, providers);

    // VERIFY
    assertSame(expected, actual);
  }

  @Test
  void queryBenchOfOfEtfUs_verifyDoQuery() {
    // SETUP
    final var smRepo = mock(CommonHoldingsSMRepository.class);
    final var holdings = new ArrayList<EtfHolding>();
    final var providers = new ArrayList<DataProvider>();

    doCallRealMethod().when(smRepo).queryBenchOfOfEtfUs(anyList(), anyList());
    // ACT
    smRepo.queryBenchOfOfEtfUs(holdings, providers);

    // VERIFY
    verify(smRepo).doQuery(
        eq(holdings),
        argThat(argument -> argument.getClass() == CommonHoldingsEtfUsEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfOfEtfUs_checkResult() {
    // SETUP
    final var smRepo = mock(CommonHoldingsSMRepository.class);
    final var holdings = new ArrayList<EtfHolding>();
    final var providers = new ArrayList<DataProvider>();
    final var expected = new HashMap<>();

    when(smRepo.doQuery(anyList(), any(), anyList())).thenReturn(expected);

    doCallRealMethod().when(smRepo).queryBenchOfOfEtfUs(anyList(), anyList());
    // ACT
    final var actual = smRepo.queryBenchOfOfEtfUs(holdings, providers);

    // VERIFY
    assertSame(expected, actual);
  }

  @Test
  void queryBenchOfFundCanada_verifyDoQuery() {
    // SETUP
    final var smRepo = mock(CommonHoldingsSMRepository.class);
    final var holdings = new ArrayList<FundSeriesHolding>();
    final var providers = new ArrayList<DataProvider>();

    doCallRealMethod().when(smRepo).queryBenchOfFundCanada(anyList(), anyList());
    // ACT
    smRepo.queryBenchOfFundCanada(holdings, providers);

    // VERIFY
    verify(smRepo).doQuery(
        eq(holdings),
        argThat(argument -> argument.getClass() == CommonHoldingsFundCanadaEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfFundCanada_checkResult() {
    // SETUP
    final var smRepo = mock(CommonHoldingsSMRepository.class);
    final var holdings = new ArrayList<FundSeriesHolding>();
    final var providers = new ArrayList<DataProvider>();
    final var expected = new HashMap<>();

    when(smRepo.doQuery(anyList(), any(), anyList())).thenReturn(expected);

    doCallRealMethod().when(smRepo).queryBenchOfFundCanada(anyList(), anyList());
    // ACT
    final var actual = smRepo.queryBenchOfFundCanada(holdings, providers);

    // VERIFY
    assertSame(expected, actual);
  }

  @Test
  void queryBenchOfStock_verifyDoQuery() {
    // SETUP
    final var smRepo = mock(CommonHoldingsSMRepository.class);
    final var holdings = new ArrayList<StockHolding>();
    final var providers = new ArrayList<DataProvider>();

    doCallRealMethod().when(smRepo).queryBenchOfStock(anyList(), anyList());
    // ACT
    smRepo.queryBenchOfStock(holdings, providers);

    // VERIFY
    verify(smRepo).doQuery(
        eq(holdings),
        argThat(argument -> argument.getClass() == CommonHoldingsStockEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfStock_checkResult() {
    // SETUP
    final var smRepo = mock(CommonHoldingsSMRepository.class);
    final var holdings = new ArrayList<StockHolding>();
    final var providers = new ArrayList<DataProvider>();
    final var expected = new HashMap<>();

    when(smRepo.doQuery(anyList(), any(), anyList())).thenReturn(expected);

    doCallRealMethod().when(smRepo).queryBenchOfStock(anyList(), anyList());
    // ACT
    final var actual = smRepo.queryBenchOfStock(holdings, providers);

    // VERIFY
    assertSame(expected, actual);
  }

  @Test
  void queryBenchOfBenchmarks_verifyDoQuery() {
    // SETUP
    final var smRepo = mock(CommonHoldingsSMRepository.class);
    final var holdings = new ArrayList<BenchmarkIndexHolding>();
    final var providers = new ArrayList<DataProvider>();

    doCallRealMethod().when(smRepo).queryBenchOfBenchmarks(anyList(), anyList());
    // ACT
    smRepo.queryBenchOfBenchmarks(holdings, providers);

    // VERIFY
    verify(smRepo).doQuery(
        eq(holdings),
        argThat(argument -> argument.getClass() == CommonHoldingsBenchmarkEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfBenchmarks_checkResult() {
    // SETUP
    final var smRepo = mock(CommonHoldingsSMRepository.class);
    final var holdings = new ArrayList<BenchmarkIndexHolding>();
    final var providers = new ArrayList<DataProvider>();
    final var expected = new HashMap<>();

    when(smRepo.doQuery(anyList(), any(), anyList())).thenReturn(expected);

    doCallRealMethod().when(smRepo).queryBenchOfBenchmarks(anyList(), anyList());
    // ACT
    final var actual = smRepo.queryBenchOfBenchmarks(holdings, providers);

    // VERIFY
    assertSame(expected, actual);
  }

  @Test
  void queryUsMutualFunds_verifyDoQuery() {
    // SETUP
    final CommonHoldingsSMRepository m = mock(CommonHoldingsSMRepository.class);
    final List<UsMutualFundHolding> holdings = List.of(mock(UsMutualFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryUsMutualFunds(any(), anyList());
    // ACT
    m.queryUsMutualFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == CommonHoldingsUsMutualFundEndpoint.class),
        eq(providers));
  }

  @Test
  void queryCanadaHedgeFunds_verifyDoQuery() {
    // SETUP
    final CommonHoldingsSMRepository m = mock(CommonHoldingsSMRepository.class);
    final List<CanadaHedgeFundHolding> holdings = List.of(mock(CanadaHedgeFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
    // ACT
    m.queryCanadaHedgeFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == CommonHoldingsCanadaHedgeFundEndpoint.class),
        eq(providers));
  }

  @Test
  void queryCanadaHedgeFunds_checkResult() {
    // SETUP
    final CommonHoldingsSMRepository m = mock(CommonHoldingsSMRepository.class);
    final List<CanadaHedgeFundHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
    // ACT
    final Map<CanadaHedgeFundHolding, CommonHoldings> actual = m.queryCanadaHedgeFunds(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryCanadaPooledFunds_verifyDoQuery() {
    // SETUP
    final CommonHoldingsSMRepository m = mock(CommonHoldingsSMRepository.class);
    final List<CanadaPooledFundHolding> holdings = List.of(mock(CanadaPooledFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
    // ACT
    m.queryCanadaPooledFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == CommonHoldingsCanadaPooledFundEndpoint.class),
        eq(providers));
  }

  @Test
  void queryCanadaPooledFunds_checkResult() {
    // SETUP
    final CommonHoldingsSMRepository m = mock(CommonHoldingsSMRepository.class);
    final List<CanadaPooledFundHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
    // ACT
    final Map<CanadaPooledFundHolding, CommonHoldings> actual = m.queryCanadaPooledFunds(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

}