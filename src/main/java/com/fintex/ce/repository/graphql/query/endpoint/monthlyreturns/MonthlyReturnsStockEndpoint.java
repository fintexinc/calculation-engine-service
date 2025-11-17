package com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns;

import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.repository.graphql.query.endpoint.core.StockAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.EquityIdentifiers;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.Stock;
import com.fintex.smclient.graphql.StockQuery;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.STOCKS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MONTHLY_RETURNS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MonthlyReturnsStockEndpoint extends StockAbstractEndpoint<RMonthlyReturns> {

    public MonthlyReturnsStockEndpoint() {
        super(GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS, List.of(), buildCacheName(MONTHLY_RETURNS, STOCKS));
    }

    @Override
    public QueryQueryDefinition queryDefinition(List<EquityIdentifiers> equityIdentifiers, UnaryOperator<StockQuery> preDefinedFDSQuery) {
        return q -> q.getStocksByTickersAndExchangeIds(equityIdentifiers, preDefinedFDSQuery::apply);
    }

    @Override
    public StockQuery requestMapper(final StockQuery query) {
        return query
                .currency(a -> a.type().type())
                .monthlyMarketReturns(
                        qMonthly -> qMonthly.returns(
                                qReturns -> qReturns.value().date()
                        ).dataProvider()
                )
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RMonthlyReturns responseMapper(final Stock stock, final StockHolding holding) {
        if (stock.getCurrency() == null
                || stock.getCurrency().getType() == null) {
            return GraphQlMapperUtils.monthlyReturns(stock.getMonthlyMarketReturns(), null, holding);
        }
        final CurrencyType currency = stock.getCurrency().getType();
        return GraphQlMapperUtils.monthlyReturns(stock.getMonthlyMarketReturns(), currency.name(), holding);
    }
}
