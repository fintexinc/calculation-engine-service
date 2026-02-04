package com.fintex.ce.adapter.graphqlclient.endpoint.countryexposure;

import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.UsMutualFundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.COUNTRY_EXPOSURE;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.countryExposureMapper;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.toDomainHoldingType;

public class CountryExposureUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<CountryExposure> {

  public CountryExposureUsMutualFundEndpoint() {
    super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(COUNTRY_EXPOSURE, US_MUTUAL_FUNDS));
  }

  @Override
  public QueryQueryDefinition queryDefinition(List<String> tickers,
      UnaryOperator<UsFundQuery> preDefinedFDSQuery) {
    return q -> q.getUsFundsByTickers(tickers, preDefinedFDSQuery::apply);
  }

  @Override
  public UsFundQuery requestMapper(UsFundQuery query) {
    return query
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION)
        .fixedIncomeCountryAllocation(
            CountryExposureEtfCanadaEndpoint.getCountryAllocationQueryDefinition());
  }

  @Override
  public CountryExposure responseMapper(UsFund fund, UsMutualFundHolding holding) {
    Map<String, BigDecimal> allocation = countryExposureMapper(fund.getFixedIncomeCountryAllocation());
    return new CountryExposure(toDomainHoldingType(holding.getType()), allocation);
  }
}
