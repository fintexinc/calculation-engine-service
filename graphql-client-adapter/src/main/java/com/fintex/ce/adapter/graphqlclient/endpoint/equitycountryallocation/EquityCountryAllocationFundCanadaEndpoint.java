package com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation;

import com.fintex.smclient.graphql.*;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.FundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_COUNTRY_ALLOCATIONS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.equityCountryAllocationMapper;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.toDomainHoldingType;

public class EquityCountryAllocationFundCanadaEndpoint extends FundAbstractEndpoint<EquityCountryAllocation> {

  public EquityCountryAllocationFundCanadaEndpoint() {
    super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(EQUITY_COUNTRY_ALLOCATIONS, CANADA_MUTUAL_FUNDS));
  }

  static CountryAllocationQueryDefinition getCountryAllocationQueryDefinition() {
    return qCountry -> qCountry.allocation(qAllocation -> qAllocation.value().name(qName -> qName.value()
        .languageCode())).dataProvider();
  }

  @Override
  public QueryQueryDefinition queryDefinition(List<FundHoldingIdentifiersCodes> identifiersCodes,
      UnaryOperator<FundSeriesQuery> preDefinedFDSQuery) {
    return q -> q.getFundSeriesByHoldingCodes(identifiersCodes, preDefinedFDSQuery::apply);
  }

  @Override
  public FundSeriesQuery requestMapper(final FundSeriesQuery query) {
    return query
        .equityCountryAllocation(getCountryAllocationQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public EquityCountryAllocation responseMapper(final FundSeries fundSeries, final FundSeriesHolding holding) {
    final var result = new EquityCountryAllocation();

    final Map<String, BigDecimal> allocations = equityCountryAllocationMapper(fundSeries.getEquityCountryAllocation());

    Optional.ofNullable(fundSeries.getEquityCountryAllocation()).ifPresent(equityCountryAllocation -> result
        .setProvider(DataProvider.of(equityCountryAllocation.getDataProvider().name()).name()));

    result.setHoldingType(toDomainHoldingType(holding.getType()));
    result.setAllocations(allocations);

    return result;
  }
}
