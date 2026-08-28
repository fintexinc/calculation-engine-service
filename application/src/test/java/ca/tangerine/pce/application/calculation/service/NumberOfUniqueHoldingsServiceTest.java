package ca.tangerine.pce.application.calculation.service;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.holding.NumberOfUniqueHoldingsResult;
import ca.tangerine.pce.model.dto.command.PortfolioHoldingsCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.domain.holding.HoldingIdentifiers;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;
import ca.tangerine.wm.commons.error.Notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class NumberOfUniqueHoldingsServiceTest {

  private static final List<FiIdentifierType> PERMITTED_ID_TYPES = List.of(
      FiIdentifierType.MORNINGSTAR_ID, FiIdentifierType.ISIN, FiIdentifierType.CUSIP);

  @Test
  void shouldReturnNumberOfUniqueHoldingsMetric_whenGetMetricInvoked() {
    var service = new NumberOfUniqueHoldingsService(List.of(FiIdentifierType.MORNINGSTAR_ID));

    assertEquals(CalculationMetric.NUMBER_OF_UNIQUE_HOLDINGS, service.getMetric());
  }

  @Test
  void shouldRequireHoldingIdentifiersAttribute_whenRequiredAttributesInvoked() {
    var service = new NumberOfUniqueHoldingsService(List.of(FiIdentifierType.MORNINGSTAR_ID));

    assertEquals(List.of(CompositeSecurityAttribute.HOLDING_IDENTIFIERS), service.requiredAttributes());
    assertEquals(CompositeSecurityAttribute.HOLDING_IDENTIFIERS, service.requiredAttribute());
  }

  @Test
  void shouldReturnResultWithEmptyWarningsAndZeroCount_whenSecurityDataIsEmpty() {
    var service = new NumberOfUniqueHoldingsService(List.of(FiIdentifierType.MORNINGSTAR_ID));

    NumberOfUniqueHoldingsResult result = service.perform(commandFor(Map.of()), Map.of());

    assertNotNull(result);
    assertEquals(0L, result.getNumberOfUniqueHoldings());
    assertNotNull(result.getWarnings());
    assertTrue(result.getWarnings().isEmpty());
  }

  @Test
  void shouldEmitAggregatedMissingIdentifiersWarning_withCountOfSecurities() {
    var service = new NumberOfUniqueHoldingsService(List.of(FiIdentifierType.MORNINGSTAR_ID));
    PortfolioHolding h1 = mock(PortfolioHolding.class);
    PortfolioHolding h2 = mock(PortfolioHolding.class);
    PortfolioHolding h3 = mock(PortfolioHolding.class);
    Map<PortfolioHolding, HoldingIdentifiers> fetched = Map.of(
        h1, holdingIdentifiers((List<SecurityIdentifier>) null),
        h2, holdingIdentifiers(List.of()),
        h3, holdingIdentifiers(id("A", FiIdentifierType.MORNINGSTAR_ID)));

    NumberOfUniqueHoldingsResult result = service.perform(commandFor(fetched), fetched);

    assertEquals(3L, result.getNumberOfUniqueHoldings());
    assertEquals(1, result.getWarnings().size());
    Notification warning = result.getWarnings().get(0);
    assertEquals(ErrorCode.Codes.MISSING_HOLDING_IDENTIFIERS, warning.getCode());
    assertTrue(warning.getMessage().contains("2"), "warning message must report count of 2 securities");
  }

  @Test
  void shouldEmitAggregatedNullIdValueWarning_withCountOfUnderlyingHoldings() {
    var service = new NumberOfUniqueHoldingsService(List.of(FiIdentifierType.MORNINGSTAR_ID));
    PortfolioHolding h1 = mock(PortfolioHolding.class);
    PortfolioHolding h2 = mock(PortfolioHolding.class);
    Map<PortfolioHolding, HoldingIdentifiers> fetched = Map.of(
        h1, holdingIdentifiers(
            id(null, FiIdentifierType.MORNINGSTAR_ID),
            id("A", FiIdentifierType.MORNINGSTAR_ID)),
        h2, holdingIdentifiers(
            id(null, FiIdentifierType.MORNINGSTAR_ID),
            id(null, FiIdentifierType.MORNINGSTAR_ID)));

    NumberOfUniqueHoldingsResult result = service.perform(commandFor(fetched), fetched);

    assertEquals(2L, result.getNumberOfUniqueHoldings());
    assertEquals(1, result.getWarnings().size());
    Notification warning = result.getWarnings().get(0);
    assertEquals(ErrorCode.Codes.MISSING_UNDERLYING_HOLDING_ID_VALUE, warning.getCode());
    assertTrue(warning.getMessage().contains("3"), "warning message must report count of 3 underlying holdings");
  }

  @Test
  void shouldEmitBothAggregatedWarnings_whenBothFailureModesOccur() {
    var service = new NumberOfUniqueHoldingsService(List.of(FiIdentifierType.MORNINGSTAR_ID));
    PortfolioHolding h1 = mock(PortfolioHolding.class);
    PortfolioHolding h2 = mock(PortfolioHolding.class);
    Map<PortfolioHolding, HoldingIdentifiers> fetched = Map.of(
        h1, holdingIdentifiers(List.of()),
        h2, holdingIdentifiers(id(null, FiIdentifierType.MORNINGSTAR_ID)));

    NumberOfUniqueHoldingsResult result = service.perform(commandFor(fetched), fetched);

    assertEquals(2L, result.getNumberOfUniqueHoldings());
    assertEquals(2, result.getWarnings().size());
    assertTrue(result.getWarnings().stream()
        .anyMatch(w -> ErrorCode.Codes.MISSING_HOLDING_IDENTIFIERS.equals(w.getCode())));
    assertTrue(result.getWarnings().stream()
        .anyMatch(w -> ErrorCode.Codes.MISSING_UNDERLYING_HOLDING_ID_VALUE.equals(w.getCode())));
  }

  @Test
  void shouldCountUnresolvedHoldingIndividuallyAndEmitSecurityNotFoundWarning_whenSmHasNoRecordOfIdentifier() {
    var service = new NumberOfUniqueHoldingsService(List.of(FiIdentifierType.MORNINGSTAR_ID));
    PortfolioHolding resolved = mock(PortfolioHolding.class);
    PortfolioHolding unresolved = mock(PortfolioHolding.class);
    Map<PortfolioHolding, HoldingIdentifiers> fetched = Map.of(
        resolved, holdingIdentifiers(id("A", FiIdentifierType.MORNINGSTAR_ID)));
    PortfolioHoldingsCommand command = PortfolioHoldingsCommand.builder()
        .holdings(List.of(resolved, unresolved))
        .dataProviders(List.of())
        .build();

    NumberOfUniqueHoldingsResult result = service.perform(command, fetched);

    assertEquals(2L, result.getNumberOfUniqueHoldings());
    assertEquals(1, result.getWarnings().size());
    assertEquals(ErrorCode.Codes.SECURITY_NOT_FOUND_FOR_METRIC, result.getWarnings().get(0).getCode());
  }

  @Test
  void shouldCountOnTheFirstConfiguredTypePresent_whenNoMorningstarIdIsPresent() {
    var service = new NumberOfUniqueHoldingsService(PERMITTED_ID_TYPES);
    PortfolioHolding holding = mock(PortfolioHolding.class);
    Map<PortfolioHolding, HoldingIdentifiers> fetched = Map.of(
        holding, holdingIdentifiers(
            id("US0378331005", FiIdentifierType.ISIN),
            id("037833100", FiIdentifierType.CUSIP)));

    NumberOfUniqueHoldingsResult result = service.perform(commandFor(fetched), fetched);

    // ISIN is the first configured type this security carries, so the CUSIP is not counted alongside it: the two
    // could be the same instrument and a flat identifier list cannot tell.
    assertThat(result.getNumberOfUniqueHoldings()).isEqualTo(1L);
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldCountOnce_whenOneHoldingCarriesSeveralConfiguredIdentifiers() {
    var service = new NumberOfUniqueHoldingsService(PERMITTED_ID_TYPES);
    PortfolioHolding holding = mock(PortfolioHolding.class);
    Map<PortfolioHolding, HoldingIdentifiers> fetched = Map.of(
        holding, holdingIdentifiers(
            id("0P0000APPL", FiIdentifierType.MORNINGSTAR_ID),
            id("US0378331005", FiIdentifierType.ISIN),
            id("037833100", FiIdentifierType.CUSIP)));

    NumberOfUniqueHoldingsResult result = service.perform(commandFor(fetched), fetched);

    assertThat(result.getNumberOfUniqueHoldings()).isEqualTo(1L);
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldDeduplicateAcrossSecurities_whenSameIsinRecurs() {
    var service = new NumberOfUniqueHoldingsService(PERMITTED_ID_TYPES);
    PortfolioHolding h1 = mock(PortfolioHolding.class);
    PortfolioHolding h2 = mock(PortfolioHolding.class);
    Map<PortfolioHolding, HoldingIdentifiers> fetched = Map.of(
        h1, holdingIdentifiers(id("US0378331005", FiIdentifierType.ISIN)),
        h2, holdingIdentifiers(id("US0378331005", FiIdentifierType.ISIN)));

    NumberOfUniqueHoldingsResult result = service.perform(commandFor(fetched), fetched);

    assertThat(result.getNumberOfUniqueHoldings()).isEqualTo(1L);
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldIgnoreTickerIdentifiers_whenTheyAreTheOnlyOnesPresent() {
    var service = new NumberOfUniqueHoldingsService(PERMITTED_ID_TYPES);
    PortfolioHolding holding = mock(PortfolioHolding.class);
    Map<PortfolioHolding, HoldingIdentifiers> fetched = Map.of(
        holding, holdingIdentifiers(id("AAPL", FiIdentifierType.TICKER)));

    NumberOfUniqueHoldingsResult result = service.perform(commandFor(fetched), fetched);

    assertThat(result.getNumberOfUniqueHoldings()).isEqualTo(1L);
    assertThat(result.getWarnings())
        .extracting(Notification::getCode)
        .containsExactly(ErrorCode.Codes.MISSING_HOLDING_IDENTIFIERS);
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("countingScenarios")
  void shouldCountDistinctHoldingIdsAndEmitWarningsForHoldingsWithNoMatchingIds_whenPerformInvoked(
      String scenario,
      FiIdentifierType configuredType,
      Map<PortfolioHolding, HoldingIdentifiers> fetched,
      Long expectedCount,
      int expectedWarnings) {
    var service = new NumberOfUniqueHoldingsService(List.of(configuredType));

    NumberOfUniqueHoldingsResult result = service.perform(commandFor(fetched), fetched);

    assertEquals(expectedCount, result.getNumberOfUniqueHoldings());
    assertEquals(expectedWarnings, result.getWarnings().size());
  }

  static Stream<Arguments> countingScenarios() {
    PortfolioHolding h1 = mock(PortfolioHolding.class);
    PortfolioHolding h2 = mock(PortfolioHolding.class);
    PortfolioHolding h3 = mock(PortfolioHolding.class);

    return Stream.of(
        Arguments.of(
            "single holding with null holdingIds list → 1 (unresolvable security), 1 warning",
            FiIdentifierType.MORNINGSTAR_ID,
            Map.of(h1, holdingIdentifiers((List<SecurityIdentifier>) null)),
            1L,
            1),
        Arguments.of(
            "single holding with empty holdingIds list → 1 (unresolvable security), 1 warning",
            FiIdentifierType.MORNINGSTAR_ID,
            Map.of(h1, holdingIdentifiers(List.of())),
            1L,
            1),
        Arguments.of(
            "single holding with three distinct ids of configured type → 3, 0 warnings",
            FiIdentifierType.MORNINGSTAR_ID,
            Map.of(h1, holdingIdentifiers(
                id("A", FiIdentifierType.MORNINGSTAR_ID),
                id("B", FiIdentifierType.MORNINGSTAR_ID),
                id("C", FiIdentifierType.MORNINGSTAR_ID))),
            3L,
            0),
        Arguments.of(
            "duplicates within a single security → counted once, 0 warnings",
            FiIdentifierType.MORNINGSTAR_ID,
            Map.of(h1, holdingIdentifiers(
                id("A", FiIdentifierType.MORNINGSTAR_ID),
                id("A", FiIdentifierType.MORNINGSTAR_ID),
                id("B", FiIdentifierType.MORNINGSTAR_ID))),
            2L,
            0),
        Arguments.of(
            "duplicates across securities → counted once, 0 warnings",
            FiIdentifierType.MORNINGSTAR_ID,
            Map.of(
                h1, holdingIdentifiers(
                    id("A", FiIdentifierType.MORNINGSTAR_ID),
                    id("B", FiIdentifierType.MORNINGSTAR_ID)),
                h2, holdingIdentifiers(
                    id("B", FiIdentifierType.MORNINGSTAR_ID),
                    id("C", FiIdentifierType.MORNINGSTAR_ID))),
            3L,
            0),
        Arguments.of(
            "mix of FiIdentifierTypes, configured MORNINGSTAR_ID → only morningstar counted, 0 warnings",
            FiIdentifierType.MORNINGSTAR_ID,
            Map.of(h1, holdingIdentifiers(
                id("A", FiIdentifierType.MORNINGSTAR_ID),
                id("TSLA", FiIdentifierType.TICKER),
                id("NVDA", FiIdentifierType.TICKER))),
            1L,
            0),
        Arguments.of(
            "mix of FiIdentifierTypes, configured TICKER → only tickers counted, 0 warnings",
            FiIdentifierType.TICKER,
            Map.of(h1, holdingIdentifiers(
                id("A", FiIdentifierType.MORNINGSTAR_ID),
                id("TSLA", FiIdentifierType.TICKER),
                id("NVDA", FiIdentifierType.TICKER))),
            2L,
            0),
        Arguments.of(
            "multiple holdings each contributing distinct ids → sum of distinct, 0 warnings",
            FiIdentifierType.MORNINGSTAR_ID,
            Map.of(
                h1, holdingIdentifiers(id("A", FiIdentifierType.MORNINGSTAR_ID)),
                h2, holdingIdentifiers(id("B", FiIdentifierType.MORNINGSTAR_ID)),
                h3, holdingIdentifiers(id("C", FiIdentifierType.MORNINGSTAR_ID))),
            3L,
            0),
        Arguments.of(
            "no ids match configured type → 1 (unresolvable security), 1 warning",
            FiIdentifierType.MORNINGSTAR_ID,
            Map.of(h1, holdingIdentifiers(
                id("TSLA", FiIdentifierType.TICKER),
                id("NVDA", FiIdentifierType.TICKER))),
            1L,
            1),
        Arguments.of(
            "mixed holdings: one with null list, one with ids → 2 ids plus the unresolvable security, 1 warning",
            FiIdentifierType.MORNINGSTAR_ID,
            Map.of(
                h1, holdingIdentifiers((List<SecurityIdentifier>) null),
                h2, holdingIdentifiers(
                    id("A", FiIdentifierType.MORNINGSTAR_ID),
                    id("B", FiIdentifierType.MORNINGSTAR_ID))),
            3L,
            1),
        Arguments.of(
            "holding with null MstarId value on configured-type identifier → counted as unique, 1 warning",
            FiIdentifierType.MORNINGSTAR_ID,
            Map.of(h1, holdingIdentifiers(id(null, FiIdentifierType.MORNINGSTAR_ID))),
            1L,
            1),
        Arguments.of(
            "mixed holdings: one with null MstarId, one with a valid MstarId → 2 count, 1 warning",
            FiIdentifierType.MORNINGSTAR_ID,
            Map.of(
                h1, holdingIdentifiers(id(null, FiIdentifierType.MORNINGSTAR_ID)),
                h2, holdingIdentifiers(id("A", FiIdentifierType.MORNINGSTAR_ID))),
            2L,
            1),
        Arguments.of(
            "two holdings each with null MstarId → both counted as distinct unique, 1 aggregated warning",
            FiIdentifierType.MORNINGSTAR_ID,
            Map.of(
                h1, holdingIdentifiers(id(null, FiIdentifierType.MORNINGSTAR_ID)),
                h2, holdingIdentifiers(id(null, FiIdentifierType.MORNINGSTAR_ID))),
            2L,
            1),
        Arguments.of(
            "holding with both null and valid MstarId values → only valid id contributes, 1 warning for the null",
            FiIdentifierType.MORNINGSTAR_ID,
            Map.of(h1, holdingIdentifiers(
                id(null, FiIdentifierType.MORNINGSTAR_ID),
                id("A", FiIdentifierType.MORNINGSTAR_ID))),
            1L,
            1),
        Arguments.of(
            "holding with multiple null MstarId values → counted as 1 unique (per holding), 1 warning",
            FiIdentifierType.MORNINGSTAR_ID,
            Map.of(h1, holdingIdentifiers(
                id(null, FiIdentifierType.MORNINGSTAR_ID),
                id(null, FiIdentifierType.MORNINGSTAR_ID))),
            1L,
            1),
        Arguments.of(
            "holding with null MstarId but configured type is TICKER → 1 (unresolvable security), 1 warning",
            FiIdentifierType.TICKER,
            Map.of(h1, holdingIdentifiers(id(null, FiIdentifierType.MORNINGSTAR_ID))),
            1L,
            1),
        Arguments.of(
            "holding with a null SecurityIdentifier element in the list → 1 (unresolvable security), 1 warning",
            FiIdentifierType.MORNINGSTAR_ID,
            Map.of(h1, holdingIdentifiers(Collections.singletonList(null))),
            1L,
            1));
  }

  private static PortfolioHoldingsCommand commandFor(Map<PortfolioHolding, HoldingIdentifiers> fetched) {
    return PortfolioHoldingsCommand.builder()
        .holdings(List.copyOf(fetched.keySet()))
        .dataProviders(List.of())
        .build();
  }

  private static HoldingIdentifiers holdingIdentifiers(SecurityIdentifier... ids) {
    return holdingIdentifiers(List.of(ids));
  }

  private static HoldingIdentifiers holdingIdentifiers(List<SecurityIdentifier> ids) {
    var identifiers = new HoldingIdentifiers();
    identifiers.setHoldingIds(ids);
    return identifiers;
  }

  private static SecurityIdentifier id(String id, FiIdentifierType idType) {
    var identifier = new SecurityIdentifier();
    identifier.setId(id);
    identifier.setIdType(idType);
    return identifier;
  }
}
