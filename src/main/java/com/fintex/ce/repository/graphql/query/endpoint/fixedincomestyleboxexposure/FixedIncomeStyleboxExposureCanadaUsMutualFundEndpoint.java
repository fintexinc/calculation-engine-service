package com.fintex.ce.repository.graphql.query.endpoint.fixedincomestyleboxexposure;

import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RFixedIncomeStyleboxExposure;
import com.fintex.ce.repository.graphql.query.endpoint.core.UsMutualFundAbstractEndpoint;

import java.util.List;
import java.util.Objects;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.FIXED_INCOME_STYLEBOX_ALLOCATION;
import static com.fintex.ce.repository.graphql.query.endpoint.equitystyleboxexposure.EquityStyleboxExposureEndpointUtil.getStyleBoxesQueryDefinition;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class FixedIncomeStyleboxExposureCanadaUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<RFixedIncomeStyleboxExposure> {

    public FixedIncomeStyleboxExposureCanadaUsMutualFundEndpoint() {
        super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(FIXED_INCOME_STYLEBOX_ALLOCATION, US_MUTUAL_FUNDS));
    }

    @Override
    public UsFundQuery requestMapper(UsFundQuery query) {
        return query
                .styleBoxes(getStyleBoxesQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);

    }

    @Override
    public RFixedIncomeStyleboxExposure responseMapper(final UsFund usFund,
                                                       final UsMutualFundHolding holding) {
        final var result = new RFixedIncomeStyleboxExposure();
        if (Objects.nonNull(usFund) && Objects.nonNull(usFund.getFixedIncomeStyleBoxes())) {
            return FixedIncomeStyleBoxesEndpointUtil.getREquityStyleboxExposure(
                    usFund.getFixedIncomeStyleBoxes(),
                    result
            );
        }
        return result;
    }

}
