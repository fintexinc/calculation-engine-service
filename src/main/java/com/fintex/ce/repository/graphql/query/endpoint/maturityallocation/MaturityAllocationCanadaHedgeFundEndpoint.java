package com.fintex.ce.repository.graphql.query.endpoint.maturityallocation;

import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.smclient.graphql.MaturitiesQueryDefinition;
import com.fintex.smclient.graphql.MaturityDurationValue;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.model.redis.RMaturityAllocation;
import com.fintex.ce.repository.graphql.query.endpoint.core.CanadaHedgeFundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MATURITY_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
public class MaturityAllocationCanadaHedgeFundEndpoint extends CanadaHedgeFundAbstractEndpoint<RMaturityAllocation> {

    public MaturityAllocationCanadaHedgeFundEndpoint() {
        super(GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(MATURITY_ALLOCATION, CANADA_HEDGE_FUNDS));
    }

    @Override
    public HedgeFundQuery requestMapper(HedgeFundQuery query) {
        return query
                .maturities(getMaturitiesQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RMaturityAllocation responseMapper(HedgeFund hedgeFund, CanadaHedgeFundHolding holding) {
        final var rMaturityAllocation = new RMaturityAllocation();
        if(Objects.isNull(hedgeFund.getMaturities()) || Objects.isNull(hedgeFund.getMaturities().getPeriods())) {
            return rMaturityAllocation;
        }

        Map<String, BigDecimal> maturityDurationValues = hedgeFund.getMaturities().getPeriods().stream()
                .filter(maturityDurationValue -> maturityDurationValue != null && maturityDurationValue.getValue() != null)
                .collect(Collectors.toMap(
                        maturityDurationType -> maturityDurationType.getMaturityDuration().toString(),
                        MaturityDurationValue::getValue
                ));

        rMaturityAllocation.setMaturityDurationValues(maturityDurationValues);
        return rMaturityAllocation;
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
}
