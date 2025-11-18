package com.fintex.ce.repository.graphql.query;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.RHistoricalNavPrices;
import com.fintex.smclient.service.GraphqlTransportComponent;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class HistoricalNavPricesSMRepositoryTest {

    @Test
    void queryBenchOfFundCanada_returnsEmptyMap() {
        //SETUP
        final var graphqlTransport = mock(GraphqlTransportComponent.class);
        final HistoricalNavPricesSMRepository repository = new HistoricalNavPricesSMRepository(graphqlTransport);
        final List<FundSeriesHolding> holdings = List.of();
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        //ACT
        final Map<FundSeriesHolding, RHistoricalNavPrices> actual = repository.queryBenchOfFundCanada(holdings, providers);

        //VERIFY - Implementation returns Map.of() until endpoint is created
        Assertions.assertTrue(actual.isEmpty(), "Should return empty map until endpoint is implemented");
    }

}
