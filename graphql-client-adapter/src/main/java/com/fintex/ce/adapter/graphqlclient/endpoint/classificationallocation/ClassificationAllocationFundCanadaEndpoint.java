package com.fintex.ce.adapter.graphqlclient.endpoint.classificationallocation;

import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.ClassificationAllocation;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.FundAbstractEndpoint;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheNameEntity.CLASSIFICATION_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class ClassificationAllocationFundCanadaEndpoint extends FundAbstractEndpoint<ClassificationAllocation> {

  public ClassificationAllocationFundCanadaEndpoint() {
    super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(CLASSIFICATION_ALLOCATION,
        CacheCategory.CANADA_MUTUAL_FUNDS));
  }

  @Override
  public FundSeriesQuery requestMapper(final FundSeriesQuery query) {
    return query
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION)
        .securityClassification(ClassificationAllocationEndpointUtil.getSecurityClassificationQueryDefinition())
        .securityClassificationAllocation(ClassificationAllocationEndpointUtil
            .getSecurityClassificationAllocationQueryDefinition());
  }

  @Override
  public ClassificationAllocation responseMapper(final FundSeries fundSeries,
      final FundSeriesHolding holding) {
    return ClassificationAllocationEndpointUtil.getResponseCacheEntity(
        fundSeries,
        fundSeries::getSecurityClassificationAllocation,
        fundSeries::getSecurityClassification);
  }

}
