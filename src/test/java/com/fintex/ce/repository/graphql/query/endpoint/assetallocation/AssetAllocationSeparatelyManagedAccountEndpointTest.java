package com.fintex.ce.repository.graphql.query.endpoint.assetallocation;

import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.DataProvider;
import com.fintex.smclient.graphql.SeparatelyManagedAccount;
import com.fintex.smclient.graphql.SeparatelyManagedAccountQuery;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.holding.SmaHolding;
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

class AssetAllocationSeparatelyManagedAccountEndpointTest {

    @Test
    void requestMapper_verify() {
        //SETUP
        final AssetAllocationSeparatelyManagedAccountEndpoint m = mock(AssetAllocationSeparatelyManagedAccountEndpoint.class);

        final SeparatelyManagedAccountQuery separatelyManagedAccountQuery = mock(SeparatelyManagedAccountQuery.class);
        when(separatelyManagedAccountQuery.assetAllocation(any())).thenReturn(separatelyManagedAccountQuery);
        when(separatelyManagedAccountQuery.externalIdentifiers(any())).thenReturn(separatelyManagedAccountQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final SeparatelyManagedAccountQuery actual = m.requestMapper(separatelyManagedAccountQuery);

        //VERIFY
        verify(actual).assetAllocation(any());
        verify(actual).externalIdentifiers(any());
    }

    @Test
    void responseMapper_verifyAssetAllocation() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final AssetAllocationSeparatelyManagedAccountEndpoint m = mock(AssetAllocationSeparatelyManagedAccountEndpoint.class);

            final SeparatelyManagedAccount separatelyManagedAccount = mock(SeparatelyManagedAccount.class);
            final AssetAllocation assetAllocation = mock(AssetAllocation.class);
            when(separatelyManagedAccount.getAssetAllocation()).thenReturn(assetAllocation);

            final SmaHolding smaHolding = mock(SmaHolding.class);
            final HoldingType cmd = HoldingType.SEPARATELY_MANAGED_ACCOUNT;
            when(smaHolding.getType()).thenReturn(cmd);

            doCallRealMethod().when(m).responseMapper(any(), any());

            //ACT
            m.responseMapper(separatelyManagedAccount, smaHolding);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.assetAllocation(assetAllocation, cmd));
        }
    }

    @Test
    void responseMapper_checkResult() {
        //SETUP
        final AssetAllocationSeparatelyManagedAccountEndpoint m = mock(AssetAllocationSeparatelyManagedAccountEndpoint.class);

        final SeparatelyManagedAccount separatelyManagedAccount = mock(SeparatelyManagedAccount.class);
        final AssetAllocation assetAllocation = mock(AssetAllocation.class);
        when(separatelyManagedAccount.getAssetAllocation()).thenReturn(assetAllocation);

        when(assetAllocation.getDataProvider()).thenReturn(DataProvider.ENVESTNET);

        final SmaHolding smaHolding = mock(SmaHolding.class);
        final HoldingType cmd = HoldingType.SEPARATELY_MANAGED_ACCOUNT;
        when(smaHolding.getType()).thenReturn(cmd);

        final RAssetAllocation expected = new RAssetAllocation(HoldingType.SEPARATELY_MANAGED_ACCOUNT, Map.of());
        expected.setProvider(DataProvider.ENVESTNET.name());
        doCallRealMethod().when(m).responseMapper(any(), any());
        //ACT
        m.responseMapper(separatelyManagedAccount, smaHolding);
        RAssetAllocation actual = GraphQlMapperUtils.assetAllocation(assetAllocation, cmd);
        //VERIFY
        assertEquals(expected, actual);
    }

}
