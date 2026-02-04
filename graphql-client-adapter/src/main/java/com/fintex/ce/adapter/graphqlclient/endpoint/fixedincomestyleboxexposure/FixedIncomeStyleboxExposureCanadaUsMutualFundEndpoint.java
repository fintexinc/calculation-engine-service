package com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure;

import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.FixedIncomeStyleboxExposure;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.UsMutualFundAbstractEndpoint;

import java.util.List;
import java.util.Objects;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.FIXED_INCOME_STYLEBOX_ALLOCATION;
import static com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure.EquityStyleboxExposureEndpointUtil.getStyleBoxesQueryDefinition;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class FixedIncomeStyleboxExposureCanadaUsMutualFundEndpoint
    extends
      UsMutualFundAbstractEndpoint<FixedIncomeStyleboxExposure> {

  public FixedIncomeStyleboxExposureCanadaUsMutualFundEndpoint() {
    super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(FIXED_INCOME_STYLEBOX_ALLOCATION, US_MUTUAL_FUNDS));
  }

  @Override
  public UsFundQuery requestMapper(UsFundQuery query) {
    return query
        .styleBoxes(getStyleBoxesQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);

  }

  @Override
  public FixedIncomeStyleboxExposure responseMapper(final UsFund usFund,
      final UsMutualFundHolding holding) {
    final var result = new FixedIncomeStyleboxExposure();
    if (Objects.nonNull(usFund) && Objects.nonNull(usFund.getFixedIncomeStyleBoxes())) {
      return FixedIncomeStyleBoxesEndpointUtil.getREquityStyleboxExposure(
          usFund.getFixedIncomeStyleBoxes(),
          result);
    }
    return result;
  }

}
