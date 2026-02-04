package com.fintex.ce.adapter.graphqlclient.endpoint.managementfee;

import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.ManagementFee;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaHedgeFundAbstractEndpoint;

import java.util.List;
import java.util.Optional;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.MANAGEMENT_FEE_DATAPOINT_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.MANAGEMENT_FEE;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphqlUtil.getBigDecimalOrNull;

public class ManagementFeeCanadaHedgeFundEndpoint extends CanadaHedgeFundAbstractEndpoint<ManagementFee> {

  public ManagementFeeCanadaHedgeFundEndpoint() {
    super(GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(MANAGEMENT_FEE, CANADA_HEDGE_FUNDS));
  }

  @Override
  public HedgeFundQuery requestMapper(final HedgeFundQuery query) {
    return query
        .managementFee(MANAGEMENT_FEE_DATAPOINT_QUERY_DEFINITION)
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public ManagementFee responseMapper(final HedgeFund fund, final CanadaHedgeFundHolding holding) {
    final var managementFee = new ManagementFee();
    Optional.ofNullable(fund.getManagementFee())
        .ifPresent(result -> managementFee.setProvider(DataProvider.of(result.getDataProvider().name()).name()));
    return managementFee.setManagementFee(getBigDecimalOrNull(fund.getManagementFee()));
  }
}
