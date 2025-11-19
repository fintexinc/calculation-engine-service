package com.fintex.ce.repository.graphql.query.endpoint.equitymarketcapitalization;

import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.model.redis.equitymarketcapitalization.REquityMarketCapitalization;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.EquityMarketCapitalization;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.smclient.graphql.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EquityMarketCapitalizationCanadaPooledFundEndpointTest {

    @Test
    void getUsEtfsByTickers_isPresent() {
        //SETUP
        final EquityMarketCapitalizationCanadaPooledFundEndpoint m = new EquityMarketCapitalizationCanadaPooledFundEndpoint();

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
        final EquityMarketCapitalizationCanadaPooledFundEndpoint m = mock(EquityMarketCapitalizationCanadaPooledFundEndpoint.class);

        final PooledFundQuery fundSeriesQuery = mock(PooledFundQuery.class);
        when(fundSeriesQuery.equityMarketCapitalization(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final PooledFundQuery actual = m.requestMapper(fundSeriesQuery);

        //VERIFY
        verify(actual).equityMarketCapitalization(any());
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Test
    void responseMapper_verifyEquitySectorMapper() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final var sut = mock(EquityMarketCapitalizationCanadaPooledFundEndpoint.class);

            final PooledFund hedgeFund = mock(PooledFund.class);
            final EquityMarketCapitalization allocation = mock(EquityMarketCapitalization.class);
            when(hedgeFund.getEquityMarketCapitalization()).thenReturn(allocation);
            final CanadaPooledFundHolding h = mock(CanadaPooledFundHolding.class);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            sut.responseMapper(hedgeFund, h);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.equityMarketCapitalizationMapper(allocation));
        }
    }

    @Test
    void responseMapper_checkResult() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final EquityMarketCapitalizationCanadaPooledFundEndpoint sut = mock(EquityMarketCapitalizationCanadaPooledFundEndpoint.class);

            final PooledFund pooledFund = mock(PooledFund.class);
            final EquityMarketCapitalization allocation = mock(EquityMarketCapitalization.class);
            when(pooledFund.getEquityMarketCapitalization()).thenReturn(allocation);
            final CanadaPooledFundHolding h = mock(CanadaPooledFundHolding.class);

            final REquityMarketCapitalization actual = mock(REquityMarketCapitalization.class);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.equityMarketCapitalizationMapper(any())).thenReturn(actual);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            final REquityMarketCapitalization expected = sut.responseMapper(pooledFund, h);

            //VERIFY
            assertSame(expected, actual);
        }
    }

}
