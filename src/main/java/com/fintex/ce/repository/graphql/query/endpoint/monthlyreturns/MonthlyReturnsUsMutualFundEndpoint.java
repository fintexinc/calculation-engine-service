package com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns;

import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.repository.graphql.query.endpoint.core.UsMutualFundAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MONTHLY_RETURNS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MonthlyReturnsUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<RMonthlyReturns> {

    public MonthlyReturnsUsMutualFundEndpoint() {
        super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(MONTHLY_RETURNS, US_MUTUAL_FUNDS));
    }

    @Override
    public QueryQueryDefinition queryDefinition(final List<String> tickers,
                                                final UnaryOperator<UsFundQuery> preDefinedFDSQuery) {
        return q -> q.getUsFundsByTickers(tickers, preDefinedFDSQuery::apply);
    }

    @Override
    public UsFundQuery requestMapper(final UsFundQuery query) {
        return query
                .currency(c -> c.type().dataProvider())
                .monthlyReturns(
                        qMonthly -> qMonthly.returns(
                                qReturns -> qReturns.date().value()
                        ).dataProvider()
                )
                .ticker(t -> t.value().dataProvider());
    }

    @Override
    public RMonthlyReturns responseMapper(final UsFund fundSeries, final UsMutualFundHolding holding) {

        if (fundSeries.getCurrency() == null || fundSeries.getCurrency().getType() == null) {
            return GraphQlMapperUtils.monthlyReturns(fundSeries.getMonthlyReturns(), null, holding);
        }
        final CurrencyType currency = fundSeries.getCurrency().getType();
        return GraphQlMapperUtils.monthlyReturns(fundSeries.getMonthlyReturns(), currency.name(), holding);
    }



}
