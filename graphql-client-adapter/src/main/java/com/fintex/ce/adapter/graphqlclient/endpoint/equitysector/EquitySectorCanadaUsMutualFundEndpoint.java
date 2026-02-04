package com.fintex.ce.adapter.graphqlclient.endpoint.equitysector;

import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.UsMutualFundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_SECTOR;
import static com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorEtfCanadaEndpoint.equitySectorAllocationQueryDefinition;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquitySectorCanadaUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<EquitySector> {

  public EquitySectorCanadaUsMutualFundEndpoint() {
    super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(EQUITY_SECTOR, US_MUTUAL_FUNDS));
  }

  @Override
  public UsFundQuery requestMapper(UsFundQuery query) {
    return query
        .equitySectorAllocation(equitySectorAllocationQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public EquitySector responseMapper(UsFund pooledFund, UsMutualFundHolding holding) {
    return GraphQlMapperUtils.equitySectorMapper(pooledFund.getEquitySectorAllocation());
  }
}
