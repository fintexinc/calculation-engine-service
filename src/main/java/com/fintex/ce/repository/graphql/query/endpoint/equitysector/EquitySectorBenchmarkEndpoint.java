package com.fintex.ce.repository.graphql.query.endpoint.equitysector;

import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.model.redis.equitysector.REquitySector;
import com.fintex.ce.repository.graphql.query.endpoint.core.BenchmarkAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_GET_INDEXES_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.BENCHMARK_INDEXES;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_SECTOR;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquitySectorBenchmarkEndpoint extends BenchmarkAbstractEndpoint<REquitySector> {

    public EquitySectorBenchmarkEndpoint() {
        super(GET_GET_INDEXES_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(EQUITY_SECTOR, BENCHMARK_INDEXES));
    }

    @Override
    public IndexQuery requestMapper(final IndexQuery query) {
        return query
                .equitySectorAllocation(EquitySectorEtfCanadaEndpoint.equitySectorAllocationQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public REquitySector responseMapper(final Index index, final BenchmarkIndexHolding holding) {
        return GraphQlMapperUtils.equitySectorMapper(index.getEquitySectorAllocation());
    }

}
