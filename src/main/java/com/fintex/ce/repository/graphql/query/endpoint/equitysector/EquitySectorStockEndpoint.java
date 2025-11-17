package com.fintex.ce.repository.graphql.query.endpoint.equitysector;

import com.google.common.base.Strings;
import com.fintex.smclient.graphql.Stock;
import com.fintex.smclient.graphql.StockQuery;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.model.redis.equitysector.REquitySectorStock;
import com.fintex.ce.repository.graphql.query.endpoint.core.StockAbstractEndpoint;

import java.util.List;
import java.util.Objects;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.STOCKS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_SECTOR;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static java.util.Objects.isNull;

public class EquitySectorStockEndpoint extends StockAbstractEndpoint<REquitySectorStock> {

    public EquitySectorStockEndpoint() {
        super(GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS, List.of(), buildCacheName(EQUITY_SECTOR, STOCKS));
    }

    @Override
    public StockQuery requestMapper(final StockQuery query) {
        return query
                .sectorName(STRING_WITH_DATA_PROVIDER_DEFINITION)
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public REquitySectorStock responseMapper(final Stock stock, final StockHolding holding) {
        if (isNull(stock) || isNull(stock.getSectorName()) || Strings.isNullOrEmpty(stock.getSectorName().getValue())) {
            return new REquitySectorStock();
        }
        final String sectorName = stock.getSectorName().getValue();
        final REquitySectorStock rEquitySectorStock = new REquitySectorStock(sectorName);
        rEquitySectorStock.setProvider(Objects.requireNonNull(stock.getSectorName().getDataProvider()).name());
        return rEquitySectorStock;
    }

}
