
package com.fintex.ce.repository.graphql.query.endpoint.yield;

import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FixedIncomeQuery;
import com.fintex.ce.config.enumeration.cache.CacheCategory;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.model.redis.RYield;
import com.fintex.ce.repository.graphql.query.endpoint.core.FixedIncomeAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_FIXED_INCOME_BY_ADP_NUMBERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.YIELD;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class YieldFixedIncomeEndpoint extends FixedIncomeAbstractEndpoint<RYield> {

    public YieldFixedIncomeEndpoint() {
        super(GET_FIXED_INCOME_BY_ADP_NUMBERS, List.of(), buildCacheName(YIELD, CacheCategory.FIXED_INCOME));
    }

    @Override
    public FixedIncomeQuery requestMapper(final FixedIncomeQuery query) {
        return query
                .interestRate(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);

    }

    @Override
    public RYield responseMapper(final FixedIncome fixedIncome,
                                 final FixedIncomeHolding holding) {
        return GraphQlMapperUtils.mapYield(fixedIncome, FixedIncome::getInterestRate);
    }

}
