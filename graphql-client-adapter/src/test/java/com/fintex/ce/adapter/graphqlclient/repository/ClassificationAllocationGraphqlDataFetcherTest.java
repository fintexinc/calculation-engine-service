package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.ce.adapter.graphqlclient.endpoint.classificationallocation.ClassificationAllocationEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.classificationallocation.ClassificationAllocationEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.classificationallocation.ClassificationAllocationFixedIncomeEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.classificationallocation.ClassificationAllocationFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.classificationallocation.ClassificationAllocationStockEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.classificationallocation.ClassificationAllocationUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.ClassificationAllocationGraphqlDataFetcher;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.ClassificationAllocation;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.adapter.graphqlclient.endpoint.classificationallocation.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class ClassificationAllocationGraphqlDataFetcherTest {

  @Test
  void queryBenchOfFundCanada_verifyDoQuery() {
    // SETUP
    final var graphqlTransport = mock(GraphqlTransportComponent.class);
    final var classificationAllocationAbstractGraphqlDataFetcher = mock(ClassificationAllocationGraphqlDataFetcher.class,
        withSettings().useConstructor(graphqlTransport));

    final var holdings = mock(List.class);
    final var provider = mock(List.class);

    doCallRealMethod().when(classificationAllocationAbstractGraphqlDataFetcher).queryBenchOfFundCanada(any(), any());

    // ACT
    final Map map = classificationAllocationAbstractGraphqlDataFetcher.queryBenchOfFundCanada(holdings, provider);

    // VERIFY
    verify(classificationAllocationAbstractGraphqlDataFetcher).doQuery(same(holdings),
        argThat(arg -> arg.getClass() == ClassificationAllocationFundCanadaEndpoint.class), same(provider));
  }

  @Test
  void queryBenchOfEtfCanada() {
    // SETUP
    final var graphqlTransport = mock(GraphqlTransportComponent.class);
    final var classificationAllocationAbstractGraphqlDataFetcher = mock(ClassificationAllocationGraphqlDataFetcher.class,
        withSettings().useConstructor(graphqlTransport));

    final var holdings = mock(List.class);
    final var provider = mock(List.class);

    doCallRealMethod().when(classificationAllocationAbstractGraphqlDataFetcher).queryBenchOfEtfCanada(any(), any());

    // ACT
    final Map map = classificationAllocationAbstractGraphqlDataFetcher.queryBenchOfEtfCanada(holdings, provider);

    // VERIFY
    verify(classificationAllocationAbstractGraphqlDataFetcher).doQuery(same(holdings),
        argThat(arg -> arg.getClass() == ClassificationAllocationEtfCanadaEndpoint.class), same(provider));
  }

  @Test
  void queryBenchOfOfEtfUs() {
    // SETUP
    final var graphqlTransport = mock(GraphqlTransportComponent.class);
    final var classificationAllocationAbstractGraphqlDataFetcher = mock(ClassificationAllocationGraphqlDataFetcher.class,
        withSettings().useConstructor(graphqlTransport));

    final var holdings = mock(List.class);
    final var provider = mock(List.class);

    doCallRealMethod().when(classificationAllocationAbstractGraphqlDataFetcher).queryBenchOfOfEtfUs(any(), any());

    // ACT
    final Map map = classificationAllocationAbstractGraphqlDataFetcher.queryBenchOfOfEtfUs(holdings, provider);

    // VERIFY
    verify(classificationAllocationAbstractGraphqlDataFetcher).doQuery(same(holdings),
        argThat(arg -> arg.getClass() == ClassificationAllocationEtfUsEndpoint.class), same(provider));
  }

  @Test
  void queryBenchOfFixedIncome() {
    // SETUP
    final var graphqlTransport = mock(GraphqlTransportComponent.class);
    final var classificationAllocationAbstractGraphqlDataFetcher = mock(ClassificationAllocationGraphqlDataFetcher.class,
        withSettings().useConstructor(graphqlTransport));

    final var holdings = mock(List.class);
    final var provider = mock(List.class);

    doCallRealMethod().when(classificationAllocationAbstractGraphqlDataFetcher).queryBenchOfFixedIncomes(any(), any());

    // ACT
    final Map map = classificationAllocationAbstractGraphqlDataFetcher.queryBenchOfFixedIncomes(holdings, provider);

    // VERIFY
    verify(classificationAllocationAbstractGraphqlDataFetcher).doQuery(same(holdings),
        argThat(arg -> arg.getClass() == ClassificationAllocationFixedIncomeEndpoint.class), same(provider));
  }

  @Test
  void queryBenchOfStock_verifyDoQuery() {
    // SETUP
    final ClassificationAllocationGraphqlDataFetcher m = mock(ClassificationAllocationGraphqlDataFetcher.class);
    final List<StockHolding> holdings = List.of(mock(StockHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfStock(any(), anyList());
    // ACT
    m.queryBenchOfStock(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == ClassificationAllocationStockEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfStock_checkResult() {
    // SETUP
    final ClassificationAllocationGraphqlDataFetcher m = mock(ClassificationAllocationGraphqlDataFetcher.class);
    final List<StockHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfStock(any(), anyList());
    // ACT
    final Map<StockHolding, ClassificationAllocation> actual = m.queryBenchOfStock(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void queryUsMutualFunds_verifyDoQuery() {
    // SETUP
    final ClassificationAllocationGraphqlDataFetcher m = mock(ClassificationAllocationGraphqlDataFetcher.class);
    final List<UsMutualFundHolding> holdings = List.of(mock(UsMutualFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryUsMutualFunds(any(), anyList());
    // ACT
    m.queryUsMutualFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == ClassificationAllocationUsMutualFundEndpoint.class),
        eq(providers));
  }

}
