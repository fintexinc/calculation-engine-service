package com.fintex.ce.repository.graphql.query;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.RHistoricalDistributions;
import com.fintex.smclient.service.GraphqlTransportComponent;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class HistoricalDistributionsSMRepositoryTest {

//    @Test
//    @Disabled
//    void queryBenchOfFundCanada_verifyDoQuery() {
//        //SETUP
//        final var graphqlTransport = mock(GraphqlTransportComponent.class);
//        final HistoricalDistributionsSMRepository m = mock(HistoricalDistributionsSMRepository.class,
//                withSettings().useConstructor(graphqlTransport));
//        final List<FundSeriesHolding> holdings = List.of();
//        final List<DataProvider> providers = List.of(DataProvider.EAGLE);
//
//        doCallRealMethod().when(m).queryBenchOfFundCanada(any(), anyList());
//        //ACT
//        m.queryBenchOfFundCanada(holdings, providers);
//
//        //VERIFY
//        // TODO FundCanadaHistoricalDistributionsEndpoint does not exist yet
//        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == FundCanadaHistoricalDistributionsEndpoint.class),
//                eq(providers));
//    }

    @Test
    void queryBenchOfFundCanada_checkResult() {
        //SETUP
        final var graphqlTransport = mock(GraphqlTransportComponent.class);
        final HistoricalDistributionsSMRepository repository = new HistoricalDistributionsSMRepository(graphqlTransport);
        final List<FundSeriesHolding> holdings = List.of();
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        //ACT
        final Map<FundSeriesHolding, RHistoricalDistributions> actual = repository.queryBenchOfFundCanada(holdings, providers);

        //VERIFY - Implementation returns Map.of() until endpoint is created
        Assertions.assertTrue(actual.isEmpty(), "Should return empty map until endpoint is implemented");
    }

}
