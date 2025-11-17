package com.fintex.ce.repository.graphql.query.endpoint.equitycountryallocation;

import com.fintex.smclient.graphql.CountryAllocationQueryDefinition;
import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.model.redis.REquityCountryAllocation;
import com.fintex.ce.repository.graphql.query.endpoint.core.BenchmarkAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_GET_INDEXES_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.BENCHMARK_INDEXES;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_COUNTRY_ALLOCATIONS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphQlMapperUtils.equityCountryAllocationMapper;

public class EquityCountryAllocationBenchmarkEndpoint extends BenchmarkAbstractEndpoint<REquityCountryAllocation> {

    public EquityCountryAllocationBenchmarkEndpoint() {
        super(GET_GET_INDEXES_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(EQUITY_COUNTRY_ALLOCATIONS, BENCHMARK_INDEXES));
    }

    static CountryAllocationQueryDefinition getCountryAllocationQueryDefinition() {
        return qCountry ->
                qCountry.allocation(qAllocation ->
                        qAllocation.value().name(qName ->
                                qName.value().languageCode()
                        )
                ).dataProvider();
    }

    @Override
    public IndexQuery requestMapper(final IndexQuery query) {
        return query
                .equityCountryAllocation(getCountryAllocationQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public REquityCountryAllocation responseMapper(final Index index, final BenchmarkIndexHolding holding) {
        final var result = new REquityCountryAllocation();
        final Map<String, BigDecimal> allocations = equityCountryAllocationMapper(index.getEquityCountryAllocation());
        Optional.ofNullable(index.getEquityCountryAllocation()).ifPresent(equityCountryAllocation
                -> result.setProvider(DataProvider.of(equityCountryAllocation.getDataProvider()).name()));
        result.setHoldingType(holding.getType());
        result.setAllocations(allocations);
        return result;
    }

}
