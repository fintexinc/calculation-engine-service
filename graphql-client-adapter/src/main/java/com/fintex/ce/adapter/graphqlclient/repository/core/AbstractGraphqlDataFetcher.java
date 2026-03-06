package com.fintex.ce.adapter.graphqlclient.repository.core;

import com.fintex.ce.adapter.graphqlclient.endpoint.core.AbstractSMEndpoint;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.service.GraphqlTransportComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

/**
 * Abstract base for GraphQL Security Master adapters. Implements the SecurityDataPort and dispatches queries by
 * HoldingType internally.
 *
 * @param <T>
 *          domain model type returned by queries
 */
public abstract class AbstractGraphqlDataFetcher<T> implements SecurityDataPort<T> {

  final GraphqlTransportComponent graphqlTransport;

  protected AbstractGraphqlDataFetcher(GraphqlTransportComponent graphqlTransport) {
    this.graphqlTransport = graphqlTransport;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Map<Holding, T> fetch(List<? extends Holding> holdings, List<DataProvider> providers) {
    Map<Holding, T> result = new HashMap<>();
    holdings.stream()
        .collect(groupingBy(Holding::getType))
        .forEach((type, group) -> {
          AbstractSMEndpoint endpoint = resolveEndpoint(type);
          if (endpoint != null) {
            endpoint.setUserEnteredProviders(providers);
            QueryQuery queryQuery = endpoint.makeQuery(group);
            List<?> responses = (List<?>) graphqlTransport.query(queryQuery, endpoint.getGetSMEntityFunction());
            Map typed = endpoint.collectResultToMap(group, responses);
            result.putAll(typed);
          }
        });
    return result;
  }

  /**
   * Subclasses provide the endpoint for each HoldingType.
   *
   * @return the endpoint, or null if the type is not supported
   */
  protected abstract AbstractSMEndpoint<?, ?, ?, ?, T> resolveEndpoint(HoldingType type);

  /**
   * Legacy doQuery method for use in the old-style per-type methods.
   */
  public <H, QId, QReq, QEntity, Res> Map<H, Res> doQuery(final List<H> holdings,
      final AbstractSMEndpoint<H, QId, QReq, QEntity, Res> endpoint,
      final List<DataProvider> providers) {
    final QueryQuery queryQuery = endpoint.setUserEnteredProviders(providers).makeQuery(holdings);
    final List<QEntity> responses = graphqlTransport.query(queryQuery, endpoint.getGetSMEntityFunction());
    return endpoint.collectResultToMap(holdings, responses);
  }

}
