package com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation;

import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.UsMutualFundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.ASSET_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class AssetAllocationUsFundEndpoint
    extends
      UsMutualFundAbstractEndpoint<com.fintex.ce.domain.model.AssetAllocation> {

  public AssetAllocationUsFundEndpoint() {
    super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(ASSET_ALLOCATION, US_MUTUAL_FUNDS));
  }

  @Override
  public QueryQueryDefinition queryDefinition(List<String> tickers,
      UnaryOperator<UsFundQuery> preDefinedFDSQuery) {
    return q -> q.getUsFundsByTickers(tickers, preDefinedFDSQuery::apply);
  }

  @Override
  public UsFundQuery requestMapper(UsFundQuery query) {
    return query
        .assetAllocation(
            al -> al
                .allocation(p -> p.name().value())
                .dataProvider())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public com.fintex.ce.domain.model.AssetAllocation responseMapper(UsFund fund, UsMutualFundHolding holding) {
    AssetAllocation assetAllocation = fund.getAssetAllocation();
    return GraphQlMapperUtils.assetAllocation(assetAllocation, holding.getType());
  }
}
