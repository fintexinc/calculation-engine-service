package com.fintex.ce.repository.graphql.query.endpoint.classificationallocation;

import com.fintex.smclient.graphql.Stock;
import com.fintex.smclient.graphql.StockQuery;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.model.redis.RClassificationAllocation;
import com.fintex.ce.repository.graphql.query.endpoint.core.StockAbstractEndpoint;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.STOCKS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.CLASSIFICATION_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class ClassificationAllocationStockEndpoint extends StockAbstractEndpoint<RClassificationAllocation> {

    public ClassificationAllocationStockEndpoint() {
        super(GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS, List.of(), buildCacheName(CLASSIFICATION_ALLOCATION, STOCKS));
    }

    @Override
    public StockQuery requestMapper(final StockQuery query) {
        return query
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION)
                .securityClassification(ClassificationAllocationEndpointUtil.getSecurityClassificationQueryDefinition())
                .securityClassificationAllocation(ClassificationAllocationEndpointUtil.getSecurityClassificationAllocationQueryDefinition());
    }

    @Override
    public RClassificationAllocation responseMapper(final Stock stock,
                                                    final StockHolding holding) {
        return ClassificationAllocationEndpointUtil.getResponseCacheEntity(
                stock,
                stock::getSecurityClassificationAllocation,
                stock::getSecurityClassification
        );
    }

}
