
package com.fintex.ce.adapter.graphqlclient.endpoint.yield;

import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FixedIncomeQuery;
import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.FixedIncomeAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_FIXED_INCOME_BY_ADP_NUMBERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheNameEntity.YIELD;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class YieldFixedIncomeEndpoint extends FixedIncomeAbstractEndpoint<Yield> {

  public YieldFixedIncomeEndpoint() {
    super(GET_FIXED_INCOME_BY_ADP_NUMBERS, List.of(), buildCacheName(YIELD, CacheCategory.FIXED_INCOME));
  }

  @Override
  public FixedIncomeQuery requestMapper(final FixedIncomeQuery query) {
    return query
        .interestRate(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);

  }

  @Override
  public Yield responseMapper(final FixedIncome fixedIncome,
      final FixedIncomeHolding holding) {
    return GraphQlMapperUtils.mapYield(fixedIncome, FixedIncome::getInterestRate);
  }

}
