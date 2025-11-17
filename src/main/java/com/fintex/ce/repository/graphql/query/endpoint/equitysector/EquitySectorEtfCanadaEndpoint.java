package com.fintex.ce.repository.graphql.query.endpoint.equitysector;

import com.fintex.smclient.graphql.EquitySectorAllocationQueryDefinition;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.equitysector.REquitySector;
import com.fintex.ce.repository.graphql.query.endpoint.core.EtfAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;
import java.util.function.Function;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_ETF;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_SECTOR;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquitySectorEtfCanadaEndpoint extends EtfAbstractEndpoint<REquitySector> {

    public EquitySectorEtfCanadaEndpoint() {
        super(GET_CANADA_ETFS_BY_TICKERS, List.of(), buildCacheName(EQUITY_SECTOR, CANADA_ETF));
    }

    public EquitySectorEtfCanadaEndpoint(final Function<Query, List<Etf>> getFDSEntityFunction,
                                         final List<DataProvider> supportedProviders,
                                         final String endpointName) {
        super(getFDSEntityFunction, supportedProviders, endpointName);
    }

    static EquitySectorAllocationQueryDefinition equitySectorAllocationQueryDefinition() {
        return qEquity -> qEquity
                .allocation(qAllocation ->
                        qAllocation
                                .value()
                                .names(qName ->
                                        qName.languageCode().value()
                                )
                )
                .dataProvider();
    }

    @Override
    public EtfQuery requestMapper(final EtfQuery query) {
        return query
                .equitySectorAllocation(equitySectorAllocationQueryDefinition())
                .ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
    }

    @Override
    public REquitySector responseMapper(final Etf etf, final EtfHolding etfHolding) {
        return GraphQlMapperUtils.equitySectorMapper(etf.getEquitySectorAllocation());
    }

}
