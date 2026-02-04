package com.fintex.ce.adapter.graphqlclient.endpoint.managementfee;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.ManagementFee;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.UsMutualFundAbstractEndpoint;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;

import java.util.List;
import java.util.Optional;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.MANAGEMENT_FEE_DATAPOINT_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.MANAGEMENT_FEE;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphqlUtil.getBigDecimalOrNull;

public class ManagementFeeUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<ManagementFee> {

  public ManagementFeeUsMutualFundEndpoint() {
    super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(MANAGEMENT_FEE, US_MUTUAL_FUNDS));
  }

  @Override
  public UsFundQuery requestMapper(final UsFundQuery query) {
    return query
        .managementFee(MANAGEMENT_FEE_DATAPOINT_QUERY_DEFINITION)
        .ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
  }

  @Override
  public ManagementFee responseMapper(final UsFund fund, final UsMutualFundHolding holding) {
    final var managementFeeUsMutualFund = new ManagementFee();
    Optional.ofNullable(fund.getManagementFee()).ifPresent(result -> managementFeeUsMutualFund.setProvider(DataProvider
        .of(result.getDataProvider().name()).name()));
    return managementFeeUsMutualFund.setManagementFee(getBigDecimalOrNull(fund.getManagementFee()));
  }
}
