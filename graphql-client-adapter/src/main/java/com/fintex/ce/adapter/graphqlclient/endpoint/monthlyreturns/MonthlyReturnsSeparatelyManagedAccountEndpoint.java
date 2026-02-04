package com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns;

import com.fintex.smclient.graphql.Currency;
import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.SeparatelyManagedAccount;
import com.fintex.smclient.graphql.SeparatelyManagedAccountQuery;
import com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants;
import com.fintex.ce.domain.model.holding.SmaHolding;
import com.fintex.ce.domain.model.MonthlyReturns;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.SeparatelyManagedAccountAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;
import java.util.Optional;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.SEPARATELY_MANAGED_ACCOUNT;
import static com.fintex.ce.constant.CacheNameEntity.MONTHLY_RETURNS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MonthlyReturnsSeparatelyManagedAccountEndpoint
    extends
      SeparatelyManagedAccountAbstractEndpoint<MonthlyReturns> {

  public MonthlyReturnsSeparatelyManagedAccountEndpoint() {
    super(
        GraphQlEndpointConstants.GET_SEPARATELY_MANAGED_ACCOUNT_BY,
        List.of(),
        buildCacheName(MONTHLY_RETURNS, SEPARATELY_MANAGED_ACCOUNT));
  }

  @Override
  public SeparatelyManagedAccountQuery requestMapper(final SeparatelyManagedAccountQuery query) {
    return query
        .currency(c -> c.type().dataProvider())
        .monthlyReturns(
            qMonthly -> qMonthly.returns(
                qReturns -> qReturns.date().value()).dataProvider())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public MonthlyReturns responseMapper(final SeparatelyManagedAccount separatelyManagedAccount,
      final SmaHolding holding) {
    final String currency = Optional.ofNullable(separatelyManagedAccount.getCurrency())
        .map(Currency::getType)
        .map(CurrencyType::name)
        .orElse(holding.getCurrency());

    return GraphQlMapperUtils.monthlyReturns(separatelyManagedAccount.getMonthlyReturns(), currency, holding);
  }

}
