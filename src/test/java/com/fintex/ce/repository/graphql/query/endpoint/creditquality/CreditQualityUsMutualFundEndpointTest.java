package com.fintex.ce.repository.graphql.query.endpoint.creditquality;

import com.fintex.smclient.graphql.CountryAllocation;
import com.fintex.smclient.graphql.CreditQualityRatings;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
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

class CreditQualityUsMutualFundEndpointTest {

    @Test
    void getUsEtfsByTickers_isPresent() {
        //SETUP
        final CreditQualityUsMutualFundEndpoint m = new CreditQualityUsMutualFundEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<UsFund> expected = new ArrayList<>();

        when(q.getGetUsFundsByTickers()).thenReturn(expected);

        //ACT
        final Function<Query, List<UsFund>> actual = m.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final CreditQualityUsMutualFundEndpoint m = mock(CreditQualityUsMutualFundEndpoint.class);

        final UsFundQuery usFundQuery = mock(UsFundQuery.class);
        when(usFundQuery.creditQualityRatings(any())).thenReturn(usFundQuery);
        when(usFundQuery.externalIdentifiers(any())).thenReturn(usFundQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final UsFundQuery actual = m.requestMapper(usFundQuery);

        //VERIFY
        verify(actual).creditQualityRatings(any());
        verify(actual).externalIdentifiers(any());
    }

    @Test
    void responseMapper_verifyEquityCountryAllocationMapper() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final CreditQualityUsMutualFundEndpoint sut = mock(CreditQualityUsMutualFundEndpoint.class);

            final UsMutualFundHolding holding = mock(UsMutualFundHolding.class);

            final UsFund entity = mock(UsFund.class);
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
            final CreditQualityUsMutualFundEndpoint sut = mock(CreditQualityUsMutualFundEndpoint.class);

            final UsFund entity = mock(UsFund.class);
            final CountryAllocation country = mock(CountryAllocation.class);
            when(entity.getEquityCountryAllocation()).thenReturn(country);
            final UsMutualFundHolding holding = mock(UsMutualFundHolding.class);
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
