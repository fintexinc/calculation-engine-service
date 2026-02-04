package com.fintex.ce.adapter.graphqlclient.endpoint.equitysector;

import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.FundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_SECTOR;
import static com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorEtfCanadaEndpoint.equitySectorAllocationQueryDefinition;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquitySectorFundCanadaEndpoint extends FundAbstractEndpoint<EquitySector> {

  public EquitySectorFundCanadaEndpoint() {
    super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(EQUITY_SECTOR, CANADA_MUTUAL_FUNDS));
  }

  @Override
  public FundSeriesQuery requestMapper(final FundSeriesQuery query) {
    return query
        .equitySectorAllocation(equitySectorAllocationQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public EquitySector responseMapper(final FundSeries fundSeries, final FundSeriesHolding holding) {
    return GraphQlMapperUtils.equitySectorMapper(fundSeries.getEquitySectorAllocation());
  }
}
