package com.fintex.ce.repository.graphql.query.endpoint.maturityallocation;

import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.Maturities;
import com.fintex.smclient.graphql.MaturityDurationValue;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.TimeDuration;
import com.fintex.ce.dto.holding.FundSeriesHolding;
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

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MaturityAllocationFundCanadaEndpointTest {

    @Test
    void getGetCanadaEtfsByTickers_isPresent() {
        //SETUP
        final MaturityAllocationFundCanadaEndpoint m = new MaturityAllocationFundCanadaEndpoint();

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
        final MaturityAllocationFundCanadaEndpoint m = mock(MaturityAllocationFundCanadaEndpoint.class);

        final FundSeriesQuery fundSeriesQuery = mock(FundSeriesQuery.class);
        when(fundSeriesQuery.maturities(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final FundSeriesQuery actual = m.requestMapper(fundSeriesQuery);

        //VERIFY
        verify(actual).maturities(any());
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }


    @Test
    void responseMapper_verify() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final MaturityAllocationFundCanadaEndpoint sut = mock(MaturityAllocationFundCanadaEndpoint.class);

            final FundSeriesHolding holding = mock(FundSeriesHolding.class);

            final FundSeries entity = mock(FundSeries.class);
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
