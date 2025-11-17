package com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns;

import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.repository.graphql.query.endpoint.core.CanadaHedgeFundAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MONTHLY_RETURNS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MonthlyReturnsCanadaHedgeFundEndpoint extends CanadaHedgeFundAbstractEndpoint<RMonthlyReturns> {

    public MonthlyReturnsCanadaHedgeFundEndpoint() {
        super(GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(MONTHLY_RETURNS, CANADA_HEDGE_FUNDS));
    }

    @Override
    public QueryQueryDefinition queryDefinition(List<String> morningstarIds,
                                                UnaryOperator<HedgeFundQuery> preDefinedFDSQuery) {
        return q -> q.getCanadaHedgeFundsByMorningstarIds(morningstarIds, preDefinedFDSQuery::apply);
    }

    @Override
    public HedgeFundQuery requestMapper(HedgeFundQuery query) {
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
    public RMonthlyReturns responseMapper(HedgeFund fund, CanadaHedgeFundHolding holding) {

        if (fund.getCurrency() == null || fund.getCurrency().getType() == null) {
            return GraphQlMapperUtils.monthlyReturns(fund.getMonthlyReturns(), null, holding);
        }
        CurrencyType currency = fund.getCurrency().getType();
        return GraphQlMapperUtils.monthlyReturns(fund.getMonthlyReturns(), currency.name(), holding);
    }



}
