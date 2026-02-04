
package com.fintex.ce.adapter.graphqlclient.endpoint.yield;

import com.fintex.smclient.graphql.SeparatelyManagedAccount;
import com.fintex.smclient.graphql.SeparatelyManagedAccountQuery;
import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.domain.model.holding.SmaHolding;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.SeparatelyManagedAccountAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_SEPARATELY_MANAGED_ACCOUNT_BY;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheNameEntity.YIELD;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class YieldSeparatelyManagedAccountEndpoint extends SeparatelyManagedAccountAbstractEndpoint<Yield> {

  public YieldSeparatelyManagedAccountEndpoint() {
    super(GET_SEPARATELY_MANAGED_ACCOUNT_BY, List.of(), buildCacheName(YIELD,
        CacheCategory.SEPARATELY_MANAGED_ACCOUNT));
  }

  @Override
  public SeparatelyManagedAccountQuery requestMapper(final SeparatelyManagedAccountQuery query) {
    return query
        .dividendYield(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);

  }

  @Override
  public Yield responseMapper(final SeparatelyManagedAccount separatelyManagedAccount,
      final SmaHolding holding) {
    return GraphQlMapperUtils.mapYield(separatelyManagedAccount, SeparatelyManagedAccount::getDividendYield);
  }

}
