package com.fintex.ce.repository.graphql.query.endpoint.equitymarketcapitalization;

import com.fintex.smclient.graphql.EquityMarketCapitalizationQueryDefinition;
import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.model.redis.equitymarketcapitalization.REquityMarketCapitalization;
import com.fintex.ce.repository.graphql.query.endpoint.core.BenchmarkAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_GET_INDEXES_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.BENCHMARK_INDEXES;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_MARKET_CAPITALIZATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquityMarketCapitalizationBenchmarkEndpoint extends BenchmarkAbstractEndpoint<REquityMarketCapitalization> {

    public EquityMarketCapitalizationBenchmarkEndpoint() {
        super(GET_GET_INDEXES_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(EQUITY_MARKET_CAPITALIZATION, BENCHMARK_INDEXES));
    }

    static EquityMarketCapitalizationQueryDefinition getEquityMarketCapitalizationQueryDefinition() {
        return qE -> qE
                .values(qV -> qV
                        .equityMarketCapitalization()
                        .value()
                )
                .dataProvider();
    }

    @Override
    public IndexQuery requestMapper(final IndexQuery query) {
        return query
                .equityMarketCapitalization(getEquityMarketCapitalizationQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public REquityMarketCapitalization responseMapper(final Index index, final BenchmarkIndexHolding holding) {
        return GraphQlMapperUtils.equityMarketCapitalizationMapper(index.getEquityMarketCapitalization());
    }

}
