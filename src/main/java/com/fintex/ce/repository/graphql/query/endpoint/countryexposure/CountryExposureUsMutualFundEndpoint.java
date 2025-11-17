package com.fintex.ce.repository.graphql.query.endpoint.countryexposure;

import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RCountryExposure;
import com.fintex.ce.repository.graphql.query.endpoint.core.UsMutualFundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.COUNTRY_EXPOSURE;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphQlMapperUtils.countryExposureMapper;

public class CountryExposureUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<RCountryExposure> {

    public CountryExposureUsMutualFundEndpoint() {
        super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(COUNTRY_EXPOSURE, US_MUTUAL_FUNDS));
    }

    @Override
    public QueryQueryDefinition queryDefinition(List<String> tickers,
                                                UnaryOperator<UsFundQuery> preDefinedFDSQuery) {
        return q -> q.getUsFundsByTickers(tickers, preDefinedFDSQuery::apply);
    }

    @Override
    public UsFundQuery requestMapper(UsFundQuery query) {
        return query
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION)
                .fixedIncomeCountryAllocation(
                        CountryExposureEtfCanadaEndpoint.getCountryAllocationQueryDefinition()
                );
    }

    @Override
    public RCountryExposure responseMapper(UsFund fund, UsMutualFundHolding holding) {
        Map<String, BigDecimal> allocation = countryExposureMapper(fund.getFixedIncomeCountryAllocation());
        return new RCountryExposure(holding.getType(), allocation);
    }
}
