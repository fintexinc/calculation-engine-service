package com.fintex.ce.repository.graphql.query.endpoint.assetallocation;

import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.FundHoldingIdentifiersCodes;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.RAssetAllocation;
import com.fintex.ce.repository.graphql.query.endpoint.core.FundAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_MUTUAL_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.ASSET_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class AssetAllocationFundCanadaEndpoint extends FundAbstractEndpoint<RAssetAllocation> {

    public AssetAllocationFundCanadaEndpoint() {
        super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(ASSET_ALLOCATION, CANADA_MUTUAL_FUNDS));
    }

    @Override
    public QueryQueryDefinition queryDefinition(List<FundHoldingIdentifiersCodes> fundHoldingIdentifiersCodes, UnaryOperator<FundSeriesQuery> preDefinedFDSQuery) {
        return q -> q.getFundSeriesByHoldingCodes(fundHoldingIdentifiersCodes, preDefinedFDSQuery::apply);
    }

    @Override
    public FundSeriesQuery requestMapper(FundSeriesQuery query) {
        return query
                .assetAllocation(al ->
                        al
                                .allocation(p -> p.name().value())
                                .dataProvider()
                )
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RAssetAllocation responseMapper(FundSeries fundSeries, FundSeriesHolding holding) {
        final AssetAllocation assetAllocation = fundSeries.getAssetAllocation();
        return GraphQlMapperUtils.assetAllocation(assetAllocation, holding.getType());
    }
}
