package com.fintex.ce.repository.graphql.query.endpoint.incomeforecast;

import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FixedIncomeQuery;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.StringDatapoint;
import com.fintex.smclient.graphql.StringsDatapoint;
import com.fintex.smclient.graphql.StringsDatapointQuery;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.model.redis.RIncomeForecast;
import com.fintex.ce.repository.graphql.query.endpoint.core.FixedIncomeAbstractEndpoint;

import java.util.List;
import java.util.Optional;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_FIXED_INCOME_BY_ADP_NUMBERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_DATAPOINT_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.FIXED_INCOME;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.INCOME_FORECAST;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class IncomeForecastFixedIncomeEndpoint extends FixedIncomeAbstractEndpoint<RIncomeForecast> {

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
    public RIncomeForecast responseMapper(final FixedIncome fixedIncome,
                                          final FixedIncomeHolding holding) {
        final var rIncomeForecast = new RIncomeForecast();
            Optional.ofNullable(fixedIncome.getInterestRate())
                    .map(FloatDatapoint::getValue)
                    .ifPresent(rIncomeForecast::setDividendYield);
            Optional.ofNullable(fixedIncome.getDistributionDates())
                    .map(StringsDatapoint::getValues)
                    .ifPresent(rIncomeForecast::setSchedule);

            rIncomeForecast.setPaymentFrequencyType(fixedIncome.getPaymentFrequency());
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
