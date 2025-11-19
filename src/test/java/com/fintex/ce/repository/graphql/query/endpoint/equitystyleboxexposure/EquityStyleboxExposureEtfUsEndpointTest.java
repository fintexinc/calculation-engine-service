package com.fintex.ce.repository.graphql.query.endpoint.equitystyleboxexposure;

import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.REquityStyleboxExposure;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
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

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EquityStyleboxExposureEtfUsEndpointTest {

    @Test
    void getGetCanadaEtfsByTickers_isPresent() {
        //SETUP
        final EquityStyleboxExposureEtfUsEndpoint m = new EquityStyleboxExposureEtfUsEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<Etf> expected = new ArrayList<>();

        when(q.getGetUsEtfsByTickers()).thenReturn(expected);

        //ACT
        final Function<Query, List<Etf>> actual = m.getGetSMEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final EquityStyleboxExposureEtfUsEndpoint m = mock(EquityStyleboxExposureEtfUsEndpoint.class);

        final EtfQuery fundSeriesQuery = mock(EtfQuery.class);
        when(fundSeriesQuery.styleBoxes(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.ticker(any())).thenReturn(fundSeriesQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final EtfQuery actual = m.requestMapper(fundSeriesQuery);

        //VERIFY
        verify(actual).styleBoxes(any());
        verify(actual).ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
    }

    @Test
    void responseMapper_verify() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final EquityStyleboxExposureEtfUsEndpoint sut = mock(EquityStyleboxExposureEtfUsEndpoint.class);

            final EtfHolding holding = mock(EtfHolding.class);

            final Etf entity = mock(Etf.class);
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
