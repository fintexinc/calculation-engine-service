package com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation;

import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.DataProvider;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssetAllocationCanadaHedgeFundEndpointTest {

  @Test
  void requestMapper_verify() {
    // SETUP
    final AssetAllocationCanadaHedgeFundEndpoint m = mock(AssetAllocationCanadaHedgeFundEndpoint.class);

    final HedgeFundQuery hedgeFundQuery = mock(HedgeFundQuery.class);
    when(hedgeFundQuery.assetAllocation(any())).thenReturn(hedgeFundQuery);
    when(hedgeFundQuery.externalIdentifiers(any())).thenReturn(hedgeFundQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final HedgeFundQuery actual = m.requestMapper(hedgeFundQuery);

    // VERIFY
    verify(actual).assetAllocation(any());
    verify(actual).externalIdentifiers(any());
  }

  @Test
  void responseMapper_verifyAssetAllocation() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final AssetAllocationCanadaHedgeFundEndpoint m = mock(AssetAllocationCanadaHedgeFundEndpoint.class);

      final HedgeFund hedgeFund = mock(HedgeFund.class);
      final AssetAllocation assetAllocation = mock(AssetAllocation.class);
      when(hedgeFund.getAssetAllocation()).thenReturn(assetAllocation);

      final CanadaHedgeFundHolding hedgeFundHolding = mock(CanadaHedgeFundHolding.class);
      final HoldingType cmd = HoldingType.CANADA_HEDGE_FUNDS;
      when(hedgeFundHolding.getType()).thenReturn(cmd);

      doCallRealMethod().when(m).responseMapper(any(), any());
      // ACT
      m.responseMapper(hedgeFund, hedgeFundHolding);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.assetAllocation(assetAllocation, cmd));
    }
  }

  @Test
  void responseMapper_checkResult() {
    // SETUP
    final AssetAllocationCanadaHedgeFundEndpoint m = mock(AssetAllocationCanadaHedgeFundEndpoint.class);

    final HedgeFund hedgeFund = mock(HedgeFund.class);
    final AssetAllocation assetAllocation = mock(AssetAllocation.class);
    when(hedgeFund.getAssetAllocation()).thenReturn(assetAllocation);

    when(assetAllocation.getDataProvider()).thenReturn(DataProvider.EAGLE);

    final CanadaHedgeFundHolding canadaHedgeFundHolding = mock(CanadaHedgeFundHolding.class);
    final HoldingType cmd = HoldingType.CANADA_HEDGE_FUNDS;
    when(canadaHedgeFundHolding.getType()).thenReturn(cmd);

    final com.fintex.ce.domain.model.AssetAllocation expected = new com.fintex.ce.domain.model.AssetAllocation(
        HoldingType.CANADA_HEDGE_FUNDS, Map.of());
    expected.setProvider(DataProvider.EAGLE.name());
    doCallRealMethod().when(m).responseMapper(any(), any());
    // ACT
    m.responseMapper(hedgeFund, canadaHedgeFundHolding);
    com.fintex.ce.domain.model.AssetAllocation actual = GraphQlMapperUtils.assetAllocation(assetAllocation, cmd);
    // VERIFY
    assertEquals(expected, actual);
  }

}
