package com.fintex.ce.repository.graphql.query.endpoint.equitystyleboxexposure;

import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.REquityStyleboxExposure;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.StyleBoxType;
import com.fintex.smclient.graphql.StyleBoxValue;
import com.fintex.smclient.graphql.StyleBoxes;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
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

class EquityStyleboxExposureCanadaUsMutualFundEndpointTest {

    @Test
    void getGetBy_isPresent() {
        //SETUP
        final EquityStyleboxExposureCanadaUsMutualFundEndpoint m = new EquityStyleboxExposureCanadaUsMutualFundEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<UsFund> expected = new ArrayList<>();

        when(q.getGetUsFundsByTickers()).thenReturn(expected);

        //ACT
        final Function<Query, List<UsFund>> actual = m.getGetSMEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final EquityStyleboxExposureCanadaUsMutualFundEndpoint m = mock(EquityStyleboxExposureCanadaUsMutualFundEndpoint.class);

        final UsFundQuery usFundQuery = mock(UsFundQuery.class);
        when(usFundQuery.styleBoxes(any())).thenReturn(usFundQuery);
        when(usFundQuery.externalIdentifiers(any())).thenReturn(usFundQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final UsFundQuery actual = m.requestMapper(usFundQuery);

        //VERIFY
        verify(actual).styleBoxes(any());
        verify(actual).externalIdentifiers(any());
    }

    @Test
    void responseMapper_verify() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final EquityStyleboxExposureCanadaUsMutualFundEndpoint sut = mock(EquityStyleboxExposureCanadaUsMutualFundEndpoint.class);

            final UsMutualFundHolding holding = mock(UsMutualFundHolding.class);

            final UsFund entity = mock(UsFund.class);
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
