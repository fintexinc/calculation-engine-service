package com.fintex.ce.repository.graphql.query.endpoint.commonholdings;

import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.model.redis.topcommonholdings.RCommonHoldings;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.Holdings;
import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;
import com.fintex.smclient.graphql.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommonHoldingsBenchmarkEndpointTest {

    @Test
    void getIndexesByMorningstarIds_isPresent() {
        //SETUP
        final var fundCanadaFDSEndpoint = new CommonHoldingsBenchmarkEndpoint();
        final var query = mock(Query.class);
        final var expected = new ArrayList<Index>();

        when(query.getGetIndexesByMorningstarIds()).thenReturn(expected);

        //ACT
        final Function<Query, List<Index>> actual = fundCanadaFDSEndpoint.getGetSMEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(query), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final var fundCanadaFDSEndpoint = mock(CommonHoldingsBenchmarkEndpoint.class);
        final var fundSeriesQuery = mock(IndexQuery.class);

        when(fundSeriesQuery.holdings(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);

        doCallRealMethod().when(fundCanadaFDSEndpoint).requestMapper(any());
        //ACT
        final IndexQuery actual = fundCanadaFDSEndpoint.requestMapper(fundSeriesQuery);

        //VERIFY
        verify(actual).holdings(any());
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Test
    void responseMapper_verifyTopCommonHoldingsMapper() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final var sut = mock(CommonHoldingsBenchmarkEndpoint.class);
            final var benchmarkIndexHolding = mock(BenchmarkIndexHolding.class);
            final var index = mock(Index.class);
            final var allocation = mock(Holdings.class);

            when(index.getHoldings()).thenReturn(allocation);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            sut.responseMapper(index, benchmarkIndexHolding);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.topCommonHoldingsMapper(allocation));
        }
    }

    @Test
    void responseMapper_checkResult() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final var sut = mock(CommonHoldingsBenchmarkEndpoint.class);
            final var index = mock(Index.class);
            final var holding = mock(BenchmarkIndexHolding.class);
            final var allocation = mock(Holdings.class);
            final RCommonHoldings actual = mock(RCommonHoldings.class);

            when(index.getHoldings()).thenReturn(allocation);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.topCommonHoldingsMapper(any())).thenReturn(actual);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            final RCommonHoldings expected = sut.responseMapper(index, holding);

            //VERIFY
            assertSame(expected, actual);
        }
    }

}
