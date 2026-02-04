package com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.EtfAbstractEndpoint;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_ETF;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_STYLEBOX_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquityStyleboxExposureEtfCanadaEndpoint extends EtfAbstractEndpoint<EquityStyleboxExposure> {

  public EquityStyleboxExposureEtfCanadaEndpoint() {
    super(GET_CANADA_ETFS_BY_TICKERS, List.of(), buildCacheName(EQUITY_STYLEBOX_ALLOCATION, CANADA_ETF));
  }

  public EquityStyleboxExposureEtfCanadaEndpoint(final Function<Query, List<Etf>> getFDSEntityFunction,
      final List<DataProvider> supportedProviders,
      final String endpointName) {
    super(getFDSEntityFunction, supportedProviders, endpointName);
  }

  @Override
  public EtfQuery requestMapper(final EtfQuery query) {
    return query
        .styleBoxes(EquityStyleboxExposureEndpointUtil.getStyleBoxesQueryDefinition())
        .ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
  }

  @Override
  public EquityStyleboxExposure responseMapper(final Etf etf, final EtfHolding holding) {
    final var result = new EquityStyleboxExposure();
    if (Objects.nonNull(etf) && Objects.nonNull(etf.getStyleBoxes())) {
      return EquityStyleboxExposureEndpointUtil.getEquityStyleboxExposure(
          etf.getStyleBoxes(),
          result);
    }
    return result;
  }
}
