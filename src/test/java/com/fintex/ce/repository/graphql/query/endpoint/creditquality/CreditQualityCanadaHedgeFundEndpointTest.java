package com.fintex.ce.repository.graphql.query.endpoint.creditquality;

import com.fintex.smclient.graphql.CountryAllocation;
import com.fintex.smclient.graphql.CreditQualityRatings;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.model.redis.RCreditQuality;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.fintex.ce.config.enumeration.HoldingType.CASH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreditQualityCanadaHedgeFundEndpointTest {

    @Test
    void getUsEtfsByTickers_isPresent() {
        //SETUP
        final CreditQualityCanadaHedgeFundEndpoint m = new CreditQualityCanadaHedgeFundEndpoint();

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
        final CreditQualityCanadaHedgeFundEndpoint m = mock(CreditQualityCanadaHedgeFundEndpoint.class);

        final HedgeFundQuery hedgeFundQuery = mock(HedgeFundQuery.class);
        when(hedgeFundQuery.creditQualityRatings(any())).thenReturn(hedgeFundQuery);
        when(hedgeFundQuery.externalIdentifiers(any())).thenReturn(hedgeFundQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final HedgeFundQuery actual = m.requestMapper(hedgeFundQuery);

        //VERIFY
        verify(actual).creditQualityRatings(any());
        verify(actual).externalIdentifiers(any());
    }

    @Test
    void responseMapper_verifyEquityCountryAllocationMapper() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final CreditQualityCanadaHedgeFundEndpoint sut = mock(CreditQualityCanadaHedgeFundEndpoint.class);

            final CanadaHedgeFundHolding holding = mock(CanadaHedgeFundHolding.class);

            final HedgeFund entity = mock(HedgeFund.class);
            final CreditQualityRatings country = mock(CreditQualityRatings.class);
            when(entity.getCreditQualityRatings()).thenReturn(country);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            sut.responseMapper(entity, holding);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.creditQualityMapper(country));
        }
    }

    @Test
    void responseMapper_checkResult() throws Exception {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final CreditQualityCanadaHedgeFundEndpoint sut = mock(CreditQualityCanadaHedgeFundEndpoint.class);

            final HedgeFund entity = mock(HedgeFund.class);
            final CountryAllocation country = mock(CountryAllocation.class);
            when(entity.getEquityCountryAllocation()).thenReturn(country);
            final CanadaHedgeFundHolding holding = mock(CanadaHedgeFundHolding.class);
            when(holding.getType()).thenReturn(CASH);

            final Map<String, BigDecimal> asset = Map.of("T", BigDecimal.ONE);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.creditQualityMapper(any())).thenReturn(asset);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            final RCreditQuality actual = sut.responseMapper(entity, holding);

            //VERIFY
            assertEquals(new RCreditQuality(holding.getType(), asset), actual);
        }
    }

}
