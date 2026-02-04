package com.fintex.ce.adapter.graphqlclient.endpoint;

import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.SalesCharge;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.FundAbstractEndpoint;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.SalesChargeQuery;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.domain.enumeration.ExceptionCode.ERR_SC_SC_001;
import static com.fintex.ce.constant.CacheCategory.CANADA_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.SALES_CHARGE;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.toValidationError;
import static java.util.Objects.isNull;

public class SalesChargeEndpoint extends FundAbstractEndpoint<SalesCharge> {

  public SalesChargeEndpoint() {
    super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(SALES_CHARGE, CANADA_MUTUAL_FUNDS));
  }

  @Override
  public FundSeriesQuery requestMapper(final FundSeriesQuery query) {
    return query
        .salesCharge(SalesChargeQuery::type)
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public SalesCharge responseMapper(final FundSeries fundSeries, final FundSeriesHolding holding) {
    final var salesCharge = fundSeries.getSalesCharge();
    var result = new SalesCharge();
    if (isNull(salesCharge) || isNull(salesCharge.getType())) {
      result.addError(toValidationError(ERR_SC_SC_001.error(holding)));
      return result;
    }
    return new SalesCharge().setValue(salesCharge.getType().name());
  }

}
