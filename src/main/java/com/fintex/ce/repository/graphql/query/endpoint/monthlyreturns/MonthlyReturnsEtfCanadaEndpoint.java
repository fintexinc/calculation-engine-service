package com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_ETF;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MONTHLY_RETURNS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MonthlyReturnsEtfCanadaEndpoint extends MonthlyReturnsEtfUsEndpoint {

    public MonthlyReturnsEtfCanadaEndpoint() {
        super(GET_CANADA_ETFS_BY_TICKERS, List.of(DataProvider.MORNINGSTAR), buildCacheName(MONTHLY_RETURNS, CANADA_ETF));
    }

    @Override
    public QueryQueryDefinition queryDefinition(final List<String> equityIdentifiers, final UnaryOperator<EtfQuery> preDefinedFDSQuery) {
        return q -> q.getCanadaEtfsByTickers(equityIdentifiers, preDefinedFDSQuery::apply);
    }

}
