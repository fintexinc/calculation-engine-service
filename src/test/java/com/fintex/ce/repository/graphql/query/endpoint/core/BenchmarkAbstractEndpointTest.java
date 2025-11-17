package com.fintex.ce.repository.graphql.query.endpoint.core;

import com.fintex.smclient.graphql.ExternalIdentifierTypeValue;
import com.fintex.smclient.graphql.ExternalIdentifiers;
import com.fintex.smclient.graphql.Index;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns.MonthlyReturnsBenchmarkEndpoint;
import com.fintex.ce.util.ComparisonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BenchmarkAbstractEndpointTest {

    @Test
    void collectIds_checkResult() {
        //SETUP
        final BenchmarkAbstractEndpoint s = mock(BenchmarkAbstractEndpoint.class);

        final BenchmarkIndexHolding h = mock(BenchmarkIndexHolding.class);
        final String code = "RBF540";
        when(h.getMrStarId()).thenReturn(code);

        doCallRealMethod().when(s).collectIds(any());
        //ACT
        final List<String> actual = s.collectIds(List.of(h));

        //VERIFY
        assertEquals(1, actual.size());
        assertEquals(actual.get(0), code);
    }

    @Test
    void findHoldingBasedOnRes_checkResults() {
        //SETUP
        final BenchmarkAbstractEndpoint s = mock(BenchmarkAbstractEndpoint.class);

        final BenchmarkIndexHolding h = mock(BenchmarkIndexHolding.class);
        final String code = "CODE";
        when(h.getMrStarId()).thenReturn(code);

        when(s.getIds(any())).thenReturn(List.of(code));

        doCallRealMethod().when(s).findHoldingBasedOnRes(any(), any());
        //ACT
        final BenchmarkIndexHolding actual = s.findHoldingBasedOnRes(List.of(h), null);

        //VERIFY
        assertEquals(h, actual);
    }

    @Test
    void getIds_checkResults() {
        //SETUP
        final BenchmarkAbstractEndpoint s = mock(BenchmarkAbstractEndpoint.class);
        final String code = "VAB";

        final Index fundSeries = mock(Index.class);
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
        final BenchmarkAbstractEndpoint e = mock(BenchmarkAbstractEndpoint.class);

        when(e.responseMapper(any(), any())).thenReturn(mock(RedisId.class));

        final BenchmarkIndexHolding h = mock(BenchmarkIndexHolding.class);
        final Index entity = mock(Index.class);
        doCallRealMethod().when(e).basicResponseMapper(any(), any());
        //ACT
        e.basicResponseMapper(entity, h);

        //VERIFY
        verify(e).responseMapper(entity, h);
    }

    @Test
    void basicResponseMapper_checkResult() {
        //SETUP
        final BenchmarkAbstractEndpoint e = mock(BenchmarkAbstractEndpoint.class);

        final RedisId expected = mock(RedisId.class);
        when(e.responseMapper(any(), any())).thenReturn(expected);

        final BenchmarkIndexHolding h = mock(BenchmarkIndexHolding.class);
        when(h.generateUserIdentifier()).thenReturn("SDF");

        final Index entity = mock(Index.class);

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
        final var sut = new MonthlyReturnsBenchmarkEndpoint();
        final var benchmarkIndexHolding = new BenchmarkIndexHolding();
        benchmarkIndexHolding.setMrStarId("mrStarId");
        final Index index = new Index();
        final ExternalIdentifiers identifiers = new ExternalIdentifiers();
        identifiers.setCodes(List.of());
        index.setExternalIdentifiers(identifiers);

        final Index index1 = new Index();
        final var mrStarId = new ExternalIdentifierTypeValue();
        mrStarId.setValue("mrStarId1");
        final ExternalIdentifiers externalIdentifiers = new ExternalIdentifiers().setCodes(List.of(mrStarId));
        index1.setExternalIdentifiers(externalIdentifiers);

        //ACT
        sut.populateEmptyResponseWithIdentifier(List.of(index, index1), benchmarkIndexHolding);

        //VERIFY
        final List<String> expected = List.of(benchmarkIndexHolding.getMrStarId());
        final List<String> actual = index.getExternalIdentifiers().getCodes().stream().map(ExternalIdentifierTypeValue::getValue).collect(Collectors.toList());
        Assertions.assertNotNull(actual);
        ComparisonUtils.compareCollections(expected, actual);
    }

    @Test
    void getNotExistingHoldings_emptyResponse() {
        //SETUP
        final var sut = new MonthlyReturnsBenchmarkEndpoint();
        final var benchmarkIndexHolding = new BenchmarkIndexHolding();
        benchmarkIndexHolding.setMrStarId("mrStarId");


        final Index index = new Index();
        final ExternalIdentifiers identifiers = new ExternalIdentifiers();
        identifiers.setCodes(List.of());
        index.setExternalIdentifiers(identifiers);
        final var holdings = List.of(benchmarkIndexHolding);
        final var responses = List.of(index);

        //ACT
        final var actual = sut.getNotExistingHoldings(holdings, responses);

        //VERIFY
        assertEquals(1, actual.size());
        assertEquals(benchmarkIndexHolding, actual.get(0));
    }

}