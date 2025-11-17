package com.fintex.ce.repository.graphql.query.endpoint.incomeforecast;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.StringsDatapoint;
import com.fintex.smclient.graphql.StringsDatapointQuery;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.RIncomeForecast;
import com.fintex.ce.repository.graphql.query.endpoint.core.EtfAbstractEndpoint;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_ETF;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.INCOME_FORECAST;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class IncomeForecastEtfCanadaEndpoint extends EtfAbstractEndpoint<RIncomeForecast> {

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
    public RIncomeForecast responseMapper(final Etf etf, final EtfHolding holding) {
        final var rIncomeForecast = new RIncomeForecast();
        Optional.ofNullable(etf.getDividendYield())
                .map(FloatDatapoint::getValue)
                .ifPresent(rIncomeForecast::setDividendYield);
        Optional.ofNullable(etf.getDistributionDates())
                .map(StringsDatapoint::getValues)
                .ifPresent(rIncomeForecast::setSchedule);
        return rIncomeForecast;
    }

}
