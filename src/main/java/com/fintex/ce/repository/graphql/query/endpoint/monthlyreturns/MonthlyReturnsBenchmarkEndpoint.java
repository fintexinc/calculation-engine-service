package com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns;

import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.repository.graphql.query.endpoint.core.BenchmarkAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_GET_INDEXES_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.BENCHMARK_INDEXES;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MONTHLY_RETURNS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MonthlyReturnsBenchmarkEndpoint extends BenchmarkAbstractEndpoint<RMonthlyReturns> {

    public MonthlyReturnsBenchmarkEndpoint() {
        super(GET_GET_INDEXES_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(MONTHLY_RETURNS, BENCHMARK_INDEXES));
    }

    @Override
    public IndexQuery requestMapper(final IndexQuery query) {
        return query
                .currency(c -> c.type().dataProvider())
                .monthlyReturns(
                        qMonthly -> qMonthly.returns(
                                qReturns -> qReturns.date().value()
                        ).dataProvider()
                )
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RMonthlyReturns responseMapper(final Index index, final BenchmarkIndexHolding holding) {
        if (index.getCurrency() == null || index.getCurrency().getType() == null) {
            return GraphQlMapperUtils.monthlyReturns(index.getMonthlyReturns(), null, holding);
        }
        final CurrencyType currency = index.getCurrency().getType();
        return GraphQlMapperUtils.monthlyReturns(index.getMonthlyReturns(), currency.name(), holding);
    }
}
