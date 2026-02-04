package com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns;

import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.MonthlyReturns;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaPooledFundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_POOLED_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.MONTHLY_RETURNS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MonthlyReturnsCanadaPooledFundEndpoint extends CanadaPooledFundAbstractEndpoint<MonthlyReturns> {

  public MonthlyReturnsCanadaPooledFundEndpoint() {
    super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(MONTHLY_RETURNS, CANADA_POOLED_FUNDS));
  }

  @Override
  public QueryQueryDefinition queryDefinition(final List<String> morningstarIds,
      final UnaryOperator<PooledFundQuery> preDefinedFDSQuery) {
    return q -> q.getCanadaPooledFundsByMorningstarIds(morningstarIds, preDefinedFDSQuery::apply);
  }

  @Override
  public PooledFundQuery requestMapper(final PooledFundQuery query) {
    return query
        .currency(c -> c.type().dataProvider())
        .monthlyReturns(
            qMonthly -> qMonthly.returns(
                qReturns -> qReturns.date().value()).dataProvider())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public MonthlyReturns responseMapper(final PooledFund fund, final CanadaPooledFundHolding holding) {

    if (fund.getCurrency() == null || fund.getCurrency().getType() == null) {
      return GraphQlMapperUtils.monthlyReturns(fund.getMonthlyReturns(), null, holding);
    }
    final CurrencyType currency = fund.getCurrency().getType();
    return GraphQlMapperUtils.monthlyReturns(fund.getMonthlyReturns(), currency.name(), holding);
  }

}
