package com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure;

import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.constant.CacheNameEntity;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.FixedIncomeStyleboxExposure;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaPooledFundAbstractEndpoint;

import java.util.List;
import java.util.Objects;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheNameEntity.FIXED_INCOME_STYLEBOX_ALLOCATION;

public class FixedIncomeStyleboxExposurePooledFundEndpoint
    extends
      CanadaPooledFundAbstractEndpoint<FixedIncomeStyleboxExposure> {

  public FixedIncomeStyleboxExposurePooledFundEndpoint() {
    super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(FIXED_INCOME_STYLEBOX_ALLOCATION,
        CacheCategory.CANADA_POOLED_FUNDS));
  }

  @Override
  public PooledFundQuery requestMapper(PooledFundQuery query) {
    return query
        .fixedIncomeStyleBoxes(FixedIncomeStyleBoxesEndpointUtil.getStyleBoxesQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);

  }

  @Override
  public FixedIncomeStyleboxExposure responseMapper(PooledFund pooledFund, CanadaPooledFundHolding holding) {
    final var result = new FixedIncomeStyleboxExposure();
    if (Objects.nonNull(pooledFund) && Objects.nonNull(pooledFund.getFixedIncomeStyleBoxes())) {
      return FixedIncomeStyleBoxesEndpointUtil.getREquityStyleboxExposure(
          pooledFund.getFixedIncomeStyleBoxes(),
          result);
    }
    return result;
  }

  public static String buildCacheName(final CacheNameEntity prefix, final CacheCategory category) {
    return Objects.requireNonNull(prefix) + "_" + Objects.requireNonNull(category);
  }
}
