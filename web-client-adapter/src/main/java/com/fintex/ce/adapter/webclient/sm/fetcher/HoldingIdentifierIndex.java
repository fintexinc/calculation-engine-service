package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.mapper.HoldingMappingUtils;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.dto.search.TypedIdentifiers;

import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.groupingBy;

/**
 * Index over a list of portfolio holdings prepared for a Security Master request. Groups holdings by financial
 * instrument type into {@link TypedIdentifiers} and keeps a reverse identifier-to-holdings index (a list, because
 * multiple holdings can share the same security, e.g. the same stock in different accounts) used to map
 * {@link SecurityAttributeResult} responses back onto the original holdings. Holdings without an identifier or with a
 * locally-sourced type (see {@link HoldingMappingUtils#isSkipped}) are excluded from Security Master requests.
 */
@Slf4j
final class HoldingIdentifierIndex {

  private static final String KEY_SEPARATOR = ":";

  private final List<TypedIdentifiers> typedIdentifiers;
  private final Map<String, List<PortfolioHolding>> identifierToHoldings;

  private HoldingIdentifierIndex(List<TypedIdentifiers> typedIdentifiers,
      Map<String, List<PortfolioHolding>> identifierToHoldings) {
    this.typedIdentifiers = typedIdentifiers;
    this.identifierToHoldings = identifierToHoldings;
  }

  static HoldingIdentifierIndex of(List<? extends PortfolioHolding> holdings) {
    if (CollectionUtils.isEmpty(holdings)) {
      return new HoldingIdentifierIndex(List.of(), Map.of());
    }

    List<PortfolioHolding> fetchableHoldings = holdings.stream()
        .filter(HoldingIdentifierIndex::isFetchable)
        .map(PortfolioHolding.class::cast)
        .toList();

    Map<String, List<PortfolioHolding>> identifierToHoldings = fetchableHoldings.stream()
        .collect(groupingBy(holding -> buildKey(holding.getSecurityIdentifier())));

    List<TypedIdentifiers> typedIdentifiers = fetchableHoldings.stream()
        .collect(groupingBy(PortfolioHolding::getHoldingType))
        .entrySet().stream()
        .map(entry -> TypedIdentifiers.builder()
            .type(entry.getKey())
            .ids(entry.getValue().stream().map(PortfolioHolding::getSecurityIdentifier).toList())
            .build())
        .toList();

    return new HoldingIdentifierIndex(typedIdentifiers, identifierToHoldings);
  }

  boolean isEmpty() {
    return typedIdentifiers.isEmpty();
  }

  List<TypedIdentifiers> getTypedIdentifiers() {
    return typedIdentifiers;
  }

  <D, R> Map<PortfolioHolding, D> mapResponses(List<SecurityAttributeResult<R>> responses,
      SecurityMasterResponseMapper<D, R> mapper) {
    if (CollectionUtils.isEmpty(responses)) {
      return Collections.emptyMap();
    }

    return responses.stream()
        .filter(HoldingIdentifierIndex::isValidResponse)
        .flatMap(response -> mapResponseToHoldings(response, mapper).entrySet().stream())
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (existing, replacement) -> existing));
  }

  private <D, R> Map<PortfolioHolding, D> mapResponseToHoldings(SecurityAttributeResult<R> response,
      SecurityMasterResponseMapper<D, R> mapper) {
    String responseKey = buildKey(response.getIdentifier());
    List<PortfolioHolding> holdings = identifierToHoldings.get(responseKey);

    if (CollectionUtils.isEmpty(holdings)) {
      log.warn("No matching holdings for identifier: {}", response.getIdentifier());
      return Collections.emptyMap();
    }

    return holdings.stream()
        .map(holding -> {
          D mapped = mapper.map(response.getData(), holding);
          return mapped != null ? Map.entry(holding, mapped) : null;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static boolean isFetchable(PortfolioHolding holding) {
    return holding.getHoldingType() != null
        && !HoldingMappingUtils.isSkipped(holding.getHoldingType())
        && holding.getSecurityIdentifier() != null;
  }

  private static boolean isValidResponse(SecurityAttributeResult<?> response) {
    return response != null && response.getIdentifier() != null && response.getData() != null;
  }

  private static String buildKey(SecurityIdentifier identifier) {
    return Objects.toString(identifier.getIdType(), "") + KEY_SEPARATOR + Objects.toString(identifier.getId(), "");
  }
}
