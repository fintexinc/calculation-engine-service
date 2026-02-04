package com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.EtfAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.STRING_DATAPOINT_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_ETF;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_COUNTRY_ALLOCATIONS;
import static com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation.EquityCountryAllocationFundCanadaEndpoint.getCountryAllocationQueryDefinition;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.equityCountryAllocationMapper;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.toDomainHoldingType;

public class EquityCountryAllocationEtfCanadaEndpoint extends EtfAbstractEndpoint<EquityCountryAllocation> {

  public EquityCountryAllocationEtfCanadaEndpoint() {
    super(GET_CANADA_ETFS_BY_TICKERS, List.of(), buildCacheName(EQUITY_COUNTRY_ALLOCATIONS, CANADA_ETF));
  }

  public EquityCountryAllocationEtfCanadaEndpoint(final Function<Query, List<Etf>> getFDSEntityFunction,
      final List<DataProvider> supportedProviders,
      final String endpointName) {
    super(getFDSEntityFunction, supportedProviders, endpointName);
  }

  @Override
  public QueryQueryDefinition queryDefinition(final List<String> equityIdentifiers,
      final UnaryOperator<EtfQuery> preDefinedFDSQuery) {
    return q -> q.getCanadaEtfsByTickers(equityIdentifiers, preDefinedFDSQuery::apply);
  }

  @Override
  public EtfQuery requestMapper(final EtfQuery query) {
    return query
        .equityCountryAllocation(getCountryAllocationQueryDefinition())
        .ticker(STRING_DATAPOINT_QUERY_DEFINITION);
  }

  @Override
  public EquityCountryAllocation responseMapper(final Etf etf, final EtfHolding etfHolding) {
    Map<String, BigDecimal> allocations = equityCountryAllocationMapper(etf.getEquityCountryAllocation());
    final var result = new EquityCountryAllocation();
    result.setHoldingType(toDomainHoldingType(etfHolding.getType()));
    result.setAllocations(allocations);

    Optional.ofNullable(etf.getEquityCountryAllocation()).ifPresent(equityCountryAllocation -> result.setProvider(
        DataProvider.of(equityCountryAllocation.getDataProvider().name()).name()));

    return result;
  }

}
