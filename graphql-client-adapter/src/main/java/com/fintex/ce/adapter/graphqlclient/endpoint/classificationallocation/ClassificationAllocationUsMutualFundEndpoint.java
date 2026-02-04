package com.fintex.ce.adapter.graphqlclient.endpoint.classificationallocation;

import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.ClassificationAllocation;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.UsMutualFundAbstractEndpoint;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.CLASSIFICATION_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class ClassificationAllocationUsMutualFundEndpoint
    extends
      UsMutualFundAbstractEndpoint<ClassificationAllocation> {

  public ClassificationAllocationUsMutualFundEndpoint() {
    super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(CLASSIFICATION_ALLOCATION, US_MUTUAL_FUNDS));
  }

  @Override
  public QueryQueryDefinition queryDefinition(final List<String> morningstarIds,
      final UnaryOperator<UsFundQuery> preDefinedFDSQuery) {
    return q -> q.getUsFundsByTickers(morningstarIds, preDefinedFDSQuery::apply);
  }

  @Override
  public UsFundQuery requestMapper(final UsFundQuery query) {
    return query
        .securityClassification(ClassificationAllocationEndpointUtil.getSecurityClassificationQueryDefinition())
        .securityClassificationAllocation(ClassificationAllocationEndpointUtil
            .getSecurityClassificationAllocationQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public ClassificationAllocation responseMapper(final UsFund fund,
      final UsMutualFundHolding holding) {
    return ClassificationAllocationEndpointUtil.getResponseCacheEntity(
        fund,
        fund::getSecurityClassificationAllocation,
        fund::getSecurityClassification);
  }

}
