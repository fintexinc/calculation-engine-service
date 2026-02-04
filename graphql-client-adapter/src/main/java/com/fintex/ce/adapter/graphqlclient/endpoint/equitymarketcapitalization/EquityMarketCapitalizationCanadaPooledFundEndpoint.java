package com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization;

import com.fintex.smclient.graphql.EquityMarketCapitalizationQueryDefinition;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.EquityMarketCapitalization;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaPooledFundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_POOLED_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_MARKET_CAPITALIZATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquityMarketCapitalizationCanadaPooledFundEndpoint
    extends
      CanadaPooledFundAbstractEndpoint<EquityMarketCapitalization> {

  public EquityMarketCapitalizationCanadaPooledFundEndpoint() {
    super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(EQUITY_MARKET_CAPITALIZATION,
        CANADA_POOLED_FUNDS));
  }

  static EquityMarketCapitalizationQueryDefinition getEquityMarketCapitalizationQueryDefinition() {
    return qE -> qE
        .values(qV -> qV
            .equityMarketCapitalization()
            .value())
        .dataProvider();
  }

  @Override
  public PooledFundQuery requestMapper(PooledFundQuery query) {
    return query
        .equityMarketCapitalization(getEquityMarketCapitalizationQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public EquityMarketCapitalization responseMapper(PooledFund fund, CanadaPooledFundHolding holding) {
    return GraphQlMapperUtils.equityMarketCapitalizationMapper(fund.getEquityMarketCapitalization());
  }
}
