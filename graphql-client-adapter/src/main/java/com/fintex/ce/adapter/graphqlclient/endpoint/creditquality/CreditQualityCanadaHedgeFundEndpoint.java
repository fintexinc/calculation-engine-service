package com.fintex.ce.adapter.graphqlclient.endpoint.creditquality;

import com.fintex.smclient.graphql.CreditQualityRatingsQueryDefinition;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaHedgeFundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.CREDIT_QUALITY;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.creditQualityMapper;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.toDomainHoldingType;

public class CreditQualityCanadaHedgeFundEndpoint extends CanadaHedgeFundAbstractEndpoint<CreditQuality> {

  public CreditQualityCanadaHedgeFundEndpoint() {
    super(GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(CREDIT_QUALITY, CANADA_HEDGE_FUNDS));
  }

  static CreditQualityRatingsQueryDefinition getCreditQualityRatingsQueryDefinition() {
    return qCredit -> qCredit.ratings(qR -> qR.rating().value()).dataProvider();
  }

  @Override
  public QueryQueryDefinition queryDefinition(List<String> morninstarIds,
      UnaryOperator<HedgeFundQuery> preDefinedFDSQuery) {
    return q -> q.getCanadaHedgeFundsByMorningstarIds(morninstarIds, preDefinedFDSQuery::apply);
  }

  @Override
  public HedgeFundQuery requestMapper(HedgeFundQuery query) {
    return query
        .creditQualityRatings(getCreditQualityRatingsQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public CreditQuality responseMapper(HedgeFund fund, CanadaHedgeFundHolding holding) {
    Map<String, BigDecimal> allocations = creditQualityMapper(fund.getCreditQualityRatings());
    return new CreditQuality(toDomainHoldingType(holding.getType()), allocations);
  }
}
