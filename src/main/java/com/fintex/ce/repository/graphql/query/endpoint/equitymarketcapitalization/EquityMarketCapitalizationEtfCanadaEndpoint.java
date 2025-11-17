package com.fintex.ce.repository.graphql.query.endpoint.equitymarketcapitalization;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.equitymarketcapitalization.REquityMarketCapitalization;
import com.fintex.ce.repository.graphql.query.endpoint.core.EtfAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;
import java.util.function.Function;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_ETF;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_MARKET_CAPITALIZATION;
import static com.fintex.ce.repository.graphql.query.endpoint.equitymarketcapitalization.EquityMarketCapitalizationFundCanadaEndpoint.getEquityMarketCapitalizationQueryDefinition;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquityMarketCapitalizationEtfCanadaEndpoint extends EtfAbstractEndpoint<REquityMarketCapitalization> {

    public EquityMarketCapitalizationEtfCanadaEndpoint() {
        super(GET_CANADA_ETFS_BY_TICKERS, List.of(), buildCacheName(EQUITY_MARKET_CAPITALIZATION, CANADA_ETF));
    }

    public EquityMarketCapitalizationEtfCanadaEndpoint(final Function<Query, List<Etf>> getFDSEntityFunction,
                                                       final List<DataProvider> supportedProviders,
                                                       final String endpointName) {
        super(getFDSEntityFunction, supportedProviders, endpointName);
    }

    @Override
    public EtfQuery requestMapper(final EtfQuery query) {
        return query
                .equityMarketCapitalization(getEquityMarketCapitalizationQueryDefinition())
                .ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
    }

    @Override
    public REquityMarketCapitalization responseMapper(final Etf etf, final EtfHolding etfHolding) {
        return GraphQlMapperUtils.equityMarketCapitalizationMapper(etf.getEquityMarketCapitalization());
    }

}
