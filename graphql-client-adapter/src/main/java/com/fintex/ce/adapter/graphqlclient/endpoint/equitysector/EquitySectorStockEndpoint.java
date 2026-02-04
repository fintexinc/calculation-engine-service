package com.fintex.ce.adapter.graphqlclient.endpoint.equitysector;

import com.google.common.base.Strings;
import com.fintex.smclient.graphql.Stock;
import com.fintex.smclient.graphql.StockQuery;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.EquitySectorStock;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.StockAbstractEndpoint;

import java.util.List;
import java.util.Objects;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.STOCKS;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_SECTOR;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static java.util.Objects.isNull;

public class EquitySectorStockEndpoint extends StockAbstractEndpoint<EquitySectorStock> {

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
  public EquitySectorStock responseMapper(final Stock stock, final StockHolding holding) {
    if (isNull(stock) || isNull(stock.getSectorName()) || Strings.isNullOrEmpty(stock.getSectorName().getValue())) {
      return new EquitySectorStock();
    }
    final String sectorName = stock.getSectorName().getValue();
    final EquitySectorStock rEquitySectorStock = new EquitySectorStock(sectorName);
    rEquitySectorStock.setProvider(Objects.requireNonNull(stock.getSectorName().getDataProvider()).name());
    return rEquitySectorStock;
  }

}
