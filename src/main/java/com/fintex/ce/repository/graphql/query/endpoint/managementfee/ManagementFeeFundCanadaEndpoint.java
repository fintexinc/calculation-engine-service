package com.fintex.ce.repository.graphql.query.endpoint.managementfee;

import com.fintex.smclient.graphql.FundHoldingIdentifiersCodes;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.managementfee.RManagementFee;
import com.fintex.ce.repository.graphql.query.endpoint.core.FundAbstractEndpoint;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.MANAGEMENT_FEE_DATAPOINT_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_MUTUAL_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MANAGEMENT_FEE;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphqlUtil.getBigDecimalOrNull;

public class ManagementFeeFundCanadaEndpoint extends FundAbstractEndpoint<RManagementFee> {

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
    public RManagementFee responseMapper(final FundSeries fundSeries, final FundSeriesHolding holding) {
        final var managementFee = new RManagementFee();
        Optional.ofNullable(fundSeries.getManagementFee()).ifPresent(result -> managementFee.setProvider(DataProvider.of(result.getDataProvider()).name()));
        return managementFee.setManagementFee(getBigDecimalOrNull(fundSeries.getManagementFee()));
    }
}
