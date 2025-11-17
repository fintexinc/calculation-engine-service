package com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns;

import com.fintex.smclient.graphql.Currency;
import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.SeparatelyManagedAccount;
import com.fintex.smclient.graphql.SeparatelyManagedAccountQuery;
import com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants;
import com.fintex.ce.dto.holding.SmaHolding;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.repository.graphql.query.endpoint.core.SeparatelyManagedAccountAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;
import java.util.Optional;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.SEPARATELY_MANAGED_ACCOUNT;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MONTHLY_RETURNS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MonthlyReturnsSeparatelyManagedAccountEndpoint extends SeparatelyManagedAccountAbstractEndpoint<RMonthlyReturns> {

    public MonthlyReturnsSeparatelyManagedAccountEndpoint() {
        super(
                GraphQlEndpointConstants.GET_SEPARATELY_MANAGED_ACCOUNT_BY,
                List.of(),
                buildCacheName(MONTHLY_RETURNS, SEPARATELY_MANAGED_ACCOUNT)
        );
    }

    @Override
    public SeparatelyManagedAccountQuery requestMapper(final SeparatelyManagedAccountQuery query) {
        return query
                .currency(c -> c.type().dataProvider())
                .monthlyReturns(
                        qMonthly -> qMonthly.returns(
                                qReturns -> qReturns.date().value()
                        ).dataProvider()
                )
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RMonthlyReturns responseMapper(final SeparatelyManagedAccount separatelyManagedAccount,
                                          final SmaHolding holding) {
        final String currency = Optional.ofNullable(separatelyManagedAccount.getCurrency())
                .map(Currency::getType)
                .map(CurrencyType::name)
                .orElse(holding.getCurrency());

        return GraphQlMapperUtils.monthlyReturns(separatelyManagedAccount.getMonthlyReturns(), currency, holding);
    }

}
