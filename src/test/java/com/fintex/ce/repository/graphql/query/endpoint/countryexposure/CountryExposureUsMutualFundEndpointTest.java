package com.fintex.ce.repository.graphql.query.endpoint.countryexposure;

import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RCountryExposure;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.CountryAllocation;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static com.fintex.ce.config.enumeration.HoldingType.CASH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CountryExposureUsMutualFundEndpointTest {

    @Test
    void getUsEtfsByTickers_isPresent() {
        //SETUP
        final CountryExposureUsMutualFundEndpoint m = new CountryExposureUsMutualFundEndpoint();

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
        final CountryExposureUsMutualFundEndpoint m = mock(CountryExposureUsMutualFundEndpoint.class);

        final UsFundQuery usFundQuery = mock(UsFundQuery.class);
        when(usFundQuery.externalIdentifiers(any())).thenReturn(usFundQuery);
        when(usFundQuery.fixedIncomeCountryAllocation(any())).thenReturn(usFundQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final UsFundQuery actual = m.requestMapper(usFundQuery);

        //VERIFY
        verify(actual).externalIdentifiers(any());
        verify(actual).fixedIncomeCountryAllocation(any());
    }

    @Test
    void responseMapper_verifyCountryExposureMapper() {
        try (MockedStatic<GraphQlMapperUtils> mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final CountryExposureUsMutualFundEndpoint m = mock(CountryExposureUsMutualFundEndpoint.class);

            final UsMutualFundHolding holding = mock(UsMutualFundHolding.class);

            final UsFund entity = mock(UsFund.class);
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
            final CountryExposureUsMutualFundEndpoint m = mock(CountryExposureUsMutualFundEndpoint.class);

            final UsFund entity = mock(UsFund.class);
            final CountryAllocation country = mock(CountryAllocation.class);
            when(entity.getFixedIncomeCountryAllocation()).thenReturn(country);
            final UsMutualFundHolding holding = mock(UsMutualFundHolding.class);
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
