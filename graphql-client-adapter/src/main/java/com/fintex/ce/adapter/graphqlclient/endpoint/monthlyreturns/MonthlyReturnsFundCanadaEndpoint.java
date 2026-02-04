package com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.MonthlyReturns;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.FundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.FundHoldingIdentifiersCodes;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.MONTHLY_RETURNS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MonthlyReturnsFundCanadaEndpoint extends FundAbstractEndpoint<MonthlyReturns> {

  public MonthlyReturnsFundCanadaEndpoint() {
    super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(DataProvider.MORNINGSTAR), buildCacheName(MONTHLY_RETURNS,
        CANADA_MUTUAL_FUNDS));
  }

  @Override
  public QueryQueryDefinition queryDefinition(final List<FundHoldingIdentifiersCodes> identifiersCodes,
      final UnaryOperator<FundSeriesQuery> preDefinedFDSQuery) {
    return q -> q.getFundSeriesByHoldingCodes(identifiersCodes, preDefinedFDSQuery::apply);
  }

  @Override
  public FundSeriesQuery requestMapper(final FundSeriesQuery query) {
    return query
        .currency(p -> p.permittedDataProviders(loadProviders()), c -> c.type().dataProvider())
        .monthlyReturns(
            qMonthly -> qMonthly.returns(
                qReturns -> qReturns.date().value()).dataProvider())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public MonthlyReturns responseMapper(final FundSeries fundSeries, final FundSeriesHolding holding) {

    if (fundSeries.getCurrency() == null || fundSeries.getCurrency().getType() == null) {
      return GraphQlMapperUtils.monthlyReturns(fundSeries.getMonthlyReturns(), null, holding);
    }
    final CurrencyType currency = fundSeries.getCurrency().getType();
    return GraphQlMapperUtils.monthlyReturns(fundSeries.getMonthlyReturns(), currency.name(), holding);
  }
}
