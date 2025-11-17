package com.fintex.ce.repository.graphql.query.endpoint.managementfee;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.managementfee.RManagementFee;
import com.fintex.ce.repository.graphql.query.endpoint.core.UsMutualFundAbstractEndpoint;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;

import java.util.List;
import java.util.Optional;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.MANAGEMENT_FEE_DATAPOINT_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MANAGEMENT_FEE;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphqlUtil.getBigDecimalOrNull;

public class ManagementFeeUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<RManagementFee> {

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
    public RManagementFee responseMapper(final UsFund fund, final UsMutualFundHolding holding) {
        final var managementFeeUsMutualFund = new RManagementFee();
        Optional.ofNullable(fund.getManagementFee()).ifPresent(result -> managementFeeUsMutualFund.setProvider(DataProvider.of(result.getDataProvider()).name()));
        return managementFeeUsMutualFund.setManagementFee(getBigDecimalOrNull(fund.getManagementFee()));
    }
}
