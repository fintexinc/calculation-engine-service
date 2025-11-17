
package com.fintex.ce.repository.graphql.query.endpoint.yield;

import com.fintex.smclient.graphql.SeparatelyManagedAccount;
import com.fintex.smclient.graphql.SeparatelyManagedAccountQuery;
import com.fintex.ce.config.enumeration.cache.CacheCategory;
import com.fintex.ce.dto.holding.SmaHolding;
import com.fintex.ce.model.redis.RYield;
import com.fintex.ce.repository.graphql.query.endpoint.core.SeparatelyManagedAccountAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_SEPARATELY_MANAGED_ACCOUNT_BY;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.YIELD;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class YieldSeparatelyManagedAccountEndpoint extends SeparatelyManagedAccountAbstractEndpoint<RYield> {

    public YieldSeparatelyManagedAccountEndpoint() {
        super(GET_SEPARATELY_MANAGED_ACCOUNT_BY, List.of(), buildCacheName(YIELD, CacheCategory.SEPARATELY_MANAGED_ACCOUNT));
    }

    @Override
    public SeparatelyManagedAccountQuery requestMapper(final SeparatelyManagedAccountQuery query) {
        return query
                .dividendYield(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);

    }

    @Override
    public RYield responseMapper(final SeparatelyManagedAccount separatelyManagedAccount,
                                 final SmaHolding holding) {
        return GraphQlMapperUtils.mapYield(separatelyManagedAccount, SeparatelyManagedAccount::getDividendYield);
    }

}
