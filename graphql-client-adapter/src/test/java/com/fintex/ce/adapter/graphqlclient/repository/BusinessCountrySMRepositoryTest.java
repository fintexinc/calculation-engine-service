package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.ce.adapter.graphqlclient.endpoint.BusinessCountryEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.BusinessCountrySMRepository;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.BusinessCountry;
import com.fintex.ce.domain.model.holding.StockHolding;
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

class BusinessCountrySMRepositoryTest {

  @Test
  void queryBenchOfStock_verifyDoQuery() {
    // SETUP
    final var graphqlTransport = mock(GraphqlTransportComponent.class);
    final BusinessCountrySMRepository m = mock(BusinessCountrySMRepository.class, withSettings().useConstructor(
        graphqlTransport));
    final List<StockHolding> holdings = List.of(mock(StockHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfStock(any(), anyList());
    // ACT
    m.queryBenchOfStock(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == BusinessCountryEndpoint.class),
        eq(providers));
  }

  @Test
  void queryBenchOfStock_checkResult() {
    // SETUP
    final BusinessCountrySMRepository m = mock(BusinessCountrySMRepository.class);
    final List<StockHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryBenchOfStock(any(), anyList());
    // ACT
    final Map<StockHolding, BusinessCountry> actual = m.queryBenchOfStock(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

}