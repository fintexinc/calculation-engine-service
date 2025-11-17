package com.fintex.ce.repository.graphql.query.endpoint.equitycountryallocation;

import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_US_ETFS_BY_TICKERS;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.US_ETF;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_COUNTRY_ALLOCATIONS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquityCountryAllocationEtfUsEndpoint extends EquityCountryAllocationEtfCanadaEndpoint {

    public EquityCountryAllocationEtfUsEndpoint() {
        super(GET_US_ETFS_BY_TICKERS, List.of(), buildCacheName(EQUITY_COUNTRY_ALLOCATIONS, US_ETF));
    }

    @Override
    public QueryQueryDefinition queryDefinition(final List<String> equityIdentifiers, final UnaryOperator<EtfQuery> preDefinedFDSQuery) {
        return q -> q.getUsEtfsByTickers(equityIdentifiers, preDefinedFDSQuery::apply);
    }

}
