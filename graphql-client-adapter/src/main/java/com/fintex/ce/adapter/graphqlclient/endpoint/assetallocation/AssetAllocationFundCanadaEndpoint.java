package com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation;

import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.FundHoldingIdentifiersCodes;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.FundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.ASSET_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class AssetAllocationFundCanadaEndpoint
    extends
      FundAbstractEndpoint<com.fintex.ce.domain.model.AssetAllocation> {

  public AssetAllocationFundCanadaEndpoint() {
    super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(ASSET_ALLOCATION, CANADA_MUTUAL_FUNDS));
  }

  @Override
  public QueryQueryDefinition queryDefinition(List<FundHoldingIdentifiersCodes> fundHoldingIdentifiersCodes,
      UnaryOperator<FundSeriesQuery> preDefinedFDSQuery) {
    return q -> q.getFundSeriesByHoldingCodes(fundHoldingIdentifiersCodes, preDefinedFDSQuery::apply);
  }

  @Override
  public FundSeriesQuery requestMapper(FundSeriesQuery query) {
    return query
        .assetAllocation(al -> al
            .allocation(p -> p.name().value())
            .dataProvider())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public com.fintex.ce.domain.model.AssetAllocation responseMapper(FundSeries fundSeries, FundSeriesHolding holding) {
    final AssetAllocation assetAllocation = fundSeries.getAssetAllocation();
    return GraphQlMapperUtils.assetAllocation(assetAllocation, holding.getType());
  }
}
