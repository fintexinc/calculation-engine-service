package com.fintex.ce.application.calculation.service;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.holding.NumberOfUniqueHoldingsResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.Warning;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.holding.HoldingIdentifiers;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NumberOfUniqueHoldingsServiceTest {

  @SuppressWarnings("unchecked")
  private final SecurityDataFetcher<HoldingIdentifiers> fetcher = mock(SecurityDataFetcher.class);

  @Test
  void shouldReturnNumberOfUniqueHoldingsMetric_whenGetMetricInvoked() {
    var service = new NumberOfUniqueHoldingsService(fetcher, FiIdentifierType.MORNINGSTAR_ID);

    assertEquals(CalculationMetric.NUMBER_OF_UNIQUE_HOLDINGS, service.getMetric());
  }

  @Test
  void shouldNotInvokeFetcher_whenGetMetricInvoked() {
    var service = new NumberOfUniqueHoldingsService(fetcher, FiIdentifierType.MORNINGSTAR_ID);

    service.getMetric();

    verifyNoInteractions(fetcher);
  }

  @Test
  void shouldPassHoldingsAndProvidersToFetcher_whenPerformInvoked() {
    var service = new NumberOfUniqueHoldingsService(fetcher, FiIdentifierType.MORNINGSTAR_ID);
    var holdings = List.of(mock(PortfolioHolding.class));
    var providers = List.of(DataProvider.MORNINGSTAR);
    var command = PortfolioHoldingsCommand.builder()
        .holdings(holdings)
        .dataProviders(providers)
        .build();
    when(fetcher.fetch(any(), any())).thenReturn(Map.of());

    service.perform(command);

    verify(fetcher).fetch(holdings, providers);
  }

  @Test
  void shouldReturnResultWithEmptyWarningsAndZeroCount_whenFetcherReturnsEmptyMap() {
    var service = new NumberOfUniqueHoldingsService(fetcher, FiIdentifierType.MORNINGSTAR_ID);
    when(fetcher.fetch(any(), any())).thenReturn(Map.of());

    NumberOfUniqueHoldingsResult result = service.perform(commandWithoutData());

    assertNotNull(result);
    assertEquals(0L, result.getNumberOfUniqueHoldings());
    assertNotNull(result.getWarnings());
    assertTrue(result.getWarnings().isEmpty());
  }

  @Test
  void shouldEmitAggregatedMissingIdentifiersWarning_withCountOfSecurities() {
    var service = new NumberOfUniqueHoldingsService(fetcher, FiIdentifierType.MORNINGSTAR_ID);
    PortfolioHolding h1 = mock(PortfolioHolding.class);
    PortfolioHolding h2 = mock(PortfolioHolding.class);
    PortfolioHolding h3 = mock(PortfolioHolding.class);
    when(fetcher.fetch(any(), any())).thenReturn(Map.of(
        h1, holdingIdentifiers((List<SecurityIdentifier>) null),
        h2, holdingIdentifiers(List.of()),
        h3, holdingIdentifiers(id("A", FiIdentifierType.MORNINGSTAR_ID))));

    NumberOfUniqueHoldingsResult result = service.perform(commandWithoutData());

    assertEquals(1L, result.getNumberOfUniqueHoldings());
    assertEquals(1, result.getWarnings().size());
    Warning warning = result.getWarnings().get(0);
    assertEquals(ErrorCode.Codes.MISSING_HOLDING_IDENTIFIERS, warning.getCode());
    assertTrue(warning.getMessage().contains("2"), "warning message must report count of 2 securities");
  }

  @Test
  void shouldEmitAggregatedNullIdValueWarning_withCountOfUnderlyingHoldings() {
    var service = new NumberOfUniqueHoldingsService(fetcher, FiIdentifierType.MORNINGSTAR_ID);
    PortfolioHolding h1 = mock(PortfolioHolding.class);
    PortfolioHolding h2 = mock(PortfolioHolding.class);
    when(fetcher.fetch(any(), any())).thenReturn(Map.of(
        h1, holdingIdentifiers(
            id(null, FiIdentifierType.MORNINGSTAR_ID),
            id("A", FiIdentifierType.MORNINGSTAR_ID)),
        h2, holdingIdentifiers(
            id(null, FiIdentifierType.MORNINGSTAR_ID),
            id(null, FiIdentifierType.MORNINGSTAR_ID))));

    NumberOfUniqueHoldingsResult result = service.perform(commandWithoutData());

    assertEquals(2L, result.getNumberOfUniqueHoldings());
    assertEquals(1, result.getWarnings().size());
    Warning warning = result.getWarnings().get(0);
    assertEquals(ErrorCode.Codes.MISSING_UNDERLYING_HOLDING_ID_VALUE, warning.getCode());
    assertTrue(warning.getMessage().contains("3"), "warning message must report count of 3 underlying holdings");
  }

  @Test
  void shouldEmitBothAggregatedWarnings_whenBothFailureModesOccur() {
    var service = new NumberOfUniqueHoldingsService(fetcher, FiIdentifierType.MORNINGSTAR_ID);
    PortfolioHolding h1 = mock(PortfolioHolding.class);
    PortfolioHolding h2 = mock(PortfolioHolding.class);
    when(fetcher.fetch(any(), any())).thenReturn(Map.of(
        h1, holdingIdentifiers(List.of()),
        h2, holdingIdentifiers(id(null, FiIdentifierType.MORNINGSTAR_ID))));

    NumberOfUniqueHoldingsResult result = service.perform(commandWithoutData());

    assertEquals(1L, result.getNumberOfUniqueHoldings());
    assertEquals(2, result.getWarnings().size());
    assertTrue(result.getWarnings().stream()
        .anyMatch(w -> ErrorCode.Codes.MISSING_HOLDING_IDENTIFIERS.equals(w.getCode())));
    assertTrue(result.getWarnings().stream()
        .anyMatch(w -> ErrorCode.Codes.MISSING_UNDERLYING_HOLDING_ID_VALUE.equals(w.getCode())));
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("countingScenarios")
  void shouldCountDistinctHoldingIdsAndEmitWarningsForHoldingsWithNoMatchingIds_whenPerformInvoked(
      String scenario,
      FiIdentifierType configuredType,
      Map<PortfolioHolding, HoldingIdentifiers> fetched,
      Long expectedCount,
      int expectedWarnings) {
    var service = new NumberOfUniqueHoldingsService(fetcher, configuredType);
    when(fetcher.fetch(any(), any())).thenReturn(fetched);

    NumberOfUniqueHoldingsResult result = service.perform(commandWithoutData());

    assertEquals(expectedCount, result.getNumberOfUniqueHoldings());
    assertEquals(expectedWarnings, result.getWarnings().size());
  }

  static Stream<Arguments> countingScenarios() {
    PortfolioHolding h1 = mock(PortfolioHolding.class);
    PortfolioHolding h2 = mock(PortfolioHolding.class);
    PortfolioHolding h3 = mock(PortfolioHolding.class);

    return Stream.of(
        Arguments.of(
            "single holding with null holdingIds list → null, 1 warning",
            FiIdentifierType.MORNINGSTAR_ID,
            Map.of(h1, holdingIdentifiers((List<SecurityIdentifier>) null)),
            null,
            1),
        Arguments.of(
            "single holding with empty holdingIds list → null, 1 warning",
            FiIdentifierType.MORNINGSTAR_ID,
            Map.of(h1, holdingIdentifiers(List.of())),
            null,
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
            "no ids match configured type → null, 1 warning",
            FiIdentifierType.MORNINGSTAR_ID,
            Map.of(h1, holdingIdentifiers(
                id("TSLA", FiIdentifierType.TICKER),
                id("NVDA", FiIdentifierType.TICKER))),
            null,
            1),
        Arguments.of(
            "mixed holdings: one with null list, one with ids → counts ids from non-null, 1 warning",
            FiIdentifierType.MORNINGSTAR_ID,
            Map.of(
                h1, holdingIdentifiers((List<SecurityIdentifier>) null),
                h2, holdingIdentifiers(
                    id("A", FiIdentifierType.MORNINGSTAR_ID),
                    id("B", FiIdentifierType.MORNINGSTAR_ID))),
            2L,
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
            "holding with null MstarId but configured type is TICKER → no configured-type id, 1 warning",
            FiIdentifierType.TICKER,
            Map.of(h1, holdingIdentifiers(id(null, FiIdentifierType.MORNINGSTAR_ID))),
            null,
            1),
        Arguments.of(
            "holding with a null SecurityIdentifier element in the list → null, 1 warning",
            FiIdentifierType.MORNINGSTAR_ID,
            Map.of(h1, holdingIdentifiers(Collections.singletonList(null))),
            null,
            1));
  }

  private static PortfolioHoldingsCommand commandWithoutData() {
    return PortfolioHoldingsCommand.builder()
        .holdings(List.of())
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
