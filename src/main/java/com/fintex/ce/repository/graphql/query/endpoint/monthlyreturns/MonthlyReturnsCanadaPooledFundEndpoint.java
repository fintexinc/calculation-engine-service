package com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns;

import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.repository.graphql.query.endpoint.core.CanadaPooledFundAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_POOLED_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MONTHLY_RETURNS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MonthlyReturnsCanadaPooledFundEndpoint extends CanadaPooledFundAbstractEndpoint<RMonthlyReturns> {

    public MonthlyReturnsCanadaPooledFundEndpoint() {
        super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(MONTHLY_RETURNS, CANADA_POOLED_FUNDS));
    }

    @Override
    public QueryQueryDefinition queryDefinition(final List<String> morningstarIds,
                                                final UnaryOperator<PooledFundQuery> preDefinedFDSQuery) {
        return q -> q.getCanadaPooledFundsByMorningstarIds(morningstarIds, preDefinedFDSQuery::apply);
    }

    @Override
    public PooledFundQuery requestMapper(final PooledFundQuery query) {
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
    public RMonthlyReturns responseMapper(final PooledFund fund, final CanadaPooledFundHolding holding) {

        if (fund.getCurrency() == null || fund.getCurrency().getType() == null) {
            return GraphQlMapperUtils.monthlyReturns(fund.getMonthlyReturns(), null, holding);
        }
        final CurrencyType currency = fund.getCurrency().getType();
        return GraphQlMapperUtils.monthlyReturns(fund.getMonthlyReturns(), currency.name(), holding);
    }



}
