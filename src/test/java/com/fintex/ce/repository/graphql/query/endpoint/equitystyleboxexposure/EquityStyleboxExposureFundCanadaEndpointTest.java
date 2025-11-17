package com.fintex.ce.repository.graphql.query.endpoint.equitystyleboxexposure;

import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.StyleBoxType;
import com.fintex.smclient.graphql.StyleBoxValue;
import com.fintex.smclient.graphql.StyleBoxes;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.REquityStyleboxExposure;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EquityStyleboxExposureFundCanadaEndpointTest {

    @Test
    void getGetCanadaEtfsByTickers_isPresent() {
        //SETUP
        final EquityStyleboxExposureFundCanadaEndpoint m = new EquityStyleboxExposureFundCanadaEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<FundSeries> expected = new ArrayList<>();

        when(q.getGetFundSeriesByHoldingCodes()).thenReturn(expected);

        //ACT
        final Function<Query, List<FundSeries>> actual = m.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final EquityStyleboxExposureFundCanadaEndpoint m = mock(EquityStyleboxExposureFundCanadaEndpoint.class);

        final FundSeriesQuery fundSeriesQuery = mock(FundSeriesQuery.class);
        when(fundSeriesQuery.styleBoxes(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final FundSeriesQuery actual = m.requestMapper(fundSeriesQuery);

        //VERIFY
        verify(actual).styleBoxes(any());
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Test
    void responseMapper_verify() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final EquityStyleboxExposureFundCanadaEndpoint sut = mock(EquityStyleboxExposureFundCanadaEndpoint.class);

            final FundSeriesHolding holding = mock(FundSeriesHolding.class);

            final FundSeries entity = mock(FundSeries.class);
            final BigDecimal value = mock(BigDecimal.class);
            final StyleBoxes styleBoxes = mock(StyleBoxes.class);
            final StyleBoxValue styleBoxValue = mock(StyleBoxValue.class);

            when(entity.getStyleBoxes()).thenReturn(styleBoxes);
            when(styleBoxes.getBoxValues()).thenReturn(List.of(styleBoxValue));
            when(styleBoxValue.getStyleBoxType()).thenReturn(StyleBoxType.LARGE_CORE);
            when(styleBoxValue.getValue()).thenReturn(value);

            doCallRealMethod().when(sut).responseMapper(any(), any());

            //ACT
            final REquityStyleboxExposure result = sut.responseMapper(entity, holding);

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
