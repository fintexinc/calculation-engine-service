package com.fintex.ce.repository.graphql.query.endpoint.incomeforecast;

import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.smclient.graphql.StringsDatapoint;
import com.fintex.smclient.graphql.StringsDatapointQuery;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.model.redis.RIncomeForecast;
import com.fintex.ce.repository.graphql.query.endpoint.core.CanadaHedgeFundAbstractEndpoint;

import java.util.List;
import java.util.Optional;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.INCOME_FORECAST;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class IncomeForecastCanadaHedgeFundEndpoint extends CanadaHedgeFundAbstractEndpoint<RIncomeForecast> {

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
    public RIncomeForecast responseMapper(final HedgeFund hedgeFund,
                                          final CanadaHedgeFundHolding holding) {
        final var rIncomeForecast = new RIncomeForecast();
        Optional.ofNullable(hedgeFund.getDividendYield())
                .map(FloatDatapoint::getValue)
                .ifPresent(rIncomeForecast::setDividendYield);
        Optional.ofNullable(hedgeFund.getDistributionDates())
                .map(StringsDatapoint::getValues)
                .ifPresent(rIncomeForecast::setSchedule);
        return rIncomeForecast;
    }
}
