package com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns;

import com.fintex.smclient.graphql.Currency;
import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FixedIncomeQuery;
import com.fintex.smclient.graphql.MonthlyReturns;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import org.junit.jupiter.api.Assertions;
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

class MonthlyReturnsFixedIncomeEndpointTest {

    @Test
    void queryDefinition_verify() {
        //SETUP
        final MonthlyReturnsFixedIncomeEndpoint sut = mock(MonthlyReturnsFixedIncomeEndpoint.class);

        final QueryQuery qq = mock(QueryQuery.class);

        final String adpNumber = "adpNumber";
        final List<String> adpNumbers = List.of(adpNumber);

        doCallRealMethod().when(sut).queryDefinition(any(), any());
        //ACT
        final QueryQueryDefinition actual = sut.queryDefinition(adpNumbers, mock(UnaryOperator.class));
        actual.define(qq);

        //VERIFY
        verify(qq).getFixedIncomeByBroadridgeAdpNumbers(eq(adpNumbers), any());
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final MonthlyReturnsFixedIncomeEndpoint sut = mock(MonthlyReturnsFixedIncomeEndpoint.class);

        final FixedIncomeQuery fixedIncomeQuery = mock(FixedIncomeQuery.class);
        when(fixedIncomeQuery.currency(any())).thenReturn(fixedIncomeQuery);
        when(fixedIncomeQuery.monthlyReturns(any())).thenReturn(fixedIncomeQuery);
        when(fixedIncomeQuery.externalIdentifiers(any())).thenReturn(fixedIncomeQuery);

        doCallRealMethod().when(sut).requestMapper(any());
        //ACT
        final FixedIncomeQuery actual = sut.requestMapper(fixedIncomeQuery);

        //VERIFY
        verify(actual).currency(any());
        verify(actual).monthlyReturns(any());
        verify(actual).externalIdentifiers(any());
    }

    @Test
    void responseMapper_verifyMonthlyReturns() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final MonthlyReturnsFixedIncomeEndpoint sut = mock(MonthlyReturnsFixedIncomeEndpoint.class);

            final FixedIncome fixedIncome = mock(FixedIncome.class);
            final MonthlyReturns monthlyReturns = mock(MonthlyReturns.class);
            when(fixedIncome.getMonthlyReturns()).thenReturn(monthlyReturns);
            final Currency currency = mock(Currency.class);
            when(fixedIncome.getCurrency()).thenReturn(currency);
            final CurrencyType cad = CurrencyType.CAD;
            when(currency.getType()).thenReturn(cad);

            final FixedIncomeHolding fixedIncomeHolding = mock(FixedIncomeHolding.class);
            final HoldingType fixedIncomeType = HoldingType.FIXED_INCOME;
            when(fixedIncomeHolding.getType()).thenReturn(fixedIncomeType);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            sut.responseMapper(fixedIncome, fixedIncomeHolding);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.monthlyReturns(monthlyReturns, cad.name(), fixedIncomeHolding));
        }
    }

    @Test
    void responseMapper_currencyIsNull() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final MonthlyReturnsFixedIncomeEndpoint sut = mock(MonthlyReturnsFixedIncomeEndpoint.class);

            final RMonthlyReturns expected = new RMonthlyReturns(null, null, null);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.monthlyReturns(any(), eq(null), any())).thenReturn(expected);

            final FixedIncome fixedIncome = mock(FixedIncome.class);
            final MonthlyReturns monthlyReturns = mock(MonthlyReturns.class);
            when(fixedIncome.getMonthlyReturns()).thenReturn(monthlyReturns);
            final Currency currency = mock(Currency.class);
            when(fixedIncome.getCurrency()).thenReturn(currency);

            final FixedIncomeHolding fixedIncomeHolding = mock(FixedIncomeHolding.class);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            final RMonthlyReturns actual = sut.responseMapper(fixedIncome, fixedIncomeHolding);

            //VERIFY
            Assertions.assertNull(actual.getCurrency());
        }
    }

}
