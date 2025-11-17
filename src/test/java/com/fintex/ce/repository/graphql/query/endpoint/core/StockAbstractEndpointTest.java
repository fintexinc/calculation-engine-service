package com.fintex.ce.repository.graphql.query.endpoint.core;

import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns.MonthlyReturnsStockEndpoint;
import com.fintex.ce.util.ComparisonUtils;
import com.fintex.smclient.graphql.EquityIdentifiers;
import com.fintex.smclient.graphql.ExternalIdentifierTypeValue;
import com.fintex.smclient.graphql.ExternalIdentifiers;
import com.fintex.smclient.graphql.Stock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StockAbstractEndpointTest {

    @Test
    void collectIds_checkResult() {
        //SETUP
        final StockAbstractEndpoint s = mock(StockAbstractEndpoint.class);

        final StockHolding h = mock(StockHolding.class);
        final String code = "RBF540";
        final String ticker = "RBF540T";
        when(h.getTicker()).thenReturn(ticker);
        when(h.getExchangeCode()).thenReturn(code);

        doCallRealMethod().when(s).collectIds(any());
        //ACT
        final List<EquityIdentifiers> actual = s.collectIds(List.of(h));

        //VERIFY
        final EquityIdentifiers expected = new EquityIdentifiers(code, ticker);
        assertEquals(1, actual.size());
        assertEquals(expected.getExchangeId(), actual.get(0).getExchangeId());
        assertEquals(expected.getTicker(), actual.get(0).getTicker());
    }

    @Test
    void findHoldingBasedOnRes_checkResults() {
        //SETUP
        final StockAbstractEndpoint s = mock(StockAbstractEndpoint.class);

        final StockHolding h = mock(StockHolding.class);
        final String ticker = "RBF540T";
        when(h.getTicker()).thenReturn(ticker);
        final String code = "CODE";
        when(h.getExchangeCode()).thenReturn(code);

        when(s.getIds(any())).thenReturn(List.of(ticker, code));

        doCallRealMethod().when(s).findHoldingBasedOnRes(any(), any());
        //ACT
        final StockHolding actual = s.findHoldingBasedOnRes(List.of(h), null);

        //VERIFY
        assertEquals(h, actual);
    }

    @Test
    void getIds_checkResults() {
        //SETUP
        final StockAbstractEndpoint s = mock(StockAbstractEndpoint.class);
        final String code = "VAB";

        final Stock fundSeries = mock(Stock.class);
        final ExternalIdentifiers identifiers = mock(ExternalIdentifiers.class);
        when(fundSeries.getExternalIdentifiers()).thenReturn(identifiers);
        final ExternalIdentifierTypeValue fundSeriesHolding = mock(ExternalIdentifierTypeValue.class);
        when(identifiers.getCodes()).thenReturn(List.of(fundSeriesHolding));
        when(fundSeriesHolding.getValue()).thenReturn(code);

        doCallRealMethod().when(s).getIds(any());
        //ACT
        final List actual = s.getIds(fundSeries);

        //VERIFY
        assertEquals(List.of(code), actual);
    }

    @Test
    void basicResponseMapper_verifyRsponseMapper() {
        //SETUP
        final StockAbstractEndpoint e = mock(StockAbstractEndpoint.class);

        when(e.responseMapper(any(), any())).thenReturn(mock(RedisId.class));

        final StockHolding h = mock(StockHolding.class);
        final Stock entity = mock(Stock.class);
        doCallRealMethod().when(e).basicResponseMapper(any(), any());
        //ACT
        e.basicResponseMapper(entity, h);

        //VERIFY
        verify(e).responseMapper(entity, h);
    }

    @Test
    void basicResponseMapper_checkResult() {
        //SETUP
        final StockAbstractEndpoint e = mock(StockAbstractEndpoint.class);

        final RedisId expected = mock(RedisId.class);
        when(e.responseMapper(any(), any())).thenReturn(expected);

        final StockHolding h = mock(StockHolding.class);
        when(h.generateUserIdentifier()).thenReturn("SDF");

        final Stock entity = mock(Stock.class);

        doCallRealMethod().when(e).basicResponseMapper(any(), any());
        //ACT
        final RedisId actual = e.basicResponseMapper(entity, h);

        //VERIFY
        verify(expected).setHoldingId(h.generateUserIdentifier());
        assertSame(expected, actual);
    }

    @Test
    void populateEmptyResponseWithIdentifier_checkResult() {
        //SETUP
        final var sut = new MonthlyReturnsStockEndpoint();
        final var stockHolding = new StockHolding();
        stockHolding.setTicker("testTicker");
        stockHolding.setExchangeCode("testExchangeCode");
        final Stock stock = new Stock();
        final ExternalIdentifiers identifiers = new ExternalIdentifiers();
        identifiers.setCodes(List.of());
        stock.setExternalIdentifiers(identifiers);

        final Stock stock1 = new Stock();
        final var ticker = new ExternalIdentifierTypeValue();
        ticker.setValue("ticker");
        final var exchangeCode = new ExternalIdentifierTypeValue();
        exchangeCode.setValue("exchangeCode");
        final ExternalIdentifiers externalIdentifiers = new ExternalIdentifiers().setCodes(List.of(ticker, exchangeCode));
        stock1.setExternalIdentifiers(externalIdentifiers);


        //ACT
        sut.populateEmptyResponseWithIdentifier(List.of(stock, stock1), stockHolding);

        //VERIFY
        final List<String> expected = List.of(stockHolding.getTicker(), stockHolding.getExchangeCode());
        final List<String> actual = stock.getExternalIdentifiers().getCodes().stream().map(ExternalIdentifierTypeValue::getValue).collect(Collectors.toList());
        Assertions.assertNotNull(actual);
        ComparisonUtils.compareCollections(expected, actual);
    }

    @Test
    void getNotExistingHoldings_emptyResponse() {
        //SETUP
        final var sut = new MonthlyReturnsStockEndpoint();
        final var stockHolding = new StockHolding();
        stockHolding.setTicker("testTicker");
        stockHolding.setExchangeCode("testExchangeCode");
        final Stock stock = new Stock();
        final ExternalIdentifiers identifiers = new ExternalIdentifiers();
        identifiers.setCodes(List.of());
        stock.setExternalIdentifiers(identifiers);
        final var holdings = List.of(stockHolding);
        final var tickersFromResponse = List.of(stock);

        //ACT
        final var actual = sut.getNotExistingHoldings(holdings, tickersFromResponse);

        //VERIFY
        assertEquals(1, actual.size());
        assertEquals(stockHolding, actual.get(0));
    }
}