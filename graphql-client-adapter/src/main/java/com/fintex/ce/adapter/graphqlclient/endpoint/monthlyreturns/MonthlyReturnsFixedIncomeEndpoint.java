package com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns;

import com.fintex.smclient.graphql.Currency;
import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FixedIncomeQuery;
import com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.MonthlyReturns;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.FixedIncomeAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;
import java.util.Optional;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.FIXED_INCOME;
import static com.fintex.ce.constant.CacheNameEntity.MONTHLY_RETURNS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MonthlyReturnsFixedIncomeEndpoint extends FixedIncomeAbstractEndpoint<MonthlyReturns> {

  public MonthlyReturnsFixedIncomeEndpoint() {
    super(
        GraphQlEndpointConstants.GET_FIXED_INCOME_BY_ADP_NUMBERS,
        List.of(),
        buildCacheName(MONTHLY_RETURNS, FIXED_INCOME));
  }

  @Override
  public FixedIncomeQuery requestMapper(final FixedIncomeQuery query) {
    return query
        .currency(c -> c.type().dataProvider())
        .monthlyReturns(
            qMonthly -> qMonthly.returns(
                qReturns -> qReturns.date().value()).dataProvider())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public MonthlyReturns responseMapper(final FixedIncome fixedIncome,
      final FixedIncomeHolding holding) {
    final String currency = Optional.ofNullable(fixedIncome.getCurrency())
        .map(Currency::getType)
        .map(CurrencyType::name)
        .orElse(null);

    return GraphQlMapperUtils.monthlyReturns(fixedIncome.getMonthlyReturns(), currency, holding);
  }

}
