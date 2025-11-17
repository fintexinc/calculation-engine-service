package com.fintex.ce.repository.graphql.query.endpoint.creditquality;

import com.fintex.smclient.graphql.CountryAllocation;
import com.fintex.smclient.graphql.CreditQualityRatings;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.dto.holding.EtfHolding;
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
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.enumeration.HoldingType.CASH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreditQualityEtfCanadaEndpointTest {

    @Test
    void getGetCanadaEtfsByTickers_isPresent() {
        //SETUP
        final CreditQualityEtfCanadaEndpoint m = new CreditQualityEtfCanadaEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<Etf> expected = new ArrayList<>();

        when(q.getGetCanadaEtfsByTickers()).thenReturn(expected);

        //ACT
        final Function<Query, List<Etf>> actual = m.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final CreditQualityEtfCanadaEndpoint m = mock(CreditQualityEtfCanadaEndpoint.class);

        final EtfQuery fundSeriesQuery = mock(EtfQuery.class);
        when(fundSeriesQuery.creditQualityRatings(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.ticker(any())).thenReturn(fundSeriesQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final EtfQuery actual = m.requestMapper(fundSeriesQuery);

        //VERIFY
        verify(actual).creditQualityRatings(any());
        verify(actual).ticker(any());
    }

    @Test
    void queryDefinition_verify() {
        //SETUP
        final CreditQualityEtfCanadaEndpoint m = mock(CreditQualityEtfCanadaEndpoint.class);

        final QueryQuery qq = mock(QueryQuery.class);

        final List<String> equityIdentifiers = List.of("TEST");

        final UnaryOperator func = mock(UnaryOperator.class);
        when(func.apply(any())).thenReturn(mock(Object.class));

        doCallRealMethod().when(m).queryDefinition(any(), any());
        //ACT
        final QueryQueryDefinition actual = m.queryDefinition(equityIdentifiers, func);
        actual.define(qq);

        //VERIFY
        verify(qq).getCanadaEtfsByTickers(eq(equityIdentifiers), any());
    }

    @Test
    void responseMapper_verifyEquityCountryAllocationMapper() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final CreditQualityEtfCanadaEndpoint sut = mock(CreditQualityEtfCanadaEndpoint.class);

            final EtfHolding holding = mock(EtfHolding.class);

            final Etf entity = mock(Etf.class);
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
            final CreditQualityEtfCanadaEndpoint sut = mock(CreditQualityEtfCanadaEndpoint.class);

            final Etf entity = mock(Etf.class);
            final CountryAllocation country = mock(CountryAllocation.class);
            when(entity.getEquityCountryAllocation()).thenReturn(country);
            final EtfHolding holding = mock(EtfHolding.class);
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