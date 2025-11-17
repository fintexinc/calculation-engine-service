package com.fintex.ce.repository.graphql.query.endpoint.incomeforecast;

import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.StringsDatapoint;
import com.fintex.smclient.graphql.StringsDatapointQuery;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RIncomeForecast;
import com.fintex.ce.repository.graphql.query.endpoint.core.UsMutualFundAbstractEndpoint;

import java.util.List;
import java.util.Optional;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.INCOME_FORECAST;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class IncomeForecastCanadaUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<RIncomeForecast> {

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
    public RIncomeForecast responseMapper(final UsFund pooledFund,
                                          final UsMutualFundHolding holding) {
        final var rIncomeForecast = new RIncomeForecast();
        Optional.ofNullable(pooledFund.getDividendYield())
                .map(FloatDatapoint::getValue)
                .ifPresent(rIncomeForecast::setDividendYield);
        Optional.ofNullable(pooledFund.getDistributionDates())
                .map(StringsDatapoint::getValues)
                .ifPresent(rIncomeForecast::setSchedule);
        return rIncomeForecast;
    }

}
