package com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization;

import com.fintex.smclient.graphql.EquityMarketCapitalizationQueryDefinition;
import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.EquityMarketCapitalization;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.BenchmarkAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_GET_INDEXES_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.BENCHMARK_INDEXES;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_MARKET_CAPITALIZATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquityMarketCapitalizationBenchmarkEndpoint extends BenchmarkAbstractEndpoint<EquityMarketCapitalization> {

  public EquityMarketCapitalizationBenchmarkEndpoint() {
    super(GET_GET_INDEXES_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(EQUITY_MARKET_CAPITALIZATION,
        BENCHMARK_INDEXES));
  }

  public static EquityMarketCapitalizationQueryDefinition getEquityMarketCapitalizationQueryDefinition() {
    return qE -> qE
        .values(qV -> qV
            .equityMarketCapitalization()
            .value())
        .dataProvider();
  }

  @Override
  public IndexQuery requestMapper(final IndexQuery query) {
    return query
        .equityMarketCapitalization(getEquityMarketCapitalizationQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public EquityMarketCapitalization responseMapper(final Index index, final BenchmarkIndexHolding holding) {
    return GraphQlMapperUtils.equityMarketCapitalizationMapper(index.getEquityMarketCapitalization());
  }

}
