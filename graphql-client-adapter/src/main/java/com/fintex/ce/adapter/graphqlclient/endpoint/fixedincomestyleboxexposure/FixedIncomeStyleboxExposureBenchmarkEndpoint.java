package com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure;

import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;
import com.fintex.smclient.graphql.StyleBoxesQueryDefinition;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.FixedIncomeStyleboxExposure;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.BenchmarkAbstractEndpoint;

import java.util.List;
import java.util.Objects;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_GET_INDEXES_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.BENCHMARK_INDEXES;
import static com.fintex.ce.constant.CacheNameEntity.FIXED_INCOME_STYLEBOX_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class FixedIncomeStyleboxExposureBenchmarkEndpoint
    extends
      BenchmarkAbstractEndpoint<FixedIncomeStyleboxExposure> {

  public FixedIncomeStyleboxExposureBenchmarkEndpoint() {
    super(GET_GET_INDEXES_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(FIXED_INCOME_STYLEBOX_ALLOCATION,
        BENCHMARK_INDEXES));
  }

  @Override
  public IndexQuery requestMapper(final IndexQuery query) {
    return query
        .styleBoxes(getStyleBoxesQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public FixedIncomeStyleboxExposure responseMapper(final Index index, final BenchmarkIndexHolding holding) {
    final var result = new FixedIncomeStyleboxExposure();
    if (Objects.nonNull(index) && Objects.nonNull(index.getStyleBoxes())) {
      return FixedIncomeStyleBoxesEndpointUtil.getREquityStyleboxExposure(
          index.getStyleBoxes(),
          result);
    }
    return result;
  }

  public StyleBoxesQueryDefinition getStyleBoxesQueryDefinition() {

    return qStyleboxes -> {
      qStyleboxes.dataProvider();
      qStyleboxes.asOfDate();
      qStyleboxes.boxValues(
          qBoxValue -> {
            qBoxValue.styleBoxType();
            qBoxValue.value();
          });
    };
  }

}
