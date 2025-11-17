package com.fintex.ce.repository.graphql.query.endpoint.averagemer;

import com.fintex.smclient.graphql.FundHoldingIdentifiersCodes;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.averagemer.RAverageMer;
import com.fintex.ce.repository.graphql.query.endpoint.core.FundAbstractEndpoint;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.MANAGEMENT_FEE_DATAPOINT_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_MUTUAL_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MER;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphqlUtil.getBigDecimalOrNull;
import static com.fintex.ce.util.graphql.GraphqlUtil.getDataProviderOrNull;
import static java.util.Optional.ofNullable;

public class AverageMERFundCanadaEndpoint extends FundAbstractEndpoint<RAverageMer> {

    public AverageMERFundCanadaEndpoint() {
        super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(MER, CANADA_MUTUAL_FUNDS));
    }

    @Override
    public QueryQueryDefinition queryDefinition(List<FundHoldingIdentifiersCodes> identifiersCodes, UnaryOperator<FundSeriesQuery> preDefinedFDSQuery) {
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
    public RAverageMer responseMapper(final FundSeries fundSeries, final FundSeriesHolding holding) {
        final var result = new RAverageMer();
        result.setMer(getBigDecimalOrNull(fundSeries.getManagementExpenseRatio()));
        result.setActualManagementFee(getBigDecimalOrNull(fundSeries.getManagementFee()));

        ofNullable(fundSeries.getManagementExpenseRatio())
                .ifPresent(mer -> result.setMerProvider(getDataProviderOrNull(mer)));

        ofNullable(fundSeries.getManagementFee())
                .ifPresent(mf -> result.setActualManagementFeeProvider(getDataProviderOrNull(mf)));

        return result;
    }
}
