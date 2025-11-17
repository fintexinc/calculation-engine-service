package com.fintex.ce.repository.graphql.query.endpoint.equitystyleboxexposure;

import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.model.redis.REquityStyleboxExposure;
import com.fintex.ce.repository.graphql.query.endpoint.core.BenchmarkAbstractEndpoint;

import java.util.List;
import java.util.Objects;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_GET_INDEXES_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.BENCHMARK_INDEXES;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_STYLEBOX_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquityStyleboxExposureBenchmarkEndpoint extends BenchmarkAbstractEndpoint<REquityStyleboxExposure> {

    public EquityStyleboxExposureBenchmarkEndpoint() {
        super(GET_GET_INDEXES_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(EQUITY_STYLEBOX_ALLOCATION, BENCHMARK_INDEXES));
    }

    @Override
    public IndexQuery requestMapper(final IndexQuery query) {
        return query
                .styleBoxes(EquityStyleboxExposureEndpointUtil.getStyleBoxesQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public REquityStyleboxExposure responseMapper(final Index index, final BenchmarkIndexHolding holding) {
        final var result = new REquityStyleboxExposure();
        if (Objects.nonNull(index) && Objects.nonNull(index.getStyleBoxes())) {
            return EquityStyleboxExposureEndpointUtil.getREquityStyleboxExposure(
                    index.getStyleBoxes(),
                    result
            );
        }
        return result;
    }
}
