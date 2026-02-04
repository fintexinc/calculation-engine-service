package com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast;

import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.smclient.graphql.StringsDatapoint;
import com.fintex.smclient.graphql.StringsDatapointQuery;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.IncomeForecast;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaHedgeFundAbstractEndpoint;

import java.util.List;
import java.util.Optional;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.INCOME_FORECAST;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class IncomeForecastCanadaHedgeFundEndpoint extends CanadaHedgeFundAbstractEndpoint<IncomeForecast> {

  public IncomeForecastCanadaHedgeFundEndpoint() {
    super(GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(INCOME_FORECAST, CANADA_HEDGE_FUNDS));
  }

  @Override
  public HedgeFundQuery requestMapper(final HedgeFundQuery query) {
    return query
        .dividendYield(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
        .distributionDates(StringsDatapointQuery::values)
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public IncomeForecast responseMapper(final HedgeFund hedgeFund,
      final CanadaHedgeFundHolding holding) {
    final var rIncomeForecast = new IncomeForecast();
    Optional.ofNullable(hedgeFund.getDividendYield())
        .map(FloatDatapoint::getValue)
        .ifPresent(rIncomeForecast::setDividendYield);
    Optional.ofNullable(hedgeFund.getDistributionDates())
        .map(StringsDatapoint::getValues)
        .ifPresent(rIncomeForecast::setSchedule);
    return rIncomeForecast;
  }
}
