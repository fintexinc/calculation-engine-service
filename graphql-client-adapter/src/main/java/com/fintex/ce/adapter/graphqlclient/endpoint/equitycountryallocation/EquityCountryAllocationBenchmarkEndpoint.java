package com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation;

import com.fintex.smclient.graphql.CountryAllocationQueryDefinition;
import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.BenchmarkAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_GET_INDEXES_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.BENCHMARK_INDEXES;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_COUNTRY_ALLOCATIONS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.equityCountryAllocationMapper;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.toDomainHoldingType;

public class EquityCountryAllocationBenchmarkEndpoint extends BenchmarkAbstractEndpoint<EquityCountryAllocation> {

  public EquityCountryAllocationBenchmarkEndpoint() {
    super(GET_GET_INDEXES_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(EQUITY_COUNTRY_ALLOCATIONS, BENCHMARK_INDEXES));
  }

  public static CountryAllocationQueryDefinition getCountryAllocationQueryDefinition() {
    return qCountry -> qCountry.allocation(qAllocation -> qAllocation.value().name(qName -> qName.value()
        .languageCode())).dataProvider();
  }

  @Override
  public IndexQuery requestMapper(final IndexQuery query) {
    return query
        .equityCountryAllocation(getCountryAllocationQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public EquityCountryAllocation responseMapper(final Index index, final BenchmarkIndexHolding holding) {
    final var result = new EquityCountryAllocation();
    final Map<String, BigDecimal> allocations = equityCountryAllocationMapper(index.getEquityCountryAllocation());
    Optional.ofNullable(index.getEquityCountryAllocation()).ifPresent(equityCountryAllocation -> result.setProvider(
        DataProvider.of(equityCountryAllocation.getDataProvider().name()).name()));
    result.setHoldingType(toDomainHoldingType(holding.getType()));
    result.setAllocations(allocations);
    return result;
  }

}
