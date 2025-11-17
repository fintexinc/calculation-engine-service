package com.fintex.ce.repository.graphql.query.endpoint.commonholdings;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.HoldingsQueryDefinition;
import com.fintex.smclient.graphql.Query;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.topcommonholdings.RCommonHoldings;
import com.fintex.ce.repository.graphql.query.endpoint.core.EtfAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;
import java.util.function.Function;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_ETF;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.TOP_COMMON_HOLDINGS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class CommonHoldingsEtfCanadaEndpoint extends EtfAbstractEndpoint<RCommonHoldings> {

    public CommonHoldingsEtfCanadaEndpoint() {
        super(GET_CANADA_ETFS_BY_TICKERS, List.of(), buildCacheName(TOP_COMMON_HOLDINGS, CANADA_ETF));
    }

    public CommonHoldingsEtfCanadaEndpoint(final Function<Query, List<Etf>> getFDSEntityFunction,
                                           final List<DataProvider> supportedProviders,
                                           final String endpointName) {
        super(getFDSEntityFunction, supportedProviders, endpointName);
    }

    static HoldingsQueryDefinition getCommonHoldingsQueryDefinition() {
        return a -> a.allocation(
                h -> h.holding(
                        n -> n.name(l -> l.languageCode().value())
                                .companyName()
                                .type()
                                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION)
                                .underlyingHoldings(
                                        hh -> hh.holding(
                                                nn -> nn.name(ll -> ll.languageCode().value())
                                                        .companyName()
                                                        .type()
                                                        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION)
                                        ).value()
                                )
                ).value()
        )
                .asOfDate()
                .dataProvider();
    }

    @Override
    public EtfQuery requestMapper(EtfQuery query) {
        return query
                .holdings(getCommonHoldingsQueryDefinition())
                .ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
    }

    @Override
    public RCommonHoldings responseMapper(Etf etf, EtfHolding holding) {
        return GraphQlMapperUtils.topCommonHoldingsMapper(etf.getHoldings());
    }
}
