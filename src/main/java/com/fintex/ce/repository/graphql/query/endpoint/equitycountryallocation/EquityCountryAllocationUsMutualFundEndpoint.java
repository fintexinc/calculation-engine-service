package com.fintex.ce.repository.graphql.query.endpoint.equitycountryallocation;

import com.fintex.smclient.graphql.CountryAllocationQueryDefinition;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.REquityCountryAllocation;
import com.fintex.ce.repository.graphql.query.endpoint.core.UsMutualFundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_COUNTRY_ALLOCATIONS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphQlMapperUtils.equityCountryAllocationMapper;

public class EquityCountryAllocationUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<REquityCountryAllocation> {

    public EquityCountryAllocationUsMutualFundEndpoint() {
        super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(EQUITY_COUNTRY_ALLOCATIONS, US_MUTUAL_FUNDS));
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
    public QueryQueryDefinition queryDefinition(List<String> tickers, UnaryOperator<UsFundQuery> preDefinedFDSQuery) {
        return q -> q.getUsFundsByTickers(tickers, preDefinedFDSQuery::apply);
    }

    @Override
    public UsFundQuery requestMapper(final UsFundQuery query) {
        return query
                .equityCountryAllocation(getCountryAllocationQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public REquityCountryAllocation responseMapper(final UsFund usFund, final UsMutualFundHolding holding) {
        final var result = new REquityCountryAllocation();

        final Map<String, BigDecimal> allocations = equityCountryAllocationMapper(usFund.getEquityCountryAllocation());

        Optional.ofNullable(usFund.getEquityCountryAllocation()).ifPresent(equityCountryAllocation
                -> result.setProvider(DataProvider.of(equityCountryAllocation.getDataProvider()).name()));

        result.setHoldingType(holding.getType());
        result.setAllocations(allocations);

        return result;
    }
}
