package com.fintex.ce.repository.graphql.query.endpoint.equitystyleboxexposure;

import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.ce.config.enumeration.cache.CacheCategory;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.REquityStyleboxExposure;
import com.fintex.ce.repository.graphql.query.endpoint.core.FundAbstractEndpoint;

import java.util.List;
import java.util.Objects;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_STYLEBOX_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquityStyleboxExposureFundCanadaEndpoint extends FundAbstractEndpoint<REquityStyleboxExposure> {

    public EquityStyleboxExposureFundCanadaEndpoint() {
        super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(EQUITY_STYLEBOX_ALLOCATION, CacheCategory.CANADA_MUTUAL_FUNDS));
    }

    @Override
    public FundSeriesQuery requestMapper(FundSeriesQuery query) {
        return query
                .styleBoxes(EquityStyleboxExposureEndpointUtil.getStyleBoxesQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public REquityStyleboxExposure responseMapper(FundSeries fundSeries, FundSeriesHolding holding) {
        final var result = new REquityStyleboxExposure();
        if (Objects.nonNull(fundSeries) && Objects.nonNull(fundSeries.getStyleBoxes())) {
            return EquityStyleboxExposureEndpointUtil.getREquityStyleboxExposure(
                    fundSeries.getStyleBoxes(),
                    result
            );
        }
        return result;
    }
}
