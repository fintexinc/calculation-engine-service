package com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector;

import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FixedIncomeQuery;
import com.fintex.smclient.graphql.FixedIncomeSecuritiesAllocation;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.FixedIncomeBondSecurities;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.FixedIncomeAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_FIXED_INCOME_BY_ADP_NUMBERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.FIXED_INCOME;
import static com.fintex.ce.constant.CacheNameEntity.FIXED_INCOME_BOND_SECURITIES;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class FixedIncomeBondSectorFixedIncomeEndpoint extends FixedIncomeAbstractEndpoint<FixedIncomeBondSecurities> {

  public FixedIncomeBondSectorFixedIncomeEndpoint() {
    super(GET_FIXED_INCOME_BY_ADP_NUMBERS, List.of(), buildCacheName(FIXED_INCOME_BOND_SECURITIES, FIXED_INCOME));
  }

  @Override
  public FixedIncomeQuery requestMapper(final FixedIncomeQuery query) {
    return query
        .fixedIncomeSecuritiesAllocation(sa -> sa.allocation(a -> a.name().value()).dataProvider())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public FixedIncomeBondSecurities responseMapper(final FixedIncome fixedIncome,
      final FixedIncomeHolding holding) {
    final FixedIncomeSecuritiesAllocation fixedIncomeSecuritiesAllocation = fixedIncome
        .getFixedIncomeSecuritiesAllocation();
    return GraphQlMapperUtils.fixedIncomeBondSectorMapper(fixedIncomeSecuritiesAllocation, holding.getType());
  }

}
