package com.fintex.ce.repository.graphql.query.endpoint.equitysector;

import com.fintex.smclient.graphql.EquitySectorAllocation;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.ce.dto.holding.FundSeriesHolding;
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

class EquitySectorFundCanadaEndpointTest {

    @Test
    void getUsEtfsByTickers_isPresent() {
        //SETUP
        final EquitySectorFundCanadaEndpoint m = new EquitySectorFundCanadaEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<FundSeries> expected = new ArrayList<>();

        when(q.getGetFundSeriesByHoldingCodes()).thenReturn(expected);

        //ACT
        final Function<Query, List<FundSeries>> actual = m.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final EquitySectorFundCanadaEndpoint m = mock(EquitySectorFundCanadaEndpoint.class);

        final FundSeriesQuery fundSeriesQuery = mock(FundSeriesQuery.class);
        when(fundSeriesQuery.equitySectorAllocation(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final FundSeriesQuery actual = m.requestMapper(fundSeriesQuery);

        //VERIFY
        verify(actual).equitySectorAllocation(any());
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Test
    void responseMapper_verifyEquitySectorMapper() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final var sut = mock(EquitySectorFundCanadaEndpoint.class);

            final FundSeries etf = mock(FundSeries.class);
            final EquitySectorAllocation allocation = mock(EquitySectorAllocation.class);
            when(etf.getEquitySectorAllocation()).thenReturn(allocation);
            final FundSeriesHolding h = mock(FundSeriesHolding.class);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            sut.responseMapper(etf, h);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.equitySectorMapper(allocation));
        }
    }

    @Test
    void responseMapper_checkResult() throws Exception {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final EquitySectorFundCanadaEndpoint sut = mock(EquitySectorFundCanadaEndpoint.class);

            final FundSeries etf = mock(FundSeries.class);
            final EquitySectorAllocation allocation = mock(EquitySectorAllocation.class);
            when(etf.getEquitySectorAllocation()).thenReturn(allocation);
            final FundSeriesHolding h = mock(FundSeriesHolding.class);

            final REquitySector actual = mock(REquitySector.class);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.equitySectorMapper(any())).thenReturn(actual);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            final REquitySector expected = sut.responseMapper(etf, h);

            //VERIFY
            assertSame(expected, actual);
        }
    }

}