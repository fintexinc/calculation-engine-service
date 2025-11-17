package com.fintex.ce.repository.graphql.query.endpoint.commonholdings;

import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.model.redis.topcommonholdings.RCommonHoldings;
import com.fintex.ce.repository.graphql.query.endpoint.core.CanadaPooledFundAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_POOLED_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.TOP_COMMON_HOLDINGS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class CommonHoldingsCanadaPooledFundEndpoint extends CanadaPooledFundAbstractEndpoint<RCommonHoldings> {

    public CommonHoldingsCanadaPooledFundEndpoint() {
        super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(TOP_COMMON_HOLDINGS, CANADA_POOLED_FUNDS));
    }

    @Override
    public PooledFundQuery requestMapper(PooledFundQuery query) {
        return query
                .holdings(CommonHoldingsEtfCanadaEndpoint.getCommonHoldingsQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RCommonHoldings responseMapper(PooledFund fund, CanadaPooledFundHolding holding) {
        return GraphQlMapperUtils.topCommonHoldingsMapper(fund.getHoldings());
    }

}
