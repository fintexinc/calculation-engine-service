package com.fintex.ce.adapter.graphqlclient.endpoint.averagemer;

import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.AverageMer;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaHedgeFundAbstractEndpoint;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.MANAGEMENT_FEE_DATAPOINT_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.MER;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphqlUtil.getBigDecimalOrNull;
import static com.fintex.ce.adapter.graphqlclient.util.GraphqlUtil.getDataProviderOrNull;
import static java.util.Optional.ofNullable;

public class AverageMERCanadaHedgeFundEndpoint extends CanadaHedgeFundAbstractEndpoint<AverageMer> {

  public AverageMERCanadaHedgeFundEndpoint() {
    super(GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(MER, CANADA_HEDGE_FUNDS));
  }

  @Override
  public HedgeFundQuery requestMapper(HedgeFundQuery query) {
    return query
        .managementExpenseRatio(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
        .managementFee(MANAGEMENT_FEE_DATAPOINT_QUERY_DEFINITION)
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public AverageMer responseMapper(final HedgeFund fund, final CanadaHedgeFundHolding holding) {
    final var result = new AverageMer();

    result.setMer(getBigDecimalOrNull(fund.getManagementExpenseRatio()));
    result.setActualManagementFee(getBigDecimalOrNull(fund.getManagementFee()));

    ofNullable(getDataProviderOrNull(fund.getManagementExpenseRatio()))
        .ifPresent(dp -> result.setMerProvider(dp.name()));

    ofNullable(getDataProviderOrNull(fund.getManagementFee()))
        .ifPresent(dp -> result.setActualManagementFeeProvider(dp.name()));

    return result;
  }
}
