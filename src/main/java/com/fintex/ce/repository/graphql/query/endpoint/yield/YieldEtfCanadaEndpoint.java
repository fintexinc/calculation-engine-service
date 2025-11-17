package com.fintex.ce.repository.graphql.query.endpoint.yield;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.RYield;
import com.fintex.ce.repository.graphql.query.endpoint.core.EtfAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;
import java.util.function.Function;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_ETF;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.YIELD;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class YieldEtfCanadaEndpoint extends EtfAbstractEndpoint<RYield> {

    public YieldEtfCanadaEndpoint() {
        super(GET_CANADA_ETFS_BY_TICKERS, List.of(), buildCacheName(YIELD, CANADA_ETF));
    }

    public YieldEtfCanadaEndpoint(final Function<Query, List<Etf>> getFDSEntityFunction,
                                  final List<DataProvider> supportedProviders,
                                  final String endpointName) {
        super(getFDSEntityFunction, supportedProviders, endpointName);
    }

    @Override
    public EtfQuery requestMapper(final EtfQuery query) {
        return query
                .currentYield(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION)
                .ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
    }

    @Override
    public RYield responseMapper(final Etf etf,
                                 final EtfHolding holding) {
        return GraphQlMapperUtils.mapYield(etf, Etf::getCurrentYield);
    }

}
