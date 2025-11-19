package com.fintex.ce.repository.graphql.query.endpoint.fixedincomestyleboxexposure;

import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.model.redis.RFixedIncomeStyleboxExposure;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.StyleBoxType;
import com.fintex.smclient.graphql.StyleBoxValue;
import com.fintex.smclient.graphql.StyleBoxes;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FixedIncomeStyleboxExposureBenchmarkEndpointTest {

    @Test
    void getUsEtfsByTickers_isPresent() {
        //SETUP
        final FixedIncomeStyleboxExposureBenchmarkEndpoint m = new FixedIncomeStyleboxExposureBenchmarkEndpoint();

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
        final FixedIncomeStyleboxExposureBenchmarkEndpoint m = mock(FixedIncomeStyleboxExposureBenchmarkEndpoint.class);

        final IndexQuery indexQuery = mock(IndexQuery.class);
        when(indexQuery.styleBoxes(any())).thenReturn(indexQuery);
        when(indexQuery.externalIdentifiers(any())).thenReturn(indexQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final IndexQuery actual = m.requestMapper(indexQuery);

        //VERIFY
        verify(actual).styleBoxes(any());
        verify(actual).externalIdentifiers(any());
    }

    @Test
    void responseMapper_verify() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final FixedIncomeStyleboxExposureBenchmarkEndpoint sut = mock(FixedIncomeStyleboxExposureBenchmarkEndpoint.class);

            final BenchmarkIndexHolding holding = mock(BenchmarkIndexHolding.class);

            final Index entity = mock(Index.class);
            final BigDecimal value = mock(BigDecimal.class);
            final StyleBoxes styleBoxes = mock(StyleBoxes.class);
            final StyleBoxValue styleBoxValue = mock(StyleBoxValue.class);

            when(entity.getStyleBoxes()).thenReturn(styleBoxes);
            when(styleBoxes.getBoxValues()).thenReturn(List.of(styleBoxValue));
            when(styleBoxValue.getStyleBoxType()).thenReturn(StyleBoxType.LARGE_CORE);
            when(styleBoxValue.getValue()).thenReturn(value);

            doCallRealMethod().when(sut).responseMapper(any(), any());

            //ACT
            final RFixedIncomeStyleboxExposure result = sut.responseMapper(entity, holding);

            //VERIFY
            assertNotNull(result);
            assertNotNull(result.getBoxValues());
            assertEquals(1, result.getBoxValues().size());

            final Map.Entry<String, BigDecimal> entry = result.getBoxValues().entrySet().stream().findFirst().orElseThrow();
            assertEquals(StyleBoxType.LARGE_CORE.toString(), entry.getKey());
            assertEquals(value, entry.getValue());
        }
    }
    
}
