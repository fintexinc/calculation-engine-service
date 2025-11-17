package com.fintex.ce.repository.graphql.query.endpoint.core;

import com.fintex.smclient.graphql.DataProvider;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.Operations;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.exception.ReqValidationException;
import com.fintex.ce.exception.SystemException;
import com.fintex.ce.model.redis.core.RedisId;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.config.enumeration.DataProvider.EAGLE;
import static com.fintex.ce.config.enumeration.DataProvider.MORNINGSTAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class AbstractFDSEndpointTest {

    @Test
    void getProviders_checkResult() {
        //SETUP
        final AbstractFDSEndpoint a = mock(AbstractFDSEndpoint.class,
                withSettings().useConstructor(null, List.of(EAGLE), "TEST"));
        a.userEnteredProviders = List.of();

        when(a.filterDataProviders()).thenReturn(List.of());

        doCallRealMethod().when(a).loadProviders();
        //ACT
        final List<DataProvider> actual = a.loadProviders();

        //VERIFY
        verify(a).loadDefaultSupportedProviders();
        verify(a, times(0)).filterDataProviders();
        assertTrue(actual.isEmpty());
    }

    @Test
    void loadDefaultSupportedProviders_checkResult() {
        //SETUP
        final AbstractFDSEndpoint a = mock(AbstractFDSEndpoint.class,
                withSettings().useConstructor(null, List.of(EAGLE), "TEST"));

        doCallRealMethod().when(a).loadDefaultSupportedProviders();
        //ACT
        final List<DataProvider> actual = a.loadDefaultSupportedProviders();

        //VERIFY
        assertEquals(List.of(DataProvider.EAGLE), actual);
    }

    @Test
    void setUserEnteredProviders_checkResult() {
        //SETUP
        final AbstractFDSEndpoint a = mock(AbstractFDSEndpoint.class,
                withSettings().useConstructor(null, List.of(EAGLE), "TEST"));

        doCallRealMethod().when(a).setUserEnteredProviders(any());
        //ACT
        a.setUserEnteredProviders(null);

        //VERIFY
        assertTrue(a.userEnteredProviders.isEmpty());
    }

    @Test
    void setUserEnteredProviders_checkResult2() {
        //SETUP
        final AbstractFDSEndpoint a = mock(AbstractFDSEndpoint.class,
                withSettings().useConstructor(null, List.of(EAGLE), "TEST"));

        doCallRealMethod().when(a).setUserEnteredProviders(any());
        //ACT
        final List<com.fintex.ce.config.enumeration.DataProvider> morningstar = List.of(MORNINGSTAR);
        a.setUserEnteredProviders(morningstar);

        //VERIFY
        assertEquals(a.userEnteredProviders, morningstar);
    }

    @Test
    void loadDefaultSupportedProviders_checkResult2() {
        //SETUP
        final AbstractFDSEndpoint a = mock(AbstractFDSEndpoint.class,
                withSettings().useConstructor(null, List.of(), "TEST"));

        doCallRealMethod().when(a).loadDefaultSupportedProviders();
        //ACT
        assertThrows(SystemException.class, a::loadDefaultSupportedProviders);

        //VERIFY
    }

    @Test
    void getProviders_checkResult2() {
        //SETUP
        final AbstractFDSEndpoint a = mock(AbstractFDSEndpoint.class,
                withSettings().useConstructor(null, List.of(EAGLE), "TEST"));
        a.userEnteredProviders = List.of(EAGLE);

        when(a.filterDataProviders()).thenReturn(List.of());

        doCallRealMethod().when(a).loadProviders();
        //ACT
        assertThrows(ReqValidationException.class, a::loadProviders);

        //VERIFY
        verify(a).filterDataProviders();
    }

    @Test
    void loadCustomDataProviders_checkResult() {
        //SETUP
        final AbstractFDSEndpoint a = mock(AbstractFDSEndpoint.class,
                withSettings().useConstructor(null, List.of(EAGLE), "TEST"));
        a.userEnteredProviders = List.of(MORNINGSTAR);

        final RedisId res = mock(RedisId.class);
        when(a.responseMapper(any(), any())).thenReturn(res);

        doCallRealMethod().when(a).filterDataProviders();
        //ACT
        final List<DataProvider> actual = a.filterDataProviders();

        //VERIFY
        assertTrue(actual.isEmpty());
    }

    @Test
    void loadCustomDataProviders_checkResult2() {
        //SETUP
        final AbstractFDSEndpoint a = mock(AbstractFDSEndpoint.class,
                withSettings().useConstructor(null, List.of(EAGLE, MORNINGSTAR), "TEST"));
        a.userEnteredProviders = List.of(MORNINGSTAR);

        final RedisId res = mock(RedisId.class);
        when(a.responseMapper(any(), any())).thenReturn(res);

        doCallRealMethod().when(a).filterDataProviders();
        //ACT
        final List<DataProvider> actual = a.filterDataProviders();

        //VERIFY
        assertEquals(List.of(DataProvider.MORNINGSTAR), actual);
    }

    @Test
    void basicResponseMapper_checkResult() {
        //SETUP
        final AbstractFDSEndpoint a = mock(AbstractFDSEndpoint.class);

        final RedisId res = mock(RedisId.class);
        when(a.responseMapper(any(), any())).thenReturn(res);

        doCallRealMethod().when(a).basicResponseMapper(any(), any());
        //ACT
        final Holding h = mock(Holding.class);
        final Object e = mock(Object.class);
        final Object actual = a.basicResponseMapper(e, h);

        //VERIFY
        verify(a).responseMapper(e, h);
        assertSame(res, actual);
    }

    @Test
    void collectResultToMap_verifyPopulateIdentifiersIfEmpty() {
        //SETUP
        final AbstractFDSEndpoint a = mock(AbstractFDSEndpoint.class);

        final ArrayList holdings = new ArrayList();
        final Holding h = mock(Holding.class);
        holdings.add(h);

        final ArrayList responses = new ArrayList();
        final FundSeries f = mock(FundSeries.class);
        responses.add(f);

        doCallRealMethod().when(a).collectResultToMap(any(), any());

        //ACT
        a.collectResultToMap(holdings, responses);

        //VERIFY
        verify(a).populateIdentifiersIfEmpty(holdings, responses);
    }

    @Test
    void collectResultToMap_checkResult() {
        //SETUP
        final AbstractFDSEndpoint a = mock(AbstractFDSEndpoint.class);

        final ArrayList holdings = new ArrayList();
        final Holding h = mock(Holding.class);
        holdings.add(h);

        final ArrayList responses = new ArrayList();
        final FundSeries f = mock(FundSeries.class);
        responses.add(f);

        final Object value = mock(Object.class);

        when(a.findHoldingBasedOnRes(holdings, f)).thenReturn(h);
        when(a.basicResponseMapper(f, h)).thenReturn(value);

        when(a.populateIdentifiersIfEmpty(anyList(), anyList())).thenReturn(responses);
        doCallRealMethod().when(a).collectResultToMap(any(), any());
        //ACT
        final Map actual = a.collectResultToMap(holdings, responses);

        //VERIFY
        assertEquals(Map.of(h, value), actual);
    }

    @Test
    void populateIdentifiersIfEmpty_checkResult() {
        //SETUP
        final AbstractFDSEndpoint a = mock(AbstractFDSEndpoint.class);

        final ArrayList holdings = new ArrayList();
        final Holding h = mock(Holding.class);
        holdings.add(h);

        final ArrayList responses = new ArrayList();
        final FundSeries f = mock(FundSeries.class);
        responses.add(f);

        doCallRealMethod().when(a).populateIdentifiersIfEmpty(any(), any());

        //ACT
        final List result = a.populateIdentifiersIfEmpty(holdings, responses);

        //VERIFY
        assertEquals(responses, result);
    }

    @Test
    void makeQuery_verifyCollectIds() {
        try (var mockedOperations = Mockito.mockStatic(Operations.class)) {
            //SETUP
            final AbstractFDSEndpoint a = mock(AbstractFDSEndpoint.class);

            final List holdings = new ArrayList();

            doCallRealMethod().when(a).makeQuery(any());
            //ACT
            a.makeQuery(holdings);

            //VERIFY
            verify(a).collectIds(argThat(argument -> argument == holdings));
        }
    }

    @Test
    void makeQuery_verifyQueryDefinition() {
        try (var mockedOperations = Mockito.mockStatic(Operations.class)) {
            //SETUP
            final AbstractFDSEndpoint a = mock(AbstractFDSEndpoint.class);

            final List holdings = List.of(mock(Object.class));
            final List<RedisId> ids = List.of(mock(RedisId.class));
            final Object rMapper = mock(Object.class);

            when(a.collectIds(any())).thenReturn(ids);
            when(a.requestMapper(any())).thenReturn(rMapper);

            doCallRealMethod().when(a).makeQuery(any());
            //ACT
            a.makeQuery(holdings);

            //VERIFY
            verify(a).queryDefinition(eq(ids), argThat(argument -> argument.apply(null) == rMapper));
        }
    }

    @Test
    void makeQuery_verifyQuery() {
        try (var mockedOperations = Mockito.mockStatic(Operations.class)) {
            //SETUP
            final AbstractFDSEndpoint a = mock(AbstractFDSEndpoint.class);

            final List holdings = new ArrayList();
            final QueryQueryDefinition qq = mock(QueryQueryDefinition.class);

            when(a.queryDefinition(any(), any())).thenReturn(qq);

            doCallRealMethod().when(a).makeQuery(any());
            //ACT
            a.makeQuery(holdings);

            //VERIFY
            mockedOperations.verify(() -> Operations.query(qq));
        }
    }

    @Test
    void makeQuery_checkResult() {
        try (var mockedOperations = Mockito.mockStatic(Operations.class)) {
            //SETUP
            final AbstractFDSEndpoint a = mock(AbstractFDSEndpoint.class);

            final List holdings = new ArrayList();
            final QueryQuery qq = mock(QueryQuery.class);

            mockedOperations.when(() -> Operations.query(any())).thenReturn(qq);
            doCallRealMethod().when(a).makeQuery(any());
            //ACT
            final QueryQuery actual = a.makeQuery(holdings);

            //VERIFY
            assertSame(qq, actual);
        }
    }

    @Test
    void populateIdentifiersIfEmpty_verifyGetNotExistingHoldings() {
        //SETUP
        final var sut = mock(AbstractFDSEndpoint.class);
        final var responses = mock(List.class);
        final var holdings = mock(List.class);

        doCallRealMethod().when(sut).populateIdentifiersIfEmpty(anyList(), anyList());
        //ACT
        sut.populateIdentifiersIfEmpty(holdings, responses);

        //VERIFY
        verify(sut).getNotExistingHoldings(holdings, responses);
    }

    @Test
    void populateIdentifiersIfEmpty_verifyPopulateEmptyResponsesWithIdentifiers() {
        //SETUP
        final var sut = mock(AbstractFDSEndpoint.class);
        final var responses = mock(List.class);
        final var holdings = mock(List.class);
        final var holdingsThatDontHaveResponsesFromFds = mock(List.class);

        doReturn(holdingsThatDontHaveResponsesFromFds).when(sut).getNotExistingHoldings(holdings, responses);

        doCallRealMethod().when(sut).populateIdentifiersIfEmpty(anyList(), anyList());
        //ACT
        sut.populateIdentifiersIfEmpty(holdings, responses);

        //VERIFY
        verify(sut).populateEmptyResponsesWithIdentifiers(holdingsThatDontHaveResponsesFromFds, responses);
    }

    @Test
    void populateIdentifiersIfEmpty_chechResult() {
        //SETUP
        final var sut = mock(AbstractFDSEndpoint.class);
        final var responses = mock(List.class);
        final var holdings = mock(List.class);
        final var holdingsThatDontHaveResponsesFromFds = mock(List.class);

        doReturn(holdingsThatDontHaveResponsesFromFds).when(sut).getNotExistingHoldings(holdings, responses);

        doCallRealMethod().when(sut).populateIdentifiersIfEmpty(anyList(), anyList());
        //ACT
        final List actual = sut.populateIdentifiersIfEmpty(holdings, responses);

        //VERIFY
        assertEquals(responses, actual);
    }

    @Test
    void populateEmptyResponsesWithIdentifiers_verifyForEachHoldingPopulateEmptyResponseWithIdentifier() {
        //SETUP
        final var sut = mock(AbstractFDSEndpoint.class);
        final var responses = mock(List.class);
        final Holding h1 = mock(Holding.class);
        final Holding h2 = mock(Holding.class);
        final var holdings = List.of(h1, h2);

        doCallRealMethod().when(sut).populateEmptyResponsesWithIdentifiers(anyList(), anyList());
        //ACT
        sut.populateEmptyResponsesWithIdentifiers(holdings, responses);

        //VERIFY
        holdings.forEach(h -> {
            verify(sut).populateEmptyResponseWithIdentifier(responses, h);
        });
    }

    @Test
    void populateEmptyResponsesWithIdentifiers_verifyWhenHoldingsThatDontHaveResponsesFromFdsIsEmpty() {
        //SETUP
        final var sut = mock(AbstractFDSEndpoint.class);
        final var responses = mock(List.class);
        final var holdings = List.of();

        doCallRealMethod().when(sut).populateEmptyResponsesWithIdentifiers(anyList(), anyList());
        //ACT
        sut.populateEmptyResponsesWithIdentifiers(holdings, responses);

        //VERIFY
        verify(sut, never()).populateEmptyResponseWithIdentifier(eq(responses), argThat(arg -> arg instanceof Holding));
    }

}