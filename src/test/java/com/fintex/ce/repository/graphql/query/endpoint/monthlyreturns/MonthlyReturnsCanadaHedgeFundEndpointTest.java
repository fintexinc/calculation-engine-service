package com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns;

import com.fintex.smclient.graphql.Currency;
import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.smclient.graphql.MonthlyReturns;
import com.fintex.smclient.graphql.Query;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MonthlyReturnsCanadaHedgeFundEndpointTest {

    @Test
    void getGetUsEtfsByTickers_isPresent() {
        //SETUP
        final MonthlyReturnsCanadaHedgeFundEndpoint m = new MonthlyReturnsCanadaHedgeFundEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<HedgeFund> expected = new ArrayList<>();

        when(q.getGetCanadaHedgeFundsByMorningstarIds()).thenReturn(expected);

        //ACT
        final Function<Query, List<HedgeFund>> actual = m.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final MonthlyReturnsCanadaHedgeFundEndpoint m = mock(MonthlyReturnsCanadaHedgeFundEndpoint.class);

        final HedgeFundQuery hedgeFundQuery = mock(HedgeFundQuery.class);
        when(hedgeFundQuery.currency(any())).thenReturn(hedgeFundQuery);
        when(hedgeFundQuery.monthlyReturns(any())).thenReturn(hedgeFundQuery);
        when(hedgeFundQuery.externalIdentifiers(any())).thenReturn(hedgeFundQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final HedgeFundQuery actual = m.requestMapper(hedgeFundQuery);

        //VERIFY
        verify(actual).currency(any());
        verify(actual).monthlyReturns(any());
        verify(actual).externalIdentifiers(any());
    }

    @Test
    void responseMapper_verifyMonthlyReturns() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final MonthlyReturnsCanadaHedgeFundEndpoint sut = mock(MonthlyReturnsCanadaHedgeFundEndpoint.class);

            final HedgeFund hedgeFund = mock(HedgeFund.class);
            final MonthlyReturns monthlyReturns = mock(MonthlyReturns.class);
            when(hedgeFund.getMonthlyReturns()).thenReturn(monthlyReturns);
            final Currency currency = mock(Currency.class);
            when(hedgeFund.getCurrency()).thenReturn(currency);
            final CurrencyType cad = CurrencyType.CAD;
            when(currency.getType()).thenReturn(cad);

            final CanadaHedgeFundHolding canadaHedgeFundHolding = mock(CanadaHedgeFundHolding.class);
            final HoldingType cash = HoldingType.CASH;
            when(canadaHedgeFundHolding.getType()).thenReturn(cash);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            sut.responseMapper(hedgeFund, canadaHedgeFundHolding);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.monthlyReturns(monthlyReturns, cad.name(), canadaHedgeFundHolding));
        }
    }

    @Test
    void responseMapper_currencyIsNull() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final MonthlyReturnsCanadaHedgeFundEndpoint sut = mock(MonthlyReturnsCanadaHedgeFundEndpoint.class);

            final RMonthlyReturns expected = new RMonthlyReturns(null, null, null);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.monthlyReturns(any(), eq(null), any())).thenReturn(expected);

            final HedgeFund hedgeFund = mock(HedgeFund.class);
            final MonthlyReturns monthlyReturns = mock(MonthlyReturns.class);
            when(hedgeFund.getMonthlyReturns()).thenReturn(monthlyReturns);
            final Currency currency = mock(Currency.class);
            when(hedgeFund.getCurrency()).thenReturn(currency);

            final CanadaHedgeFundHolding fundSeriesHolding = mock(CanadaHedgeFundHolding.class);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            final RMonthlyReturns actual = sut.responseMapper(hedgeFund, fundSeriesHolding);

            //VERIFY
            Assertions.assertNull(actual.getCurrency());
        }
    }
    
}
