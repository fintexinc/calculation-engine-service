package com.fintex.ce.repository.graphql.query.endpoint.maturityallocation;

import com.fintex.smclient.graphql.MaturityDurationValue;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.ce.config.enumeration.cache.CacheCategory;
import com.fintex.ce.config.enumeration.cache.CacheNameEntity;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.model.redis.RMaturityAllocation;
import com.fintex.ce.repository.graphql.query.endpoint.core.CanadaPooledFundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MATURITY_ALLOCATION;
import static com.fintex.ce.repository.graphql.query.endpoint.maturityallocation.MaturityAllocationFundCanadaEndpoint.getMaturitiesQueryDefinition;

public class MaturityAllocationPooledFundEndpoint extends CanadaPooledFundAbstractEndpoint<RMaturityAllocation> {

    public MaturityAllocationPooledFundEndpoint() {
        super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(MATURITY_ALLOCATION, CacheCategory.CANADA_POOLED_FUNDS));
    }

    @Override
    public PooledFundQuery requestMapper(PooledFundQuery query) {
        return query
                .maturities(getMaturitiesQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);

    }

    @Override
    public RMaturityAllocation responseMapper(PooledFund pooledFund, CanadaPooledFundHolding holding) {
        final var rMaturityAllocation = new RMaturityAllocation();
        if(Objects.isNull(pooledFund.getMaturities()) || Objects.isNull(pooledFund.getMaturities().getPeriods())) {
            return rMaturityAllocation;
        }
        Map<String, BigDecimal> maturityDurationValues = pooledFund.getMaturities().getPeriods().stream()
                .filter(maturityDurationValue -> maturityDurationValue != null && maturityDurationValue.getValue() != null)
                .collect(Collectors.toMap(
                        maturityDurationType -> maturityDurationType.getMaturityDuration().toString(),
                        MaturityDurationValue::getValue
                ));

        rMaturityAllocation.setMaturityDurationValues(maturityDurationValues);
        return rMaturityAllocation;
    }

    public static String buildCacheName(final CacheNameEntity prefix, final CacheCategory category) {
        return Objects.requireNonNull(prefix) + "_" + Objects.requireNonNull(category);
    }
}
