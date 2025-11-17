package com.fintex.ce.repository.graphql.query.endpoint.countryexposure;

import com.fintex.smclient.graphql.CountryAllocation;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.model.redis.RCountryExposure;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
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

class CountryExposureCanadaPooledFundEndpointTest {

    @Test
    void getUsEtfsByTickers_isPresent() {
        //SETUP
        final CountryExposureCanadaPooledFundEndpoint m = new CountryExposureCanadaPooledFundEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<PooledFund> expected = new ArrayList<>();

        when(q.getGetCanadaPooledFundsByMorningstarIds()).thenReturn(expected);

        //ACT
        final Function<Query, List<PooledFund>> actual = m.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final CountryExposureCanadaPooledFundEndpoint m = mock(CountryExposureCanadaPooledFundEndpoint.class);

        final PooledFundQuery pooledFundQuery = mock(PooledFundQuery.class);
        when(pooledFundQuery.externalIdentifiers(any())).thenReturn(pooledFundQuery);
        when(pooledFundQuery.fixedIncomeCountryAllocation(any())).thenReturn(pooledFundQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final PooledFundQuery actual = m.requestMapper(pooledFundQuery);

        //VERIFY
        verify(actual).externalIdentifiers(any());
        verify(actual).fixedIncomeCountryAllocation(any());
    }

    @Test
    void responseMapper_verifyCountryExposureMapper() {
        try (MockedStatic<GraphQlMapperUtils> mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final CountryExposureCanadaPooledFundEndpoint m = mock(CountryExposureCanadaPooledFundEndpoint.class);

            final CanadaPooledFundHolding holding = mock(CanadaPooledFundHolding.class);

            final PooledFund entity = mock(PooledFund.class);
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
            final CountryExposureCanadaPooledFundEndpoint m = mock(CountryExposureCanadaPooledFundEndpoint.class);

            final PooledFund entity = mock(PooledFund.class);
            final CountryAllocation country = mock(CountryAllocation.class);
            when(entity.getFixedIncomeCountryAllocation()).thenReturn(country);
            final CanadaPooledFundHolding holding = mock(CanadaPooledFundHolding.class);
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
