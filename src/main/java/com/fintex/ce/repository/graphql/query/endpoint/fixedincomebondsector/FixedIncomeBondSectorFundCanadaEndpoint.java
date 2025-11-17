package com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector;

import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.RFixedIncomeBondSecurities;
import com.fintex.ce.repository.graphql.query.endpoint.core.FundAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.FixedIncomeSecuritiesAllocation;
import com.fintex.smclient.graphql.FundHoldingIdentifiersCodes;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_MUTUAL_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.FIXED_INCOME_BOND_SECURITIES;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class FixedIncomeBondSectorFundCanadaEndpoint extends FundAbstractEndpoint<RFixedIncomeBondSecurities> {

    public FixedIncomeBondSectorFundCanadaEndpoint() {
        super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(FIXED_INCOME_BOND_SECURITIES, CANADA_MUTUAL_FUNDS));
    }

    @Override
    public QueryQueryDefinition queryDefinition(final List<FundHoldingIdentifiersCodes> fundHoldingIdentifiersCodes,
                                                final UnaryOperator<FundSeriesQuery> preDefinedFDSQuery) {
        return q -> q.getFundSeriesByHoldingCodes(fundHoldingIdentifiersCodes, preDefinedFDSQuery::apply);
    }

    @Override
    public FundSeriesQuery requestMapper(final FundSeriesQuery query) {
        return query
                .fixedIncomeSecuritiesAllocation(sa -> sa.allocation(a -> a.name().value()).dataProvider())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RFixedIncomeBondSecurities responseMapper(final FundSeries fundSeries, final FundSeriesHolding holding) {
        final FixedIncomeSecuritiesAllocation fixedIncomeSecuritiesAllocation = fundSeries.getFixedIncomeSecuritiesAllocation();
        return GraphQlMapperUtils.fixedIncomeBondSectorMapper(fixedIncomeSecuritiesAllocation, holding.getType());
    }

}
