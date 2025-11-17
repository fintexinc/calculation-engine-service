package com.fintex.ce.repository.graphql.query.endpoint.commonholdings;

import com.fintex.smclient.graphql.Holdings;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.model.redis.topcommonholdings.RCommonHoldings;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

 class CommonHoldingsCanadaPooledFundEndpointTest {

    @Test
    void getUsEtfsByTickers_isPresent() {
        //SETUP
        final var fundCanadaFDSEndpoint = new CommonHoldingsCanadaPooledFundEndpoint();
        final var query = mock(Query.class);
        final var expected = new ArrayList<PooledFund>();

        when(query.getGetCanadaPooledFundsByMorningstarIds()).thenReturn(expected);

        //ACT
        final Function<Query, List<PooledFund>> actual = fundCanadaFDSEndpoint.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(query), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final var fundCanadaFDSEndpoint = mock(CommonHoldingsCanadaPooledFundEndpoint.class);
        final var pooledFundQuery = mock(PooledFundQuery.class);

        when(pooledFundQuery.holdings(any())).thenReturn(pooledFundQuery);
        when(pooledFundQuery.externalIdentifiers(any())).thenReturn(pooledFundQuery);

        doCallRealMethod().when(fundCanadaFDSEndpoint).requestMapper(any());
        //ACT
        final PooledFundQuery actual = fundCanadaFDSEndpoint.requestMapper(pooledFundQuery);

        //VERIFY
        verify(actual).holdings(any());
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }


    @Test
    void responseMapper_verifyTopCommonHoldingsMapper() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final var fundCanadaFDSEndpoint = mock(CommonHoldingsCanadaPooledFundEndpoint.class);
            final var canadaPooledFundHolding = mock(CanadaPooledFundHolding.class);
            final var pooledFund = mock(PooledFund.class);
            final var allocation = mock(Holdings.class);

            when(pooledFund.getHoldings()).thenReturn(allocation);

            doCallRealMethod().when(fundCanadaFDSEndpoint).responseMapper(any(), any());
            //ACT
            fundCanadaFDSEndpoint.responseMapper(pooledFund, canadaPooledFundHolding);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.topCommonHoldingsMapper(allocation));
        }
    }

    @Test
    void responseMapper_checkResult() throws Exception {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP

            final var fundCanadaFDSEndpoint = mock(CommonHoldingsCanadaPooledFundEndpoint.class);
            final var pooledFund = mock(PooledFund.class);
            final var holding = mock(CanadaPooledFundHolding.class);
            final var allocation = mock(Holdings.class);
            final RCommonHoldings actual = mock(RCommonHoldings.class);

            when(pooledFund.getHoldings()).thenReturn(allocation);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.topCommonHoldingsMapper(any())).thenReturn(actual);

            doCallRealMethod().when(fundCanadaFDSEndpoint).responseMapper(any(), any());
            //ACT
            final RCommonHoldings expected = fundCanadaFDSEndpoint.responseMapper(pooledFund, holding);

            //VERIFY
            assertSame(expected, actual);
        }
    }
    
}
