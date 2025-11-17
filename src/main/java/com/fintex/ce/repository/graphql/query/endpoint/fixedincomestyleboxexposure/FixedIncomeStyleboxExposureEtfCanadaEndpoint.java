package com.fintex.ce.repository.graphql.query.endpoint.fixedincomestyleboxexposure;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.RFixedIncomeStyleboxExposure;
import com.fintex.ce.repository.graphql.query.endpoint.core.EtfAbstractEndpoint;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_ETF;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.FIXED_INCOME_STYLEBOX_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class FixedIncomeStyleboxExposureEtfCanadaEndpoint extends EtfAbstractEndpoint<RFixedIncomeStyleboxExposure> {

    public FixedIncomeStyleboxExposureEtfCanadaEndpoint() {
        super(GET_CANADA_ETFS_BY_TICKERS, List.of(), buildCacheName(FIXED_INCOME_STYLEBOX_ALLOCATION, CANADA_ETF));
    }

    public FixedIncomeStyleboxExposureEtfCanadaEndpoint(final Function<Query, List<Etf>> getFDSEntityFunction,
                                                        final List<DataProvider> supportedProviders,
                                                        final String endpointName) {
        super(getFDSEntityFunction, supportedProviders, endpointName);
    }

    @Override
    public EtfQuery requestMapper(final EtfQuery query) {
        return query
                .fixedIncomeStyleBoxes(FixedIncomeStyleBoxesEndpointUtil.getStyleBoxesQueryDefinition())
                .ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
    }

    @Override
    public RFixedIncomeStyleboxExposure responseMapper(final Etf etf, final EtfHolding holding) {
        final var result = new RFixedIncomeStyleboxExposure();
        if (Objects.nonNull(etf) && Objects.nonNull(etf.getFixedIncomeStyleBoxes())) {
            return FixedIncomeStyleBoxesEndpointUtil.getREquityStyleboxExposure(
                    etf.getFixedIncomeStyleBoxes(),
                    result
            );
        }
        return result;
    }
}