package com.fintex.ce.adapter.graphqlclient.endpoint.countryexposure;

import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.BenchmarkAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_GET_INDEXES_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.BENCHMARK_INDEXES;
import static com.fintex.ce.constant.CacheNameEntity.COUNTRY_EXPOSURE;
import static com.fintex.ce.adapter.graphqlclient.endpoint.countryexposure.CountryExposureEtfCanadaEndpoint.getCountryAllocationQueryDefinition;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.countryExposureMapper;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.toDomainHoldingType;

public class CountryExposureBenchmarkEndpoint extends BenchmarkAbstractEndpoint<CountryExposure> {

  public CountryExposureBenchmarkEndpoint() {
    super(GET_GET_INDEXES_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(COUNTRY_EXPOSURE, BENCHMARK_INDEXES));
  }

  @Override
  public IndexQuery requestMapper(final IndexQuery query) {
    return query
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION)
        .fixedIncomeCountryAllocation(
            getCountryAllocationQueryDefinition());
  }

  @Override
  public CountryExposure responseMapper(final Index index, final BenchmarkIndexHolding holding) {
    Map<String, BigDecimal> allocation = countryExposureMapper(index.getFixedIncomeCountryAllocation());
    return new CountryExposure(toDomainHoldingType(holding.getType()), allocation);
  }

}
