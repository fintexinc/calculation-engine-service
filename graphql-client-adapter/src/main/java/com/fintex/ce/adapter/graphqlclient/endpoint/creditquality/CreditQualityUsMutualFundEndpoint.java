package com.fintex.ce.adapter.graphqlclient.endpoint.creditquality;

import com.fintex.smclient.graphql.CreditQualityRatingsQueryDefinition;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.UsMutualFundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.CREDIT_QUALITY;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.creditQualityMapper;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.toDomainHoldingType;

public class CreditQualityUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<CreditQuality> {

  public CreditQualityUsMutualFundEndpoint() {
    super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(CREDIT_QUALITY, US_MUTUAL_FUNDS));
  }

  static CreditQualityRatingsQueryDefinition getCreditQualityRatingsQueryDefinition() {
    return qCredit -> qCredit.ratings(qR -> qR.rating().value()).dataProvider();
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
  public CreditQuality responseMapper(UsFund fund, UsMutualFundHolding holding) {
    Map<String, BigDecimal> allocations = creditQualityMapper(fund.getCreditQualityRatings());
    return new CreditQuality(toDomainHoldingType(holding.getType()), allocations);
  }
}
