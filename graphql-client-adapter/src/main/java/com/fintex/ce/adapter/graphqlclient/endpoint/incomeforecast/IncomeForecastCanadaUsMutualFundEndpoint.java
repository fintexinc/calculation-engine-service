package com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast;

import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.StringsDatapoint;
import com.fintex.smclient.graphql.StringsDatapointQuery;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.IncomeForecast;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.UsMutualFundAbstractEndpoint;

import java.util.List;
import java.util.Optional;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.INCOME_FORECAST;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class IncomeForecastCanadaUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<IncomeForecast> {

  public IncomeForecastCanadaUsMutualFundEndpoint() {
    super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(INCOME_FORECAST, US_MUTUAL_FUNDS));
  }

  @Override
  public UsFundQuery requestMapper(final UsFundQuery query) {
    return query
        .dividendYield(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
        .distributionDates(StringsDatapointQuery::values)
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);

  }

  @Override
  public IncomeForecast responseMapper(final UsFund pooledFund,
      final UsMutualFundHolding holding) {
    final var rIncomeForecast = new IncomeForecast();
    Optional.ofNullable(pooledFund.getDividendYield())
        .map(FloatDatapoint::getValue)
        .ifPresent(rIncomeForecast::setDividendYield);
    Optional.ofNullable(pooledFund.getDistributionDates())
        .map(StringsDatapoint::getValues)
        .ifPresent(rIncomeForecast::setSchedule);
    return rIncomeForecast;
  }

}
