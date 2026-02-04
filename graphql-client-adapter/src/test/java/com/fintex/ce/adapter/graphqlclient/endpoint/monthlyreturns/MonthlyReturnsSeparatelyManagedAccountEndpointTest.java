package com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns;

import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsSeparatelyManagedAccountEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.SmaHolding;
import com.fintex.smclient.graphql.Currency;
import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.MonthlyReturns;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.SeparatelyManagedAccount;
import com.fintex.smclient.graphql.SeparatelyManagedAccountQuery;
import com.fintex.smclient.graphql.SmaIdentifier;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.function.UnaryOperator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MonthlyReturnsSeparatelyManagedAccountEndpointTest {

  @Test
  void queryDefinition_verify() {
    // SETUP
    final MonthlyReturnsSeparatelyManagedAccountEndpoint sut = mock(
        MonthlyReturnsSeparatelyManagedAccountEndpoint.class);

    final QueryQuery qq = mock(QueryQuery.class);

    final SmaIdentifier smaIdentifier = mock(SmaIdentifier.class);
    final List<SmaIdentifier> smaIdentifiers = List.of(smaIdentifier);

    doCallRealMethod().when(sut).queryDefinition(any(), any());
    // ACT
    final QueryQueryDefinition actual = sut.queryDefinition(smaIdentifiers, mock(UnaryOperator.class));
    actual.define(qq);

    // VERIFY
    verify(qq).getSeparatelyManagedAccountsBy(eq(smaIdentifiers), any());
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final MonthlyReturnsSeparatelyManagedAccountEndpoint sut = mock(
        MonthlyReturnsSeparatelyManagedAccountEndpoint.class);

    final SeparatelyManagedAccountQuery separatelyManagedAccountQuery = mock(SeparatelyManagedAccountQuery.class);
    when(separatelyManagedAccountQuery.currency(any())).thenReturn(separatelyManagedAccountQuery);
    when(separatelyManagedAccountQuery.monthlyReturns(any())).thenReturn(separatelyManagedAccountQuery);
    when(separatelyManagedAccountQuery.externalIdentifiers(any())).thenReturn(separatelyManagedAccountQuery);

    doCallRealMethod().when(sut).requestMapper(any());
    // ACT
    final SeparatelyManagedAccountQuery actual = sut.requestMapper(separatelyManagedAccountQuery);

    // VERIFY
    verify(actual).currency(any());
    verify(actual).monthlyReturns(any());
    verify(actual).externalIdentifiers(any());
  }

  @Test
  void responseMapper_verifyMonthlyReturns() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final MonthlyReturnsSeparatelyManagedAccountEndpoint sut = mock(
          MonthlyReturnsSeparatelyManagedAccountEndpoint.class);

      final SeparatelyManagedAccount separatelyManagedAccount = mock(SeparatelyManagedAccount.class);
      final MonthlyReturns monthlyReturns = mock(MonthlyReturns.class);
      when(separatelyManagedAccount.getMonthlyReturns()).thenReturn(monthlyReturns);
      final Currency currency = mock(Currency.class);
      when(separatelyManagedAccount.getCurrency()).thenReturn(currency);
      final CurrencyType cad = CurrencyType.CAD;
      when(currency.getType()).thenReturn(cad);

      final SmaHolding smaHolding = mock(SmaHolding.class);
      final HoldingType holdingType = HoldingType.SEPARATELY_MANAGED_ACCOUNT;
      when(smaHolding.getType()).thenReturn(holdingType);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      sut.responseMapper(separatelyManagedAccount, smaHolding);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.monthlyReturns(monthlyReturns, cad.name(), smaHolding));
    }
  }

  @Test
  void responseMapper_verifyMonthlyReturns_FASCurrencyMissing() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final MonthlyReturnsSeparatelyManagedAccountEndpoint sut = mock(
          MonthlyReturnsSeparatelyManagedAccountEndpoint.class);

      final SeparatelyManagedAccount separatelyManagedAccount = mock(SeparatelyManagedAccount.class);
      final MonthlyReturns monthlyReturns = mock(MonthlyReturns.class);
      when(separatelyManagedAccount.getMonthlyReturns()).thenReturn(monthlyReturns);
      when(separatelyManagedAccount.getCurrency()).thenReturn(null);

      final SmaHolding smaHolding = mock(SmaHolding.class);
      final HoldingType holdingType = HoldingType.SEPARATELY_MANAGED_ACCOUNT;
      when(smaHolding.getType()).thenReturn(holdingType);
      when(smaHolding.getCurrency()).thenReturn(CurrencyType.USD.name());

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      sut.responseMapper(separatelyManagedAccount, smaHolding);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.monthlyReturns(monthlyReturns, CurrencyType.USD.name(),
          smaHolding));
    }
  }

}
