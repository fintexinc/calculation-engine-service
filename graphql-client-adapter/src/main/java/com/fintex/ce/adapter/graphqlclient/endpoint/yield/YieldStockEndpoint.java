
package com.fintex.ce.adapter.graphqlclient.endpoint.yield;

import com.fintex.smclient.graphql.Stock;
import com.fintex.smclient.graphql.StockQuery;
import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.StockAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheNameEntity.YIELD;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class YieldStockEndpoint extends StockAbstractEndpoint<Yield> {

  public YieldStockEndpoint() {
    super(GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS, List.of(), buildCacheName(YIELD, CacheCategory.STOCKS));
  }

  @Override
  public StockQuery requestMapper(final StockQuery query) {
    return query
        .dividendYield(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);

  }

  @Override
  public Yield responseMapper(final Stock stock,
      final StockHolding holding) {
    return GraphQlMapperUtils.mapYield(stock, Stock::getDividendYield);
  }

}
