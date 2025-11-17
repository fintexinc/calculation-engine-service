package com.fintex.ce.repository.graphql.query.endpoint.creditquality;

import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FixedIncomeQuery;
import com.fintex.smclient.graphql.StringDatapoint;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.model.redis.RCreditQuality;
import com.fintex.ce.repository.graphql.query.endpoint.core.FixedIncomeAbstractEndpoint;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_FIXED_INCOME_BY_ADP_NUMBERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.FIXED_INCOME;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.CREDIT_QUALITY;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class CreditQualityFixedIncomeEndpoint extends FixedIncomeAbstractEndpoint<RCreditQuality> {

    public CreditQualityFixedIncomeEndpoint() {
        super(GET_FIXED_INCOME_BY_ADP_NUMBERS, List.of(), buildCacheName(CREDIT_QUALITY, FIXED_INCOME));
    }

    @Override
    public FixedIncomeQuery requestMapper(final FixedIncomeQuery query) {
        return query
                .creditRating(STRING_WITH_DATA_PROVIDER_DEFINITION)
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RCreditQuality responseMapper(final FixedIncome fixedIncome,
                                         final FixedIncomeHolding holding) {
        final StringDatapoint creditRating = fixedIncome.getCreditRating();
        final Map<String, BigDecimal> allocations = getAllocations(creditRating);
        return new RCreditQuality(holding.getType(), allocations);
    }

    private Map<String, BigDecimal> getAllocations(final StringDatapoint creditRating) {
        return Optional.ofNullable(creditRating)
                .map(StringDatapoint::getValue)
                .map(rating -> Map.of(rating, BigDecimal.ONE))
                .orElse(Collections.emptyMap());
    }

}
