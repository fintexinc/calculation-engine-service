package com.fintex.ce.repository.graphql.query.endpoint.equitysector;

import com.fintex.smclient.graphql.EquitySectorAllocation;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.model.redis.equitysector.REquitySector;
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

class EquitySectorCanadaPooledFundEndpointTest {

    @Test
    void getUsEtfsByTickers_isPresent() {
        //SETUP
        final EquitySectorCanadaPooledFundEndpoint m = new EquitySectorCanadaPooledFundEndpoint();

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
        final EquitySectorCanadaPooledFundEndpoint m = mock(EquitySectorCanadaPooledFundEndpoint.class);

        final PooledFundQuery pooledFundQuery = mock(PooledFundQuery.class);
        when(pooledFundQuery.equitySectorAllocation(any())).thenReturn(pooledFundQuery);
        when(pooledFundQuery.externalIdentifiers(any())).thenReturn(pooledFundQuery);

        doCallRealMethod().when(m).requestMapper(any());

        //ACT
        final PooledFundQuery actual = m.requestMapper(pooledFundQuery);

        //VERIFY
        verify(actual).equitySectorAllocation(any());
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Test
    void responseMapper_verifyEquitySectorMapper() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final var sut = mock(EquitySectorCanadaPooledFundEndpoint.class);

            final PooledFund pooledFund = mock(PooledFund.class);
            final EquitySectorAllocation allocation = mock(EquitySectorAllocation.class);
            when(pooledFund.getEquitySectorAllocation()).thenReturn(allocation);
            final CanadaPooledFundHolding h = mock(CanadaPooledFundHolding.class);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            sut.responseMapper(pooledFund, h);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.equitySectorMapper(allocation));
        }
    }

    @Test
    void responseMapper_checkResult() throws Exception {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final EquitySectorCanadaPooledFundEndpoint sut = mock(EquitySectorCanadaPooledFundEndpoint.class);

            final PooledFund pooledFund = mock(PooledFund.class);
            final EquitySectorAllocation allocation = mock(EquitySectorAllocation.class);
            when(pooledFund.getEquitySectorAllocation()).thenReturn(allocation);
            final CanadaPooledFundHolding h = mock(CanadaPooledFundHolding.class);

            final REquitySector actual = mock(REquitySector.class);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.equitySectorMapper(any())).thenReturn(actual);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            final REquitySector expected = sut.responseMapper(pooledFund, h);

            //VERIFY
            assertSame(expected, actual);
        }
    }

}
