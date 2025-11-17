package com.fintex.ce.repository.graphql.query.endpoint.maturityallocation;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.MaturitiesQueryDefinition;
import com.fintex.smclient.graphql.MaturityDurationValue;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.StyleBoxesQueryDefinition;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.RMaturityAllocation;
import com.fintex.ce.repository.graphql.query.endpoint.core.EtfAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_ETF;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MATURITY_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MaturityAllocationEtfCanadaEndpoint extends EtfAbstractEndpoint<RMaturityAllocation> {

    public MaturityAllocationEtfCanadaEndpoint() {
        super(GET_CANADA_ETFS_BY_TICKERS, List.of(), buildCacheName(MATURITY_ALLOCATION, CANADA_ETF));
    }

    public MaturityAllocationEtfCanadaEndpoint(final Function<Query, List<Etf>> getFDSEntityFunction,
                                               final List<DataProvider> supportedProviders,
                                               final String endpointName) {
        super(getFDSEntityFunction, supportedProviders, endpointName);
    }

    static MaturitiesQueryDefinition getMaturitiesQueryDefinition() {
        return qMaturities -> {
            qMaturities.dataProvider();
            qMaturities.asOfDate();
            qMaturities.periods(
                    qMaturity -> {
                        qMaturity.maturityDuration();
                        qMaturity.value();
                    });
        };
    }

    @Override
    public EtfQuery requestMapper(final EtfQuery query) {
        return query
                .maturities(getMaturitiesQueryDefinition())
                .ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
    }

    @Override
    public RMaturityAllocation responseMapper(final Etf etf, final EtfHolding holding) {
        final var rMaturityAllocation = new RMaturityAllocation();
        if(Objects.isNull(etf.getMaturities()) || Objects.isNull(etf.getMaturities().getPeriods())) {
            return rMaturityAllocation;
        }
        Map<String, BigDecimal> maturityDurationValues = etf.getMaturities().getPeriods().stream()
                .filter(maturityDurationValue -> maturityDurationValue != null && maturityDurationValue.getValue() != null)
                .collect(Collectors.toMap(
                        maturityDurationType -> maturityDurationType.getMaturityDuration().toString(),
                        MaturityDurationValue::getValue
                ));

        rMaturityAllocation.setMaturityDurationValues(maturityDurationValues);
        return rMaturityAllocation;
    }

    static StyleBoxesQueryDefinition getStyleBoxesQueryDefinition() {

        return qStyleboxes -> {
            qStyleboxes.dataProvider();
            qStyleboxes.asOfDate();
            qStyleboxes.boxValues(
                    qBoxValue -> {
                        qBoxValue.styleBoxType();
                        qBoxValue.value();
                    });
        };
    }
}
