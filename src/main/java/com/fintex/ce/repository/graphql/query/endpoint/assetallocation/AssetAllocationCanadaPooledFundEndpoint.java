package com.fintex.ce.repository.graphql.query.endpoint.assetallocation;

import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.model.redis.RAssetAllocation;
import com.fintex.ce.repository.graphql.query.endpoint.core.CanadaPooledFundAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_POOLED_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MONTHLY_RETURNS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class AssetAllocationCanadaPooledFundEndpoint extends CanadaPooledFundAbstractEndpoint<RAssetAllocation> {

    public AssetAllocationCanadaPooledFundEndpoint() {
        super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(MONTHLY_RETURNS, CANADA_POOLED_FUNDS));
    }

    @Override
    public QueryQueryDefinition queryDefinition(List<String> morningstarIds,
                                                UnaryOperator<PooledFundQuery> preDefinedFDSQuery) {
        return q -> q.getCanadaPooledFundsByMorningstarIds(morningstarIds, preDefinedFDSQuery::apply);
    }


    @Override
    public PooledFundQuery requestMapper(PooledFundQuery query) {
        return query
                .assetAllocation(
                        al -> al
                                .allocation(p -> p.name().value())
                                .dataProvider()
                )
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RAssetAllocation responseMapper(PooledFund fund, CanadaPooledFundHolding holding) {
        final AssetAllocation assetAllocation = fund.getAssetAllocation();
        return GraphQlMapperUtils.assetAllocation(assetAllocation, holding.getType());
    }
}
