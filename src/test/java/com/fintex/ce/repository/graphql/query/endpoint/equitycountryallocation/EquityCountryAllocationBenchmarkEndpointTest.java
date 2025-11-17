package com.fintex.ce.repository.graphql.query.endpoint.equitycountryallocation;

import com.fintex.smclient.graphql.CountryAllocation;
import com.fintex.smclient.graphql.CountryAllocationQuery;
import com.fintex.smclient.graphql.CountryAllocationQueryDefinition;
import com.fintex.smclient.graphql.DataProvider;
import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.model.redis.REquityCountryAllocation;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Map;

import static com.fintex.ce.config.enumeration.HoldingType.CASH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EquityCountryAllocationBenchmarkEndpointTest {

    @Test
    void requestMapper_verify() {
        //SETUP
        final EquityCountryAllocationBenchmarkEndpoint m = mock(EquityCountryAllocationBenchmarkEndpoint.class);

        final IndexQuery indexQuery = mock(IndexQuery.class);
        when(indexQuery.equityCountryAllocation(any())).thenReturn(indexQuery);
        when(indexQuery.externalIdentifiers(any())).thenReturn(indexQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final IndexQuery actual = m.requestMapper(indexQuery);

        //VERIFY
        verify(actual).equityCountryAllocation(any());
        verify(actual).externalIdentifiers(any());
    }

    @Test
    void getCountryAllocationQueryDefinition_checkResult() {
        //SETUP
        final CountryAllocationQueryDefinition actual = EquityCountryAllocationBenchmarkEndpoint.getCountryAllocationQueryDefinition();

        final CountryAllocationQuery countryAllocationQuery = mock(CountryAllocationQuery.class);

        when(countryAllocationQuery.dataProvider()).thenReturn(countryAllocationQuery);
        when(countryAllocationQuery.allocation(any())).thenReturn(countryAllocationQuery);

        //ACT
        actual.define(countryAllocationQuery);

        //VERIFY
        verify(countryAllocationQuery).dataProvider();
        verify(countryAllocationQuery).allocation(any());
    }

    @Test
    void responseMapper_verifyEquityCountryAllocationMapper() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final var sut = mock(EquityCountryAllocationBenchmarkEndpoint.class);

            final BenchmarkIndexHolding holding = mock(BenchmarkIndexHolding.class);
            final Index entity = mock(Index.class);
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
            final var sut = mock(EquityCountryAllocationBenchmarkEndpoint.class);

            final Index entity = mock(Index.class);
            final CountryAllocation country = mock(CountryAllocation.class);
            final BenchmarkIndexHolding holding = mock(BenchmarkIndexHolding.class);
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
