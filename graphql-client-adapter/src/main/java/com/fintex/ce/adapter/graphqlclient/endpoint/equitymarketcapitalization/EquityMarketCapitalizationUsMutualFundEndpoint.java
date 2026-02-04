package com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization;

import com.fintex.smclient.graphql.EquityMarketCapitalizationQueryDefinition;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.EquityMarketCapitalization;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.UsMutualFundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_MARKET_CAPITALIZATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquityMarketCapitalizationUsMutualFundEndpoint
    extends
      UsMutualFundAbstractEndpoint<EquityMarketCapitalization> {

  public EquityMarketCapitalizationUsMutualFundEndpoint() {
    super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(EQUITY_MARKET_CAPITALIZATION, US_MUTUAL_FUNDS));
  }

  static EquityMarketCapitalizationQueryDefinition getEquityMarketCapitalizationQueryDefinition() {
    return qE -> qE
        .values(qV -> qV
            .equityMarketCapitalization()
            .value())
        .dataProvider();
  }

  @Override
  public UsFundQuery requestMapper(UsFundQuery query) {
    return query
        .equityMarketCapitalization(getEquityMarketCapitalizationQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public EquityMarketCapitalization responseMapper(UsFund fund, UsMutualFundHolding holding) {
    return GraphQlMapperUtils.equityMarketCapitalizationMapper(fund.getEquityMarketCapitalization());
  }
}
