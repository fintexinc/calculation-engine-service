package com.fintex.ce.repository.graphql.query.endpoint.classificationallocation;

import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.ce.config.enumeration.cache.CacheCategory;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.RClassificationAllocation;
import com.fintex.ce.repository.graphql.query.endpoint.core.FundAbstractEndpoint;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.CLASSIFICATION_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class ClassificationAllocationFundCanadaEndpoint extends FundAbstractEndpoint<RClassificationAllocation> {

    public ClassificationAllocationFundCanadaEndpoint() {
        super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(CLASSIFICATION_ALLOCATION, CacheCategory.CANADA_MUTUAL_FUNDS));
    }

    @Override
    public FundSeriesQuery requestMapper(final FundSeriesQuery query) {
        return query
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION)
                .securityClassification(ClassificationAllocationEndpointUtil.getSecurityClassificationQueryDefinition())
                .securityClassificationAllocation(ClassificationAllocationEndpointUtil.getSecurityClassificationAllocationQueryDefinition());
    }

    @Override
    public RClassificationAllocation responseMapper(final FundSeries fundSeries,
                                                    final FundSeriesHolding holding) {
        return ClassificationAllocationEndpointUtil.getResponseCacheEntity(
                fundSeries,
                fundSeries::getSecurityClassificationAllocation,
                fundSeries::getSecurityClassification
        );
    }


}
