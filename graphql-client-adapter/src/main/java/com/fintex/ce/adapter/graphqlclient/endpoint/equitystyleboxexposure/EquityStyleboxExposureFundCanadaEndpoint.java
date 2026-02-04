package com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure;

import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.FundAbstractEndpoint;

import java.util.List;
import java.util.Objects;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_STYLEBOX_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquityStyleboxExposureFundCanadaEndpoint extends FundAbstractEndpoint<EquityStyleboxExposure> {

  public EquityStyleboxExposureFundCanadaEndpoint() {
    super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(EQUITY_STYLEBOX_ALLOCATION,
        CacheCategory.CANADA_MUTUAL_FUNDS));
  }

  @Override
  public FundSeriesQuery requestMapper(FundSeriesQuery query) {
    return query
        .styleBoxes(EquityStyleboxExposureEndpointUtil.getStyleBoxesQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public EquityStyleboxExposure responseMapper(FundSeries fundSeries, FundSeriesHolding holding) {
    final var result = new EquityStyleboxExposure();
    if (Objects.nonNull(fundSeries) && Objects.nonNull(fundSeries.getStyleBoxes())) {
      return EquityStyleboxExposureEndpointUtil.getEquityStyleboxExposure(
          fundSeries.getStyleBoxes(),
          result);
    }
    return result;
  }
}
