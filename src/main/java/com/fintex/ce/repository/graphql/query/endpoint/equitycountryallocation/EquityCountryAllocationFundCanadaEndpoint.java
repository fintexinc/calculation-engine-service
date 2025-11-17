package com.fintex.ce.repository.graphql.query.endpoint.equitycountryallocation;

import com.fintex.smclient.graphql.*;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.REquityCountryAllocation;
import com.fintex.ce.repository.graphql.query.endpoint.core.FundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_MUTUAL_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_COUNTRY_ALLOCATIONS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphQlMapperUtils.equityCountryAllocationMapper;

public class EquityCountryAllocationFundCanadaEndpoint extends FundAbstractEndpoint<REquityCountryAllocation> {

    public EquityCountryAllocationFundCanadaEndpoint() {
        super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(EQUITY_COUNTRY_ALLOCATIONS, CANADA_MUTUAL_FUNDS));
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
    public QueryQueryDefinition queryDefinition(List<FundHoldingIdentifiersCodes> identifiersCodes, UnaryOperator<FundSeriesQuery> preDefinedFDSQuery) {
        return q -> q.getFundSeriesByHoldingCodes(identifiersCodes, preDefinedFDSQuery::apply);
    }

    @Override
    public FundSeriesQuery requestMapper(final FundSeriesQuery query) {
        return query
                .equityCountryAllocation(getCountryAllocationQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public REquityCountryAllocation responseMapper(final FundSeries fundSeries, final FundSeriesHolding holding) {
        final var result = new REquityCountryAllocation();

        final Map<String, BigDecimal> allocations = equityCountryAllocationMapper(fundSeries.getEquityCountryAllocation());

        Optional.ofNullable(fundSeries.getEquityCountryAllocation()).ifPresent(equityCountryAllocation
                -> result.setProvider(DataProvider.of(equityCountryAllocation.getDataProvider()).name()));

        result.setHoldingType(holding.getType());
        result.setAllocations(allocations);

        return result;
    }
}
