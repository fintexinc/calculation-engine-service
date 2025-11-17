package com.fintex.ce.repository.graphql.query.endpoint.equitymarketcapitalization;

import com.fintex.smclient.graphql.EquityMarketCapitalizationQueryDefinition;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.equitymarketcapitalization.REquityMarketCapitalization;
import com.fintex.ce.repository.graphql.query.endpoint.core.FundAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_MUTUAL_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.EQUITY_MARKET_CAPITALIZATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class EquityMarketCapitalizationFundCanadaEndpoint extends FundAbstractEndpoint<REquityMarketCapitalization> {

    public EquityMarketCapitalizationFundCanadaEndpoint() {
        super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(EQUITY_MARKET_CAPITALIZATION, CANADA_MUTUAL_FUNDS));
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
    public FundSeriesQuery requestMapper(final FundSeriesQuery query) {
        return query
                .equityMarketCapitalization(getEquityMarketCapitalizationQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public REquityMarketCapitalization responseMapper(final FundSeries fundSeries, final FundSeriesHolding holding) {
        return GraphQlMapperUtils.equityMarketCapitalizationMapper(fundSeries.getEquityMarketCapitalization());
    }
}
