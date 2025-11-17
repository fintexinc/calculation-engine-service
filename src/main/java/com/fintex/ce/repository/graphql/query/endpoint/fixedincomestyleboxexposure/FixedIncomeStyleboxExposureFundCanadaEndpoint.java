package com.fintex.ce.repository.graphql.query.endpoint.fixedincomestyleboxexposure;

import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.ce.config.enumeration.cache.CacheCategory;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.RFixedIncomeStyleboxExposure;
import com.fintex.ce.repository.graphql.query.endpoint.core.FundAbstractEndpoint;

import java.util.List;
import java.util.Objects;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.FIXED_INCOME_STYLEBOX_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class FixedIncomeStyleboxExposureFundCanadaEndpoint extends FundAbstractEndpoint<RFixedIncomeStyleboxExposure> {

    public FixedIncomeStyleboxExposureFundCanadaEndpoint() {
        super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(FIXED_INCOME_STYLEBOX_ALLOCATION, CacheCategory.CANADA_MUTUAL_FUNDS));
    }

    @Override
    public FundSeriesQuery requestMapper(FundSeriesQuery query) {
        return query
                .fixedIncomeStyleBoxes(FixedIncomeStyleBoxesEndpointUtil.getStyleBoxesQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RFixedIncomeStyleboxExposure responseMapper(FundSeries fundSeries, FundSeriesHolding holding) {
        final var result = new RFixedIncomeStyleboxExposure();
        if (Objects.nonNull(fundSeries) && Objects.nonNull(fundSeries.getFixedIncomeStyleBoxes())) {
            return FixedIncomeStyleBoxesEndpointUtil.getREquityStyleboxExposure(
                    fundSeries.getFixedIncomeStyleBoxes(),
                    result
            );
        }
        return result;
    }
}
