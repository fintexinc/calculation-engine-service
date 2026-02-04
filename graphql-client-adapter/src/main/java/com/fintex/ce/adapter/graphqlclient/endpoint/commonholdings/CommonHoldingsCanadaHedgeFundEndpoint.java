package com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings;

import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.CommonHoldings;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaHedgeFundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.TOP_COMMON_HOLDINGS;
import static com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsEtfCanadaEndpoint.getCommonHoldingsQueryDefinition;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class CommonHoldingsCanadaHedgeFundEndpoint extends CanadaHedgeFundAbstractEndpoint<CommonHoldings> {

  public CommonHoldingsCanadaHedgeFundEndpoint() {
    super(GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(TOP_COMMON_HOLDINGS,
        CANADA_HEDGE_FUNDS));
  }

  @Override
  public HedgeFundQuery requestMapper(HedgeFundQuery query) {
    return query
        .holdings(getCommonHoldingsQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public CommonHoldings responseMapper(HedgeFund fund, CanadaHedgeFundHolding holding) {
    return GraphQlMapperUtils.topCommonHoldingsMapper(fund.getHoldings());
  }

}
