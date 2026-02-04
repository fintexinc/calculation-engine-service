package com.fintex.ce.adapter.graphqlclient.endpoint.countryexposure;

import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaPooledFundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_POOLED_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.COUNTRY_EXPOSURE;
import static com.fintex.ce.adapter.graphqlclient.endpoint.countryexposure.CountryExposureEtfCanadaEndpoint.getCountryAllocationQueryDefinition;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.countryExposureMapper;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.toDomainHoldingType;

public class CountryExposureCanadaPooledFundEndpoint extends CanadaPooledFundAbstractEndpoint<CountryExposure> {

  public CountryExposureCanadaPooledFundEndpoint() {
    super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(COUNTRY_EXPOSURE, CANADA_POOLED_FUNDS));
  }

  @Override
  public QueryQueryDefinition queryDefinition(List<String> morninstarIds,
      UnaryOperator<PooledFundQuery> preDefinedFDSQuery) {
    return q -> q.getCanadaPooledFundsByMorningstarIds(morninstarIds, preDefinedFDSQuery::apply);
  }

  @Override
  public PooledFundQuery requestMapper(PooledFundQuery query) {
    return query
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION)
        .fixedIncomeCountryAllocation(
            getCountryAllocationQueryDefinition());
  }

  @Override
  public CountryExposure responseMapper(PooledFund fund, CanadaPooledFundHolding holding) {
    Map<String, BigDecimal> allocation = countryExposureMapper(fund.getFixedIncomeCountryAllocation());
    return new CountryExposure(toDomainHoldingType(holding.getType()), allocation);
  }
}
