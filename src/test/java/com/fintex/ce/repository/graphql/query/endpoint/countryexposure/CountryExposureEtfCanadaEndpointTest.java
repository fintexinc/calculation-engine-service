package com.fintex.ce.repository.graphql.query.endpoint.countryexposure;

import com.fintex.smclient.graphql.CountryAllocation;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.RCountryExposure;
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

import static com.fintex.ce.config.enumeration.HoldingType.CANADA_ETF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CountryExposureEtfCanadaEndpointTest {

    @Test
    void getGetCanadaEtfsByTickers_isPresent() {
        //SETUP
        final CountryExposureEtfCanadaEndpoint m = new CountryExposureEtfCanadaEndpoint();

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
        final CountryExposureEtfCanadaEndpoint m = mock(CountryExposureEtfCanadaEndpoint.class);

        final EtfQuery fundSeriesQuery = mock(EtfQuery.class);
        when(fundSeriesQuery.ticker(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.fixedIncomeCountryAllocation(any())).thenReturn(fundSeriesQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final EtfQuery actual = m.requestMapper(fundSeriesQuery);

        //VERIFY
        verify(actual).fixedIncomeCountryAllocation(any());
        verify(actual).ticker(any());
    }

    @Test
    void queryDefinition_verify() {
        //SETUP
        final CountryExposureEtfCanadaEndpoint m = mock(CountryExposureEtfCanadaEndpoint.class);

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
    void responseMapper_verifyCountryExposureMapper() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final CountryExposureEtfCanadaEndpoint sut = mock(CountryExposureEtfCanadaEndpoint.class);

            final EtfHolding holding = mock(EtfHolding.class);

            final Etf entity = mock(Etf.class);
            final CountryAllocation country = mock(CountryAllocation.class);
            when(entity.getFixedIncomeCountryAllocation()).thenReturn(country);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            sut.responseMapper(entity, holding);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.countryExposureMapper(country));
        }
    }

    @Test
    void responseMapper_checkResult() throws Exception {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final CountryExposureEtfCanadaEndpoint sut = mock(CountryExposureEtfCanadaEndpoint.class);

            final Etf entity = mock(Etf.class);
            final CountryAllocation country = mock(CountryAllocation.class);
            when(entity.getFixedIncomeCountryAllocation()).thenReturn(country);
            final EtfHolding holding = mock(EtfHolding.class);
            when(holding.getType()).thenReturn(CANADA_ETF);

            final Map<String, BigDecimal> asset = Map.of("T", BigDecimal.ONE);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.countryExposureMapper(any())).thenReturn(asset);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            final RCountryExposure actual = sut.responseMapper(entity, holding);

            //VERIFY
            assertEquals(new RCountryExposure(holding.getType(), asset), actual);
        }
    }

}