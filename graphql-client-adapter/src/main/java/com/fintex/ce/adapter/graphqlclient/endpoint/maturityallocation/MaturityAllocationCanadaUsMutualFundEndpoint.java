package com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation;

import com.fintex.smclient.graphql.MaturityDurationValue;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.MaturityAllocation;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.UsMutualFundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.EQUITY_SECTOR;
import static com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation.MaturityAllocationCanadaHedgeFundEndpoint.getMaturitiesQueryDefinition;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MaturityAllocationCanadaUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<MaturityAllocation> {

  public MaturityAllocationCanadaUsMutualFundEndpoint() {
    super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(EQUITY_SECTOR, US_MUTUAL_FUNDS));
  }

  @Override
  public UsFundQuery requestMapper(UsFundQuery query) {
    return query
        .maturities(getMaturitiesQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);

  }

  @Override
  public MaturityAllocation responseMapper(UsFund pooledFund, UsMutualFundHolding holding) {
    final var maturityAllocation = new MaturityAllocation();
    if (Objects.isNull(pooledFund.getMaturities()) || Objects.isNull(pooledFund.getMaturities().getPeriods())) {
      return maturityAllocation;
    }

    Map<String, BigDecimal> maturityDurationValues = pooledFund.getMaturities().getPeriods().stream()
        .filter(maturityDurationValue -> maturityDurationValue != null && maturityDurationValue.getValue() != null)
        .collect(Collectors.toMap(
            maturityDurationType -> maturityDurationType.getMaturityDuration().toString(),
            MaturityDurationValue::getValue));

    maturityAllocation.setMaturityDurationValues(maturityDurationValues);
    return maturityAllocation;
  }
}
