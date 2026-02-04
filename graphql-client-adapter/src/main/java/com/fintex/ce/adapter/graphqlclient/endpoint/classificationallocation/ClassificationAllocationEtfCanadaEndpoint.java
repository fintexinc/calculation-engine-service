package com.fintex.ce.adapter.graphqlclient.endpoint.classificationallocation;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.ClassificationAllocation;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.EtfAbstractEndpoint;

import java.util.List;
import java.util.function.Function;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_ETF;
import static com.fintex.ce.constant.CacheNameEntity.CLASSIFICATION_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class ClassificationAllocationEtfCanadaEndpoint extends EtfAbstractEndpoint<ClassificationAllocation> {

  public ClassificationAllocationEtfCanadaEndpoint() {
    super(GET_CANADA_ETFS_BY_TICKERS, List.of(), buildCacheName(CLASSIFICATION_ALLOCATION, CANADA_ETF));
  }

  public ClassificationAllocationEtfCanadaEndpoint(final Function<Query, List<Etf>> getFDSEntityFunction,
      final List<DataProvider> supportedProviders,
      final String endpointName) {
    super(getFDSEntityFunction, supportedProviders, endpointName);
  }

  @Override
  public EtfQuery requestMapper(final EtfQuery query) {
    return query
        .securityClassification(ClassificationAllocationEndpointUtil.getSecurityClassificationQueryDefinition())
        .securityClassificationAllocation(ClassificationAllocationEndpointUtil
            .getSecurityClassificationAllocationQueryDefinition())
        .ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
  }

  @Override
  public ClassificationAllocation responseMapper(final Etf etf, final EtfHolding holding) {
    return ClassificationAllocationEndpointUtil.getResponseCacheEntity(
        etf,
        etf::getSecurityClassificationAllocation,
        etf::getSecurityClassification);
  }

}