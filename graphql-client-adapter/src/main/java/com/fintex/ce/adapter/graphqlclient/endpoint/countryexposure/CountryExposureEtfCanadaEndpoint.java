package com.fintex.ce.adapter.graphqlclient.endpoint.countryexposure;

import com.fintex.smclient.graphql.*;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.EtfAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_ETF;
import static com.fintex.ce.constant.CacheNameEntity.COUNTRY_EXPOSURE;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.countryExposureMapper;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.toDomainHoldingType;

public class CountryExposureEtfCanadaEndpoint extends EtfAbstractEndpoint<CountryExposure> {

  public CountryExposureEtfCanadaEndpoint() {
    super(GET_CANADA_ETFS_BY_TICKERS, List.of(), buildCacheName(COUNTRY_EXPOSURE, CANADA_ETF));
  }

  public CountryExposureEtfCanadaEndpoint(final Function<Query, List<Etf>> getFDSEntityFunction,
      final List<DataProvider> supportedProviders,
      final String endpointName) {
    super(getFDSEntityFunction, supportedProviders, endpointName);
  }

  static CountryAllocationQueryDefinition getCountryAllocationQueryDefinition() {
    return query -> query.allocation(q -> q.isoCode().value()).dataProvider();
  }

  @Override
  public QueryQueryDefinition queryDefinition(final List<String> equityIdentifiers,
      final UnaryOperator<EtfQuery> preDefinedFDSQuery) {
    return q -> q.getCanadaEtfsByTickers(equityIdentifiers, preDefinedFDSQuery::apply);
  }

  @Override
  public EtfQuery requestMapper(final EtfQuery query) {
    return query
        .ticker(STRING_WITH_DATA_PROVIDER_DEFINITION)
        .fixedIncomeCountryAllocation(
            getCountryAllocationQueryDefinition());
  }

  @Override
  public CountryExposure responseMapper(final Etf etf, final EtfHolding holding) {
    final Map<String, BigDecimal> allocation = countryExposureMapper(etf.getFixedIncomeCountryAllocation());
    return new CountryExposure(toDomainHoldingType(holding.getType()), allocation);
  }
}
