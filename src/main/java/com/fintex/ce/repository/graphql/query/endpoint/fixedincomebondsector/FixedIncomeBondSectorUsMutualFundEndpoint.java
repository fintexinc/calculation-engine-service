package com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector;

import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RFixedIncomeBondSecurities;
import com.fintex.ce.repository.graphql.query.endpoint.core.UsMutualFundAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.FixedIncomeSecuritiesAllocation;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.FIXED_INCOME_BOND_SECURITIES;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class FixedIncomeBondSectorUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<RFixedIncomeBondSecurities> {

    public FixedIncomeBondSectorUsMutualFundEndpoint() {
        super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(FIXED_INCOME_BOND_SECURITIES, US_MUTUAL_FUNDS));
    }

    @Override
    public QueryQueryDefinition queryDefinition(List<String> morningstarIds,
                                                UnaryOperator<UsFundQuery> preDefinedFDSQuery) {
        return q -> q.getUsFundsByTickers(morningstarIds, preDefinedFDSQuery::apply);
    }

    @Override
    public UsFundQuery requestMapper(UsFundQuery query) {
        return query
                .fixedIncomeSecuritiesAllocation(sa -> sa.allocation(a -> a.name().value()).dataProvider())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RFixedIncomeBondSecurities responseMapper(UsFund fund, UsMutualFundHolding holding) {
        FixedIncomeSecuritiesAllocation fixedIncomeSecuritiesAllocation = fund.getFixedIncomeSecuritiesAllocation();
        return GraphQlMapperUtils.fixedIncomeBondSectorMapper(fixedIncomeSecuritiesAllocation, holding.getType());
    }

}

