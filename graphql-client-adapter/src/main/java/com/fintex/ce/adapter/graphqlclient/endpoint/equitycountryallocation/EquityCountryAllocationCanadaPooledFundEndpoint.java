package com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation;

import com.fintex.smclient.graphql.CountryAllocationQueryDefinition;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaPooledFundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_POOLED_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_COUNTRY_ALLOCATIONS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.equityCountryAllocationMapper;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.toDomainHoldingType;

public class EquityCountryAllocationCanadaPooledFundEndpoint
    extends
      CanadaPooledFundAbstractEndpoint<EquityCountryAllocation> {

  public EquityCountryAllocationCanadaPooledFundEndpoint() {
    super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(EQUITY_COUNTRY_ALLOCATIONS,
        CANADA_POOLED_FUNDS));
  }

  static CountryAllocationQueryDefinition getCountryAllocationQueryDefinition() {
    return qCountry -> qCountry.allocation(qAllocation -> qAllocation.value().name(qName -> qName.value()
        .languageCode())).dataProvider();
  }

  @Override
  public QueryQueryDefinition queryDefinition(List<String> morningstarIds,
      UnaryOperator<PooledFundQuery> preDefinedFDSQuery) {
    return q -> q.getCanadaPooledFundsByMorningstarIds(morningstarIds, preDefinedFDSQuery::apply);
  }

  @Override
  public PooledFundQuery requestMapper(final PooledFundQuery query) {
    return query
        .equityCountryAllocation(getCountryAllocationQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public EquityCountryAllocation responseMapper(final PooledFund pooledFund, final CanadaPooledFundHolding holding) {
    final var result = new EquityCountryAllocation();

    final Map<String, BigDecimal> allocations = equityCountryAllocationMapper(pooledFund.getEquityCountryAllocation());

    Optional.ofNullable(pooledFund.getEquityCountryAllocation()).ifPresent(equityCountryAllocation -> result
        .setProvider(DataProvider.of(equityCountryAllocation.getDataProvider().name()).name()));

    result.setHoldingType(toDomainHoldingType(holding.getType()));
    result.setAllocations(allocations);

    return result;
  }
}
