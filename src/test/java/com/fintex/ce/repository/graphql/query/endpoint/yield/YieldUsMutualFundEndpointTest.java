package com.fintex.ce.repository.graphql.query.endpoint.yield;

import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RYield;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class YieldUsMutualFundEndpointTest {

    @Test
    void getGetBy_isPresent() {
        //SETUP
        final YieldUsMutualFundEndpoint m = new YieldUsMutualFundEndpoint();

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
        final YieldUsMutualFundEndpoint m = mock(YieldUsMutualFundEndpoint.class);

        final UsFundQuery usFundQuery = mock(UsFundQuery.class);
        when(usFundQuery.dividendYield(any())).thenReturn(usFundQuery);
        when(usFundQuery.externalIdentifiers(any())).thenReturn(usFundQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final UsFundQuery actual = m.requestMapper(usFundQuery);

        //VERIFY
        verify(actual).dividendYield(any());
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }


    @Test
    void responseMapper_verify() {
            //SETUP
            final var sut = mock(YieldUsMutualFundEndpoint.class);

            final var usFund = mock(UsFund.class);
            final BigDecimal dividendYield = mock(BigDecimal.class);
            final var dividendYieldDatapoint = mock(FloatDatapoint.class);
            when(usFund.getDividendYield()).thenReturn(dividendYieldDatapoint);
            when(dividendYieldDatapoint.getValue()).thenReturn(dividendYield);
            final UsMutualFundHolding h = mock(UsMutualFundHolding.class);

            doCallRealMethod().when(sut).responseMapper(any(), any());

            //ACT
            final RYield response = sut.responseMapper(usFund, h);

            //VERIFY
            assertNotNull(response);
            assertEquals(dividendYield, response.getDividendYield());
    }
    
}
