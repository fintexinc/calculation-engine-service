package com.fintex.ce.repository.graphql.query.endpoint.maturityallocation;

import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.smclient.graphql.Maturities;
import com.fintex.smclient.graphql.MaturityDurationValue;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.TimeDuration;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.model.redis.RMaturityAllocation;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MaturityAllocationCanadaHedgeFundEndpointTest {

    @Test
    void getUsEtfsByTickers_isPresent() {
        //SETUP
        final MaturityAllocationCanadaHedgeFundEndpoint m = new MaturityAllocationCanadaHedgeFundEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<HedgeFund> expected = new ArrayList<>();

        when(q.getGetCanadaHedgeFundsByMorningstarIds()).thenReturn(expected);

        //ACT
        final Function<Query, List<HedgeFund>> actual = m.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final MaturityAllocationCanadaHedgeFundEndpoint m = mock(MaturityAllocationCanadaHedgeFundEndpoint.class);

        final HedgeFundQuery hedgeFundQuery = mock(HedgeFundQuery.class);
        when(hedgeFundQuery.maturities(any())).thenReturn(hedgeFundQuery);
        when(hedgeFundQuery.externalIdentifiers(any())).thenReturn(hedgeFundQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final HedgeFundQuery actual = m.requestMapper(hedgeFundQuery);

        //VERIFY
        verify(actual).maturities(any());
        verify(actual).externalIdentifiers(any());
    }

    @Test
    void responseMapper_verify() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final MaturityAllocationCanadaHedgeFundEndpoint sut = mock(MaturityAllocationCanadaHedgeFundEndpoint.class);

            final CanadaHedgeFundHolding holding = mock(CanadaHedgeFundHolding.class);

            final HedgeFund entity = mock(HedgeFund.class);
            final BigDecimal value = mock(BigDecimal.class);
            final Maturities maturities = mock(Maturities.class);
            final MaturityDurationValue maturityDurationValue = mock(MaturityDurationValue.class);
            when(entity.getMaturities()).thenReturn(maturities);
            when(maturities.getPeriods()).thenReturn(List.of(maturityDurationValue));
            when(maturityDurationValue.getValue()).thenReturn(value);
            when(maturityDurationValue.getMaturityDuration()).thenReturn(TimeDuration.FIVE_TO_SEVEN_YEARS);

            doCallRealMethod().when(sut).responseMapper(any(), any());

            //ACT
            final RMaturityAllocation result = sut.responseMapper(entity, holding);

            //VERIFY
            assertNotNull(result);
            assertNotNull(result.getMaturityDurationValues());
            assertEquals(1, result.getMaturityDurationValues().size());
            final Map.Entry<String, BigDecimal> entry = result.getMaturityDurationValues().entrySet().stream().findFirst().orElseThrow();
            assertEquals(TimeDuration.FIVE_TO_SEVEN_YEARS.toString(), entry.getKey());
            assertEquals(value, entry.getValue());
        }
    }

}
