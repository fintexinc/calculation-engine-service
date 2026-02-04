package com.fintex.ce.adapter.graphqlclient.endpoint.yield;

import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaPooledFundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheNameEntity.YIELD;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class YieldPooledFundEndpoint extends CanadaPooledFundAbstractEndpoint<Yield> {

  public YieldPooledFundEndpoint() {
    super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(YIELD,
        CacheCategory.CANADA_POOLED_FUNDS));
  }

  @Override
  public PooledFundQuery requestMapper(PooledFundQuery query) {
    return query
        .dividendYield(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);

  }

  @Override
  public Yield responseMapper(final PooledFund pooledFund,
      final CanadaPooledFundHolding holding) {
    return GraphQlMapperUtils.mapYield(pooledFund, PooledFund::getDividendYield);
  }

}
