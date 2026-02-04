package com.fintex.ce.adapter.graphqlclient.endpoint.managementfee;

import com.fintex.smclient.graphql.FundHoldingIdentifiersCodes;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.ManagementFee;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.FundAbstractEndpoint;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.MANAGEMENT_FEE_DATAPOINT_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.MANAGEMENT_FEE;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphqlUtil.getBigDecimalOrNull;

public class ManagementFeeFundCanadaEndpoint extends FundAbstractEndpoint<ManagementFee> {

  public ManagementFeeFundCanadaEndpoint() {
    super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(MANAGEMENT_FEE, CANADA_MUTUAL_FUNDS));
  }

  @Override
  public QueryQueryDefinition queryDefinition(final List<FundHoldingIdentifiersCodes> identifiersCodes,
      final UnaryOperator<FundSeriesQuery> preDefinedFDSQuery) {
    return q -> q.getFundSeriesByHoldingCodes(identifiersCodes, preDefinedFDSQuery::apply);
  }

  @Override
  public FundSeriesQuery requestMapper(final FundSeriesQuery query) {
    return query
        .managementFee(MANAGEMENT_FEE_DATAPOINT_QUERY_DEFINITION)
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public ManagementFee responseMapper(final FundSeries fundSeries, final FundSeriesHolding holding) {
    final var managementFee = new ManagementFee();
    Optional.ofNullable(fundSeries.getManagementFee()).ifPresent(result -> managementFee.setProvider(DataProvider.of(
        result.getDataProvider().name()).name()));
    return managementFee.setManagementFee(getBigDecimalOrNull(fundSeries.getManagementFee()));
  }
}
