package com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation;

import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.DataProvider;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssetAllocationCanadaPooledFundEndpointTest {

  @Test
  void requestMapper_verify() {
    // SETUP
    final AssetAllocationCanadaPooledFundEndpoint m = mock(AssetAllocationCanadaPooledFundEndpoint.class);

    final PooledFundQuery pooledFundQuery = mock(PooledFundQuery.class);
    when(pooledFundQuery.assetAllocation(any())).thenReturn(pooledFundQuery);
    when(pooledFundQuery.externalIdentifiers(any())).thenReturn(pooledFundQuery);

    doCallRealMethod().when(m).requestMapper(any());

    // ACT
    final PooledFundQuery actual = m.requestMapper(pooledFundQuery);

    // VERIFY
    verify(actual).assetAllocation(any());
    verify(actual).externalIdentifiers(any());
  }

  @Test
  void responseMapper_verifyAssetAllocation() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final AssetAllocationCanadaPooledFundEndpoint m = mock(AssetAllocationCanadaPooledFundEndpoint.class);

      final PooledFund pooledFund = mock(PooledFund.class);
      final AssetAllocation assetAllocation = mock(AssetAllocation.class);
      when(pooledFund.getAssetAllocation()).thenReturn(assetAllocation);

      final CanadaPooledFundHolding canadaPooledFundHolding = mock(CanadaPooledFundHolding.class);
      final HoldingType cmd = HoldingType.CANADA_POOLED_FUNDS;
      when(canadaPooledFundHolding.getType()).thenReturn(cmd);

      doCallRealMethod().when(m).responseMapper(any(), any());

      // ACT
      m.responseMapper(pooledFund, canadaPooledFundHolding);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.assetAllocation(assetAllocation, cmd));
    }
  }

  @Test
  void responseMapper_checkResult() {
    // SETUP
    final AssetAllocationCanadaPooledFundEndpoint m = mock(AssetAllocationCanadaPooledFundEndpoint.class);

    final PooledFund pooledFund = mock(PooledFund.class);
    final AssetAllocation assetAllocation = mock(AssetAllocation.class);
    when(pooledFund.getAssetAllocation()).thenReturn(assetAllocation);

    when(assetAllocation.getDataProvider()).thenReturn(DataProvider.EAGLE);

    final CanadaPooledFundHolding canadaPooledFundHolding = mock(CanadaPooledFundHolding.class);
    final HoldingType cmd = HoldingType.CANADA_POOLED_FUNDS;
    when(canadaPooledFundHolding.getType()).thenReturn(cmd);

    final com.fintex.ce.domain.model.AssetAllocation expected = new com.fintex.ce.domain.model.AssetAllocation(
        HoldingType.CANADA_POOLED_FUNDS, Map.of());
    expected.setProvider(DataProvider.EAGLE.name());
    doCallRealMethod().when(m).responseMapper(any(), any());

    // ACT
    m.responseMapper(pooledFund, canadaPooledFundHolding);
    com.fintex.ce.domain.model.AssetAllocation actual = GraphQlMapperUtils.assetAllocation(assetAllocation, cmd);

    // VERIFY
    assertEquals(expected, actual);
  }

}
