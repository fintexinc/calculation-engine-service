package com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation;

import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.BenchmarkAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_GET_INDEXES_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.BENCHMARK_INDEXES;
import static com.fintex.ce.constant.CacheNameEntity.ASSET_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class AssetAllocationBenchmarkEndpoint
    extends
      BenchmarkAbstractEndpoint<com.fintex.ce.domain.model.AssetAllocation> {

  public AssetAllocationBenchmarkEndpoint() {
    super(GET_GET_INDEXES_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(ASSET_ALLOCATION, BENCHMARK_INDEXES));
  }

  @Override
  public IndexQuery requestMapper(final IndexQuery query) {
    return query
        .assetAllocation(al -> al
            .allocation(p -> p.name().value())
            .dataProvider())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public com.fintex.ce.domain.model.AssetAllocation responseMapper(final Index index,
      final BenchmarkIndexHolding holding) {
    final AssetAllocation assetAllocation = index.getAssetAllocation();
    return GraphQlMapperUtils.assetAllocation(assetAllocation, holding.getType());
  }

}
