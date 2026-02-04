package com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.constant.CacheCategory.CANADA_ETF;
import static com.fintex.ce.constant.CacheNameEntity.MONTHLY_RETURNS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MonthlyReturnsEtfCanadaEndpoint extends MonthlyReturnsEtfUsEndpoint {

  public MonthlyReturnsEtfCanadaEndpoint() {
    super(GET_CANADA_ETFS_BY_TICKERS, List.of(DataProvider.MORNINGSTAR), buildCacheName(MONTHLY_RETURNS, CANADA_ETF));
  }

  @Override
  public QueryQueryDefinition queryDefinition(final List<String> equityIdentifiers,
      final UnaryOperator<EtfQuery> preDefinedFDSQuery) {
    return q -> q.getCanadaEtfsByTickers(equityIdentifiers, preDefinedFDSQuery::apply);
  }

}
