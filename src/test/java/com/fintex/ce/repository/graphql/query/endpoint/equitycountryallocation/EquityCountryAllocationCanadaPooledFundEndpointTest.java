package com.fintex.ce.repository.graphql.query.endpoint.equitycountryallocation;

import com.fintex.smclient.graphql.CountryAllocation;
import com.fintex.smclient.graphql.DataProvider;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.model.redis.REquityCountryAllocation;
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

class EquityCountryAllocationCanadaPooledFundEndpointTest {

    @Test
    void getUsEtfsByTickers_isPresent() {
        //SETUP
        final EquityCountryAllocationCanadaPooledFundEndpoint m = new EquityCountryAllocationCanadaPooledFundEndpoint();

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
        final EquityCountryAllocationCanadaPooledFundEndpoint m = mock(EquityCountryAllocationCanadaPooledFundEndpoint.class);

        final PooledFundQuery pooledFundQuery = mock(PooledFundQuery.class);
        when(pooledFundQuery.equityCountryAllocation(any())).thenReturn(pooledFundQuery);
        when(pooledFundQuery.externalIdentifiers(any())).thenReturn(pooledFundQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final PooledFundQuery actual = m.requestMapper(pooledFundQuery);

        //VERIFY
        verify(actual).equityCountryAllocation(any());
        verify(actual).externalIdentifiers(any());
    }

    @Test
    void responseMapper_verifyEquityCountryAllocationMapper() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final var sut = mock(EquityCountryAllocationCanadaPooledFundEndpoint.class);

            final CanadaPooledFundHolding holding = mock(CanadaPooledFundHolding.class);
            final PooledFund entity = mock(PooledFund.class);
            final CountryAllocation country = mock(CountryAllocation.class);

            when(entity.getEquityCountryAllocation()).thenReturn(country);
            when(country.getDataProvider()).thenReturn(DataProvider.EAGLE);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            sut.responseMapper(entity, holding);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.equityCountryAllocationMapper(country));
        }
    }

    @Test
    void responseMapper_checkResult() throws Exception {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final var sut = mock(EquityCountryAllocationCanadaPooledFundEndpoint.class);

            final PooledFund entity = mock(PooledFund.class);
            final CountryAllocation country = mock(CountryAllocation.class);
            final CanadaPooledFundHolding holding = mock(CanadaPooledFundHolding.class);
            final Map<String, BigDecimal> asset = Map.of("T", BigDecimal.ONE);
            final var expected = new REquityCountryAllocation(CASH, asset);
            expected.setProvider(DataProvider.EAGLE.name());

            when(entity.getEquityCountryAllocation()).thenReturn(country);
            when(country.getDataProvider()).thenReturn(DataProvider.EAGLE);
            when(holding.getType()).thenReturn(CASH);

            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.equityCountryAllocationMapper(any())).thenReturn(asset);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            final var actual = sut.responseMapper(entity, holding);

            //VERIFY
            assertEquals(expected, actual);
        }
    }
    
}
