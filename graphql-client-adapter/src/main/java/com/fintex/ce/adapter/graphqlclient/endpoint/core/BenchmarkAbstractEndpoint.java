package com.fintex.ce.adapter.graphqlclient.endpoint.core;

import com.fintex.smclient.graphql.*;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

public abstract class BenchmarkAbstractEndpoint<Res>
    extends
      AbstractSMEndpoint<BenchmarkIndexHolding, String, IndexQuery, Index, Res> {

  public BenchmarkAbstractEndpoint(final Function<Query, List<Index>> getSMEntityFunction,
      final List<DataProvider> supportedProviders,
      final String endpointName) {
    super(getSMEntityFunction, supportedProviders, endpointName);
  }

  @Override
  public QueryQueryDefinition queryDefinition(final List<String> morningstarIds,
      final UnaryOperator<IndexQuery> preDefinedSMQuery) {
    return q -> q.getIndexesByMorningstarIds(morningstarIds, (args) -> args.includeNonExistingIndexes(true),
        preDefinedSMQuery::apply);
  }

  @Override
  public List<String> collectIds(final List<BenchmarkIndexHolding> holdings) {
    return holdings.stream().map(BenchmarkIndexHolding::getMrStarId).collect(Collectors.toList());
  }

  @Override
  public BenchmarkIndexHolding findHoldingBasedOnRes(final List<BenchmarkIndexHolding> holdings, final Index index) {
    final List<String> ids = getIds(index);
    return holdings.stream()
        .filter(holding -> ids.contains(holding.getMrStarId()))
        .findFirst().orElseThrow();
  }

  /**
   * Collect ids from the FDS response
   *
   * @param index
   *          index object from FDS
   * @return ids
   */
  public List<String> getIds(final Index index) {
    if (Objects.isNull(index.getExternalIdentifiers())) {
      return List.of();
    }
    final List<ExternalIdentifierTypeValue> codes = Objects.requireNonNull(index.getExternalIdentifiers()).getCodes();
    return Objects.requireNonNull(codes).stream().map(ExternalIdentifierTypeValue::getValue).collect(Collectors
        .toList());
  }

  @Override
  public void populateEmptyResponseWithIdentifier(final List<Index> responses, final BenchmarkIndexHolding holding) {
    final Index index = responses.stream()
        .filter(r -> isNull(r.getExternalIdentifiers()) || isEmpty(r.getExternalIdentifiers().getCodes()))
        .findFirst().orElseThrow();
    final var morningStarId = new ExternalIdentifierTypeValue();
    morningStarId.setValue(holding.getMrStarId());

    final ExternalIdentifiers externalIdentifiers = new ExternalIdentifiers().setCodes(List.of(morningStarId));
    index.setExternalIdentifiers(externalIdentifiers);
  }

  @Override
  public List<BenchmarkIndexHolding> getNotExistingHoldings(final List<BenchmarkIndexHolding> holdings,
      final List<Index> responses) {
    final List<List<String>> responseIds = responses.stream().map(this::getIds).collect(toList());
    return holdings
        .stream()
        .filter(h -> holdingsThatDontHaveCorrespondingResponseFromFds(responseIds, h))
        .collect(toList());
  }

  private boolean holdingsThatDontHaveCorrespondingResponseFromFds(final List<List<String>> responseIds,
      final BenchmarkIndexHolding h) {
    return responseIds.stream().noneMatch(r -> r.contains(h.getMrStarId()));
  }

}
