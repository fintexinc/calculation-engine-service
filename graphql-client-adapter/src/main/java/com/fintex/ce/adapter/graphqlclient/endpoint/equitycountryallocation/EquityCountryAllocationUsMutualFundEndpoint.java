package com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation;

import com.fintex.smclient.graphql.CountryAllocationQueryDefinition;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.UsMutualFundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_COUNTRY_ALLOCATIONS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.equityCountryAllocationMapper;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.toDomainHoldingType;

public class EquityCountryAllocationUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<EquityCountryAllocation> {

  public EquityCountryAllocationUsMutualFundEndpoint() {
    super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(EQUITY_COUNTRY_ALLOCATIONS, US_MUTUAL_FUNDS));
  }

  static CountryAllocationQueryDefinition getCountryAllocationQueryDefinition() {
    return qCountry -> qCountry.allocation(qAllocation -> qAllocation.value().name(qName -> qName.value()
        .languageCode())).dataProvider();
  }

  @Override
  public QueryQueryDefinition queryDefinition(List<String> tickers, UnaryOperator<UsFundQuery> preDefinedFDSQuery) {
    return q -> q.getUsFundsByTickers(tickers, preDefinedFDSQuery::apply);
  }

  @Override
  public UsFundQuery requestMapper(final UsFundQuery query) {
    return query
        .equityCountryAllocation(getCountryAllocationQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public EquityCountryAllocation responseMapper(final UsFund usFund, final UsMutualFundHolding holding) {
    final var result = new EquityCountryAllocation();

    final Map<String, BigDecimal> allocations = equityCountryAllocationMapper(usFund.getEquityCountryAllocation());

    Optional.ofNullable(usFund.getEquityCountryAllocation()).ifPresent(equityCountryAllocation -> result.setProvider(
        DataProvider.of(equityCountryAllocation.getDataProvider().name()).name()));

    result.setHoldingType(toDomainHoldingType(holding.getType()));
    result.setAllocations(allocations);

    return result;
  }
}
