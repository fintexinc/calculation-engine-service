package com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast;

import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FixedIncomeQuery;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.StringDatapoint;
import com.fintex.smclient.graphql.StringsDatapoint;
import com.fintex.smclient.graphql.StringsDatapointQuery;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.IncomeForecast;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.FixedIncomeAbstractEndpoint;

import java.util.List;
import java.util.Optional;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_FIXED_INCOME_BY_ADP_NUMBERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.STRING_DATAPOINT_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.FIXED_INCOME;
import static com.fintex.ce.constant.CacheNameEntity.INCOME_FORECAST;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class IncomeForecastFixedIncomeEndpoint extends FixedIncomeAbstractEndpoint<IncomeForecast> {

  public IncomeForecastFixedIncomeEndpoint() {
    super(GET_FIXED_INCOME_BY_ADP_NUMBERS, List.of(), buildCacheName(INCOME_FORECAST, FIXED_INCOME));
  }

  @Override
  public FixedIncomeQuery requestMapper(final FixedIncomeQuery query) {
    return query
        .interestRate(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
        .distributionDates(StringsDatapointQuery::values)
        .maturityDate(STRING_DATAPOINT_QUERY_DEFINITION)
        .issueDate(STRING_DATAPOINT_QUERY_DEFINITION)
        .paymentFrequency()
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public IncomeForecast responseMapper(final FixedIncome fixedIncome,
      final FixedIncomeHolding holding) {
    final var rIncomeForecast = new IncomeForecast();
    Optional.ofNullable(fixedIncome.getInterestRate())
        .map(FloatDatapoint::getValue)
        .ifPresent(rIncomeForecast::setDividendYield);
    Optional.ofNullable(fixedIncome.getDistributionDates())
        .map(StringsDatapoint::getValues)
        .ifPresent(rIncomeForecast::setSchedule);

    Optional.ofNullable(fixedIncome.getPaymentFrequency())
        .ifPresent(pf -> rIncomeForecast.setPaymentFrequencyType(pf.name()));
    rIncomeForecast.setMaturityDate(getStringValue(fixedIncome.getMaturityDate()));
    rIncomeForecast.setIssueDate(getStringValue(fixedIncome.getIssueDate()));

    return rIncomeForecast;
  }

  private String getStringValue(final StringDatapoint stringDatapoint) {
    return Optional.ofNullable(stringDatapoint)
        .map(StringDatapoint::getValue)
        .orElse(null);
  }

}
