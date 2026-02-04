package com.fintex.ce.adapter.graphqlclient.endpoint.creditquality;

import com.fintex.smclient.graphql.CreditQualityRatingsQueryDefinition;
import com.fintex.smclient.graphql.FundHoldingIdentifiersCodes;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.FundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.CREDIT_QUALITY;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.creditQualityMapper;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.toDomainHoldingType;

public class CreditQualityFundCanadaEndpoint extends FundAbstractEndpoint<CreditQuality> {

  public CreditQualityFundCanadaEndpoint() {
    super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(CREDIT_QUALITY, CANADA_MUTUAL_FUNDS));
  }

  static CreditQualityRatingsQueryDefinition getCreditQualityRatingsQueryDefinition() {
    return qCredit -> qCredit.ratings(qR -> qR.rating().value()).dataProvider();
  }

  @Override
  public QueryQueryDefinition queryDefinition(List<FundHoldingIdentifiersCodes> identifiersCodes,
      UnaryOperator<FundSeriesQuery> preDefinedFDSQuery) {
    return q -> q.getFundSeriesByHoldingCodes(identifiersCodes, preDefinedFDSQuery::apply);
  }

  @Override
  public FundSeriesQuery requestMapper(final FundSeriesQuery query) {
    return query
        .creditQualityRatings(getCreditQualityRatingsQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public CreditQuality responseMapper(final FundSeries fundSeries, final FundSeriesHolding holding) {
    final Map<String, BigDecimal> allocations = creditQualityMapper(fundSeries.getCreditQualityRatings());
    return new CreditQuality(toDomainHoldingType(holding.getType()), allocations);
  }
}
