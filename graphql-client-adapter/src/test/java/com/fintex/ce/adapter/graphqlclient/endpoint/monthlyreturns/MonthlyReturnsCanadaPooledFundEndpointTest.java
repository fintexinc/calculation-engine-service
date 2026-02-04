package com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns;

import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.smclient.graphql.Currency;
import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.MonthlyReturns;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.smclient.graphql.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MonthlyReturnsCanadaPooledFundEndpointTest {

  @Test
  void getGetUsEtfsByTickers_isPresent() {
    // SETUP
    final MonthlyReturnsCanadaPooledFundEndpoint m = new MonthlyReturnsCanadaPooledFundEndpoint();

    final Query q = mock(Query.class);
    final ArrayList<PooledFund> expected = new ArrayList<>();

    when(q.getGetCanadaPooledFundsByMorningstarIds()).thenReturn(expected);

    // ACT
    final Function<Query, List<PooledFund>> actual = m.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(q), expected);
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final MonthlyReturnsCanadaPooledFundEndpoint m = mock(MonthlyReturnsCanadaPooledFundEndpoint.class);

    final PooledFundQuery pooledFundQuery = mock(PooledFundQuery.class);
    when(pooledFundQuery.currency(any())).thenReturn(pooledFundQuery);
    when(pooledFundQuery.monthlyReturns(any())).thenReturn(pooledFundQuery);
    when(pooledFundQuery.externalIdentifiers(any())).thenReturn(pooledFundQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final PooledFundQuery actual = m.requestMapper(pooledFundQuery);

    // VERIFY
    verify(actual).currency(any());
    verify(actual).monthlyReturns(any());
    verify(actual).externalIdentifiers(any());
  }

  @Test
  void responseMapper_verifyMonthlyReturns() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final MonthlyReturnsCanadaPooledFundEndpoint sut = mock(MonthlyReturnsCanadaPooledFundEndpoint.class);

      final PooledFund pooledFund = mock(PooledFund.class);
      final MonthlyReturns monthlyReturns = mock(MonthlyReturns.class);
      when(pooledFund.getMonthlyReturns()).thenReturn(monthlyReturns);
      final Currency currency = mock(Currency.class);
      when(pooledFund.getCurrency()).thenReturn(currency);
      final CurrencyType cad = CurrencyType.CAD;
      when(currency.getType()).thenReturn(cad);

      final CanadaPooledFundHolding canadaPooledFundHolding = mock(CanadaPooledFundHolding.class);
      final HoldingType cash = HoldingType.CASH;
      when(canadaPooledFundHolding.getType()).thenReturn(cash);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      sut.responseMapper(pooledFund, canadaPooledFundHolding);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.monthlyReturns(monthlyReturns, cad.name(),
          canadaPooledFundHolding));
    }
  }

  @Test
  void responseMapper_currencyIsNull() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final MonthlyReturnsCanadaPooledFundEndpoint sut = mock(MonthlyReturnsCanadaPooledFundEndpoint.class);

      final com.fintex.ce.domain.model.MonthlyReturns expected = new com.fintex.ce.domain.model.MonthlyReturns();
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.monthlyReturns(any(), eq(null), any())).thenReturn(
          expected);

      final PooledFund pooledFund = mock(PooledFund.class);
      final MonthlyReturns monthlyReturns = mock(MonthlyReturns.class);
      when(pooledFund.getMonthlyReturns()).thenReturn(monthlyReturns);
      final Currency currency = mock(Currency.class);
      when(pooledFund.getCurrency()).thenReturn(currency);

      final CanadaPooledFundHolding canadaPooledFundHolding = mock(CanadaPooledFundHolding.class);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      final com.fintex.ce.domain.model.MonthlyReturns actual = sut.responseMapper(pooledFund, canadaPooledFundHolding);

      // VERIFY
      Assertions.assertNull(actual.getCurrency());
    }
  }

}
