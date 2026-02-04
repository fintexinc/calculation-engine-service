package com.fintex.ce.adapter.graphqlclient.endpoint.averagemer;

import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.AverageMer;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.UsMutualFundAbstractEndpoint;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.MER;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphqlUtil.getBigDecimalOrNull;
import static com.fintex.ce.adapter.graphqlclient.util.GraphqlUtil.getDataProviderOrNull;
import static java.util.Optional.ofNullable;

public class AverageMERUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<AverageMer> {

  public AverageMERUsMutualFundEndpoint() {
    super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(MER, US_MUTUAL_FUNDS));
  }

  @Override
  public UsFundQuery requestMapper(final UsFundQuery query) {
    return query
        .netExpenseRatio(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
        .grossExpenseRatio(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public AverageMer responseMapper(final UsFund fund, final UsMutualFundHolding holding) {
    final var result = new AverageMer();

    result.setNetExpenseRatio(getBigDecimalOrNull(fund.getNetExpenseRatio()));
    result.setGrossExpenseRatio(getBigDecimalOrNull(fund.getGrossExpenseRatio()));

    ofNullable(getDataProviderOrNull(fund.getNetExpenseRatio()))
        .ifPresent(dp -> result.setNetExpenseRatioProvider(dp.name()));

    ofNullable(getDataProviderOrNull(fund.getGrossExpenseRatio()))
        .ifPresent(dp -> result.setGrossExpenseRatioProvider(dp.name()));

    return result;
  }
}
