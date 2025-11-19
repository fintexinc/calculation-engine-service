package com.fintex.ce.repository.graphql.query.endpoint.yield;

import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.RYield;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class YieldEtfUsEndpointTest {

    @Test
    void getGetCanadaEtfsByTickers_isPresent() {
        //SETUP
        final YieldEtfUsEndpoint m = new YieldEtfUsEndpoint();

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
        final YieldEtfUsEndpoint m = mock(YieldEtfUsEndpoint.class);

        final EtfQuery fundSeriesQuery = mock(EtfQuery.class);
        when(fundSeriesQuery.currentYield(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.ticker(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final EtfQuery actual = m.requestMapper(fundSeriesQuery);

        //VERIFY
        verify(actual).currentYield(any());
        verify(actual).ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }


    @Test
    void responseMapper_verifyEquityMarketCapitalizationMapper() {
            //SETUP
            final var sut = mock(YieldEtfUsEndpoint.class);

            final var etf = mock(Etf.class);
            final BigDecimal currentYieldValue = mock(BigDecimal.class);
            final var currentYield = mock(FloatDatapoint.class);
            when(etf.getCurrentYield()).thenReturn(currentYield);
            when(currentYield.getValue()).thenReturn(currentYieldValue);
            final EtfHolding h = mock(EtfHolding.class);

            doCallRealMethod().when(sut).responseMapper(any(), any());

            //ACT
            final RYield response = sut.responseMapper(etf, h);

            //VERIFY
            assertNotNull(response);
            assertEquals(currentYieldValue, response.getDividendYield());
    }
    
}
