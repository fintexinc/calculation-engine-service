package com.fintex.ce.adapter.graphqlclient.endpoint.classificationallocation;

import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_US_ETFS_BY_TICKERS;
import static com.fintex.ce.constant.CacheCategory.US_ETF;
import static com.fintex.ce.constant.CacheNameEntity.CLASSIFICATION_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class ClassificationAllocationEtfUsEndpoint extends ClassificationAllocationEtfCanadaEndpoint {

  public ClassificationAllocationEtfUsEndpoint() {
    super(GET_US_ETFS_BY_TICKERS, List.of(), buildCacheName(CLASSIFICATION_ALLOCATION, US_ETF));
  }

  @Override
  public QueryQueryDefinition queryDefinition(final List<String> equityIdentifiers,
      final UnaryOperator<EtfQuery> preDefinedFDSQuery) {
    return q -> q.getUsEtfsByTickers(equityIdentifiers, preDefinedFDSQuery::apply);
  }

}
