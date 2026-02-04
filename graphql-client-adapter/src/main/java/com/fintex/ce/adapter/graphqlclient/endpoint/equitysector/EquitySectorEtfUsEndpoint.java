package com.fintex.ce.adapter.graphqlclient.endpoint.equitysector;

import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_US_ETFS_BY_TICKERS;
import static com.fintex.ce.constant.CacheCategory.US_ETF;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_SECTOR;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquitySectorEtfUsEndpoint extends EquitySectorEtfCanadaEndpoint {

  public EquitySectorEtfUsEndpoint() {
    super(GET_US_ETFS_BY_TICKERS, List.of(), buildCacheName(EQUITY_SECTOR, US_ETF));
  }

  @Override
  public QueryQueryDefinition queryDefinition(final List<String> equityIdentifiers,
      final UnaryOperator<EtfQuery> preDefinedFDSQuery) {
    return q -> q.getUsEtfsByTickers(equityIdentifiers, preDefinedFDSQuery::apply);
  }

}
