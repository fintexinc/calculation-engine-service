package com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation;

import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_US_ETFS_BY_TICKERS;
import static com.fintex.ce.constant.CacheCategory.US_ETF;
import static com.fintex.ce.constant.CacheNameEntity.ASSET_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class AssetAllocationEtfUsEndpoint extends AssetAllocationEtfCanadaEndpoint {

  public AssetAllocationEtfUsEndpoint() {
    super(GET_US_ETFS_BY_TICKERS, List.of(), buildCacheName(ASSET_ALLOCATION, US_ETF));
  }

  @Override
  public QueryQueryDefinition queryDefinition(final List<String> equityIdentifiers,
      final UnaryOperator<EtfQuery> preDefinedFDSQuery) {
    return q -> q.getUsEtfsByTickers(equityIdentifiers, preDefinedFDSQuery::apply);
  }

}
