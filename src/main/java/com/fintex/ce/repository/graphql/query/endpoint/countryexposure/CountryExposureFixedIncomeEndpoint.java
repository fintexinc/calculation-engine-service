package com.fintex.ce.repository.graphql.query.endpoint.countryexposure;

import com.fintex.smclient.graphql.*;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.model.redis.RCountryExposure;
import com.fintex.ce.repository.graphql.query.endpoint.core.FixedIncomeAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_FIXED_INCOME_BY_ADP_NUMBERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.FIXED_INCOME;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.FIXED_INCOME_BOND_SECURITIES;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphQlMapperUtils.countryExposureMapper;

public class CountryExposureFixedIncomeEndpoint extends FixedIncomeAbstractEndpoint<RCountryExposure> {

    public CountryExposureFixedIncomeEndpoint() {
        super(GET_FIXED_INCOME_BY_ADP_NUMBERS, List.of(), buildCacheName(FIXED_INCOME_BOND_SECURITIES, FIXED_INCOME));
    }

    @Override
    public FixedIncomeQuery requestMapper(FixedIncomeQuery query) {
        return query
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION)
                .countryAllocation(
                        CountryExposureEtfCanadaEndpoint.getCountryAllocationQueryDefinition()
                );
    }

    @Override
    public RCountryExposure responseMapper(FixedIncome fixedIncome, FixedIncomeHolding holding) {
        Map<String, BigDecimal> allocation = countryExposureMapper(fixedIncome.getCountryAllocation());
        return new RCountryExposure(holding.getType(), allocation);
    }
}