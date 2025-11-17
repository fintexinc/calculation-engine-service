package com.fintex.ce.repository.graphql.query.endpoint.assetallocation;

import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FixedIncomeQuery;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.model.redis.RAssetAllocation;
import com.fintex.ce.repository.graphql.query.endpoint.core.FixedIncomeAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_FIXED_INCOME_BY_ADP_NUMBERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.FIXED_INCOME;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.ASSET_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class AssetAllocationFixedIncomeEndpoint extends FixedIncomeAbstractEndpoint<RAssetAllocation> {

    public AssetAllocationFixedIncomeEndpoint() {
        super(GET_FIXED_INCOME_BY_ADP_NUMBERS, List.of(), buildCacheName(ASSET_ALLOCATION, FIXED_INCOME));
    }

    @Override
    public FixedIncomeQuery requestMapper(final FixedIncomeQuery query) {
        return query
                .assetAllocation(al ->
                        al
                                .allocation(p -> p.name().value())
                                .dataProvider()
                )
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RAssetAllocation responseMapper(final FixedIncome fixedIncome,
                                           final FixedIncomeHolding holding) {
        final AssetAllocation assetAllocation = fixedIncome.getAssetAllocation();
        return GraphQlMapperUtils.assetAllocation(assetAllocation, holding.getType());
    }

}
