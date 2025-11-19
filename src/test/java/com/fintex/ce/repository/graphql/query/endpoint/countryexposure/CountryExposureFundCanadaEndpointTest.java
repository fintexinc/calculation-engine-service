package com.fintex.ce.repository.graphql.query.endpoint.countryexposure;

import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.RCountryExposure;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.CountryAllocation;
import com.fintex.smclient.graphql.FundHoldingIdentifier;
import com.fintex.smclient.graphql.FundHoldingIdentifiersCodes;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static com.fintex.ce.config.enumeration.HoldingType.CASH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CountryExposureFundCanadaEndpointTest {

    @Test
    void getUsEtfsByTickers_isPresent() {
        //SETUP
        final CountryExposureFundCanadaEndpoint m = new CountryExposureFundCanadaEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<FundSeries> expected = new ArrayList<>();

        when(q.getGetFundSeriesByHoldingCodes()).thenReturn(expected);

        //ACT
        final Function<Query, List<FundSeries>> actual = m.getGetSMEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final CountryExposureFundCanadaEndpoint m = mock(CountryExposureFundCanadaEndpoint.class);

        final FundSeriesQuery fundSeriesQuery = mock(FundSeriesQuery.class);
        when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.fixedIncomeCountryAllocation(any())).thenReturn(fundSeriesQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final FundSeriesQuery actual = m.requestMapper(fundSeriesQuery);

        //VERIFY
        verify(actual).externalIdentifiers(any());
        verify(actual).fixedIncomeCountryAllocation(any());
    }

    @Test
    void queryDefinition_verify() {
        //SETUP
        final CountryExposureFundCanadaEndpoint m = mock(CountryExposureFundCanadaEndpoint.class);

        final QueryQuery qq = mock(QueryQuery.class);

        final FundHoldingIdentifiersCodes codes = mock(FundHoldingIdentifiersCodes.class);
        final String code = "CODE";
        when(codes.getCode()).thenReturn(code);
        final FundHoldingIdentifier cash = FundHoldingIdentifier.CASH;
        when(codes.getFundholdingIdentifier()).thenReturn(cash);
        final List<FundHoldingIdentifiersCodes> equityIdentifiers = List.of(codes);

        final UnaryOperator func = mock(UnaryOperator.class);
        when(func.apply(any())).thenReturn(mock(Object.class));

        doCallRealMethod().when(m).queryDefinition(any(), any());
        //ACT
        final QueryQueryDefinition actual = m.queryDefinition(equityIdentifiers, func);
        actual.define(qq);

        //VERIFY
        verify(qq).getFundSeriesByHoldingCodes(eq(equityIdentifiers), any());
    }

    @Test
    void responseMapper_verifyCountryExposureMapper() {
        try (MockedStatic<GraphQlMapperUtils> mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final CountryExposureFundCanadaEndpoint m = mock(CountryExposureFundCanadaEndpoint.class);

            final FundSeriesHolding holding = mock(FundSeriesHolding.class);

            final FundSeries entity = mock(FundSeries.class);
            final CountryAllocation country = mock(CountryAllocation.class);
            when(entity.getFixedIncomeCountryAllocation()).thenReturn(country);

            doCallRealMethod().when(m).responseMapper(any(), any());

            //ACT
            m.responseMapper(entity, holding);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.countryExposureMapper(country));
        }
    }

    @Test
    void responseMapper_checkResult() throws Exception {
        try (MockedStatic<GraphQlMapperUtils> mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final CountryExposureFundCanadaEndpoint m = mock(CountryExposureFundCanadaEndpoint.class);

            final FundSeries entity = mock(FundSeries.class);
            final CountryAllocation country = mock(CountryAllocation.class);
            when(entity.getFixedIncomeCountryAllocation()).thenReturn(country);
            final FundSeriesHolding holding = mock(FundSeriesHolding.class);
            when(holding.getType()).thenReturn(CASH);

            final Map<String, BigDecimal> asset = Map.of("T", BigDecimal.ONE);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.countryExposureMapper(any())).thenReturn(asset);

            doCallRealMethod().when(m).responseMapper(any(), any());
            //ACT
            final RCountryExposure actual = m.responseMapper(entity, holding);

            //VERIFY
            assertEquals(new RCountryExposure(holding.getType(), asset), actual);
        }
    }

}