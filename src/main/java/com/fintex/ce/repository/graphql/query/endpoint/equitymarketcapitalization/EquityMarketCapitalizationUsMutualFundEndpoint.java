package com.fintex.ce.repository.graphql.query.endpoint.equitymarketcapitalization;

import com.fintex.smclient.graphql.EquityMarketCapitalizationQueryDefinition;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.equitymarketcapitalization.REquityMarketCapitalization;
import com.fintex.ce.repository.graphql.query.endpoint.core.UsMutualFundAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_MARKET_CAPITALIZATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquityMarketCapitalizationUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<REquityMarketCapitalization> {

    public EquityMarketCapitalizationUsMutualFundEndpoint() {
        super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(EQUITY_MARKET_CAPITALIZATION, US_MUTUAL_FUNDS));
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
    public UsFundQuery requestMapper(UsFundQuery query) {
        return query
                .equityMarketCapitalization(getEquityMarketCapitalizationQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public REquityMarketCapitalization responseMapper(UsFund fund, UsMutualFundHolding holding) {
        return GraphQlMapperUtils.equityMarketCapitalizationMapper(fund.getEquityMarketCapitalization());
    }
}
