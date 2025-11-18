package com.fintex.ce.repository.graphql.query;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.RSalesCharge;
import com.fintex.ce.repository.graphql.query.endpoint.SalesChargeEndpoint;
import com.fintex.smclient.service.GraphqlTransportComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SalesChargeSMRepositoryTest {

    @Test
    void queryBenchOfFundCanada_verifyDoQuery() {
        //SETUP
        final var graphqlTransport = mock(GraphqlTransportComponent.class);
        final SalesChargeSMRepository sut = mock(SalesChargeSMRepository.class, withSettings().useConstructor(graphqlTransport));
        final List<FundSeriesHolding> holdings = List.of(mock(FundSeriesHolding.class));
        final List<DataProvider> providers = mock(List.class);

        doCallRealMethod().when(sut).queryBenchOfFundCanada(any(), anyList());

        //ACT
        sut.queryBenchOfFundCanada(holdings, providers);

        //VERIFY
        verify(sut).doQuery(eq(holdings), argThat(argument -> argument.getClass() == SalesChargeEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfStock_checkResult() {
        //SETUP
        final SalesChargeSMRepository sut = mock(SalesChargeSMRepository.class);
        final List<FundSeriesHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(sut.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = mock(List.class);

        doCallRealMethod().when(sut).queryBenchOfFundCanada(any(), anyList());
        //ACT
        final Map<FundSeriesHolding, RSalesCharge> actual = sut.queryBenchOfFundCanada(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

}
