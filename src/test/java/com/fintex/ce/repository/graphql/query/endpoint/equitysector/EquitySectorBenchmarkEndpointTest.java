package com.fintex.ce.repository.graphql.query.endpoint.equitysector;

import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.model.redis.equitysector.REquitySector;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.EquitySectorAllocation;
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

class EquitySectorBenchmarkEndpointTest {

    @Test
    void getIndexesByMorningstarIds_isPresent() {
        //SETUP
        final EquitySectorBenchmarkEndpoint m = new EquitySectorBenchmarkEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<Index> expected = new ArrayList<>();

        when(q.getGetIndexesByMorningstarIds()).thenReturn(expected);

        //ACT
        final Function<Query, List<Index>> actual = m.getGetSMEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final EquitySectorBenchmarkEndpoint m = mock(EquitySectorBenchmarkEndpoint.class);

        final IndexQuery indexQuery = mock(IndexQuery.class);
        when(indexQuery.equitySectorAllocation(any())).thenReturn(indexQuery);
        when(indexQuery.externalIdentifiers(any())).thenReturn(indexQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final IndexQuery actual = m.requestMapper(indexQuery);

        //VERIFY
        verify(actual).equitySectorAllocation(any());
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Test
    void responseMapper_verifyEquitySectorMapper() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final var sut = mock(EquitySectorBenchmarkEndpoint.class);

            final Index index = mock(Index.class);
            final EquitySectorAllocation allocation = mock(EquitySectorAllocation.class);
            when(index.getEquitySectorAllocation()).thenReturn(allocation);
            final BenchmarkIndexHolding h = mock(BenchmarkIndexHolding.class);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            sut.responseMapper(index, h);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.equitySectorMapper(allocation));
        }
    }

    @Test
    void responseMapper_checkResult() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final EquitySectorBenchmarkEndpoint sut = mock(EquitySectorBenchmarkEndpoint.class);

            final Index index = mock(Index.class);
            final EquitySectorAllocation allocation = mock(EquitySectorAllocation.class);
            when(index.getEquitySectorAllocation()).thenReturn(allocation);
            final BenchmarkIndexHolding h = mock(BenchmarkIndexHolding.class);

            final REquitySector actual = mock(REquitySector.class);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.equitySectorMapper(any())).thenReturn(actual);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            final REquitySector expected = sut.responseMapper(index, h);

            //VERIFY
            assertSame(expected, actual);
        }
    }

}
