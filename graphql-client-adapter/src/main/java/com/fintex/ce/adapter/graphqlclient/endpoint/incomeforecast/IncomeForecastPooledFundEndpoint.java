package com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast;

import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.smclient.graphql.StringsDatapoint;
import com.fintex.smclient.graphql.StringsDatapointQuery;
import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.IncomeForecast;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaPooledFundAbstractEndpoint;

import java.util.List;
import java.util.Optional;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheNameEntity.INCOME_FORECAST;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class IncomeForecastPooledFundEndpoint extends CanadaPooledFundAbstractEndpoint<IncomeForecast> {

  public IncomeForecastPooledFundEndpoint() {
    super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(INCOME_FORECAST,
        CacheCategory.CANADA_POOLED_FUNDS));
  }

  @Override
  public PooledFundQuery requestMapper(PooledFundQuery query) {
    return query
        .dividendYield(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
        .distributionDates(StringsDatapointQuery::values)
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);

  }

  @Override
  public IncomeForecast responseMapper(final PooledFund pooledFund,
      final CanadaPooledFundHolding holding) {
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
