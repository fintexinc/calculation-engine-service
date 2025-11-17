package com.fintex.ce.repository.graphql.query.endpoint.equitymarketcapitalization;

import com.google.common.base.Strings;
import com.fintex.smclient.graphql.Stock;
import com.fintex.smclient.graphql.StockQuery;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.model.redis.equitymarketcapitalization.REquityMarketCapitalizationStock;
import com.fintex.ce.repository.graphql.query.endpoint.core.StockAbstractEndpoint;

import java.util.List;
import java.util.Objects;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.STOCKS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_MARKET_CAPITALIZATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static java.util.Objects.isNull;

public class EquityMarketCapitalizationStockEndpoint extends StockAbstractEndpoint<REquityMarketCapitalizationStock> {

    public EquityMarketCapitalizationStockEndpoint() {
        super(GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS, List.of(), buildCacheName(EQUITY_MARKET_CAPITALIZATION, STOCKS));
    }

    @Override
    public StockQuery requestMapper(final StockQuery query) {
        return query
                .stylebox(STRING_WITH_DATA_PROVIDER_DEFINITION)
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public REquityMarketCapitalizationStock responseMapper(final Stock stock, final StockHolding holding) {
        if (isNull(stock) || isNull(stock.getStylebox()) || Strings.isNullOrEmpty(stock.getStylebox().getValue())) {
            return new REquityMarketCapitalizationStock();
        }
        final String sectorName = stock.getStylebox().getValue();
        final REquityMarketCapitalizationStock rEquitySectorStock = new REquityMarketCapitalizationStock(sectorName);
        rEquitySectorStock.setProvider(Objects.requireNonNull(stock.getStylebox().getDataProvider()).name());
        return rEquitySectorStock;
    }

}
