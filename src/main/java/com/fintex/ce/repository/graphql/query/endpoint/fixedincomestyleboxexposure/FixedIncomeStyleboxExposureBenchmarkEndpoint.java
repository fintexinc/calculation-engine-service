package com.fintex.ce.repository.graphql.query.endpoint.fixedincomestyleboxexposure;

import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;
import com.fintex.smclient.graphql.StyleBoxesQueryDefinition;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.model.redis.RFixedIncomeStyleboxExposure;
import com.fintex.ce.repository.graphql.query.endpoint.core.BenchmarkAbstractEndpoint;

import java.util.List;
import java.util.Objects;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_GET_INDEXES_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.BENCHMARK_INDEXES;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.FIXED_INCOME_STYLEBOX_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class FixedIncomeStyleboxExposureBenchmarkEndpoint extends BenchmarkAbstractEndpoint<RFixedIncomeStyleboxExposure> {

    public FixedIncomeStyleboxExposureBenchmarkEndpoint() {
        super(GET_GET_INDEXES_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(FIXED_INCOME_STYLEBOX_ALLOCATION, BENCHMARK_INDEXES));
    }

    @Override
    public IndexQuery requestMapper(final IndexQuery query) {
        return query
                .styleBoxes(getStyleBoxesQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RFixedIncomeStyleboxExposure responseMapper(final Index index, final BenchmarkIndexHolding holding) {
        final var result = new RFixedIncomeStyleboxExposure();
        if (Objects.nonNull(index) && Objects.nonNull(index.getStyleBoxes())) {
            return FixedIncomeStyleBoxesEndpointUtil.getREquityStyleboxExposure(
                    index.getStyleBoxes(),
                    result
            );
        }
        return result;
    }

    public StyleBoxesQueryDefinition getStyleBoxesQueryDefinition() {

        return qStyleboxes -> {
            qStyleboxes.dataProvider();
            qStyleboxes.asOfDate();
            qStyleboxes.boxValues(
                    qBoxValue -> {
                        qBoxValue.styleBoxType();
                        qBoxValue.value();
                    });
        };
    }

}
