package com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns;

import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.MonthlyReturns;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaHedgeFundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.MONTHLY_RETURNS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MonthlyReturnsCanadaHedgeFundEndpoint extends CanadaHedgeFundAbstractEndpoint<MonthlyReturns> {

  public MonthlyReturnsCanadaHedgeFundEndpoint() {
    super(GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(MONTHLY_RETURNS, CANADA_HEDGE_FUNDS));
  }

  @Override
  public QueryQueryDefinition queryDefinition(List<String> morningstarIds,
      UnaryOperator<HedgeFundQuery> preDefinedFDSQuery) {
    return q -> q.getCanadaHedgeFundsByMorningstarIds(morningstarIds, preDefinedFDSQuery::apply);
  }

  @Override
  public HedgeFundQuery requestMapper(HedgeFundQuery query) {
    return query
        .currency(c -> c.type().dataProvider())
        .monthlyReturns(
            qMonthly -> qMonthly.returns(
                qReturns -> qReturns.date().value()).dataProvider())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public MonthlyReturns responseMapper(HedgeFund fund, CanadaHedgeFundHolding holding) {

    if (fund.getCurrency() == null || fund.getCurrency().getType() == null) {
      return GraphQlMapperUtils.monthlyReturns(fund.getMonthlyReturns(), null, holding);
    }
    CurrencyType currency = fund.getCurrency().getType();
    return GraphQlMapperUtils.monthlyReturns(fund.getMonthlyReturns(), currency.name(), holding);
  }

}
