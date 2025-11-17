package com.fintex.ce.repository.graphql.query.endpoint.countryexposure;

import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.model.redis.RCountryExposure;
import com.fintex.ce.repository.graphql.query.endpoint.core.CanadaHedgeFundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.COUNTRY_EXPOSURE;
import static com.fintex.ce.repository.graphql.query.endpoint.countryexposure.CountryExposureEtfCanadaEndpoint.getCountryAllocationQueryDefinition;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphQlMapperUtils.countryExposureMapper;

public class CountryExposureCanadaHedgeFundEndpoint extends CanadaHedgeFundAbstractEndpoint<RCountryExposure> {

    public CountryExposureCanadaHedgeFundEndpoint() {
        super(GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(COUNTRY_EXPOSURE, CANADA_HEDGE_FUNDS));
    }

    @Override
    public QueryQueryDefinition queryDefinition(List<String> morninstarIds,
                                                UnaryOperator<HedgeFundQuery> preDefinedFDSQuery) {
        return q -> q.getCanadaHedgeFundsByMorningstarIds(morninstarIds, preDefinedFDSQuery::apply);
    }

    @Override
    public HedgeFundQuery requestMapper(HedgeFundQuery query) {
        return query
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION)
                .fixedIncomeCountryAllocation(
                        getCountryAllocationQueryDefinition()
                );
    }

    @Override
    public RCountryExposure responseMapper(HedgeFund fund, CanadaHedgeFundHolding holding) {
        Map<String, BigDecimal> allocation = countryExposureMapper(fund.getFixedIncomeCountryAllocation());
        return new RCountryExposure(holding.getType(), allocation);
    }
}
