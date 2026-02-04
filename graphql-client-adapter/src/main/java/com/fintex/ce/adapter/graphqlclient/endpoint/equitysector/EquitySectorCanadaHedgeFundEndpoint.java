package com.fintex.ce.adapter.graphqlclient.endpoint.equitysector;

import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaHedgeFundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_SECTOR;
import static com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorEtfCanadaEndpoint.equitySectorAllocationQueryDefinition;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquitySectorCanadaHedgeFundEndpoint extends CanadaHedgeFundAbstractEndpoint<EquitySector> {

  public EquitySectorCanadaHedgeFundEndpoint() {
    super(GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(EQUITY_SECTOR, CANADA_HEDGE_FUNDS));
  }

  @Override
  public HedgeFundQuery requestMapper(HedgeFundQuery query) {
    return query
        .equitySectorAllocation(equitySectorAllocationQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public EquitySector responseMapper(HedgeFund hedgeFund, CanadaHedgeFundHolding holding) {
    return GraphQlMapperUtils.equitySectorMapper(hedgeFund.getEquitySectorAllocation());
  }
}
