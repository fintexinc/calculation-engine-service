package com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns;

import com.fintex.smclient.graphql.Currency;
import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FixedIncomeQuery;
import com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.repository.graphql.query.endpoint.core.FixedIncomeAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;
import java.util.Optional;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.FIXED_INCOME;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MONTHLY_RETURNS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MonthlyReturnsFixedIncomeEndpoint extends FixedIncomeAbstractEndpoint<RMonthlyReturns> {


    public MonthlyReturnsFixedIncomeEndpoint() {
        super(
                GraphQlEndpointConstants.GET_FIXED_INCOME_BY_ADP_NUMBERS,
                List.of(),
                buildCacheName(MONTHLY_RETURNS, FIXED_INCOME)
        );
    }

    @Override
    public FixedIncomeQuery requestMapper(final FixedIncomeQuery query) {
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
    public RMonthlyReturns responseMapper(final FixedIncome fixedIncome,
                                          final FixedIncomeHolding holding) {
        final String currency = Optional.ofNullable(fixedIncome.getCurrency())
                .map(Currency::getType)
                .map(CurrencyType::name)
                .orElse(null);

        return GraphQlMapperUtils.monthlyReturns(fixedIncome.getMonthlyReturns(), currency, holding);
    }

}
