package com.fintex.ce.repository.graphql.query.endpoint.equitymarketcapitalization;

import com.fintex.smclient.graphql.EquityMarketCapitalizationQueryDefinition;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.model.redis.equitymarketcapitalization.REquityMarketCapitalization;
import com.fintex.ce.repository.graphql.query.endpoint.core.CanadaPooledFundAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_POOLED_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_MARKET_CAPITALIZATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquityMarketCapitalizationCanadaPooledFundEndpoint extends CanadaPooledFundAbstractEndpoint<REquityMarketCapitalization> {

    public EquityMarketCapitalizationCanadaPooledFundEndpoint() {
        super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(EQUITY_MARKET_CAPITALIZATION, CANADA_POOLED_FUNDS));
    }

    static EquityMarketCapitalizationQueryDefinition getEquityMarketCapitalizationQueryDefinition() {
        return qE -> qE
                .values(qV -> qV
                        .equityMarketCapitalization()
                        .value()
                )
                .dataProvider();
    }

    @Override
    public PooledFundQuery requestMapper(PooledFundQuery query) {
        return query
                .equityMarketCapitalization(getEquityMarketCapitalizationQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public REquityMarketCapitalization responseMapper(PooledFund fund, CanadaPooledFundHolding holding) {
        return GraphQlMapperUtils.equityMarketCapitalizationMapper(fund.getEquityMarketCapitalization());
    }
}
