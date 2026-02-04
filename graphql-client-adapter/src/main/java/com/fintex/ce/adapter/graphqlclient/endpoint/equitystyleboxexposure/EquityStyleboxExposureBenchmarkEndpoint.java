package com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure;

import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.BenchmarkAbstractEndpoint;

import java.util.List;
import java.util.Objects;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_GET_INDEXES_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.BENCHMARK_INDEXES;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_STYLEBOX_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquityStyleboxExposureBenchmarkEndpoint extends BenchmarkAbstractEndpoint<EquityStyleboxExposure> {

  public EquityStyleboxExposureBenchmarkEndpoint() {
    super(GET_GET_INDEXES_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(EQUITY_STYLEBOX_ALLOCATION, BENCHMARK_INDEXES));
  }

  @Override
  public IndexQuery requestMapper(final IndexQuery query) {
    return query
        .styleBoxes(EquityStyleboxExposureEndpointUtil.getStyleBoxesQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public EquityStyleboxExposure responseMapper(final Index index, final BenchmarkIndexHolding holding) {
    final var result = new EquityStyleboxExposure();
    if (Objects.nonNull(index) && Objects.nonNull(index.getStyleBoxes())) {
      return EquityStyleboxExposureEndpointUtil.getEquityStyleboxExposure(
          index.getStyleBoxes(),
          result);
    }
    return result;
  }
}
