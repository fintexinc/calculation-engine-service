package com.fintex.ce.repository.graphql.query.endpoint.countryexposure;

import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.model.redis.RCountryExposure;
import com.fintex.ce.repository.graphql.query.endpoint.core.CanadaPooledFundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_POOLED_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.COUNTRY_EXPOSURE;
import static com.fintex.ce.repository.graphql.query.endpoint.countryexposure.CountryExposureEtfCanadaEndpoint.getCountryAllocationQueryDefinition;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphQlMapperUtils.countryExposureMapper;

public class CountryExposureCanadaPooledFundEndpoint extends CanadaPooledFundAbstractEndpoint<RCountryExposure> {

    public CountryExposureCanadaPooledFundEndpoint() {
        super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(COUNTRY_EXPOSURE, CANADA_POOLED_FUNDS));
    }

    @Override
    public QueryQueryDefinition queryDefinition(List<String> morninstarIds,
                                                UnaryOperator<PooledFundQuery> preDefinedFDSQuery) {
        return q -> q.getCanadaPooledFundsByMorningstarIds(morninstarIds, preDefinedFDSQuery::apply);
    }

    @Override
    public PooledFundQuery requestMapper(PooledFundQuery query) {
        return query
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION)
                .fixedIncomeCountryAllocation(
                        getCountryAllocationQueryDefinition()
                );
    }

    @Override
    public RCountryExposure responseMapper(PooledFund fund, CanadaPooledFundHolding holding) {
        Map<String, BigDecimal> allocation = countryExposureMapper(fund.getFixedIncomeCountryAllocation());
        return new RCountryExposure(holding.getType(), allocation);
    }
}
