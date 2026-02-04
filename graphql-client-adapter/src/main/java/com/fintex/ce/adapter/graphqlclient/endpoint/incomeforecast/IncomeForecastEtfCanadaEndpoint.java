package com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.StringsDatapoint;
import com.fintex.smclient.graphql.StringsDatapointQuery;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.IncomeForecast;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.EtfAbstractEndpoint;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_ETF;
import static com.fintex.ce.constant.CacheNameEntity.INCOME_FORECAST;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class IncomeForecastEtfCanadaEndpoint extends EtfAbstractEndpoint<IncomeForecast> {

  public IncomeForecastEtfCanadaEndpoint() {
    super(GET_CANADA_ETFS_BY_TICKERS, List.of(), buildCacheName(INCOME_FORECAST, CANADA_ETF));
  }

  public IncomeForecastEtfCanadaEndpoint(final Function<Query, List<Etf>> getFDSEntityFunction,
      final List<DataProvider> supportedProviders,
      final String endpointName) {
    super(getFDSEntityFunction, supportedProviders, endpointName);
  }

  @Override
  public EtfQuery requestMapper(final EtfQuery query) {
    return query
        .dividendYield(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
        .distributionDates(StringsDatapointQuery::values)
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION)
        .ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
  }

  @Override
  public IncomeForecast responseMapper(final Etf etf, final EtfHolding holding) {
    final var rIncomeForecast = new IncomeForecast();
    Optional.ofNullable(etf.getDividendYield())
        .map(FloatDatapoint::getValue)
        .ifPresent(rIncomeForecast::setDividendYield);
    Optional.ofNullable(etf.getDistributionDates())
        .map(StringsDatapoint::getValues)
        .ifPresent(rIncomeForecast::setSchedule);
    return rIncomeForecast;
  }

}
