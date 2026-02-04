package com.fintex.ce.adapter.graphqlclient.endpoint.equitysector;

import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaPooledFundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_POOLED_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_SECTOR;
import static com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorEtfCanadaEndpoint.equitySectorAllocationQueryDefinition;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquitySectorCanadaPooledFundEndpoint extends CanadaPooledFundAbstractEndpoint<EquitySector> {

  public EquitySectorCanadaPooledFundEndpoint() {
    super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(EQUITY_SECTOR, CANADA_POOLED_FUNDS));
  }

  @Override
  public PooledFundQuery requestMapper(PooledFundQuery query) {
    return query
        .equitySectorAllocation(equitySectorAllocationQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public EquitySector responseMapper(PooledFund pooledFund, CanadaPooledFundHolding holding) {
    return GraphQlMapperUtils.equitySectorMapper(pooledFund.getEquitySectorAllocation());
  }
}
