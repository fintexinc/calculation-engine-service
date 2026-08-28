package ca.tangerine.pce.application.calculation.service;

import ca.tangerine.pce.model.domain.CurrencyExchangePair;
import ca.tangerine.pce.model.domain.calculation.DateRange;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.error.exceptions.ExternalServiceUnavailableException;
import ca.tangerine.pce.port.webclient.boc.FxRatesFetcher;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;
import ca.tangerine.wm.commons.error.Notification;
import ca.tangerine.wm.commons.error.Severity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import static ca.tangerine.pce.model.error.ErrorCode.Codes.FX_RATES_UNAVAILABLE;
import static ca.tangerine.pce.util.DateTimeUtils.toLastDayOfMonth;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holdingWithoutCountry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FxRateServiceTest {

  private static final DateRange RANGE = new DateRange(LocalDate.parse("2025-01-01"), LocalDate.parse("2025-12-31"));
  private static final LocalDate DECEMBER_END = LocalDate.parse("2020-12-31");
  private static final LocalDate JANUARY_END = LocalDate.parse("2021-01-31");
  private static final LocalDate FEBRUARY_END = LocalDate.parse("2021-02-28");
  private static final LocalDate MARCH_END = LocalDate.parse("2021-03-31");
  private static final LocalDate APRIL_END = LocalDate.parse("2021-04-30");

  private final FxRatesFetcher fxRatesFetcher = mock(FxRatesFetcher.class);
  private final FxRateService service = new FxRateService(fxRatesFetcher);

  @Test
  void shouldConvert_whenCheckResultConvertUsdToCad() {
    var fxRates = Map.of(
        new CurrencyExchangePair(Currency.USD, Currency.CAD),
        (NavigableMap<LocalDate, BigDecimal>) getUsdToCadRates());

    PortfolioHolding etfHolding = holdingWithoutCountry(new SecurityIdentifier("Ticker",
        FiIdentifierType.TICKER), null, null);
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns = getReturns(etfHolding);
    Map<PortfolioHolding, Currency> holdingCurrencies = Map.of(etfHolding, Currency.USD);
    List<Notification> warnings = new ArrayList<>();

    var actual = service.convertReturns(returns, holdingCurrencies, fxRates, Currency.CAD, warnings);

    assertEquals(0, BigDecimal.valueOf(102).compareTo(
        actual.get(etfHolding).get(toLastDayOfMonth(LocalDate.now().plusMonths(1)))));
    assertEquals(0, BigDecimal.valueOf(308).compareTo(
        actual.get(etfHolding).get(toLastDayOfMonth(LocalDate.now().plusMonths(2)))));
    assertTrue(warnings.isEmpty());
  }

  @Test
  void shouldConvert_whenCheckResultConvertCadToUsd() {
    var fxRates = Map.of(
        new CurrencyExchangePair(Currency.CAD, Currency.USD),
        (NavigableMap<LocalDate, BigDecimal>) getCadToUsdRates());

    PortfolioHolding etfHolding = holdingWithoutCountry(new SecurityIdentifier("Ticker",
        FiIdentifierType.TICKER), null, null);
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns = getReturns(etfHolding);
    Map<PortfolioHolding, Currency> holdingCurrencies = Map.of(etfHolding, Currency.CAD);
    List<Notification> warnings = new ArrayList<>();

    var actual = service.convertReturns(returns, holdingCurrencies, fxRates, Currency.USD, warnings);

    assertEquals(0, BigDecimal.valueOf(102).compareTo(
        actual.get(etfHolding).get(toLastDayOfMonth(LocalDate.now().plusMonths(1)))));
    assertEquals(0, BigDecimal.valueOf(410).compareTo(
        actual.get(etfHolding).get(toLastDayOfMonth(LocalDate.now().plusMonths(2)))));
    assertTrue(warnings.isEmpty());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("incompleteFxCoverage")
  void shouldConvertOnlyMonthsWithCompleteFxCoverage_whenRatesDoNotCoverFullDateRange(
      String scenario,
      NavigableMap<LocalDate, BigDecimal> rates,
      TreeMap<LocalDate, BigDecimal> returnsPerHolding,
      Map<LocalDate, BigDecimal> expectedReturns) {
    var fxRates = Map.of(
        new CurrencyExchangePair(Currency.CAD, Currency.USD),
        rates);

    PortfolioHolding etfHolding = holdingWithoutCountry(new SecurityIdentifier("Ticker",
        FiIdentifierType.TICKER), null, null);
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns = Map.of(etfHolding, returnsPerHolding);
    Map<PortfolioHolding, Currency> holdingCurrencies = Map.of(etfHolding, Currency.CAD);
    List<Notification> warnings = new ArrayList<>();

    var actual = service.convertReturns(returns, holdingCurrencies, fxRates, Currency.USD, warnings);

    assertEquals(1, warnings.size());
    Notification warning = warnings.getFirst();
    assertEquals(FX_RATES_UNAVAILABLE, warning.getCode());
    assertEquals(Severity.WARNING, warning.getSeverity());
    assertEquals(etfHolding.getIdsString(), warning.getMetadata().get("holdingId"));
    assertEquals("FX rates unavailable for holding " + etfHolding.getIdsString()
        + ": CAD -> USD", warning.getMessage());
    assertEquals(etfHolding.getIdsString(), warning.getMetadata().get("param-1"));
    assertEquals(Currency.CAD, warning.getMetadata().get("param-2"));
    assertEquals(Currency.USD, warning.getMetadata().get("param-3"));
    TreeMap<LocalDate, BigDecimal> actualReturns = actual.get(etfHolding);
    assertThat(actualReturns).containsOnlyKeys(expectedReturns.keySet());
    expectedReturns.forEach((date, value) -> assertThat(actualReturns.get(date)).isEqualByComparingTo(value));
    assertCadToUsdWarning(warnings, etfHolding);
  }

  @Test
  void shouldUseLatestRateFromSameMonth_whenMonthEndIsNonTradingDay() {
    NavigableMap<LocalDate, BigDecimal> rates = new TreeMap<>(Map.of(
        DECEMBER_END, BigDecimal.ONE,
        LocalDate.parse("2021-01-29"), BigDecimal.valueOf(2),
        LocalDate.parse("2021-02-26"), BigDecimal.valueOf(2)));
    var fxRates = Map.of(new CurrencyExchangePair(Currency.CAD, Currency.USD), rates);

    PortfolioHolding etfHolding = holdingWithoutCountry(new SecurityIdentifier("Ticker",
        FiIdentifierType.TICKER), null, null);
    TreeMap<LocalDate, BigDecimal> returnsPerHolding = new TreeMap<>(Map.of(
        JANUARY_END, BigDecimal.ONE,
        FEBRUARY_END, BigDecimal.valueOf(2)));
    List<Notification> warnings = new ArrayList<>();

    var actual = service.convertReturns(
        Map.of(etfHolding, returnsPerHolding),
        Map.of(etfHolding, Currency.CAD),
        fxRates,
        Currency.USD,
        warnings);

    assertThat(actual.get(etfHolding)).containsOnlyKeys(JANUARY_END, FEBRUARY_END);
    assertThat(actual.get(etfHolding).get(JANUARY_END)).isEqualByComparingTo("102");
    assertThat(actual.get(etfHolding).get(FEBRUARY_END)).isEqualByComparingTo("2");
    assertThat(warnings).isEmpty();
  }

  @Test
  void shouldReturnEmptySeriesAndEmitWarning_whenRatesMapIsEmpty() {
    var fxRates = Map.of(
        new CurrencyExchangePair(Currency.CAD, Currency.USD),
        (NavigableMap<LocalDate, BigDecimal>) new TreeMap<LocalDate, BigDecimal>());

    PortfolioHolding etfHolding = holdingWithoutCountry(new SecurityIdentifier("Ticker",
        FiIdentifierType.TICKER), null, null);
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns = getReturns(etfHolding);
    Map<PortfolioHolding, Currency> holdingCurrencies = Map.of(etfHolding, Currency.CAD);
    List<Notification> warnings = new ArrayList<>();

    var actual = service.convertReturns(returns, holdingCurrencies, fxRates, Currency.USD, warnings);

    assertThat(actual.get(etfHolding)).isEmpty();
    assertCadToUsdWarning(warnings, etfHolding);
  }

  @Test
  void shouldReturnEmptySeriesAndEmitWarning_whenPairIsNotInRatesMap() {
    Map<CurrencyExchangePair, NavigableMap<LocalDate, BigDecimal>> fxRates = Map.of();

    PortfolioHolding etfHolding = holdingWithoutCountry(new SecurityIdentifier("Ticker",
        FiIdentifierType.TICKER), null, null);
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns = getReturns(etfHolding);
    Map<PortfolioHolding, Currency> holdingCurrencies = Map.of(etfHolding, Currency.CAD);
    List<Notification> warnings = new ArrayList<>();

    var actual = service.convertReturns(returns, holdingCurrencies, fxRates, Currency.USD, warnings);

    assertThat(actual.get(etfHolding)).isEmpty();
    assertCadToUsdWarning(warnings, etfHolding);
  }

  @Test
  void shouldFetchEachDistinctSourceCurrency_whenRatesIsCalled() {
    PortfolioHolding usdHolding = holdingWithoutCountry(new SecurityIdentifier("USD-ETF",
        FiIdentifierType.TICKER), null, null);
    PortfolioHolding eurHolding = holdingWithoutCountry(new SecurityIdentifier("EUR-ETF",
        FiIdentifierType.TICKER), null, null);
    Map<PortfolioHolding, Currency> holdingCurrencies = Map.of(usdHolding, Currency.USD, eurHolding, Currency.EUR);

    var usdCadRates = new TreeMap<LocalDate, BigDecimal>();
    usdCadRates.put(RANGE.start(), BigDecimal.valueOf(1.35));
    var eurCadRates = new TreeMap<LocalDate, BigDecimal>();
    eurCadRates.put(RANGE.start(), BigDecimal.valueOf(1.45));
    when(fxRatesFetcher.fetch(new CurrencyExchangePair(Currency.USD, Currency.CAD), RANGE)).thenReturn(usdCadRates);
    when(fxRatesFetcher.fetch(new CurrencyExchangePair(Currency.EUR, Currency.CAD), RANGE)).thenReturn(eurCadRates);

    var actual = service.rates(holdingCurrencies, Currency.CAD, RANGE);

    assertThat(actual)
        .containsEntry(new CurrencyExchangePair(Currency.USD, Currency.CAD), usdCadRates)
        .containsEntry(new CurrencyExchangePair(Currency.EUR, Currency.CAD), eurCadRates)
        .hasSize(2);
  }

  @Test
  void shouldDeduplicateSourceCurrencies_whenMultipleHoldingsShareCurrency() {
    PortfolioHolding firstUsdHolding = holdingWithoutCountry(new SecurityIdentifier("USD-1",
        FiIdentifierType.TICKER), null, null);
    PortfolioHolding secondUsdHolding = holdingWithoutCountry(new SecurityIdentifier("USD-2",
        FiIdentifierType.TICKER), null, null);
    Map<PortfolioHolding, Currency> holdingCurrencies = Map.of(firstUsdHolding, Currency.USD, secondUsdHolding,
        Currency.USD);
    when(fxRatesFetcher.fetch(any(), any())).thenReturn(new TreeMap<>());

    service.rates(holdingCurrencies, Currency.CAD, RANGE);

    verify(fxRatesFetcher, times(1)).fetch(new CurrencyExchangePair(Currency.USD, Currency.CAD), RANGE);
  }

  @Test
  void shouldSkipSelfCurrencyPairs_whenSourceCurrencyEqualsTargetCurrency() {
    PortfolioHolding cadHolding = holdingWithoutCountry(new SecurityIdentifier("CAD-ETF",
        FiIdentifierType.TICKER), null, null);
    Map<PortfolioHolding, Currency> holdingCurrencies = Map.of(cadHolding, Currency.CAD);

    var actual = service.rates(holdingCurrencies, Currency.CAD, RANGE);

    assertThat(actual).isEmpty();
    verify(fxRatesFetcher, never()).fetch(any(), any());
  }

  @Test
  void shouldReturnEmptyTreeMapForFailedPair_whenFetcherThrowsBasePceException() {
    PortfolioHolding usdHolding = holdingWithoutCountry(new SecurityIdentifier("USD-ETF",
        FiIdentifierType.TICKER), null, null);
    PortfolioHolding eurHolding = holdingWithoutCountry(new SecurityIdentifier("EUR-ETF",
        FiIdentifierType.TICKER), null, null);
    Map<PortfolioHolding, Currency> holdingCurrencies = Map.of(usdHolding, Currency.USD, eurHolding, Currency.EUR);

    var eurCadRates = new TreeMap<LocalDate, BigDecimal>();
    eurCadRates.put(RANGE.start(), BigDecimal.valueOf(1.45));
    when(fxRatesFetcher.fetch(new CurrencyExchangePair(Currency.USD, Currency.CAD), RANGE))
        .thenThrow(new ExternalServiceUnavailableException("Bank of Canada"));
    when(fxRatesFetcher.fetch(new CurrencyExchangePair(Currency.EUR, Currency.CAD), RANGE)).thenReturn(eurCadRates);

    var actual = service.rates(holdingCurrencies, Currency.CAD, RANGE);

    assertThat(actual.get(new CurrencyExchangePair(Currency.USD, Currency.CAD))).isEmpty();
    assertThat(actual).containsEntry(new CurrencyExchangePair(Currency.EUR, Currency.CAD), eurCadRates);
  }

  @Test
  void shouldReturnEmptyTreeMapForPair_whenFetcherReturnsNull() {
    PortfolioHolding usdHolding = holdingWithoutCountry(new SecurityIdentifier("USD-ETF",
        FiIdentifierType.TICKER), null, null);
    Map<PortfolioHolding, Currency> holdingCurrencies = Map.of(usdHolding, Currency.USD);
    when(fxRatesFetcher.fetch(new CurrencyExchangePair(Currency.USD, Currency.CAD), RANGE)).thenReturn(null);

    var actual = service.rates(holdingCurrencies, Currency.CAD, RANGE);

    assertThat(actual.get(new CurrencyExchangePair(Currency.USD, Currency.CAD))).isEmpty();
  }

  private Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> getReturns(PortfolioHolding etfHolding) {
    // Returns are in percent form (1 = 1%, 2 = 2%) — same convention as Market Investment Catalogue's monthly returns
    // payload (`MonthlyReturnsMapper` stores values verbatim).
    HashMap<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns = new HashMap<>();
    TreeMap<LocalDate, BigDecimal> returnsPerHolding = new TreeMap<>();
    returnsPerHolding.put(toLastDayOfMonth(LocalDate.now().plusMonths(1)), BigDecimal.valueOf(1));
    returnsPerHolding.put(toLastDayOfMonth(LocalDate.now().plusMonths(2)), BigDecimal.valueOf(2));
    returns.put(etfHolding, returnsPerHolding);
    return returns;
  }

  private TreeMap<LocalDate, BigDecimal> getUsdToCadRates() {
    TreeMap<LocalDate, BigDecimal> rates = new TreeMap<>();
    rates.put(toLastDayOfMonth(LocalDate.now()), BigDecimal.valueOf(2));
    rates.put(toLastDayOfMonth(LocalDate.now().plusMonths(1)), BigDecimal.valueOf(4));
    rates.put(toLastDayOfMonth(LocalDate.now().plusMonths(2)), BigDecimal.valueOf(16));
    return rates;
  }

  private TreeMap<LocalDate, BigDecimal> getCadToUsdRates() {
    TreeMap<LocalDate, BigDecimal> rates = new TreeMap<>();
    rates.put(toLastDayOfMonth(LocalDate.now()), BigDecimal.valueOf(1));
    rates.put(toLastDayOfMonth(LocalDate.now().plusMonths(1)), BigDecimal.valueOf(2));
    rates.put(toLastDayOfMonth(LocalDate.now().plusMonths(2)), BigDecimal.valueOf(10));
    return rates;
  }

  private static Stream<Arguments> incompleteFxCoverage() {
    TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(
        JANUARY_END, BigDecimal.ONE,
        FEBRUARY_END, BigDecimal.valueOf(2)));
    TreeMap<LocalDate, BigDecimal> extendedReturns = new TreeMap<>(returns);
    extendedReturns.put(MARCH_END, BigDecimal.valueOf(3));
    extendedReturns.put(APRIL_END, BigDecimal.valueOf(4));

    return Stream.of(
        Arguments.of(
            "leading range is missing",
            new TreeMap<>(Map.of(
                JANUARY_END, BigDecimal.valueOf(2),
                FEBRUARY_END, BigDecimal.valueOf(2))),
            returns,
            Map.of(FEBRUARY_END, BigDecimal.valueOf(2))),
        Arguments.of(
            "trailing range is missing",
            new TreeMap<>(Map.of(
                DECEMBER_END, BigDecimal.ONE,
                JANUARY_END, BigDecimal.valueOf(2))),
            returns,
            Map.of(JANUARY_END, BigDecimal.valueOf(102))),
        Arguments.of(
            "internal month is missing",
            new TreeMap<>(Map.of(
                DECEMBER_END, BigDecimal.ONE,
                JANUARY_END, BigDecimal.valueOf(2),
                MARCH_END, BigDecimal.valueOf(4),
                APRIL_END, BigDecimal.valueOf(4))),
            extendedReturns,
            Map.of(
                JANUARY_END, BigDecimal.valueOf(102),
                APRIL_END, BigDecimal.valueOf(4))));
  }

  private static void assertCadToUsdWarning(List<Notification> warnings, PortfolioHolding holding) {
    assertThat(warnings).hasSize(1);
    Notification warning = warnings.getFirst();
    assertEquals(FX_RATES_UNAVAILABLE, warning.getCode());
    assertEquals(Severity.WARNING, warning.getSeverity());
    assertThat(warning.getUuid()).isNotBlank();
    assertEquals("FX rates unavailable for holding " + holding.getIdsString()
        + ": CAD -> USD", warning.getMessage());
    assertEquals(holding.getIdsString(), warning.getMetadata().get("param-1"));
    assertEquals(Currency.CAD, warning.getMetadata().get("param-2"));
    assertEquals(Currency.USD, warning.getMetadata().get("param-3"));
  }
}