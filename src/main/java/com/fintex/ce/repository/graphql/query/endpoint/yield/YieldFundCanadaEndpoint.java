package com.fintex.ce.repository.graphql.query.endpoint.yield;

import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.ce.config.enumeration.cache.CacheCategory;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.RYield;
import com.fintex.ce.repository.graphql.query.endpoint.core.FundAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.YIELD;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class YieldFundCanadaEndpoint extends FundAbstractEndpoint<RYield> {

    public YieldFundCanadaEndpoint() {
        super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(YIELD, CacheCategory.CANADA_MUTUAL_FUNDS));
    }

    @Override
    public FundSeriesQuery requestMapper(final FundSeriesQuery query) {
        return query
                .dividendYield(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RYield responseMapper(final FundSeries fundSeries,
                                 final FundSeriesHolding holding) {
        return GraphQlMapperUtils.mapYield(fundSeries, FundSeries::getDividendYield);
    }

}
