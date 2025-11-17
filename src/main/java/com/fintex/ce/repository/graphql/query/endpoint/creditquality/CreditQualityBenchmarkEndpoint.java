package com.fintex.ce.repository.graphql.query.endpoint.creditquality;

import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.model.redis.RCreditQuality;
import com.fintex.ce.repository.graphql.query.endpoint.core.BenchmarkAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_GET_INDEXES_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.BENCHMARK_INDEXES;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.CREDIT_QUALITY;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphQlMapperUtils.creditQualityMapper;

public class CreditQualityBenchmarkEndpoint extends BenchmarkAbstractEndpoint<RCreditQuality> {

    public CreditQualityBenchmarkEndpoint() {
        super(GET_GET_INDEXES_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(CREDIT_QUALITY, BENCHMARK_INDEXES));
    }

    @Override
    public IndexQuery requestMapper(final IndexQuery query) {
        return query
                .creditQualityRatings(
                        CreditQualityFundCanadaEndpoint.getCreditQualityRatingsQueryDefinition()
                )
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RCreditQuality responseMapper(final Index index, final BenchmarkIndexHolding holding) {
        final Map<String, BigDecimal> allocations = creditQualityMapper(index.getCreditQualityRatings());
        return new RCreditQuality(holding.getType(), allocations);
    }

}
