package com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings;

import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.CommonHoldings;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.UsMutualFundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.TOP_COMMON_HOLDINGS;
import static com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsEtfCanadaEndpoint.getCommonHoldingsQueryDefinition;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class CommonHoldingsUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<CommonHoldings> {

  public CommonHoldingsUsMutualFundEndpoint() {
    super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(TOP_COMMON_HOLDINGS, US_MUTUAL_FUNDS));
  }

  @Override
  public UsFundQuery requestMapper(final UsFundQuery query) {
    return query
        .holdings(getCommonHoldingsQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public CommonHoldings responseMapper(final UsFund fund, final UsMutualFundHolding holding) {
    return GraphQlMapperUtils.topCommonHoldingsMapper(fund.getHoldings());
  }

}
