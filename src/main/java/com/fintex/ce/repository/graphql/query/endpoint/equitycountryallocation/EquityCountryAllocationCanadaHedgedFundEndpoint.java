package com.fintex.ce.repository.graphql.query.endpoint.equitycountryallocation;

import com.fintex.smclient.graphql.CountryAllocationQueryDefinition;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.model.redis.REquityCountryAllocation;
import com.fintex.ce.repository.graphql.query.endpoint.core.CanadaHedgeFundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_COUNTRY_ALLOCATIONS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphQlMapperUtils.equityCountryAllocationMapper;

public class EquityCountryAllocationCanadaHedgedFundEndpoint extends CanadaHedgeFundAbstractEndpoint<REquityCountryAllocation> {

    public EquityCountryAllocationCanadaHedgedFundEndpoint() {
        super(GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(EQUITY_COUNTRY_ALLOCATIONS, CANADA_HEDGE_FUNDS));
    }

    static CountryAllocationQueryDefinition getCountryAllocationQueryDefinition() {
        return qCountry ->
                qCountry.allocation(qAllocation ->
                        qAllocation.value().name(qName ->
                                qName.value().languageCode()
                        )
                ).dataProvider();
    }

    @Override
    public QueryQueryDefinition queryDefinition(List<String> morningstarIds, UnaryOperator<HedgeFundQuery> preDefinedFDSQuery) {
        return q -> q.getCanadaHedgeFundsByMorningstarIds(morningstarIds, preDefinedFDSQuery::apply);
    }

    @Override
    public HedgeFundQuery requestMapper(final HedgeFundQuery query) {
        return query
                .equityCountryAllocation(getCountryAllocationQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public REquityCountryAllocation responseMapper(final HedgeFund hedgeFund, final CanadaHedgeFundHolding holding) {
        final var result = new REquityCountryAllocation();

        final Map<String, BigDecimal> allocations = equityCountryAllocationMapper(hedgeFund.getEquityCountryAllocation());

        Optional.ofNullable(hedgeFund.getEquityCountryAllocation()).ifPresent(equityCountryAllocation
                -> result.setProvider(DataProvider.of(equityCountryAllocation.getDataProvider()).name()));

        result.setHoldingType(holding.getType());
        result.setAllocations(allocations);

        return result;
    }
}
