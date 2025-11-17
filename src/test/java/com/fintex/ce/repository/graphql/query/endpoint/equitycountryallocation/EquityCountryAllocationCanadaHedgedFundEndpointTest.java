package com.fintex.ce.repository.graphql.query.endpoint.equitycountryallocation;

import com.fintex.smclient.graphql.CountryAllocation;
import com.fintex.smclient.graphql.DataProvider;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
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

class EquityCountryAllocationCanadaHedgedFundEndpointTest {

    @Test
    void getUsEtfsByTickers_isPresent() {
        //SETUP
        final EquityCountryAllocationCanadaHedgedFundEndpoint m = new EquityCountryAllocationCanadaHedgedFundEndpoint();

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
        final EquityCountryAllocationCanadaHedgedFundEndpoint m = mock(EquityCountryAllocationCanadaHedgedFundEndpoint.class);

        final HedgeFundQuery fundSeriesQuery = mock(HedgeFundQuery.class);
        when(fundSeriesQuery.equityCountryAllocation(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final HedgeFundQuery actual = m.requestMapper(fundSeriesQuery);

        //VERIFY
        verify(actual).equityCountryAllocation(any());
        verify(actual).externalIdentifiers(any());
    }

    @Test
    void responseMapper_verifyEquityCountryAllocationMapper() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final var sut = mock(EquityCountryAllocationCanadaHedgedFundEndpoint.class);

            final CanadaHedgeFundHolding holding = mock(CanadaHedgeFundHolding.class);
            final HedgeFund entity = mock(HedgeFund.class);
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
            final var sut = mock(EquityCountryAllocationCanadaHedgedFundEndpoint.class);

            final HedgeFund entity = mock(HedgeFund.class);
            final CountryAllocation country = mock(CountryAllocation.class);
            final CanadaHedgeFundHolding holding = mock(CanadaHedgeFundHolding.class);
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
