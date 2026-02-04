package com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure;

import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.constant.CacheNameEntity;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaPooledFundAbstractEndpoint;

import java.util.List;
import java.util.Objects;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_STYLEBOX_ALLOCATION;

public class EquityStyleboxExposurePooledFundEndpoint extends CanadaPooledFundAbstractEndpoint<EquityStyleboxExposure> {

  public EquityStyleboxExposurePooledFundEndpoint() {
    super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(EQUITY_STYLEBOX_ALLOCATION,
        CacheCategory.CANADA_POOLED_FUNDS));
  }

  @Override
  public PooledFundQuery requestMapper(PooledFundQuery query) {
    return query
        .styleBoxes(EquityStyleboxExposureEndpointUtil.getStyleBoxesQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);

  }

  @Override
  public EquityStyleboxExposure responseMapper(PooledFund pooledFund, CanadaPooledFundHolding holding) {
    final var result = new EquityStyleboxExposure();
    if (Objects.nonNull(pooledFund) && Objects.nonNull(pooledFund.getStyleBoxes())) {
      return EquityStyleboxExposureEndpointUtil.getEquityStyleboxExposure(
          pooledFund.getStyleBoxes(),
          result);
    }
    return result;
  }

  public static String buildCacheName(final CacheNameEntity prefix, final CacheCategory category) {
    return Objects.requireNonNull(prefix) + "_" + Objects.requireNonNull(category);
  }
}
