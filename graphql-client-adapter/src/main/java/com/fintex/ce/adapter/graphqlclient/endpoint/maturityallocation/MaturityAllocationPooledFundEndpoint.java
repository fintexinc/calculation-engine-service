package com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation;

import com.fintex.smclient.graphql.MaturityDurationValue;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.constant.CacheNameEntity;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.MaturityAllocation;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaPooledFundAbstractEndpoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheNameEntity.MATURITY_ALLOCATION;
import static com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation.MaturityAllocationFundCanadaEndpoint.getMaturitiesQueryDefinition;

public class MaturityAllocationPooledFundEndpoint extends CanadaPooledFundAbstractEndpoint<MaturityAllocation> {

  public MaturityAllocationPooledFundEndpoint() {
    super(GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(MATURITY_ALLOCATION,
        CacheCategory.CANADA_POOLED_FUNDS));
  }

  @Override
  public PooledFundQuery requestMapper(PooledFundQuery query) {
    return query
        .maturities(getMaturitiesQueryDefinition())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);

  }

  @Override
  public MaturityAllocation responseMapper(PooledFund pooledFund, CanadaPooledFundHolding holding) {
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

  public static String buildCacheName(final CacheNameEntity prefix, final CacheCategory category) {
    return Objects.requireNonNull(prefix) + "_" + Objects.requireNonNull(category);
  }
}
