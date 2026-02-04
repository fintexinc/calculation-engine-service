package com.fintex.ce.adapter.graphqlclient.endpoint.countryexposure;

import com.fintex.smclient.graphql.FundHoldingIdentifiersCodes;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.FundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.COUNTRY_EXPOSURE;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.countryExposureMapper;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.toDomainHoldingType;

public class CountryExposureFundCanadaEndpoint extends FundAbstractEndpoint<CountryExposure> {

  public CountryExposureFundCanadaEndpoint() {
    super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(COUNTRY_EXPOSURE, CANADA_MUTUAL_FUNDS));
  }

  @Override
  public QueryQueryDefinition queryDefinition(final List<FundHoldingIdentifiersCodes> fundHoldingIdentifiersCodes,
      final UnaryOperator<FundSeriesQuery> preDefinedFDSQuery) {
    return q -> q.getFundSeriesByHoldingCodes(fundHoldingIdentifiersCodes, preDefinedFDSQuery::apply);
  }

  @Override
  public FundSeriesQuery requestMapper(final FundSeriesQuery query) {
    return query
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION)
        .fixedIncomeCountryAllocation(
            CountryExposureEtfCanadaEndpoint.getCountryAllocationQueryDefinition());
  }

  @Override
  public CountryExposure responseMapper(final FundSeries fundSeries, final FundSeriesHolding holding) {
    Map<String, BigDecimal> allocation = countryExposureMapper(fundSeries.getFixedIncomeCountryAllocation());
    return new CountryExposure(toDomainHoldingType(holding.getType()), allocation);
  }
}
