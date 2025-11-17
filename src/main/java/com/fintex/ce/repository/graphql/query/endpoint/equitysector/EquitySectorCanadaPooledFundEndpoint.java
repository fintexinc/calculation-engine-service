package com.fintex.ce.repository.graphql.query.endpoint.equitysector;

import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.model.redis.equitysector.REquitySector;
import com.fintex.ce.repository.graphql.query.endpoint.core.CanadaPooledFundAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_POOLED_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_SECTOR;
import static com.fintex.ce.repository.graphql.query.endpoint.equitysector.EquitySectorEtfCanadaEndpoint.equitySectorAllocationQueryDefinition;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquitySectorCanadaPooledFundEndpoint extends CanadaPooledFundAbstractEndpoint<REquitySector> {

    public EquitySectorCanadaPooledFundEndpoint() {
        super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(EQUITY_SECTOR, CANADA_POOLED_FUNDS));
    }

    @Override
    public PooledFundQuery requestMapper(PooledFundQuery query) {
        return query
                .equitySectorAllocation(equitySectorAllocationQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public REquitySector responseMapper(PooledFund pooledFund, CanadaPooledFundHolding holding) {
        return GraphQlMapperUtils.equitySectorMapper(pooledFund.getEquitySectorAllocation());
    }
}
