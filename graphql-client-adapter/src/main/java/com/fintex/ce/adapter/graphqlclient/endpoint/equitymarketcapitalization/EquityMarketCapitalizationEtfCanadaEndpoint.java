package com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.EquityMarketCapitalization;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.EtfAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;
import java.util.function.Function;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_ETF;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_MARKET_CAPITALIZATION;
import static com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationFundCanadaEndpoint.getEquityMarketCapitalizationQueryDefinition;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquityMarketCapitalizationEtfCanadaEndpoint extends EtfAbstractEndpoint<EquityMarketCapitalization> {

  public EquityMarketCapitalizationEtfCanadaEndpoint() {
    super(GET_CANADA_ETFS_BY_TICKERS, List.of(), buildCacheName(EQUITY_MARKET_CAPITALIZATION, CANADA_ETF));
  }

  public EquityMarketCapitalizationEtfCanadaEndpoint(final Function<Query, List<Etf>> getFDSEntityFunction,
      final List<DataProvider> supportedProviders,
      final String endpointName) {
    super(getFDSEntityFunction, supportedProviders, endpointName);
  }

  @Override
  public EtfQuery requestMapper(final EtfQuery query) {
    return query
        .equityMarketCapitalization(getEquityMarketCapitalizationQueryDefinition())
        .ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
  }

  @Override
  public EquityMarketCapitalization responseMapper(final Etf etf, final EtfHolding etfHolding) {
    return GraphQlMapperUtils.equityMarketCapitalizationMapper(etf.getEquityMarketCapitalization());
  }

}
