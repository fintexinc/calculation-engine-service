package com.fintex.ce.repository.graphql.query.endpoint.equitymarketcapitalization;

import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.model.redis.equitymarketcapitalization.REquityMarketCapitalization;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.EquityMarketCapitalization;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
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

class EquityMarketCapitalizationCanadaHedgeFundEndpointTest {

    @Test
    void getUsEtfsByTickers_isPresent() {
        //SETUP
        final EquityMarketCapitalizationCanadaHedgeFundEndpoint m = new EquityMarketCapitalizationCanadaHedgeFundEndpoint();

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
        final EquityMarketCapitalizationCanadaHedgeFundEndpoint m = mock(EquityMarketCapitalizationCanadaHedgeFundEndpoint.class);

        final HedgeFundQuery fundSeriesQuery = mock(HedgeFundQuery.class);
        when(fundSeriesQuery.equityMarketCapitalization(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final HedgeFundQuery actual = m.requestMapper(fundSeriesQuery);

        //VERIFY
        verify(actual).equityMarketCapitalization(any());
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Test
    void responseMapper_verifyEquitySectorMapper() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final var sut = mock(EquityMarketCapitalizationCanadaHedgeFundEndpoint.class);

            final HedgeFund hedgeFund = mock(HedgeFund.class);
            final EquityMarketCapitalization allocation = mock(EquityMarketCapitalization.class);
            when(hedgeFund.getEquityMarketCapitalization()).thenReturn(allocation);
            final CanadaHedgeFundHolding h = mock(CanadaHedgeFundHolding.class);

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
            final EquityMarketCapitalizationCanadaHedgeFundEndpoint sut = mock(EquityMarketCapitalizationCanadaHedgeFundEndpoint.class);

            final HedgeFund hedgeFund = mock(HedgeFund.class);
            final EquityMarketCapitalization allocation = mock(EquityMarketCapitalization.class);
            when(hedgeFund.getEquityMarketCapitalization()).thenReturn(allocation);
            final CanadaHedgeFundHolding h = mock(CanadaHedgeFundHolding.class);

            final REquityMarketCapitalization actual = mock(REquityMarketCapitalization.class);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.equityMarketCapitalizationMapper(any())).thenReturn(actual);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            final REquityMarketCapitalization expected = sut.responseMapper(hedgeFund, h);

            //VERIFY
            assertSame(expected, actual);
        }
    }

}
