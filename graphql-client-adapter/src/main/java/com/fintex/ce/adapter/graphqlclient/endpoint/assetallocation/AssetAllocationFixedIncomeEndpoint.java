package com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation;

import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FixedIncomeQuery;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.FixedIncomeAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_FIXED_INCOME_BY_ADP_NUMBERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.FIXED_INCOME;
import static com.fintex.ce.constant.CacheNameEntity.ASSET_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class AssetAllocationFixedIncomeEndpoint
    extends
      FixedIncomeAbstractEndpoint<com.fintex.ce.domain.model.AssetAllocation> {

  public AssetAllocationFixedIncomeEndpoint() {
    super(GET_FIXED_INCOME_BY_ADP_NUMBERS, List.of(), buildCacheName(ASSET_ALLOCATION, FIXED_INCOME));
  }

  @Override
  public FixedIncomeQuery requestMapper(final FixedIncomeQuery query) {
    return query
        .assetAllocation(al -> al
            .allocation(p -> p.name().value())
            .dataProvider())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public com.fintex.ce.domain.model.AssetAllocation responseMapper(final FixedIncome fixedIncome,
      final FixedIncomeHolding holding) {
    final AssetAllocation assetAllocation = fixedIncome.getAssetAllocation();
    return GraphQlMapperUtils.assetAllocation(assetAllocation, holding.getType());
  }

}
