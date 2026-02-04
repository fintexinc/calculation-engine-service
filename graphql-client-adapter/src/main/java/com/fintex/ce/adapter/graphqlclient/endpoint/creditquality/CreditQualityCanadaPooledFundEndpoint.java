package com.fintex.ce.adapter.graphqlclient.endpoint.creditquality;

import com.fintex.smclient.graphql.CreditQualityRatingsQueryDefinition;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaPooledFundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_POOLED_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.CREDIT_QUALITY;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.creditQualityMapper;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.toDomainHoldingType;

public class CreditQualityCanadaPooledFundEndpoint extends CanadaPooledFundAbstractEndpoint<CreditQuality> {

  public CreditQualityCanadaPooledFundEndpoint() {
    super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(CREDIT_QUALITY, CANADA_POOLED_FUNDS));
  }

  static CreditQualityRatingsQueryDefinition getCreditQualityRatingsQueryDefinition() {
    return qCredit -> qCredit.ratings(qR -> qR.rating().value()).dataProvider();
  }

  @Override
  public QueryQueryDefinition queryDefinition(List<String> morninstarIds,
      UnaryOperator<PooledFundQuery> preDefinedFDSQuery) {
    return q -> q.getCanadaPooledFundsByMorningstarIds(morninstarIds, preDefinedFDSQuery::apply);
  }

  @Override
  public PooledFundQuery requestMapper(PooledFundQuery query) {
    return query
        .creditQualityRatings(getCreditQualityRatingsQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public CreditQuality responseMapper(PooledFund fund, CanadaPooledFundHolding holding) {
    Map<String, BigDecimal> allocations = creditQualityMapper(fund.getCreditQualityRatings());
    return new CreditQuality(toDomainHoldingType(holding.getType()), allocations);
  }
}
