package com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns;

import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.smclient.graphql.Currency;
import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.FundHoldingIdentifier;
import com.fintex.smclient.graphql.FundHoldingIdentifiersCodes;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
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

class MonthlyReturnsFundCanadaEndpointTest {

  @Test
  void getGetUsEtfsByTickers_isPresent() {
    // SETUP
    final MonthlyReturnsFundCanadaEndpoint m = new MonthlyReturnsFundCanadaEndpoint();

    final Query q = mock(Query.class);
    final ArrayList<FundSeries> expected = new ArrayList<>();

    when(q.getGetFundSeriesByHoldingCodes()).thenReturn(expected);

    // ACT
    final Function<Query, List<FundSeries>> actual = m.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(q), expected);
  }

  @Test
  void queryDefinition_verify() {
    // SETUP
    final MonthlyReturnsFundCanadaEndpoint m = mock(MonthlyReturnsFundCanadaEndpoint.class);

    final QueryQuery qq = mock(QueryQuery.class);

    final FundHoldingIdentifiersCodes codes = mock(FundHoldingIdentifiersCodes.class);
    final String code = "CODE";
    when(codes.getCode()).thenReturn(code);
    final FundHoldingIdentifier cash = FundHoldingIdentifier.CASH;
    when(codes.getFundholdingIdentifier()).thenReturn(cash);
    final List<FundHoldingIdentifiersCodes> equityIdentifiers = List.of(codes);

    doCallRealMethod().when(m).queryDefinition(any(), any());
    // ACT
    final QueryQueryDefinition actual = m.queryDefinition(equityIdentifiers, mock(UnaryOperator.class));
    actual.define(qq);

    // VERIFY
    verify(qq).getFundSeriesByHoldingCodes(eq(equityIdentifiers), any());
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final MonthlyReturnsFundCanadaEndpoint m = mock(MonthlyReturnsFundCanadaEndpoint.class);

    final FundSeriesQuery fundSeriesQuery = mock(FundSeriesQuery.class);
    when(fundSeriesQuery.currency(any(), any())).thenReturn(fundSeriesQuery);
    when(fundSeriesQuery.monthlyReturns(any())).thenReturn(fundSeriesQuery);
    when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final FundSeriesQuery actual = m.requestMapper(fundSeriesQuery);

    // VERIFY
    verify(actual).currency(any(), any());
    verify(actual).monthlyReturns(any());
    verify(actual).externalIdentifiers(any());
  }

  @Test
  void responseMapper_verifyMonthlyReturns() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final MonthlyReturnsFundCanadaEndpoint sut = mock(MonthlyReturnsFundCanadaEndpoint.class);

      final FundSeries etf = mock(FundSeries.class);
      final MonthlyReturns monthlyReturns = mock(MonthlyReturns.class);
      when(etf.getMonthlyReturns()).thenReturn(monthlyReturns);
      final Currency currency = mock(Currency.class);
      when(etf.getCurrency()).thenReturn(currency);
      final CurrencyType cad = CurrencyType.CAD;
      when(currency.getType()).thenReturn(cad);

      final FundSeriesHolding fundSeriesHolding = mock(FundSeriesHolding.class);
      final HoldingType cash = HoldingType.CASH;
      when(fundSeriesHolding.getType()).thenReturn(cash);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      sut.responseMapper(etf, fundSeriesHolding);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.monthlyReturns(monthlyReturns, cad.name(),
          fundSeriesHolding));
    }
  }

  @Test
  void responseMapper_currencyIsNull() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final MonthlyReturnsFundCanadaEndpoint sut = mock(MonthlyReturnsFundCanadaEndpoint.class);

      final com.fintex.ce.domain.model.MonthlyReturns expected = new com.fintex.ce.domain.model.MonthlyReturns();
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.monthlyReturns(any(), eq(null), any())).thenReturn(
          expected);

      final FundSeries etf = mock(FundSeries.class);
      final MonthlyReturns monthlyReturns = mock(MonthlyReturns.class);
      when(etf.getMonthlyReturns()).thenReturn(monthlyReturns);
      final Currency currency = mock(Currency.class);
      when(etf.getCurrency()).thenReturn(currency);

      final FundSeriesHolding fundSeriesHolding = mock(FundSeriesHolding.class);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      final com.fintex.ce.domain.model.MonthlyReturns actual = sut.responseMapper(etf, fundSeriesHolding);

      // VERIFY
      Assertions.assertNull(actual.getCurrency());
    }
  }

}
