package com.fintex.ce.repository.graphql.query.endpoint.commonholdings;

import com.fintex.smclient.graphql.Holdings;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
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

class CommonHoldingsUsMutualFundEndpointTest {

    @Test
    void getUsEtfsByTickers_isPresent() {
        //SETUP
        final var fundCanadaFDSEndpoint = new CommonHoldingsUsMutualFundEndpoint();
        final var query = mock(Query.class);
        final var expected = new ArrayList<UsFund>();

        when(query.getGetUsFundsByTickers()).thenReturn(expected);

        //ACT
        final Function<Query, List<UsFund>> actual = fundCanadaFDSEndpoint.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(query), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final var fundCanadaFDSEndpoint = mock(CommonHoldingsUsMutualFundEndpoint.class);
        final var usFundQuery = mock(UsFundQuery.class);

        when(usFundQuery.holdings(any())).thenReturn(usFundQuery);
        when(usFundQuery.externalIdentifiers(any())).thenReturn(usFundQuery);

        doCallRealMethod().when(fundCanadaFDSEndpoint).requestMapper(any());
        //ACT
        final UsFundQuery actual = fundCanadaFDSEndpoint.requestMapper(usFundQuery);

        //VERIFY
        verify(actual).holdings(any());
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }


    @Test
    void responseMapper_verifyTopCommonHoldingsMapper() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final var fundCanadaFDSEndpoint = mock(CommonHoldingsUsMutualFundEndpoint.class);
            final var usMutualFundHolding = mock(UsMutualFundHolding.class);
            final var usFund = mock(UsFund.class);
            final var allocation = mock(Holdings.class);

            when(usFund.getHoldings()).thenReturn(allocation);

            doCallRealMethod().when(fundCanadaFDSEndpoint).responseMapper(any(), any());
            //ACT
            fundCanadaFDSEndpoint.responseMapper(usFund, usMutualFundHolding);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.topCommonHoldingsMapper(allocation));
        }
    }

    @Test
    void responseMapper_checkResult() throws Exception {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP

            final var fundCanadaFDSEndpoint = mock(CommonHoldingsUsMutualFundEndpoint.class);
            final var usFund = mock(UsFund.class);
            final var holding = mock(UsMutualFundHolding.class);
            final var allocation = mock(Holdings.class);
            final RCommonHoldings actual = mock(RCommonHoldings.class);

            when(usFund.getHoldings()).thenReturn(allocation);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.topCommonHoldingsMapper(any())).thenReturn(actual);

            doCallRealMethod().when(fundCanadaFDSEndpoint).responseMapper(any(), any());
            //ACT
            final RCommonHoldings expected = fundCanadaFDSEndpoint.responseMapper(usFund, holding);

            //VERIFY
            assertSame(expected, actual);
        }
    }

}
