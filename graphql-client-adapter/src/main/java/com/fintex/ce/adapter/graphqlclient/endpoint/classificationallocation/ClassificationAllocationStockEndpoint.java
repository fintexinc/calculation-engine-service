package com.fintex.ce.adapter.graphqlclient.endpoint.classificationallocation;

import com.fintex.smclient.graphql.Stock;
import com.fintex.smclient.graphql.StockQuery;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.ClassificationAllocation;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.StockAbstractEndpoint;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.STOCKS;
import static com.fintex.ce.constant.CacheNameEntity.CLASSIFICATION_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class ClassificationAllocationStockEndpoint extends StockAbstractEndpoint<ClassificationAllocation> {

  public ClassificationAllocationStockEndpoint() {
    super(GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS, List.of(), buildCacheName(CLASSIFICATION_ALLOCATION, STOCKS));
  }

  @Override
  public StockQuery requestMapper(final StockQuery query) {
    return query
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION)
        .securityClassification(ClassificationAllocationEndpointUtil.getSecurityClassificationQueryDefinition())
        .securityClassificationAllocation(ClassificationAllocationEndpointUtil
            .getSecurityClassificationAllocationQueryDefinition());
  }

  @Override
  public ClassificationAllocation responseMapper(final Stock stock,
      final StockHolding holding) {
    return ClassificationAllocationEndpointUtil.getResponseCacheEntity(
        stock,
        stock::getSecurityClassificationAllocation,
        stock::getSecurityClassification);
  }

}
