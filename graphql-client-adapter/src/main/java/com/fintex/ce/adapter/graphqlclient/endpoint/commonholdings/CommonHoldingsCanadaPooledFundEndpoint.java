package com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings;

import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.CommonHoldings;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaPooledFundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_POOLED_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.TOP_COMMON_HOLDINGS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class CommonHoldingsCanadaPooledFundEndpoint extends CanadaPooledFundAbstractEndpoint<CommonHoldings> {

  public CommonHoldingsCanadaPooledFundEndpoint() {
    super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(TOP_COMMON_HOLDINGS,
        CANADA_POOLED_FUNDS));
  }

  @Override
  public PooledFundQuery requestMapper(PooledFundQuery query) {
    return query
        .holdings(CommonHoldingsEtfCanadaEndpoint.getCommonHoldingsQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public CommonHoldings responseMapper(PooledFund fund, CanadaPooledFundHolding holding) {
    return GraphQlMapperUtils.topCommonHoldingsMapper(fund.getHoldings());
  }

}
