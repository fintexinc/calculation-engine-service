package com.fintex.ce.repository.graphql.query.endpoint.incomeforecast;

import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.Stock;
import com.fintex.smclient.graphql.StockQuery;
import com.fintex.smclient.graphql.StringsDatapoint;
import com.fintex.smclient.graphql.StringsDatapointQuery;
import com.fintex.ce.config.enumeration.cache.CacheCategory;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.model.redis.RIncomeForecast;
import com.fintex.ce.repository.graphql.query.endpoint.core.StockAbstractEndpoint;

import java.util.List;
import java.util.Optional;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.INCOME_FORECAST;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class IncomeForecastStockEndpoint extends StockAbstractEndpoint<RIncomeForecast> {

    public IncomeForecastStockEndpoint() {
        super(GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS, List.of(), buildCacheName(INCOME_FORECAST, CacheCategory.STOCKS));
    }

    @Override
    public StockQuery requestMapper(final StockQuery query) {
        return query
                .dividendYield(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
                .distributionDates(StringsDatapointQuery::values)
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RIncomeForecast responseMapper(final Stock stock,
                                          final StockHolding holding) {
        final var rIncomeForecast = new RIncomeForecast();
        Optional.ofNullable(stock.getDividendYield())
                .map(FloatDatapoint::getValue)
                .ifPresent(rIncomeForecast::setDividendYield);
        Optional.ofNullable(stock.getDistributionDates())
                .map(StringsDatapoint::getValues)
                .ifPresent(rIncomeForecast::setSchedule);
        return rIncomeForecast;
    }

}
