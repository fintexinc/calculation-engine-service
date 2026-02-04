package com.fintex.ce.adapter.graphqlclient.endpoint.equitysector;

import com.fintex.smclient.graphql.EquitySectorAllocationQueryDefinition;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.EtfAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;
import java.util.function.Function;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_ETF;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_SECTOR;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquitySectorEtfCanadaEndpoint extends EtfAbstractEndpoint<EquitySector> {

  public EquitySectorEtfCanadaEndpoint() {
    super(GET_CANADA_ETFS_BY_TICKERS, List.of(), buildCacheName(EQUITY_SECTOR, CANADA_ETF));
  }

  public EquitySectorEtfCanadaEndpoint(final Function<Query, List<Etf>> getFDSEntityFunction,
      final List<DataProvider> supportedProviders,
      final String endpointName) {
    super(getFDSEntityFunction, supportedProviders, endpointName);
  }

  public static EquitySectorAllocationQueryDefinition equitySectorAllocationQueryDefinition() {
    return qEquity -> qEquity
        .allocation(qAllocation -> qAllocation
            .value()
            .names(qName -> qName.languageCode().value()))
        .dataProvider();
  }

  @Override
  public EtfQuery requestMapper(final EtfQuery query) {
    return query
        .equitySectorAllocation(equitySectorAllocationQueryDefinition())
        .ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
  }

  @Override
  public EquitySector responseMapper(final Etf etf, final EtfHolding etfHolding) {
    return GraphQlMapperUtils.equitySectorMapper(etf.getEquitySectorAllocation());
  }

}
