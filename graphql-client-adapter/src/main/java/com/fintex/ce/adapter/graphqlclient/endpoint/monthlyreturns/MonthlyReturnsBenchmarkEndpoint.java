package com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns;

import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.MonthlyReturns;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.BenchmarkAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_GET_INDEXES_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.BENCHMARK_INDEXES;
import static com.fintex.ce.constant.CacheNameEntity.MONTHLY_RETURNS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MonthlyReturnsBenchmarkEndpoint extends BenchmarkAbstractEndpoint<MonthlyReturns> {

  public MonthlyReturnsBenchmarkEndpoint() {
    super(GET_GET_INDEXES_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(MONTHLY_RETURNS, BENCHMARK_INDEXES));
  }

  @Override
  public IndexQuery requestMapper(final IndexQuery query) {
    return query
        .currency(c -> c.type().dataProvider())
        .monthlyReturns(
            qMonthly -> qMonthly.returns(
                qReturns -> qReturns.date().value()).dataProvider())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public MonthlyReturns responseMapper(final Index index, final BenchmarkIndexHolding holding) {
    if (index.getCurrency() == null || index.getCurrency().getType() == null) {
      return GraphQlMapperUtils.monthlyReturns(index.getMonthlyReturns(), null, holding);
    }
    final CurrencyType currency = index.getCurrency().getType();
    return GraphQlMapperUtils.monthlyReturns(index.getMonthlyReturns(), currency.name(), holding);
  }
}
