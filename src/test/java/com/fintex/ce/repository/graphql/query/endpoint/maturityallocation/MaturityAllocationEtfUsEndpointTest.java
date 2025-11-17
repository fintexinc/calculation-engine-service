package com.fintex.ce.repository.graphql.query.endpoint.maturityallocation;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.Maturities;
import com.fintex.smclient.graphql.MaturityDurationValue;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.TimeDuration;
import com.fintex.ce.dto.holding.EtfHolding;
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

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MaturityAllocationEtfUsEndpointTest {

    @Test
    void getGetCanadaEtfsByTickers_isPresent() {
        //SETUP
        final MaturityAllocationEtfUsEndpoint m = new MaturityAllocationEtfUsEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<Etf> expected = new ArrayList<>();

        when(q.getGetUsEtfsByTickers()).thenReturn(expected);

        //ACT
        final Function<Query, List<Etf>> actual = m.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final MaturityAllocationEtfUsEndpoint m = mock(MaturityAllocationEtfUsEndpoint.class);

        final EtfQuery fundSeriesQuery = mock(EtfQuery.class);
        when(fundSeriesQuery.maturities(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.ticker(any())).thenReturn(fundSeriesQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final EtfQuery actual = m.requestMapper(fundSeriesQuery);

        //VERIFY
        verify(actual).maturities(any());
        verify(actual).ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
    }


    @Test
    void responseMapper_verify() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final MaturityAllocationEtfUsEndpoint sut = mock(MaturityAllocationEtfUsEndpoint.class);

            final EtfHolding holding = mock(EtfHolding.class);

            final Etf entity = mock(Etf.class);
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
