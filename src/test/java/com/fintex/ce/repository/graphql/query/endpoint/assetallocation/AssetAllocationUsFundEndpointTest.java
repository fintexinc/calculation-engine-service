package com.fintex.ce.repository.graphql.query.endpoint.assetallocation;

import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.DataProvider;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RAssetAllocation;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssetAllocationUsFundEndpointTest {

    @Test
    void requestMapper_verify() {
        //SETUP
        final AssetAllocationUsFundEndpoint m = mock(AssetAllocationUsFundEndpoint.class);

        final UsFundQuery usFundQuery = mock(UsFundQuery.class);
        when(usFundQuery.assetAllocation(any())).thenReturn(usFundQuery);
        when(usFundQuery.externalIdentifiers(any())).thenReturn(usFundQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final UsFundQuery actual = m.requestMapper(usFundQuery);

        //VERIFY
        verify(actual).assetAllocation(any());
        verify(actual).externalIdentifiers(any());
    }

    @Test
    void responseMapper_verifyAssetAllocation() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final AssetAllocationUsFundEndpoint m = mock(AssetAllocationUsFundEndpoint.class);

            final UsFund usFund = mock(UsFund.class);
            final AssetAllocation assetAllocation = mock(AssetAllocation.class);
            when(usFund.getAssetAllocation()).thenReturn(assetAllocation);

            final UsMutualFundHolding usMutualFundHolding = mock(UsMutualFundHolding.class);
            final HoldingType cmd = HoldingType.US_MUTUAL_FUNDS;
            when(usMutualFundHolding.getType()).thenReturn(cmd);

            doCallRealMethod().when(m).responseMapper(any(), any());
            //ACT
            m.responseMapper(usFund, usMutualFundHolding);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.assetAllocation(assetAllocation, cmd));
        }
    }

    @Test
    void responseMapper_checkResult() {
        //SETUP
        final AssetAllocationUsFundEndpoint m = mock(AssetAllocationUsFundEndpoint.class);

        final UsFund usFund = mock(UsFund.class);
        final AssetAllocation assetAllocation = mock(AssetAllocation.class);
        when(usFund.getAssetAllocation()).thenReturn(assetAllocation);

        when(assetAllocation.getDataProvider()).thenReturn(DataProvider.EAGLE);

        final UsMutualFundHolding usMutualFundHolding = mock(UsMutualFundHolding.class);
        final HoldingType cmd = HoldingType.US_MUTUAL_FUNDS;
        when(usMutualFundHolding.getType()).thenReturn(cmd);

        final RAssetAllocation expected = new RAssetAllocation(HoldingType.US_MUTUAL_FUNDS, Map.of());
        expected.setProvider(DataProvider.EAGLE.name());
        doCallRealMethod().when(m).responseMapper(any(), any());
        //ACT
        m.responseMapper(usFund, usMutualFundHolding);
        RAssetAllocation actual = GraphQlMapperUtils.assetAllocation(assetAllocation, cmd);
        //VERIFY
        assertEquals(expected, actual);
    }

}
