package com.fintex.ce.repository.graphql.query.endpoint.assetallocation;

import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.SeparatelyManagedAccount;
import com.fintex.smclient.graphql.SeparatelyManagedAccountQuery;
import com.fintex.ce.dto.holding.SmaHolding;
import com.fintex.ce.model.redis.RAssetAllocation;
import com.fintex.ce.repository.graphql.query.endpoint.core.SeparatelyManagedAccountAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_SEPARATELY_MANAGED_ACCOUNT_BY;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.SEPARATELY_MANAGED_ACCOUNT;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.ASSET_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class AssetAllocationSeparatelyManagedAccountEndpoint extends SeparatelyManagedAccountAbstractEndpoint<RAssetAllocation> {

    public AssetAllocationSeparatelyManagedAccountEndpoint() {
        super(GET_SEPARATELY_MANAGED_ACCOUNT_BY, List.of(), buildCacheName(ASSET_ALLOCATION, SEPARATELY_MANAGED_ACCOUNT));
    }

    @Override
    public SeparatelyManagedAccountQuery requestMapper(final SeparatelyManagedAccountQuery query) {
        return query
                .assetAllocation(al -> al
                        .allocation(p -> p.name().value())
                        .dataProvider()
                )
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RAssetAllocation responseMapper(final SeparatelyManagedAccount separatelyManagedAccount,
                                           final SmaHolding holding) {
        final AssetAllocation assetAllocation = separatelyManagedAccount.getAssetAllocation();
        return GraphQlMapperUtils.assetAllocation(assetAllocation, holding.getType());
    }

}
