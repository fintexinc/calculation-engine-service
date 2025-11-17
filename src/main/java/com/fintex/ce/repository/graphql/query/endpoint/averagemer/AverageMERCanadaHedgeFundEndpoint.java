package com.fintex.ce.repository.graphql.query.endpoint.averagemer;

import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.model.redis.averagemer.RAverageMer;
import com.fintex.ce.repository.graphql.query.endpoint.core.CanadaHedgeFundAbstractEndpoint;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.MANAGEMENT_FEE_DATAPOINT_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MER;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphqlUtil.getBigDecimalOrNull;
import static com.fintex.ce.util.graphql.GraphqlUtil.getDataProviderOrNull;
import static java.util.Optional.ofNullable;

public class AverageMERCanadaHedgeFundEndpoint extends CanadaHedgeFundAbstractEndpoint<RAverageMer> {

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
    public RAverageMer responseMapper(final HedgeFund fund, final CanadaHedgeFundHolding holding) {
        final var result = new RAverageMer();

        result.setMer(getBigDecimalOrNull(fund.getManagementExpenseRatio()));
        result.setActualManagementFee(getBigDecimalOrNull(fund.getManagementFee()));

        ofNullable(fund.getManagementExpenseRatio())
                .ifPresent(mer -> result.setMerProvider(getDataProviderOrNull(mer)));

        ofNullable(fund.getManagementFee())
                .ifPresent(mf -> result.setActualManagementFeeProvider(getDataProviderOrNull(mf)));

        return result;
    }
}
