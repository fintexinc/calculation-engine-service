package com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation;

import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.DataProvider;
import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssetAllocationBenchmarkEndpointTest {

  @Test
  void requestMapper_verify() {
    // SETUP
    final AssetAllocationBenchmarkEndpoint m = mock(AssetAllocationBenchmarkEndpoint.class);

    final IndexQuery indexQuery = mock(IndexQuery.class);
    when(indexQuery.assetAllocation(any())).thenReturn(indexQuery);
    when(indexQuery.externalIdentifiers(any())).thenReturn(indexQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final IndexQuery actual = m.requestMapper(indexQuery);

    // VERIFY
    verify(actual).assetAllocation(any());
    verify(actual).externalIdentifiers(any());
  }

  @Test
  void responseMapper_verifyAssetAllocation() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final AssetAllocationBenchmarkEndpoint m = mock(AssetAllocationBenchmarkEndpoint.class);

      final Index index = mock(Index.class);
      final AssetAllocation assetAllocation = mock(AssetAllocation.class);
      when(index.getAssetAllocation()).thenReturn(assetAllocation);

      final BenchmarkIndexHolding benchmarkIndexHolding = mock(BenchmarkIndexHolding.class);
      final HoldingType cmd = HoldingType.BENCHMARK_INDEX;
      when(benchmarkIndexHolding.getType()).thenReturn(cmd);

      doCallRealMethod().when(m).responseMapper(any(), any());
      // ACT
      m.responseMapper(index, benchmarkIndexHolding);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.assetAllocation(assetAllocation, cmd));
    }
  }

  @Test
  void responseMapper_checkResult() {
    // SETUP
    final AssetAllocationBenchmarkEndpoint m = mock(AssetAllocationBenchmarkEndpoint.class);

    final Index index = mock(Index.class);
    final AssetAllocation assetAllocation = mock(AssetAllocation.class);
    when(index.getAssetAllocation()).thenReturn(assetAllocation);

    when(assetAllocation.getDataProvider()).thenReturn(DataProvider.EAGLE);

    final BenchmarkIndexHolding benchmarkIndexHolding = mock(BenchmarkIndexHolding.class);
    final HoldingType cmd = HoldingType.BENCHMARK_INDEX;
    when(benchmarkIndexHolding.getType()).thenReturn(cmd);

    final com.fintex.ce.domain.model.AssetAllocation expected = new com.fintex.ce.domain.model.AssetAllocation(
        HoldingType.BENCHMARK_INDEX, Map.of());
    expected.setProvider(DataProvider.EAGLE.name());
    doCallRealMethod().when(m).responseMapper(any(), any());
    // ACT
    m.responseMapper(index, benchmarkIndexHolding);
    com.fintex.ce.domain.model.AssetAllocation actual = GraphQlMapperUtils.assetAllocation(assetAllocation, cmd);
    // VERIFY
    assertEquals(expected, actual);
  }

}