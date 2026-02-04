package com.fintex.ce.adapter.graphqlclient.endpoint.averagemer;

import com.fintex.smclient.graphql.FundHoldingIdentifiersCodes;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.AverageMer;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.FundAbstractEndpoint;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.MANAGEMENT_FEE_DATAPOINT_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.MER;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphqlUtil.getBigDecimalOrNull;
import static com.fintex.ce.adapter.graphqlclient.util.GraphqlUtil.getDataProviderOrNull;
import static java.util.Optional.ofNullable;

public class AverageMERFundCanadaEndpoint extends FundAbstractEndpoint<AverageMer> {

  public AverageMERFundCanadaEndpoint() {
    super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(MER, CANADA_MUTUAL_FUNDS));
  }

  @Override
  public QueryQueryDefinition queryDefinition(List<FundHoldingIdentifiersCodes> identifiersCodes,
      UnaryOperator<FundSeriesQuery> preDefinedFDSQuery) {
    return q -> q.getFundSeriesByHoldingCodes(identifiersCodes, preDefinedFDSQuery::apply);
  }

  @Override
  public FundSeriesQuery requestMapper(final FundSeriesQuery query) {
    return query
        .managementExpenseRatio(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
        .managementFee(MANAGEMENT_FEE_DATAPOINT_QUERY_DEFINITION)
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public AverageMer responseMapper(final FundSeries fundSeries, final FundSeriesHolding holding) {
    final var result = new AverageMer();
    result.setMer(getBigDecimalOrNull(fundSeries.getManagementExpenseRatio()));
    result.setActualManagementFee(getBigDecimalOrNull(fundSeries.getManagementFee()));

    ofNullable(getDataProviderOrNull(fundSeries.getManagementExpenseRatio()))
        .ifPresent(dp -> result.setMerProvider(dp.name()));

    ofNullable(getDataProviderOrNull(fundSeries.getManagementFee()))
        .ifPresent(dp -> result.setActualManagementFeeProvider(dp.name()));

    return result;
  }
}
