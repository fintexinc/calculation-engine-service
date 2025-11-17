package com.fintex.ce.repository.graphql.query.endpoint;

import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.RSalesCharge;
import com.fintex.ce.repository.graphql.query.endpoint.core.FundAbstractEndpoint;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.SalesChargeQuery;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_SC_SC_001;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_MUTUAL_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.SALES_CHARGE;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static java.util.Objects.isNull;

public class SalesChargeEndpoint extends FundAbstractEndpoint<RSalesCharge> {

    public SalesChargeEndpoint() {
        super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(SALES_CHARGE, CANADA_MUTUAL_FUNDS));
    }

    @Override
    public FundSeriesQuery requestMapper(final FundSeriesQuery query) {
        return query
                .salesCharge(SalesChargeQuery::type)
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RSalesCharge responseMapper(final FundSeries fundSeries, final FundSeriesHolding holding) {
        final var salesCharge = fundSeries.getSalesCharge();
        var result = new RSalesCharge();
        if (isNull(salesCharge) || isNull(salesCharge.getType())) {
            result.addError(ERR_SC_SC_001.error(holding));
            return result;
        }
        return new RSalesCharge().setValue(salesCharge.getType().name());
    }

}
