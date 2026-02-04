package com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns;

import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.smclient.graphql.Currency;
import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.MonthlyReturns;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.UsFund;
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
import static org.mockito.Mockito.when;

class MonthlyReturnsUsMutualFundEndpointTest {

  @Test
  void getGetUsEtfsByTickers_isPresent() {
    // SETUP
    final MonthlyReturnsUsMutualFundEndpoint m = new MonthlyReturnsUsMutualFundEndpoint();

    final Query q = mock(Query.class);
    final ArrayList<UsFund> expected = new ArrayList<>();

    when(q.getGetUsFundsByTickers()).thenReturn(expected);

    // ACT
    final Function<Query, List<UsFund>> actual = m.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(q), expected);
  }

  // @Test
  // void requestMapper_verify() {
  // //SETUP
  // final MonthlyReturnsUsMutualFundEndpoint m = mock(MonthlyReturnsUsMutualFundEndpoint.class);
  //
  // final UsFundQuery usFundQuery = mock(UsFundQuery.class);
  // when(usFundQuery.currency(any())).thenReturn(usFundQuery);
  // when(usFundQuery.monthlyReturns(any())).thenReturn(usFundQuery);
  // when(usFundQuery.ticker(any())).thenReturn(usFundQuery);
  //
  // doCallRealMethod().when(m).requestMapper(any());
  // //ACT
  // final UsFundQuery actual = m.requestMapper(usFundQuery);
  //
  // //VERIFY
  // verify(actual).currency(any());
  // verify(actual).monthlyReturns(any());
  // verify(actual).ticker(any());
  // }

  @Test
  void responseMapper_verifyMonthlyReturns() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final MonthlyReturnsUsMutualFundEndpoint sut = mock(MonthlyReturnsUsMutualFundEndpoint.class);

      final UsFund usFund = mock(UsFund.class);
      final MonthlyReturns monthlyReturns = mock(MonthlyReturns.class);
      when(usFund.getMonthlyReturns()).thenReturn(monthlyReturns);
      final Currency currency = mock(Currency.class);
      when(usFund.getCurrency()).thenReturn(currency);
      final CurrencyType cad = CurrencyType.CAD;
      when(currency.getType()).thenReturn(cad);

      final UsMutualFundHolding usMutualFundHolding = mock(UsMutualFundHolding.class);
      final HoldingType cash = HoldingType.CASH;
      when(usMutualFundHolding.getType()).thenReturn(cash);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      sut.responseMapper(usFund, usMutualFundHolding);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.monthlyReturns(monthlyReturns, cad.name(),
          usMutualFundHolding));
    }
  }

  @Test
  void responseMapper_currencyIsNull() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final MonthlyReturnsUsMutualFundEndpoint sut = mock(MonthlyReturnsUsMutualFundEndpoint.class);

      final com.fintex.ce.domain.model.MonthlyReturns expected = new com.fintex.ce.domain.model.MonthlyReturns();
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.monthlyReturns(any(), eq(null), any())).thenReturn(
          expected);

      final UsFund usFund = mock(UsFund.class);
      final MonthlyReturns monthlyReturns = mock(MonthlyReturns.class);
      when(usFund.getMonthlyReturns()).thenReturn(monthlyReturns);
      final Currency currency = mock(Currency.class);
      when(usFund.getCurrency()).thenReturn(currency);

      final UsMutualFundHolding usMutualFundHolding = mock(UsMutualFundHolding.class);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      final com.fintex.ce.domain.model.MonthlyReturns actual = sut.responseMapper(usFund, usMutualFundHolding);

      // VERIFY
      Assertions.assertNull(actual.getCurrency());
    }
  }

}
