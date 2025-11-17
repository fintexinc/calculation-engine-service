package com.fintex.ce.repository.graphql.query.endpoint.creditquality;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.RCreditQuality;
import com.fintex.ce.repository.graphql.query.endpoint.core.EtfAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_ETF;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.CREDIT_QUALITY;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphQlMapperUtils.creditQualityMapper;

public class CreditQualityEtfCanadaEndpoint extends EtfAbstractEndpoint<RCreditQuality> {

    public CreditQualityEtfCanadaEndpoint() {
        super(GET_CANADA_ETFS_BY_TICKERS, List.of(), buildCacheName(CREDIT_QUALITY, CANADA_ETF));
    }

    public CreditQualityEtfCanadaEndpoint(final Function<Query, List<Etf>> getFDSEntityFunction,
                                          final List<DataProvider> supportedProviders,
                                          final String endpointName) {
        super(getFDSEntityFunction, supportedProviders, endpointName);
    }

    @Override
    public QueryQueryDefinition queryDefinition(final List<String> equityIdentifiers, final UnaryOperator<EtfQuery> preDefinedFDSQuery) {
        return q -> q.getCanadaEtfsByTickers(equityIdentifiers, preDefinedFDSQuery::apply);
    }

    @Override
    public EtfQuery requestMapper(final EtfQuery query) {
        return query
                .creditQualityRatings(CreditQualityFundCanadaEndpoint.getCreditQualityRatingsQueryDefinition())
                .ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
    }

    @Override
    public RCreditQuality responseMapper(final Etf etf, final EtfHolding etfHolding) {
        Map<String, BigDecimal> allocations = creditQualityMapper(etf.getCreditQualityRatings());
        return new RCreditQuality(etfHolding.getType(), allocations);
    }

}
