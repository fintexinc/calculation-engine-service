package com.fintex.ce.repository.graphql.query.endpoint.yield;

import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.model.redis.RYield;
import com.fintex.ce.repository.graphql.query.endpoint.core.CanadaHedgeFundAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.YIELD;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class YieldCanadaHedgeFundEndpoint extends CanadaHedgeFundAbstractEndpoint<RYield> {

    public YieldCanadaHedgeFundEndpoint() {
        super(GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(YIELD, CANADA_HEDGE_FUNDS));
    }

    @Override
    public HedgeFundQuery requestMapper(HedgeFundQuery query) {
        return query
                .dividendYield(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RYield responseMapper(final HedgeFund hedgeFund,
                                 final CanadaHedgeFundHolding holding) {
        return GraphQlMapperUtils.mapYield(hedgeFund, HedgeFund::getDividendYield);
    }
}
