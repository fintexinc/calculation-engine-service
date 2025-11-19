package com.fintex.ce.repository.graphql.query.endpoint.fixedincomestyleboxexposure;

import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RFixedIncomeStyleboxExposure;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.FixedIncomeStyleBoxType;
import com.fintex.smclient.graphql.FixedIncomeStyleBoxValue;
import com.fintex.smclient.graphql.FixedIncomeStyleBoxes;
import com.fintex.smclient.graphql.Query;
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

class FixedIncomeStyleboxExposureCanadaUsMutualFundEndpointTest {

    @Test
    void getGetBy_isPresent() {
        //SETUP
        final FixedIncomeStyleboxExposureCanadaUsMutualFundEndpoint m = new FixedIncomeStyleboxExposureCanadaUsMutualFundEndpoint();

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
        final FixedIncomeStyleboxExposureCanadaUsMutualFundEndpoint m = mock(FixedIncomeStyleboxExposureCanadaUsMutualFundEndpoint.class);

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
            final FixedIncomeStyleboxExposureCanadaUsMutualFundEndpoint sut = mock(FixedIncomeStyleboxExposureCanadaUsMutualFundEndpoint.class);

            final UsMutualFundHolding holding = mock(UsMutualFundHolding.class);

            final UsFund entity = mock(UsFund.class);
            final BigDecimal value = mock(BigDecimal.class);
            final FixedIncomeStyleBoxValue styleBoxValue = mock(FixedIncomeStyleBoxValue.class);
            final FixedIncomeStyleBoxes fixedIncomeStyleBoxes = mock(FixedIncomeStyleBoxes.class);

            when(entity.getFixedIncomeStyleBoxes()).thenReturn(fixedIncomeStyleBoxes);
            when(fixedIncomeStyleBoxes.getBoxValues()).thenReturn(List.of(styleBoxValue));
            when(styleBoxValue.getStyleBoxType()).thenReturn(FixedIncomeStyleBoxType.HIGH_LIMITED);
            when(styleBoxValue.getValue()).thenReturn(value);

            doCallRealMethod().when(sut).responseMapper(any(), any());

            //ACT
            final RFixedIncomeStyleboxExposure result = sut.responseMapper(entity, holding);

            //VERIFY
            assertNotNull(result);
            assertNotNull(result.getBoxValues());
            assertEquals(1, result.getBoxValues().size());

            final Map.Entry<String, BigDecimal> entry = result.getBoxValues().entrySet().stream().findFirst().orElseThrow();
            assertEquals(FixedIncomeStyleBoxType.HIGH_LIMITED.toString(), entry.getKey());
            assertEquals(value, entry.getValue());
        }
    }
    
}
