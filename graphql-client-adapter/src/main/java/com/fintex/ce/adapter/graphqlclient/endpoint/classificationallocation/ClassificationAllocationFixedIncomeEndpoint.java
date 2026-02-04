package com.fintex.ce.adapter.graphqlclient.endpoint.classificationallocation;

import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FixedIncomeQuery;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.ClassificationAllocation;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.FixedIncomeAbstractEndpoint;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_FIXED_INCOME_BY_ADP_NUMBERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.FIXED_INCOME;
import static com.fintex.ce.constant.CacheNameEntity.CLASSIFICATION_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class ClassificationAllocationFixedIncomeEndpoint extends FixedIncomeAbstractEndpoint<ClassificationAllocation> {

  public ClassificationAllocationFixedIncomeEndpoint() {
    super(GET_FIXED_INCOME_BY_ADP_NUMBERS, List.of(), buildCacheName(CLASSIFICATION_ALLOCATION, FIXED_INCOME));
  }

  @Override
  public FixedIncomeQuery requestMapper(final FixedIncomeQuery query) {
    return query
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION)
        .securityClassification(ClassificationAllocationEndpointUtil.getSecurityClassificationQueryDefinition());
  }

  @Override
  public ClassificationAllocation responseMapper(final FixedIncome fixedIncome,
      final FixedIncomeHolding holding) {
    return ClassificationAllocationEndpointUtil.getResponseCacheEntity(
        fixedIncome,
        null,
        fixedIncome::getSecurityClassification);
  }

}
