package com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector;

import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.model.redis.RFixedIncomeBondSecurities;
import com.fintex.ce.repository.graphql.query.endpoint.core.CanadaPooledFundAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.FixedIncomeSecuritiesAllocation;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_POOLED_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.FIXED_INCOME_BOND_SECURITIES;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class FixedIncomeBondSectorCanadaPooledFundEndpoint extends CanadaPooledFundAbstractEndpoint<RFixedIncomeBondSecurities> {

    public FixedIncomeBondSectorCanadaPooledFundEndpoint() {
        super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(FIXED_INCOME_BOND_SECURITIES, CANADA_POOLED_FUNDS));
    }

    @Override
    public QueryQueryDefinition queryDefinition(List<String> morningstarIds,
                                                UnaryOperator<PooledFundQuery> preDefinedFDSQuery) {
        return q -> q.getCanadaPooledFundsByMorningstarIds(morningstarIds, preDefinedFDSQuery::apply);
    }

    @Override
    public PooledFundQuery requestMapper(PooledFundQuery query) {
        return query
                .fixedIncomeSecuritiesAllocation(sa -> sa.allocation(a -> a.name().value()).dataProvider())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RFixedIncomeBondSecurities responseMapper(PooledFund pooledFund, CanadaPooledFundHolding holding) {
        FixedIncomeSecuritiesAllocation fixedIncomeSecuritiesAllocation = pooledFund.getFixedIncomeSecuritiesAllocation();
        return GraphQlMapperUtils.fixedIncomeBondSectorMapper(fixedIncomeSecuritiesAllocation, holding.getType());
    }

}
