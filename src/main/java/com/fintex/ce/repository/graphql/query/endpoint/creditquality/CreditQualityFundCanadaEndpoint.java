package com.fintex.ce.repository.graphql.query.endpoint.creditquality;

import com.fintex.smclient.graphql.CreditQualityRatingsQueryDefinition;
import com.fintex.smclient.graphql.FundHoldingIdentifiersCodes;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.RCreditQuality;
import com.fintex.ce.repository.graphql.query.endpoint.core.FundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_MUTUAL_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.CREDIT_QUALITY;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphQlMapperUtils.creditQualityMapper;

public class CreditQualityFundCanadaEndpoint extends FundAbstractEndpoint<RCreditQuality> {

    public CreditQualityFundCanadaEndpoint() {
        super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(CREDIT_QUALITY, CANADA_MUTUAL_FUNDS));
    }

    static CreditQualityRatingsQueryDefinition getCreditQualityRatingsQueryDefinition() {
        return qCredit -> qCredit.ratings(qR ->
                qR.rating().value()
        ).dataProvider();
    }

    @Override
    public QueryQueryDefinition queryDefinition(List<FundHoldingIdentifiersCodes> identifiersCodes, UnaryOperator<FundSeriesQuery> preDefinedFDSQuery) {
        return q -> q.getFundSeriesByHoldingCodes(identifiersCodes, preDefinedFDSQuery::apply);
    }

    @Override
    public FundSeriesQuery requestMapper(final FundSeriesQuery query) {
        return query
                .creditQualityRatings(getCreditQualityRatingsQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RCreditQuality responseMapper(final FundSeries fundSeries, final FundSeriesHolding holding) {
        final Map<String, BigDecimal> allocations = creditQualityMapper(fundSeries.getCreditQualityRatings());
        return new RCreditQuality(holding.getType(), allocations);
    }
}
