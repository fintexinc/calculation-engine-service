package com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector;

import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.model.redis.RFixedIncomeBondSecurities;
import com.fintex.ce.repository.graphql.query.endpoint.core.BenchmarkAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.FixedIncomeSecuritiesAllocation;
import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_GET_INDEXES_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.BENCHMARK_INDEXES;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.FIXED_INCOME_BOND_SECURITIES;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class FixedIncomeBondSectorBenchmarkEndpoint extends BenchmarkAbstractEndpoint<RFixedIncomeBondSecurities> {

    public FixedIncomeBondSectorBenchmarkEndpoint() {
        super(GET_GET_INDEXES_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(FIXED_INCOME_BOND_SECURITIES, BENCHMARK_INDEXES));
    }

    @Override
    public IndexQuery requestMapper(final IndexQuery query) {
        return query
                .fixedIncomeSecuritiesAllocation(sa -> sa.allocation(a -> a.name().value()).dataProvider())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RFixedIncomeBondSecurities responseMapper(final Index index, final BenchmarkIndexHolding holding) {
        final FixedIncomeSecuritiesAllocation fixedIncomeSecuritiesAllocation = index.getFixedIncomeSecuritiesAllocation();
        return GraphQlMapperUtils.fixedIncomeBondSectorMapper(fixedIncomeSecuritiesAllocation, holding.getType());
    }

}
