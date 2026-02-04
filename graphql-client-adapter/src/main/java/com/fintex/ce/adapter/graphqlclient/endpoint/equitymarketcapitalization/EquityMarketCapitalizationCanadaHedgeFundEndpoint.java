package com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization;

import com.fintex.smclient.graphql.EquityMarketCapitalizationQueryDefinition;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.EquityMarketCapitalization;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaHedgeFundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_MARKET_CAPITALIZATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquityMarketCapitalizationCanadaHedgeFundEndpoint
    extends
      CanadaHedgeFundAbstractEndpoint<EquityMarketCapitalization> {

  public EquityMarketCapitalizationCanadaHedgeFundEndpoint() {
    super(GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(EQUITY_MARKET_CAPITALIZATION,
        CANADA_HEDGE_FUNDS));
  }

  static EquityMarketCapitalizationQueryDefinition getEquityMarketCapitalizationQueryDefinition() {
    return qE -> qE
        .values(qV -> qV
            .equityMarketCapitalization()
            .value())
        .dataProvider();
  }

  @Override
  public HedgeFundQuery requestMapper(HedgeFundQuery query) {
    return query
        .equityMarketCapitalization(getEquityMarketCapitalizationQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public EquityMarketCapitalization responseMapper(HedgeFund fund, CanadaHedgeFundHolding holding) {
    return GraphQlMapperUtils.equityMarketCapitalizationMapper(fund.getEquityMarketCapitalization());
  }
}
