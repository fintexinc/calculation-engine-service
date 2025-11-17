package com.fintex.ce.repository.graphql.query.endpoint.averagemer;

import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.averagemer.RAverageMer;
import com.fintex.ce.repository.graphql.query.endpoint.core.UsMutualFundAbstractEndpoint;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MER;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphqlUtil.getBigDecimalOrNull;
import static com.fintex.ce.util.graphql.GraphqlUtil.getDataProviderOrNull;
import static java.util.Optional.ofNullable;

public class AverageMERUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<RAverageMer> {

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
    public RAverageMer responseMapper(final UsFund fund, final UsMutualFundHolding holding) {
        final var result = new RAverageMer();

        result.setNetExpenseRatio(getBigDecimalOrNull(fund.getNetExpenseRatio()));
        result.setGrossExpenseRatio(getBigDecimalOrNull(fund.getGrossExpenseRatio()));

        ofNullable(fund.getNetExpenseRatio())
                .ifPresent(net -> result.setNetExpenseRatioProvider(getDataProviderOrNull(net)));

        ofNullable(fund.getGrossExpenseRatio())
                .ifPresent(gross -> result.setGrossExpenseRatioProvider(getDataProviderOrNull(gross)));

        return result;
    }
}
