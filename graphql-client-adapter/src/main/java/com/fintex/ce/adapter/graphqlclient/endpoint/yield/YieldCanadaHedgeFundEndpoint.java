package com.fintex.ce.adapter.graphqlclient.endpoint.yield;

import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaHedgeFundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.YIELD;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class YieldCanadaHedgeFundEndpoint extends CanadaHedgeFundAbstractEndpoint<Yield> {

  public YieldCanadaHedgeFundEndpoint() {
    super(GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(YIELD, CANADA_HEDGE_FUNDS));
  }

  @Override
  public HedgeFundQuery requestMapper(HedgeFundQuery query) {
    return query
        .dividendYield(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public Yield responseMapper(final HedgeFund hedgeFund,
      final CanadaHedgeFundHolding holding) {
    return GraphQlMapperUtils.mapYield(hedgeFund, HedgeFund::getDividendYield);
  }
}
