package com.fintex.ce.repository.graphql.query.endpoint.commonholdings;

import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.topcommonholdings.RCommonHoldings;
import com.fintex.ce.repository.graphql.query.endpoint.core.UsMutualFundAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.TOP_COMMON_HOLDINGS;
import static com.fintex.ce.repository.graphql.query.endpoint.commonholdings.CommonHoldingsEtfCanadaEndpoint.getCommonHoldingsQueryDefinition;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class CommonHoldingsUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<RCommonHoldings> {

    public CommonHoldingsUsMutualFundEndpoint() {
        super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(TOP_COMMON_HOLDINGS, US_MUTUAL_FUNDS));
    }

    @Override
    public UsFundQuery requestMapper(final UsFundQuery query) {
        return query
                .holdings(getCommonHoldingsQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RCommonHoldings responseMapper(final UsFund fund, final UsMutualFundHolding holding) {
        return GraphQlMapperUtils.topCommonHoldingsMapper(fund.getHoldings());
    }

}
