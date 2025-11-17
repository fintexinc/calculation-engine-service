package com.fintex.ce.repository.graphql.query.endpoint.managementfee;

import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.model.redis.managementfee.RManagementFee;
import com.fintex.ce.repository.graphql.query.endpoint.core.CanadaHedgeFundAbstractEndpoint;

import java.util.List;
import java.util.Optional;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.MANAGEMENT_FEE_DATAPOINT_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MANAGEMENT_FEE;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphqlUtil.getBigDecimalOrNull;

public class ManagementFeeCanadaHedgeFundEndpoint extends CanadaHedgeFundAbstractEndpoint<RManagementFee> {

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
    public RManagementFee responseMapper(final HedgeFund fund, final CanadaHedgeFundHolding holding) {
        final var managementFee = new RManagementFee();
        Optional.ofNullable(fund.getManagementFee())
                .ifPresent(result -> managementFee.setProvider(DataProvider.of(result.getDataProvider()).name()));
        return managementFee.setManagementFee(getBigDecimalOrNull(fund.getManagementFee()));
    }
}
