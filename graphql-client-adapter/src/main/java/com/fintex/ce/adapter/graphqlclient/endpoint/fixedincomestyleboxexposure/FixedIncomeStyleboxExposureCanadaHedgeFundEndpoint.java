package com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure;

import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.FixedIncomeStyleboxExposure;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaHedgeFundAbstractEndpoint;

import java.util.List;
import java.util.Objects;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.FIXED_INCOME_STYLEBOX_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class FixedIncomeStyleboxExposureCanadaHedgeFundEndpoint
    extends
      CanadaHedgeFundAbstractEndpoint<FixedIncomeStyleboxExposure> {

  public FixedIncomeStyleboxExposureCanadaHedgeFundEndpoint() {
    super(GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(FIXED_INCOME_STYLEBOX_ALLOCATION,
        CANADA_HEDGE_FUNDS));
  }

  @Override
  public HedgeFundQuery requestMapper(HedgeFundQuery query) {
    return query
        .fixedIncomeStyleBoxes(FixedIncomeStyleBoxesEndpointUtil.getStyleBoxesQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public FixedIncomeStyleboxExposure responseMapper(HedgeFund hedgeFund, CanadaHedgeFundHolding holding) {
    final var result = new FixedIncomeStyleboxExposure();
    if (Objects.nonNull(hedgeFund) && Objects.nonNull(hedgeFund.getFixedIncomeStyleBoxes())) {
      return FixedIncomeStyleBoxesEndpointUtil.getREquityStyleboxExposure(
          hedgeFund.getFixedIncomeStyleBoxes(),
          result);
    }
    return result;
  }
}
