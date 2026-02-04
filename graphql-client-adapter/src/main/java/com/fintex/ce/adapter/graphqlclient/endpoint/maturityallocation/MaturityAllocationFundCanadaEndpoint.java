package com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation;

import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.MaturitiesQueryDefinition;
import com.fintex.smclient.graphql.MaturityDurationValue;
import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.MaturityAllocation;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.FundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_FUND_SERIES_BY_HOLDING_CODES;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheNameEntity.MATURITY_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MaturityAllocationFundCanadaEndpoint extends FundAbstractEndpoint<MaturityAllocation> {

  public MaturityAllocationFundCanadaEndpoint() {
    super(GET_FUND_SERIES_BY_HOLDING_CODES, List.of(), buildCacheName(MATURITY_ALLOCATION,
        CacheCategory.CANADA_MUTUAL_FUNDS));
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

  @Override
  public FundSeriesQuery requestMapper(FundSeriesQuery query) {
    return query
        .maturities(getMaturitiesQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public MaturityAllocation responseMapper(FundSeries fundSeries, FundSeriesHolding holding) {
    final var maturityAllocation = new MaturityAllocation();
    if (Objects.isNull(fundSeries.getMaturities()) || Objects.isNull(fundSeries.getMaturities().getPeriods())) {
      return maturityAllocation;
    }

    Map<String, BigDecimal> maturityDurationValues = fundSeries.getMaturities().getPeriods().stream()
        .filter(maturityDurationValue -> maturityDurationValue != null && maturityDurationValue.getValue() != null)
        .collect(Collectors.toMap(
            maturityDurationType -> maturityDurationType.getMaturityDuration().toString(),
            MaturityDurationValue::getValue));

    maturityAllocation.setMaturityDurationValues(maturityDurationValues);
    return maturityAllocation;
  }
}
