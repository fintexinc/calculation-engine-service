package com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns;

import com.fintex.ce.config.enumeration.ExceptionCode;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.Currency;
import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;
import com.fintex.smclient.graphql.MonthlyReturns;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.fintex.smclient.graphql.CurrencyType.CAD;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MonthlyReturnsBenchmarkEndpointTest {

    @Test
    void getGetUsEtfsByTickers_isPresent() {
        //SETUP
        final MonthlyReturnsBenchmarkEndpoint m = new MonthlyReturnsBenchmarkEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<Index> expected = new ArrayList<>();

        when(q.getGetIndexesByMorningstarIds()).thenReturn(expected);

        //ACT
        final Function<Query, List<Index>> actual = m.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void queryDefinition_verify() {
        //SETUP
        final MonthlyReturnsBenchmarkEndpoint m = mock(MonthlyReturnsBenchmarkEndpoint.class);

        final QueryQuery qq = mock(QueryQuery.class);

        final String code = "CODE";
        final List<String> equityIdentifiers = List.of(code);

        doCallRealMethod().when(m).queryDefinition(any(), any());
        //ACT
        final QueryQueryDefinition actual = m.queryDefinition(equityIdentifiers, mock(UnaryOperator.class));
        actual.define(qq);

        //VERIFY
        verify(qq).getIndexesByMorningstarIds(eq(equityIdentifiers), any(), any());
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final MonthlyReturnsBenchmarkEndpoint m = mock(MonthlyReturnsBenchmarkEndpoint.class);

        final IndexQuery fundSeriesQuery = mock(IndexQuery.class);
        when(fundSeriesQuery.currency(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.monthlyReturns(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final IndexQuery actual = m.requestMapper(fundSeriesQuery);

        //VERIFY
        verify(actual).currency(any());
        verify(actual).monthlyReturns(any());
        verify(actual).externalIdentifiers(any());
    }

    @Test
    void responseMapper_verifyMonthlyReturns() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final MonthlyReturnsBenchmarkEndpoint sut = mock(MonthlyReturnsBenchmarkEndpoint.class);

            final Index etf = mock(Index.class);
            final MonthlyReturns monthlyReturns = mock(MonthlyReturns.class);
            when(etf.getMonthlyReturns()).thenReturn(monthlyReturns);
            final Currency currency = mock(Currency.class);
            when(etf.getCurrency()).thenReturn(currency);
            final CurrencyType cad = CAD;
            when(currency.getType()).thenReturn(cad);

            final BenchmarkIndexHolding benchmarkIndexHolding = mock(BenchmarkIndexHolding.class);
            final HoldingType cash = HoldingType.CASH;
            when(benchmarkIndexHolding.getType()).thenReturn(cash);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            sut.responseMapper(etf, benchmarkIndexHolding);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.monthlyReturns(monthlyReturns, cad.name(), benchmarkIndexHolding));
        }
    }

    @Test
    void responseMapper_currencyIsNull() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final MonthlyReturnsBenchmarkEndpoint sut = mock(MonthlyReturnsBenchmarkEndpoint.class);

            final RMonthlyReturns expected = new RMonthlyReturns(null, null, null);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.monthlyReturns(any(), eq(null), any())).thenReturn(expected);

            final Index etf = mock(Index.class);
            final MonthlyReturns monthlyReturns = mock(MonthlyReturns.class);
            when(etf.getMonthlyReturns()).thenReturn(monthlyReturns);
            final Currency currency = mock(Currency.class);
            when(etf.getCurrency()).thenReturn(currency);

            final BenchmarkIndexHolding fundSeriesHolding = mock(BenchmarkIndexHolding.class);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            final RMonthlyReturns actual = sut.responseMapper(etf, fundSeriesHolding);

            //VERIFY
            Assertions.assertNull(actual.getCurrency());
        }
    }

    @Test
    void responseMapper_throwsException2() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final MonthlyReturnsBenchmarkEndpoint sut = mock(MonthlyReturnsBenchmarkEndpoint.class);

            final Index etf = mock(Index.class);
            final Currency currency = mock(Currency.class);
            when(etf.getCurrency()).thenReturn(currency);
            when(etf.getCurrency().getType()).thenReturn(CAD);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.monthlyReturns(any(), any(), any())).thenCallRealMethod();

            final BenchmarkIndexHolding fundSeriesHolding = mock(BenchmarkIndexHolding.class);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            var actual = sut.responseMapper(etf, fundSeriesHolding);

            //VERIFY
            assertTrue(actual.getErrors().stream().anyMatch(e -> e.getCode().equals(ExceptionCode.ERR_RRC_MMR_001)));
        }
    }

}
