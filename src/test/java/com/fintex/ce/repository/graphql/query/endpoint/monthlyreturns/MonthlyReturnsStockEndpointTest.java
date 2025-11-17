package com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns;

import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.Currency;
import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.CurrencyValue;
import com.fintex.smclient.graphql.EquityIdentifiers;
import com.fintex.smclient.graphql.MarketCapitalization;
import com.fintex.smclient.graphql.MonthlyReturns;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.Stock;
import com.fintex.smclient.graphql.StockQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MonthlyReturnsStockEndpointTest {

    @Test
    void getGetStocksByTickersAndExchangeIds_isPresent() {
        //SETUP
        final MonthlyReturnsStockEndpoint m = new MonthlyReturnsStockEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<Stock> expected = new ArrayList<>();

        when(q.getGetStocksByTickersAndExchangeIds()).thenReturn(expected);

        //ACT
        final Function<Query, List<Stock>> actual = m.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void queryDefinition_verify() {
        //SETUP
        final MonthlyReturnsStockEndpoint m = mock(MonthlyReturnsStockEndpoint.class);

        final QueryQuery qq = mock(QueryQuery.class);

        final EquityIdentifiers identifiers = mock(EquityIdentifiers.class);
        final List<EquityIdentifiers> equityIdentifiers = List.of(identifiers);

        doCallRealMethod().when(m).queryDefinition(any(), any());
        //ACT
        final QueryQueryDefinition actual = m.queryDefinition(equityIdentifiers, mock(UnaryOperator.class));
        actual.define(qq);

        //VERIFY
        verify(qq).getStocksByTickersAndExchangeIds(eq(equityIdentifiers), any());
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final MonthlyReturnsStockEndpoint m = mock(MonthlyReturnsStockEndpoint.class);

        final StockQuery etfQuery = mock(StockQuery.class);
        when(etfQuery.currency(any())).thenReturn(etfQuery);
        when(etfQuery.monthlyMarketReturns(any())).thenReturn(etfQuery);
        when(etfQuery.externalIdentifiers(any())).thenReturn(etfQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final StockQuery actual = m.requestMapper(etfQuery);

        //VERIFY
        verify(actual).currency(any());
        verify(actual).externalIdentifiers(any());
        verify(actual).monthlyMarketReturns(any());
    }

    @Test
    void responseMapper_verifyMonthlyReturns() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final MonthlyReturnsStockEndpoint sut = mock(MonthlyReturnsStockEndpoint.class);

            final Stock stock = mock(Stock.class);
            final Currency currency = mock(Currency.class);
            when(stock.getCurrency()).thenReturn(currency);
            when(currency.getType()).thenReturn(CurrencyType.CAD);

            final MonthlyReturns monthlyReturns = mock(MonthlyReturns.class);
            when(stock.getMonthlyMarketReturns()).thenReturn(monthlyReturns);

            final StockHolding holding = mock(StockHolding.class);
            final HoldingType cash = HoldingType.CASH;
            when(holding.getType()).thenReturn(cash);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            sut.responseMapper(stock, holding);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.monthlyReturns(monthlyReturns, CurrencyType.CAD.name(), holding));
        }
    }

    @Test
    void responseMapper_currencyIsNull() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final MonthlyReturnsStockEndpoint sut = mock(MonthlyReturnsStockEndpoint.class);

            final RMonthlyReturns expected = new RMonthlyReturns(null, null, null);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.monthlyReturns(any(), eq(null), any())).thenReturn(expected);

            final Stock stock = mock(Stock.class);
            final MarketCapitalization marketCapitalization = mock(MarketCapitalization.class);
            when(stock.getMarketCapitalization()).thenReturn(marketCapitalization);
            final CurrencyValue currencyValue = mock(CurrencyValue.class);
            when(marketCapitalization.getValues()).thenReturn(List.of(currencyValue));

            final MonthlyReturns monthlyReturns = mock(MonthlyReturns.class);
            when(stock.getMonthlyMarketReturns()).thenReturn(monthlyReturns);

            final StockHolding holding = mock(StockHolding.class);
            final HoldingType cash = HoldingType.CASH;
            when(holding.getType()).thenReturn(cash);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            final RMonthlyReturns actual = sut.responseMapper(stock, holding);

            //VERIFY
            Assertions.assertNull(actual.getCurrency());
        }
    }

}
