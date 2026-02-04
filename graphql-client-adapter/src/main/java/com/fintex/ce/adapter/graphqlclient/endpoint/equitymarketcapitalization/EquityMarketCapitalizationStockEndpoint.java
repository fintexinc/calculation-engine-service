package com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization;

import com.google.common.base.Strings;
import com.fintex.smclient.graphql.Stock;
import com.fintex.smclient.graphql.StockQuery;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.EquityMarketCapitalizationStock;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.StockAbstractEndpoint;

import java.util.List;
import java.util.Objects;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.STOCKS;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_MARKET_CAPITALIZATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static java.util.Objects.isNull;

public class EquityMarketCapitalizationStockEndpoint extends StockAbstractEndpoint<EquityMarketCapitalizationStock> {

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
  public EquityMarketCapitalizationStock responseMapper(final Stock stock, final StockHolding holding) {
    if (isNull(stock) || isNull(stock.getStylebox()) || Strings.isNullOrEmpty(stock.getStylebox().getValue())) {
      return new EquityMarketCapitalizationStock();
    }
    final String sectorName = stock.getStylebox().getValue();
    final EquityMarketCapitalizationStock rEquitySectorStock = new EquityMarketCapitalizationStock(sectorName);
    rEquitySectorStock.setProvider(Objects.requireNonNull(stock.getStylebox().getDataProvider()).name());
    return rEquitySectorStock;
  }

}
