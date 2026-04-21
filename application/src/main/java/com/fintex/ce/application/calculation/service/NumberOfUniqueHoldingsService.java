package com.fintex.ce.application.calculation.service;

import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.holding.NumberOfUniqueHoldingsResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.Warning;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.holding.HoldingIdentifiers;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

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
      CalculationService<PortfolioHoldingsCommand, NumberOfUniqueHoldingsResult> {

  private final SecurityDataFetcher<HoldingIdentifiers> holdingIdentifiersFetcher;
  private final FiIdentifierType defaultComparisonIdType;

  public NumberOfUniqueHoldingsService(
      SecurityDataFetcher<HoldingIdentifiers> holdingIdentifiersFetcher,
      @Value("${default.holdings-identifier-type}") FiIdentifierType defaultComparisonIdType) {
    this.holdingIdentifiersFetcher = holdingIdentifiersFetcher;
    this.defaultComparisonIdType = defaultComparisonIdType;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.NUMBER_OF_UNIQUE_HOLDINGS;
  }

  @Override
  public NumberOfUniqueHoldingsResult perform(PortfolioHoldingsCommand command) {
    Map<PortfolioHolding, HoldingIdentifiers> fetched = holdingIdentifiersFetcher.fetch(command.getHoldings(), command
        .getDataProviders());

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

    List<Warning> warnings = collectWarnings(securitiesWithoutIdentifiers, underlyingHoldingsWithNullIdValue);

    Long count = uniqueIds.isEmpty() && holdingsWithNullId == 0 && !warnings.isEmpty()
        ? null
        : (long) uniqueIds.size() + holdingsWithNullId;
    return new NumberOfUniqueHoldingsResult(count, warnings);
  }

  private static @NonNull List<Warning> collectWarnings(int securitiesWithoutIdentifiers,
      int underlyingHoldingsWithNullIdValue) {
    List<Warning> warnings = new ArrayList<>();
    if (securitiesWithoutIdentifiers > 0) {
      warnings.add(ErrorCode.MISSING_HOLDING_IDENTIFIERS.warning(null, securitiesWithoutIdentifiers));
    }
    if (underlyingHoldingsWithNullIdValue > 0) {
      warnings.add(ErrorCode.MISSING_UNDERLYING_HOLDING_ID_VALUE.warning(null, underlyingHoldingsWithNullIdValue));
    }
    return warnings;
  }

  private List<SecurityIdentifier> configuredTypeIdentifiers(HoldingIdentifiers identifiers) {
    return Optional.ofNullable(identifiers)
        .map(HoldingIdentifiers::getHoldingIds)
        .orElseGet(List::of)
        .stream()
        .filter(Objects::nonNull)
        .filter(id -> defaultComparisonIdType.equals(id.getIdType()))
        .toList();
  }
}
