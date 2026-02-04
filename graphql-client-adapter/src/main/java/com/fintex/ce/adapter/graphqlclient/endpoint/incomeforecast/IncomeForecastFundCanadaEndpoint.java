package com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast;

import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.StringsDatapoint;
import com.fintex.smclient.graphql.StringsDatapointQuery;
import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.IncomeForecast;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.FundAbstractEndpoint;

import java.util.List;
import java.util.Optional;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheNameEntity.INCOME_FORECAST;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class IncomeForecastFundCanadaEndpoint extends FundAbstractEndpoint<IncomeForecast> {

  public IncomeForecastFundCanadaEndpoint() {
    super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(INCOME_FORECAST,
        CacheCategory.CANADA_MUTUAL_FUNDS));
  }

  @Override
  public FundSeriesQuery requestMapper(final FundSeriesQuery query) {
    return query
        .dividendYield(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
        .distributionDates(StringsDatapointQuery::values)
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public IncomeForecast responseMapper(final FundSeries fundSeries,
      final FundSeriesHolding holding) {
    final var rIncomeForecast = new IncomeForecast();
    Optional.ofNullable(fundSeries.getDividendYield())
        .map(FloatDatapoint::getValue)
        .ifPresent(rIncomeForecast::setDividendYield);
    Optional.ofNullable(fundSeries.getDistributionDates())
        .map(StringsDatapoint::getValues)
        .ifPresent(rIncomeForecast::setSchedule);
    return rIncomeForecast;
  }

}
