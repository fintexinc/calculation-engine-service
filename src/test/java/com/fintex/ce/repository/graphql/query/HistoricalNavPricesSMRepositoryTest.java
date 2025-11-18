package com.fintex.ce.repository.graphql.query;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.RHistoricalNavPrices;
import com.fintex.ce.repository.graphql.query.endpoint.historicalnavprices.FundCanadaHistoricalNavPricesEndpoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class HistoricalNavPricesSMRepositoryTest {

    @Test
    void queryBenchOfFundCanada_verifyDoQuery() {
        //SETUP
        final var graphqlTransport = mock(GraphqlTransportComponent.class);
        final HistoricalNavPricesSMRepository m = mock(HistoricalNavPricesSMRepository.class,
                withSettings().useConstructor(graphqlTransport));
        final List<FundSeriesHolding> holdings = List.of();
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryBenchOfFundCanada(any(), anyList());
        //ACT
        m.queryBenchOfFundCanada(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == FundCanadaHistoricalNavPricesEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfFundCanada_checkResult() {
        //SETUP
        final HistoricalNavPricesSMRepository m = mock(HistoricalNavPricesSMRepository.class);
        final List<FundSeriesHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);
        doCallRealMethod().when(m).queryBenchOfFundCanada(any(), anyList());
        //ACT
        final Map<FundSeriesHolding, RHistoricalNavPrices> actual = m.queryBenchOfFundCanada(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

}
