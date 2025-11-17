package com.fintex.ce.repository.graphql.query.endpoint.creditquality;

import com.fintex.smclient.graphql.CreditQualityRatingsQueryDefinition;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RCreditQuality;
import com.fintex.ce.repository.graphql.query.endpoint.core.UsMutualFundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.CREDIT_QUALITY;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphQlMapperUtils.creditQualityMapper;

public class CreditQualityUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<RCreditQuality> {

    public CreditQualityUsMutualFundEndpoint() {
        super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(CREDIT_QUALITY, US_MUTUAL_FUNDS));
    }

    static CreditQualityRatingsQueryDefinition getCreditQualityRatingsQueryDefinition() {
        return qCredit -> qCredit.ratings(qR ->
                qR.rating().value()
        ).dataProvider();
    }

    @Override
    public QueryQueryDefinition queryDefinition(List<String> tickers, UnaryOperator<UsFundQuery> preDefinedFDSQuery) {
        return q -> q.getUsFundsByTickers(tickers, preDefinedFDSQuery::apply);
    }

    @Override
    public UsFundQuery requestMapper(UsFundQuery query) {
        return query
                .creditQualityRatings(getCreditQualityRatingsQueryDefinition())
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RCreditQuality responseMapper(UsFund fund, UsMutualFundHolding holding) {
        Map<String, BigDecimal> allocations = creditQualityMapper(fund.getCreditQualityRatings());
        return new RCreditQuality(holding.getType(), allocations);
    }
}
