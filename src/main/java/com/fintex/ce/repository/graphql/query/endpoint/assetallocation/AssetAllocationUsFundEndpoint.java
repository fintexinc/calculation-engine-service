package com.fintex.ce.repository.graphql.query.endpoint.assetallocation;

import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RAssetAllocation;
import com.fintex.ce.repository.graphql.query.endpoint.core.UsMutualFundAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.ASSET_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class AssetAllocationUsFundEndpoint extends UsMutualFundAbstractEndpoint<RAssetAllocation> {

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
                                .dataProvider()
                )
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RAssetAllocation responseMapper(UsFund fund, UsMutualFundHolding holding) {
        AssetAllocation assetAllocation = fund.getAssetAllocation();
        return GraphQlMapperUtils.assetAllocation(assetAllocation, holding.getType());
    }
}
