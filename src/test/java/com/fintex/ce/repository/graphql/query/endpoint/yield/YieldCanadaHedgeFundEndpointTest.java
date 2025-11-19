package com.fintex.ce.repository.graphql.query.endpoint.yield;

import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.model.redis.RYield;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.smclient.graphql.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class YieldCanadaHedgeFundEndpointTest {

    @Test
    void getUsEtfsByTickers_isPresent() {
        //SETUP
        final YieldCanadaHedgeFundEndpoint m = new YieldCanadaHedgeFundEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<HedgeFund> expected = new ArrayList<>();

        when(q.getGetCanadaHedgeFundsByMorningstarIds()).thenReturn(expected);

        //ACT
        final Function<Query, List<HedgeFund>> actual = m.getGetSMEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final YieldCanadaHedgeFundEndpoint m = mock(YieldCanadaHedgeFundEndpoint.class);

        final HedgeFundQuery hedgeFundQuery = mock(HedgeFundQuery.class);
        when(hedgeFundQuery.dividendYield(any())).thenReturn(hedgeFundQuery);
        when(hedgeFundQuery.externalIdentifiers(any())).thenReturn(hedgeFundQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final HedgeFundQuery actual = m.requestMapper(hedgeFundQuery);

        //VERIFY
        verify(actual).dividendYield(any());
        verify(actual).externalIdentifiers(any());
    }

    @Test
    void responseMapper_verify() {
            //SETUP
            final YieldCanadaHedgeFundEndpoint sut = mock(YieldCanadaHedgeFundEndpoint.class);

            final CanadaHedgeFundHolding holding = mock(CanadaHedgeFundHolding.class);

            final HedgeFund entity = mock(HedgeFund.class);
            final BigDecimal yield = mock(BigDecimal.class);
            final FloatDatapoint dividendYield = mock(FloatDatapoint.class);
            when(entity.getDividendYield()).thenReturn(dividendYield);
            when(dividendYield.getValue()).thenReturn(yield);

            doCallRealMethod().when(sut).responseMapper(any(), any());

            //ACT
            final RYield result = sut.responseMapper(entity, holding);

            //VERIFY
            assertNotNull(result);
            assertEquals(yield, result.getDividendYield());
    }
    
}
