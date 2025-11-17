package com.fintex.ce.repository.graphql.query.endpoint.core;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.ExternalIdentifierTypeValue;
import com.fintex.smclient.graphql.ExternalIdentifiers;
import com.fintex.smclient.graphql.StringDatapoint;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns.MonthlyReturnsEtfUsEndpoint;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EtfAbstractEndpointTest {

    @Test
    void collectIds_checkResult() {
        //SETUP
        final EtfAbstractEndpoint e = mock(EtfAbstractEndpoint.class);

        final EtfHolding etf = mock(EtfHolding.class);
        when(etf.getTicker()).thenReturn("TICKER");

        doCallRealMethod().when(e).collectIds(any());
        //ACT
        final List actual = e.collectIds(List.of(etf));

        //VERIFY
        assertEquals(List.of(etf.getTicker()), actual);
    }

    @Test
    void findHoldingBasedOnRes_checkResult() {
        //SETUP
        final EtfAbstractEndpoint e = mock(EtfAbstractEndpoint.class);

        final EtfHolding etf = mock(EtfHolding.class);
        final String ticker = "TICKER";
        when(etf.getTicker()).thenReturn(ticker);

        final Etf eFds = mock(Etf.class);
        final StringDatapoint stringDatapoint = mock(StringDatapoint.class);
        when(stringDatapoint.getValue()).thenReturn(ticker);
        when(eFds.getTicker()).thenReturn(stringDatapoint);

        doCallRealMethod().when(e).findHoldingBasedOnRes(any(), any());
        //ACT
        final EtfHolding actual = e.findHoldingBasedOnRes(List.of(etf), eFds);

        //VERIFY
        assertEquals(etf, actual);
    }

    @Test
    void basicResponseMapper_verifyRsponseMapper() {
        //SETUP
        final EtfAbstractEndpoint e = mock(EtfAbstractEndpoint.class);

        when(e.responseMapper(any(), any())).thenReturn(mock(RedisId.class));

        final EtfHolding entity = mock(EtfHolding.class);
        final Etf h = mock(Etf.class);
        doCallRealMethod().when(e).basicResponseMapper(any(), any());
        //ACT
        e.basicResponseMapper(h, entity);

        //VERIFY
        verify(e).responseMapper(h, entity);
    }

    @Test
    void basicResponseMapper_checkResult() {
        //SETUP
        final EtfAbstractEndpoint e = mock(EtfAbstractEndpoint.class);

        final RedisId expected = mock(RedisId.class);
        when(e.responseMapper(any(), any())).thenReturn(expected);

        final EtfHolding h = mock(EtfHolding.class);
        when(h.generateUserIdentifier()).thenReturn("SDF");

        final Etf entity = mock(Etf.class);

        doCallRealMethod().when(e).basicResponseMapper(any(), any());
        //ACT
        final RedisId actual = e.basicResponseMapper(entity, h);

        //VERIFY
        verify(expected).setHoldingId(h.generateUserIdentifier());
        assertSame(expected, actual);
    }

    @Test
    void populateIdentifiersIfEmpty_verifyGetTickersFromResponse() {
        //SETUP
        final var e = mock(EtfAbstractEndpoint.class);
        final var holdings = mock(List.class);
        final var responses = mock(List.class);

        doCallRealMethod().when(e).getNotExistingHoldings(anyList(), anyList());
        doCallRealMethod().when(e).populateIdentifiersIfEmpty(anyList(), anyList());
        //ACT
        e.populateIdentifiersIfEmpty(holdings, responses);

        //VERIFY
        verify(e).getTickersFromResponse(responses);
    }

    @Test
    void populateIdentifiersIfEmpty_checkResult() {
        //SETUP
        final var e = mock(EtfAbstractEndpoint.class);
        final var holdings = mock(List.class);
        final var responses = mock(List.class);

        when(e.populateIdentifiersIfEmpty(anyList(), anyList())).thenReturn(responses);
        doCallRealMethod().when(e).populateIdentifiersIfEmpty(anyList(), anyList());
        //ACT
        final List result = e.populateIdentifiersIfEmpty(holdings, responses);

        //VERIFY
        assertEquals(responses, result);
    }

    @Test
    void populateEmptyTicker_checkResult() {
        //SETUP
        final var e = mock(EtfAbstractEndpoint.class);
        final EtfHolding etfHolding = new EtfHolding();
        final var ticker = "Ticker";
        etfHolding.setTicker(ticker);
        final var etf = new Etf();
        final var responses = List.of(etf);

        doCallRealMethod().when(e).populateEmptyResponseWithIdentifier(anyList(), any());
        //ACT
        e.populateEmptyResponseWithIdentifier(responses, etfHolding);

        //VERIFY
        assertEquals(ticker, etf.getTicker().getValue());
    }

    @Test
    void getNotExistingTickers_checkResult() {
        //SETUP
        final var e = mock(EtfAbstractEndpoint.class);
        final EtfHolding etfHolding = new EtfHolding();
        final var holdings = List.of(etfHolding);
        final var tickersFromResponse = mock(List.class);

        doCallRealMethod().when(e).getNotExistingHoldings(anyList(), anyList());
        //ACT
        final var result = e.getNotExistingHoldings(holdings, tickersFromResponse);

        //VERIFY
        assertEquals(1, result.size());
        assertEquals(etfHolding, result.get(0));
    }

    @Test
    void getTickersFromResponse_checkResult() {
        //SETUP
        final var e = mock(EtfAbstractEndpoint.class);
        final var ticker = "Ticker";
        final var etf = new Etf();
        etf.setTicker(new StringDatapoint().setValue(ticker));
        final var responses = List.of(etf);

        doCallRealMethod().when(e).getTickersFromResponse(anyList());
        //ACT
        final var result = e.getTickersFromResponse(responses);

        //VERIFY
        assertEquals(1, result.size());
        assertEquals(ticker, result.get(0));
    }

    @Test
    void populateEmptyResponseWithIdentifier_checkResult() {
        //SETUP
        final var sut = new MonthlyReturnsEtfUsEndpoint();
        final var etfHolding = new EtfHolding();
        etfHolding.setTicker("testTicker");

        final Etf etf = new Etf();
        final ExternalIdentifiers identifiers = new ExternalIdentifiers();
        identifiers.setCodes(List.of());
        etf.setExternalIdentifiers(identifiers);

        final Etf etf1 = new Etf();
        final var ticker = new ExternalIdentifierTypeValue();
        ticker.setValue("ticker");
        final ExternalIdentifiers externalIdentifiers = new ExternalIdentifiers().setCodes(List.of(ticker));
        etf1.setExternalIdentifiers(externalIdentifiers);

        //ACT
        sut.populateEmptyResponseWithIdentifier(List.of(etf, etf1), etfHolding);

        //VERIFY
        final String actual = etf.getTicker().getValue();
        assertEquals("testTicker", actual);
    }

    @Test
    void getNotExistingHoldings_emptyResponse() {
        //SETUP
        final var sut = new MonthlyReturnsEtfUsEndpoint();
        final var etfHolding = new EtfHolding();
        etfHolding.setTicker("testTicker");
        final var stock = new Etf();
        final ExternalIdentifiers identifiers = new ExternalIdentifiers();
        identifiers.setCodes(List.of());
        stock.setExternalIdentifiers(identifiers);
        final var holdings = List.of(etfHolding);
        final var tickersFromResponse = List.of(stock);

        //ACT
        final var actual = sut.getNotExistingHoldings(holdings, tickersFromResponse);

        //VERIFY
        assertEquals(1, actual.size());
        assertEquals(etfHolding, actual.get(0));
    }

}