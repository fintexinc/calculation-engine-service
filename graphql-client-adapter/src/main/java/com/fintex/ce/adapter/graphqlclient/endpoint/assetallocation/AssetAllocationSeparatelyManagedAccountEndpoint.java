package com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation;

import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.SeparatelyManagedAccount;
import com.fintex.smclient.graphql.SeparatelyManagedAccountQuery;
import com.fintex.ce.domain.model.holding.SmaHolding;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.SeparatelyManagedAccountAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_SEPARATELY_MANAGED_ACCOUNT_BY;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.SEPARATELY_MANAGED_ACCOUNT;
import static com.fintex.ce.constant.CacheNameEntity.ASSET_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class AssetAllocationSeparatelyManagedAccountEndpoint
    extends
      SeparatelyManagedAccountAbstractEndpoint<com.fintex.ce.domain.model.AssetAllocation> {

  public AssetAllocationSeparatelyManagedAccountEndpoint() {
    super(GET_SEPARATELY_MANAGED_ACCOUNT_BY, List.of(), buildCacheName(ASSET_ALLOCATION, SEPARATELY_MANAGED_ACCOUNT));
  }

  @Override
  public SeparatelyManagedAccountQuery requestMapper(final SeparatelyManagedAccountQuery query) {
    return query
        .assetAllocation(al -> al
            .allocation(p -> p.name().value())
            .dataProvider())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public com.fintex.ce.domain.model.AssetAllocation responseMapper(
      final SeparatelyManagedAccount separatelyManagedAccount,
      final SmaHolding holding) {
    final AssetAllocation assetAllocation = separatelyManagedAccount.getAssetAllocation();
    return GraphQlMapperUtils.assetAllocation(assetAllocation, holding.getType());
  }

}
