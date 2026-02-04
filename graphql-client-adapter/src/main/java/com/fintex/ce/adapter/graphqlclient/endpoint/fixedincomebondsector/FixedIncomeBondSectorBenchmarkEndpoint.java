package com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector;

import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.FixedIncomeBondSecurities;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.BenchmarkAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.smclient.graphql.FixedIncomeSecuritiesAllocation;
import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_GET_INDEXES_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.BENCHMARK_INDEXES;
import static com.fintex.ce.constant.CacheNameEntity.FIXED_INCOME_BOND_SECURITIES;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class FixedIncomeBondSectorBenchmarkEndpoint extends BenchmarkAbstractEndpoint<FixedIncomeBondSecurities> {

  public FixedIncomeBondSectorBenchmarkEndpoint() {
    super(GET_GET_INDEXES_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(FIXED_INCOME_BOND_SECURITIES,
        BENCHMARK_INDEXES));
  }

  @Override
  public IndexQuery requestMapper(final IndexQuery query) {
    return query
        .fixedIncomeSecuritiesAllocation(sa -> sa.allocation(a -> a.name().value()).dataProvider())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public FixedIncomeBondSecurities responseMapper(final Index index, final BenchmarkIndexHolding holding) {
    final FixedIncomeSecuritiesAllocation fixedIncomeSecuritiesAllocation = index.getFixedIncomeSecuritiesAllocation();
    return GraphQlMapperUtils.fixedIncomeBondSectorMapper(fixedIncomeSecuritiesAllocation, holding.getType());
  }

}
