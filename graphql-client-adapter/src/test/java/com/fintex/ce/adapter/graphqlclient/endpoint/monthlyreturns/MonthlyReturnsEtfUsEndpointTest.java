package com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns;

import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.smclient.graphql.Currency;
import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.MonthlyReturns;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MonthlyReturnsEtfUsEndpointTest {

  @Test
  void getGetUsEtfsByTickers_isPresent() {
    // SETUP
    final MonthlyReturnsEtfUsEndpoint m = new MonthlyReturnsEtfUsEndpoint();

    final Query q = mock(Query.class);
    final ArrayList<Etf> expected = new ArrayList<>();

    when(q.getGetUsEtfsByTickers()).thenReturn(expected);

    // ACT
    final Function<Query, List<Etf>> actual = m.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(q), expected);
  }

  @Test
  void queryDefinition_verify() {
    // SETUP
    final MonthlyReturnsEtfUsEndpoint m = mock(MonthlyReturnsEtfUsEndpoint.class);

    final QueryQuery qq = mock(QueryQuery.class);

    final List<String> equityIdentifiers = List.of("TEST");

    doCallRealMethod().when(m).queryDefinition(any(), any());
    // ACT
    final QueryQueryDefinition actual = m.queryDefinition(equityIdentifiers, mock(UnaryOperator.class));
    actual.define(qq);

    // VERIFY
    verify(qq).getUsEtfsByTickers(eq(equityIdentifiers), any());
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final MonthlyReturnsEtfUsEndpoint m = mock(MonthlyReturnsEtfUsEndpoint.class);

    final EtfQuery etfQuery = mock(EtfQuery.class);
    when(etfQuery.currency(any(), any())).thenReturn(etfQuery);
    when(etfQuery.monthlyReturns(any())).thenReturn(etfQuery);
    when(etfQuery.ticker(any())).thenReturn(etfQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final EtfQuery actual = m.requestMapper(etfQuery);

    // VERIFY
    verify(actual).currency(any(), any());
    verify(actual).monthlyReturns(any());
    verify(actual).ticker(any());
  }

  @Test
  void responseMapper_verifyMonthlyReturns() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final MonthlyReturnsEtfUsEndpoint sut = mock(MonthlyReturnsEtfUsEndpoint.class);

      final Etf etf = mock(Etf.class);
      final MonthlyReturns monthlyReturns = mock(MonthlyReturns.class);
      when(etf.getMonthlyReturns()).thenReturn(monthlyReturns);
      final Currency currency = mock(Currency.class);
      when(etf.getCurrency()).thenReturn(currency);
      final CurrencyType cad = CurrencyType.CAD;
      when(currency.getType()).thenReturn(cad);

      final EtfHolding etfHolding = mock(EtfHolding.class);
      final HoldingType cash = HoldingType.CASH;
      when(etfHolding.getType()).thenReturn(cash);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      sut.responseMapper(etf, etfHolding);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.monthlyReturns(monthlyReturns, cad.name(), etfHolding));
    }
  }

  @Test
  void responseMapper_currencyIsNull() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final MonthlyReturnsEtfUsEndpoint sut = mock(MonthlyReturnsEtfUsEndpoint.class);

      final com.fintex.ce.domain.model.MonthlyReturns expected = new com.fintex.ce.domain.model.MonthlyReturns();
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.monthlyReturns(any(), eq(null), any())).thenReturn(
          expected);

      final Etf etf = mock(Etf.class);
      final MonthlyReturns monthlyReturns = mock(MonthlyReturns.class);
      when(etf.getMonthlyReturns()).thenReturn(monthlyReturns);
      final Currency currency = mock(Currency.class);
      when(etf.getCurrency()).thenReturn(currency);

      final EtfHolding etfHolding = mock(EtfHolding.class);
      final HoldingType cash = HoldingType.CASH;
      when(etfHolding.getType()).thenReturn(cash);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      final com.fintex.ce.domain.model.MonthlyReturns actual = sut.responseMapper(etf, etfHolding);

      // VERIFY
      Assertions.assertNull(actual.getCurrency());
    }
  }

}
