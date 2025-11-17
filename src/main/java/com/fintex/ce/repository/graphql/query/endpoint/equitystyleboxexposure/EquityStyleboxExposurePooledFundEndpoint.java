package com.fintex.ce.repository.graphql.query.endpoint.equitystyleboxexposure;

import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.ce.config.enumeration.cache.CacheCategory;
import com.fintex.ce.config.enumeration.cache.CacheNameEntity;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.model.redis.REquityStyleboxExposure;
import com.fintex.ce.repository.graphql.query.endpoint.core.CanadaPooledFundAbstractEndpoint;

import java.util.List;
import java.util.Objects;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_STYLEBOX_ALLOCATION;

public class EquityStyleboxExposurePooledFundEndpoint extends CanadaPooledFundAbstractEndpoint<REquityStyleboxExposure> {

    public EquityStyleboxExposurePooledFundEndpoint() {
        super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(EQUITY_STYLEBOX_ALLOCATION, CacheCategory.CANADA_POOLED_FUNDS));
    }

    @Override
    public PooledFundQuery requestMapper(PooledFundQuery query) {
        return query
                .styleBoxes(EquityStyleboxExposureEndpointUtil.getStyleBoxesQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);

    }

    @Override
    public REquityStyleboxExposure responseMapper(PooledFund pooledFund, CanadaPooledFundHolding holding) {
        final var result = new REquityStyleboxExposure();
        if (Objects.nonNull(pooledFund) && Objects.nonNull(pooledFund.getStyleBoxes())) {
            return EquityStyleboxExposureEndpointUtil.getREquityStyleboxExposure(
                    pooledFund.getStyleBoxes(),
                    result
            );
        }
        return result;
    }

    public static String buildCacheName(final CacheNameEntity prefix, final CacheCategory category) {
        return Objects.requireNonNull(prefix) + "_" + Objects.requireNonNull(category);
    }
}
