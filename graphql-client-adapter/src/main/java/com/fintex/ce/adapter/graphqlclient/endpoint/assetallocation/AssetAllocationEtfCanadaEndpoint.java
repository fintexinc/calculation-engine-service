package com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation;

import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.EtfAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.constant.CacheCategory.CANADA_ETF;
import static com.fintex.ce.constant.CacheNameEntity.ASSET_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class AssetAllocationEtfCanadaEndpoint extends EtfAbstractEndpoint<com.fintex.ce.domain.model.AssetAllocation> {

  public AssetAllocationEtfCanadaEndpoint() {
    super(GET_CANADA_ETFS_BY_TICKERS, List.of(), buildCacheName(ASSET_ALLOCATION, CANADA_ETF));
  }

  public AssetAllocationEtfCanadaEndpoint(Function<Query, List<Etf>> getFDSEntityFunction,
      List<DataProvider> supportedProviders,
      String endpointName) {
    super(getFDSEntityFunction, supportedProviders, endpointName);
  }

  @Override
  public QueryQueryDefinition queryDefinition(final List<String> equityIdentifiers,
      final UnaryOperator<EtfQuery> preDefinedFDSQuery) {
    return q -> q.getCanadaEtfsByTickers(equityIdentifiers, preDefinedFDSQuery::apply);
  }

  @Override
  public EtfQuery requestMapper(EtfQuery query) {
    return query
        .assetAllocation(al -> al
            .allocation(p -> p.name().value())
            .dataProvider())
        .ticker(t -> t.value().dataProvider());
  }

  @Override
  public com.fintex.ce.domain.model.AssetAllocation responseMapper(Etf etf, EtfHolding holding) {
    final AssetAllocation assetAllocation = etf.getAssetAllocation();
    return GraphQlMapperUtils.assetAllocation(assetAllocation, holding.getType());
  }
}
