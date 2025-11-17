package com.fintex.ce.repository.graphql.query.endpoint.assetallocation;

import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.DataProvider;
import com.fintex.smclient.graphql.FundHoldingIdentifier;
import com.fintex.smclient.graphql.FundHoldingIdentifiersCodes;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.RAssetAllocation;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssetAllocationFundCanadaEndpointTest {

    @Test
    void queryDefinition_verify() {
        //SETUP
        final AssetAllocationFundCanadaEndpoint m = mock(AssetAllocationFundCanadaEndpoint.class);

        final QueryQuery qq = mock(QueryQuery.class);

        final FundHoldingIdentifiersCodes codes = mock(FundHoldingIdentifiersCodes.class);
        final String code = "CODE";
        when(codes.getCode()).thenReturn(code);
        final FundHoldingIdentifier cash = FundHoldingIdentifier.FUNDSERV;
        when(codes.getFundholdingIdentifier()).thenReturn(cash);
        final List<FundHoldingIdentifiersCodes> equityIdentifiers = List.of(codes);

        doCallRealMethod().when(m).queryDefinition(any(), any());
        //ACT
        final QueryQueryDefinition actual = m.queryDefinition(equityIdentifiers, mock(UnaryOperator.class));
        actual.define(qq);

        //VERIFY
        verify(qq).getFundSeriesByHoldingCodes(eq(equityIdentifiers), any());
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final AssetAllocationFundCanadaEndpoint m = mock(AssetAllocationFundCanadaEndpoint.class);

        final FundSeriesQuery fundSeriesQuery = mock(FundSeriesQuery.class);
        when(fundSeriesQuery.assetAllocation(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final FundSeriesQuery actual = m.requestMapper(fundSeriesQuery);

        //VERIFY
        verify(actual).assetAllocation(any());
        verify(actual).externalIdentifiers(any());
    }

    @Test
    void responseMapper_verifyAssetAllocation() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final AssetAllocationFundCanadaEndpoint m = mock(AssetAllocationFundCanadaEndpoint.class);

            final FundSeries fund = mock(FundSeries.class);
            final AssetAllocation assetAllocation = mock(AssetAllocation.class);
            when(fund.getAssetAllocation()).thenReturn(assetAllocation);

            final FundSeriesHolding fundSeriesHolding = mock(FundSeriesHolding.class);
            final HoldingType cmd = HoldingType.CANADA_MUTUAL_FUNDS;
            when(fundSeriesHolding.getType()).thenReturn(cmd);

            doCallRealMethod().when(m).responseMapper(any(), any());
            //ACT
            m.responseMapper(fund, fundSeriesHolding);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.assetAllocation(assetAllocation, cmd));
        }
    }

    @Test
    void responseMapper_checkResult() {
        //SETUP
        final AssetAllocationFundCanadaEndpoint m = mock(AssetAllocationFundCanadaEndpoint.class);

        final FundSeries fund = mock(FundSeries.class);
        final AssetAllocation assetAllocation = mock(AssetAllocation.class);
        when(fund.getAssetAllocation()).thenReturn(assetAllocation);
        when(assetAllocation.getDataProvider()).thenReturn(DataProvider.MORNINGSTAR);

        final FundSeriesHolding fundSeriesHolding = mock(FundSeriesHolding.class);
        final HoldingType cmd = HoldingType.CANADA_MUTUAL_FUNDS;
        when(fundSeriesHolding.getType()).thenReturn(cmd);

        final RAssetAllocation expected = new RAssetAllocation(HoldingType.CANADA_MUTUAL_FUNDS, Map.of());
        expected.setProvider(DataProvider.MORNINGSTAR.name());
        doCallRealMethod().when(m).responseMapper(any(), any());
        //ACT
        m.responseMapper(fund, fundSeriesHolding);
        RAssetAllocation actual = GraphQlMapperUtils.assetAllocation(assetAllocation, cmd);
        //VERIFY
        assertEquals(expected, actual);
    }

}