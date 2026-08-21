package com.fintex.ce.application.calculation.service;

import com.fintex.ce.calculation.SingleAttributeCalculationService;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.holding.NumberOfUniqueHoldingsResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.holding.HoldingIdentifiers;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.error.Notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class NumberOfUniqueHoldingsService
    implements
      SingleAttributeCalculationService<PortfolioHoldingsCommand, HoldingIdentifiers, NumberOfUniqueHoldingsResult> {

  private final List<FiIdentifierType> comparisonIdTypes;

  public NumberOfUniqueHoldingsService(
      @Value("${default.holdings-identifier-types}") List<FiIdentifierType> comparisonIdTypes) {
    this.comparisonIdTypes = comparisonIdTypes;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.NUMBER_OF_UNIQUE_HOLDINGS;
  }

  @Override
  public CompositeSecurityAttribute requiredAttribute() {
    return CompositeSecurityAttribute.HOLDING_IDENTIFIERS;
  }

  @Override
  public NumberOfUniqueHoldingsResult perform(PortfolioHoldingsCommand command,
      Map<PortfolioHolding, HoldingIdentifiers> data) {
    Map<PortfolioHolding, HoldingIdentifiers> fetched = FilterUtils.restrictToHoldings(data, command.getHoldings());
    List<PortfolioHolding> unresolved = command.getHoldings().stream()
        .filter(holding -> !fetched.containsKey(holding))
        .toList();

    Set<String> uniqueIds = new HashSet<>();
    int securitiesWithoutIdentifiers = 0;
    int underlyingHoldingsWithNullIdValue = 0;
    int holdingsWithNullId = 0;

    for (Map.Entry<PortfolioHolding, HoldingIdentifiers> entry : fetched.entrySet()) {
      List<SecurityIdentifier> configured = configuredTypeIdentifiers(entry.getValue());
      if (configured.isEmpty()) {
        securitiesWithoutIdentifiers++;
        continue;
      }
      boolean hasRealId = false;
      for (SecurityIdentifier id : configured) {
        if (id.getId() != null) {
          uniqueIds.add(id.getId());
          hasRealId = true;
        } else {
          underlyingHoldingsWithNullIdValue++;
        }
      }
      if (!hasRealId) {
        // configured-type identifier exists but its value is null; cannot deduplicate, count as its own unique holding
        holdingsWithNullId++;
      }
    }

    List<Notification> warnings = collectWarnings(securitiesWithoutIdentifiers, underlyingHoldingsWithNullIdValue,
        unresolved);

    long count = (long) uniqueIds.size() + holdingsWithNullId + securitiesWithoutIdentifiers + unresolved.size();
    return new NumberOfUniqueHoldingsResult(count, warnings);
  }

  private static @NonNull List<Notification> collectWarnings(int securitiesWithoutIdentifiers,
      int underlyingHoldingsWithNullIdValue, List<PortfolioHolding> unresolvedHoldings) {
    List<Notification> warnings = new ArrayList<>();
    if (securitiesWithoutIdentifiers > 0) {
      warnings.add(ErrorCode.MISSING_HOLDING_IDENTIFIERS.asNotification(securitiesWithoutIdentifiers));
    }
    if (underlyingHoldingsWithNullIdValue > 0) {
      warnings.add(ErrorCode.MISSING_UNDERLYING_HOLDING_ID_VALUE.asNotification(underlyingHoldingsWithNullIdValue));
    }
    unresolvedHoldings.forEach(holding -> warnings.add(ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC
        .toNotificationForHolding(holding, CalculationMetric.NUMBER_OF_UNIQUE_HOLDINGS.getUserFriendlyName())));
    return warnings;
  }

  /**
   * The identifiers of one security to count, narrowed to a single type so that a producer sending several identifiers
   * for the same underlying holding cannot inflate the result. The first configured type with a usable value wins,
   * which is why {@code comparisonIdTypes} is ordered; a type present only with null values is skipped so the
   * null-value warning still reports against a type that had nothing better behind it.
   */
  private List<SecurityIdentifier> configuredTypeIdentifiers(HoldingIdentifiers identifiers) {
    List<SecurityIdentifier> present = Optional.ofNullable(identifiers)
        .map(HoldingIdentifiers::getHoldingIds)
        .orElseGet(List::of)
        .stream()
        .filter(Objects::nonNull)
        .filter(id -> comparisonIdTypes.contains(id.getIdType()))
        .toList();

    return comparisonIdTypes.stream()
        .map(type -> ofType(present, type))
        .filter(tier -> tier.stream().anyMatch(id -> id.getId() != null))
        .findFirst()
        .orElseGet(() -> comparisonIdTypes.stream()
            .map(type -> ofType(present, type))
            .filter(tier -> !tier.isEmpty())
            .findFirst()
            .orElseGet(List::of));
  }

  private static List<SecurityIdentifier> ofType(List<SecurityIdentifier> identifiers, FiIdentifierType type) {
    return identifiers.stream()
        .filter(id -> type.equals(id.getIdType()))
        .toList();
  }
}
