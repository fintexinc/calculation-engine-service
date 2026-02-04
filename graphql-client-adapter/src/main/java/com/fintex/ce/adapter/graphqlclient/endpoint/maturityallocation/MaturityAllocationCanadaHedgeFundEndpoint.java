package com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation;

import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.smclient.graphql.MaturitiesQueryDefinition;
import com.fintex.smclient.graphql.MaturityDurationValue;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.MaturityAllocation;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaHedgeFundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.MATURITY_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
public class MaturityAllocationCanadaHedgeFundEndpoint extends CanadaHedgeFundAbstractEndpoint<MaturityAllocation> {

  public MaturityAllocationCanadaHedgeFundEndpoint() {
    super(GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(MATURITY_ALLOCATION,
        CANADA_HEDGE_FUNDS));
  }

  @Override
  public HedgeFundQuery requestMapper(HedgeFundQuery query) {
    return query
        .maturities(getMaturitiesQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public MaturityAllocation responseMapper(HedgeFund hedgeFund, CanadaHedgeFundHolding holding) {
    final var maturityAllocation = new MaturityAllocation();
    if (Objects.isNull(hedgeFund.getMaturities()) || Objects.isNull(hedgeFund.getMaturities().getPeriods())) {
      return maturityAllocation;
    }

    Map<String, BigDecimal> maturityDurationValues = hedgeFund.getMaturities().getPeriods().stream()
        .filter(maturityDurationValue -> maturityDurationValue != null && maturityDurationValue.getValue() != null)
        .collect(Collectors.toMap(
            maturityDurationType -> maturityDurationType.getMaturityDuration().toString(),
            MaturityDurationValue::getValue));

    maturityAllocation.setMaturityDurationValues(maturityDurationValues);
    return maturityAllocation;
  }

  static MaturitiesQueryDefinition getMaturitiesQueryDefinition() {
    return qMaturities -> {
      qMaturities.dataProvider();
      qMaturities.asOfDate();
      qMaturities.periods(
          qMaturity -> {
            qMaturity.maturityDuration();
            qMaturity.value();
          });
    };
  }
}
