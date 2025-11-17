package com.fintex.ce.repository.graphql.query.endpoint.assetallocation;

import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.model.redis.RAssetAllocation;
import com.fintex.ce.repository.graphql.query.endpoint.core.CanadaHedgeFundAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.ASSET_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class AssetAllocationCanadaHedgeFundEndpoint extends CanadaHedgeFundAbstractEndpoint<RAssetAllocation> {

    public AssetAllocationCanadaHedgeFundEndpoint() {
        super(GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(ASSET_ALLOCATION, CANADA_HEDGE_FUNDS));
    }

    @Override
    public QueryQueryDefinition queryDefinition(List<String> morningstarIds,
                                                UnaryOperator<HedgeFundQuery> preDefinedFDSQuery) {
        return q -> q.getCanadaHedgeFundsByMorningstarIds(morningstarIds, preDefinedFDSQuery::apply);
    }


    @Override
    public HedgeFundQuery requestMapper(HedgeFundQuery query) {
        return query
                .assetAllocation(
                        al -> al
                                .allocation(p -> p.name().value())
                                .dataProvider()
                )
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RAssetAllocation responseMapper(HedgeFund fund, CanadaHedgeFundHolding holding) {
        AssetAllocation assetAllocation = fund.getAssetAllocation();
        return GraphQlMapperUtils.assetAllocation(assetAllocation, holding.getType());
    }

}
