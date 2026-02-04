package com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization;

import com.fintex.smclient.graphql.EquityMarketCapitalizationQueryDefinition;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.EquityMarketCapitalization;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.FundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_MARKET_CAPITALIZATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquityMarketCapitalizationFundCanadaEndpoint extends FundAbstractEndpoint<EquityMarketCapitalization> {

  public EquityMarketCapitalizationFundCanadaEndpoint() {
    super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(EQUITY_MARKET_CAPITALIZATION,
        CANADA_MUTUAL_FUNDS));
  }

  public static EquityMarketCapitalizationQueryDefinition getEquityMarketCapitalizationQueryDefinition() {
    return qE -> qE
        .values(qV -> qV
            .equityMarketCapitalization()
            .value())
        .dataProvider();
  }

  @Override
  public FundSeriesQuery requestMapper(final FundSeriesQuery query) {
    return query
        .equityMarketCapitalization(getEquityMarketCapitalizationQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public EquityMarketCapitalization responseMapper(final FundSeries fundSeries, final FundSeriesHolding holding) {
    return GraphQlMapperUtils.equityMarketCapitalizationMapper(fundSeries.getEquityMarketCapitalization());
  }
}
