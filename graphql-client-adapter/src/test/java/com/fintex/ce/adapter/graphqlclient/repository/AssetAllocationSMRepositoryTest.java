package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationFixedIncomeEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationSeparatelyManagedAccountEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationUsFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.AssetAllocationSMRepository;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.AssetAllocation;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.SmaHolding;
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

class AssetAllocationSMRepositoryTest {

  @Test
  void queryBenchOfFundCanada_verifyDoQuery() {
    // SETUP
    final var graphqlTransport = mock(GraphqlTransportComponent.class);
    final AssetAllocationSMRepository m = mock(AssetAllocationSMRepository.class,
        withSettings().useConstructor(graphqlTransport));
    final List<FundSeriesHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfFundCanada(any(), anyList());
    // ACT
    m.queryBenchOfFundCanada(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == AssetAllocationFundCanadaEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfFundCanada_checkResult() {
    // SETUP
    final AssetAllocationSMRepository m = mock(AssetAllocationSMRepository.class);
    final List<FundSeriesHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);
    doCallRealMethod().when(m).queryBenchOfFundCanada(any(), anyList());
    // ACT
    final Map<FundSeriesHolding, AssetAllocation> actual = m.queryBenchOfFundCanada(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfOfEtfUs_verifyDoQuery() {
    // SETUP
    final AssetAllocationSMRepository m = mock(AssetAllocationSMRepository.class);
    final List<EtfHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfOfEtfUs(any(), anyList());
    // ACT
    m.queryBenchOfOfEtfUs(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == AssetAllocationEtfUsEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfOfEtfUs_checkResult() {
    // SETUP
    final AssetAllocationSMRepository m = mock(AssetAllocationSMRepository.class);
    final List<EtfHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);
    doCallRealMethod().when(m).queryBenchOfOfEtfUs(any(), anyList());
    // ACT
    final Map<EtfHolding, AssetAllocation> actual = m.queryBenchOfOfEtfUs(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfEtfCanada_verifyDoQuery() {
    // SETUP
    final AssetAllocationSMRepository m = mock(AssetAllocationSMRepository.class);
    final List<EtfHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfEtfCanada(any(), anyList());
    // ACT
    m.queryBenchOfEtfCanada(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == AssetAllocationEtfCanadaEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfEtfCanada_checkResult() {
    // SETUP
    final AssetAllocationSMRepository m = mock(AssetAllocationSMRepository.class);
    final List<EtfHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfEtfCanada(any(), anyList());
    // ACT
    final Map<EtfHolding, AssetAllocation> actual = m.queryBenchOfEtfCanada(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfBenchmarks_verifyDoQuery() {
    // SETUP
    final AssetAllocationSMRepository m = mock(AssetAllocationSMRepository.class);
    final List<BenchmarkIndexHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfBenchmarks(any(), anyList());
    // ACT
    m.queryBenchOfBenchmarks(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == AssetAllocationBenchmarkEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfBenchmarks_checkResult() {
    // SETUP
    final AssetAllocationSMRepository m = mock(AssetAllocationSMRepository.class);
    final List<BenchmarkIndexHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfBenchmarks(any(), anyList());
    // ACT
    final Map<BenchmarkIndexHolding, AssetAllocation> actual = m.queryBenchOfBenchmarks(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfFixedIncomes_verifyDoQuery() {
    // SETUP
    final AssetAllocationSMRepository m = mock(AssetAllocationSMRepository.class);
    final List<FixedIncomeHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.BROADRIDGE);

    doCallRealMethod().when(m).queryBenchOfFixedIncomes(any(), anyList());
    // ACT
    m.queryBenchOfFixedIncomes(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == AssetAllocationFixedIncomeEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfFixedIncomes_checkResult() {
    // SETUP
    final AssetAllocationSMRepository m = mock(AssetAllocationSMRepository.class);
    final List<FixedIncomeHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.BROADRIDGE);

    doCallRealMethod().when(m).queryBenchOfFixedIncomes(any(), anyList());
    // ACT
    final Map<FixedIncomeHolding, AssetAllocation> actual = m.queryBenchOfFixedIncomes(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryBenchOfSeparatelyManagedAccount_verifyDoQuery() {
    // SETUP
    final AssetAllocationSMRepository m = mock(AssetAllocationSMRepository.class);
    final List<SmaHolding> holdings = List.of();
    final List<DataProvider> providers = List.of(DataProvider.ENVESTNET);

    doCallRealMethod().when(m).queryBenchOfSeparatelyManagedAccounts(any(), anyList());
    // ACT
    m.queryBenchOfSeparatelyManagedAccounts(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == AssetAllocationSeparatelyManagedAccountEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfSeparatelyManagedAccount_checkResult() {
    // SETUP
    final AssetAllocationSMRepository m = mock(AssetAllocationSMRepository.class);
    final List<SmaHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.ENVESTNET);

    doCallRealMethod().when(m).queryBenchOfSeparatelyManagedAccounts(any(), anyList());
    // ACT
    final Map<SmaHolding, AssetAllocation> actual = m.queryBenchOfSeparatelyManagedAccounts(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryCanadaHedgeFunds_verifyDoQuery() {
    // SETUP
    final AssetAllocationSMRepository m = mock(AssetAllocationSMRepository.class);
    final List<CanadaHedgeFundHolding> holdings = List.of(mock(CanadaHedgeFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
    // ACT
    m.queryCanadaHedgeFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == AssetAllocationCanadaHedgeFundEndpoint.class),
        eq(providers));
  }

  @Test
  void queryCanadaHedgeFunds_checkResult() {
    // SETUP
    final AssetAllocationSMRepository m = mock(AssetAllocationSMRepository.class);
    final List<CanadaHedgeFundHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
    // ACT
    final Map<CanadaHedgeFundHolding, AssetAllocation> actual = m.queryCanadaHedgeFunds(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryCanadaPooledFunds_verifyDoQuery() {
    // SETUP
    final AssetAllocationSMRepository m = mock(AssetAllocationSMRepository.class);
    final List<CanadaPooledFundHolding> holdings = List.of(mock(CanadaPooledFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
    // ACT
    m.queryCanadaPooledFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == AssetAllocationCanadaPooledFundEndpoint.class),
        eq(providers));
  }

  @Test
  void queryCanadaPooledFunds_checkResult() {
    // SETUP
    final AssetAllocationSMRepository m = mock(AssetAllocationSMRepository.class);
    final List<CanadaPooledFundHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
    // ACT
    final Map<CanadaPooledFundHolding, AssetAllocation> actual = m.queryCanadaPooledFunds(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryUsMutualFunds_verifyDoQuery() {
    // SETUP
    final AssetAllocationSMRepository m = mock(AssetAllocationSMRepository.class);
    final List<UsMutualFundHolding> holdings = List.of(mock(UsMutualFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryUsMutualFunds(any(), anyList());
    // ACT
    m.queryUsMutualFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == AssetAllocationUsFundEndpoint.class),
        eq(providers));
  }

}