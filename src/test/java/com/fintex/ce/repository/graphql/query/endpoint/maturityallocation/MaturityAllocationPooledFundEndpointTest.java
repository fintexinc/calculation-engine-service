package com.fintex.ce.repository.graphql.query.endpoint.maturityallocation;

import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.model.redis.RMaturityAllocation;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.Maturities;
import com.fintex.smclient.graphql.MaturityDurationValue;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.TimeDuration;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MaturityAllocationPooledFundEndpointTest {

    @Test
    void getGetBy_isPresent() {
        //SETUP
        final MaturityAllocationPooledFundEndpoint m = new MaturityAllocationPooledFundEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<PooledFund> expected = new ArrayList<>();

        when(q.getGetCanadaPooledFundsByMorningstarIds()).thenReturn(expected);

        //ACT
        final Function<Query, List<PooledFund>> actual = m.getGetSMEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final MaturityAllocationPooledFundEndpoint m = mock(MaturityAllocationPooledFundEndpoint.class);

        final PooledFundQuery pooledFundQuery = mock(PooledFundQuery.class);
        when(pooledFundQuery.maturities(any())).thenReturn(pooledFundQuery);
        when(pooledFundQuery.externalIdentifiers(any())).thenReturn(pooledFundQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final PooledFundQuery actual = m.requestMapper(pooledFundQuery);

        //VERIFY
        verify(actual).maturities(any());
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Test
    void responseMapper_verify() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final MaturityAllocationPooledFundEndpoint sut = mock(MaturityAllocationPooledFundEndpoint.class);

            final CanadaPooledFundHolding holding = mock(CanadaPooledFundHolding.class);

            final PooledFund entity = mock(PooledFund.class);
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
