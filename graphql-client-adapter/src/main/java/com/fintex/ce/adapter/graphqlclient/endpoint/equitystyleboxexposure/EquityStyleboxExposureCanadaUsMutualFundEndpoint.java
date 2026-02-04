package com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure;

import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.UsMutualFundAbstractEndpoint;

import java.util.List;
import java.util.Objects;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_SECTOR;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquityStyleboxExposureCanadaUsMutualFundEndpoint
    extends
      UsMutualFundAbstractEndpoint<EquityStyleboxExposure> {

  public EquityStyleboxExposureCanadaUsMutualFundEndpoint() {
    super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(EQUITY_SECTOR, US_MUTUAL_FUNDS));
  }

  @Override
  public UsFundQuery requestMapper(final UsFundQuery query) {
    return query
        .styleBoxes(EquityStyleboxExposureEndpointUtil.getStyleBoxesQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public EquityStyleboxExposure responseMapper(final UsFund usFund,
      final UsMutualFundHolding holding) {
    final var result = new EquityStyleboxExposure();
    if (Objects.nonNull(usFund) && Objects.nonNull(usFund.getStyleBoxes())) {
      return EquityStyleboxExposureEndpointUtil.getEquityStyleboxExposure(
          usFund.getStyleBoxes(),
          result);
    }
    return result;
  }

}
