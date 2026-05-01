package com.fintex.ce.application.calculation.service;

import com.fintex.ce.model.domain.CurrencyExchangePair;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.PceExceptionCollector;
import com.fintex.ce.model.error.exceptions.ExternalServiceUnavailableException;
import com.fintex.ce.port.webclient.boc.FxRatesFetcher;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.fintex.ce.model.error.ErrorCode.FX_RATES_UNAVAILABLE;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
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

  private final FxRatesFetcher fxRatesFetcher = mock(FxRatesFetcher.class);
  private final FxRateService service = new FxRateService(fxRatesFetcher);

  @Test
  void shouldConvert_whenCheckResultConvertUsdToCad() {
    var fxRates = Map.of(
        new CurrencyExchangePair(Currency.USD, Currency.CAD),
        (NavigableMap<LocalDate, BigDecimal>) getUsdToCadRates());

    PortfolioHolding etfHolding = new PortfolioHolding(null, null, new SecurityIdentifier("Ticker",
        FiIdentifierType.TICKER));
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns = getReturns(etfHolding);
    Map<PortfolioHolding, Currency> holdingCurrencies = Map.of(etfHolding, Currency.USD);
    PceExceptionCollector notification = new PceExceptionCollector();

    var actual = service.convertReturns(returns, holdingCurrencies, fxRates, Currency.CAD, notification);

    assertEquals(0, BigDecimal.valueOf(102).compareTo(
        actual.get(etfHolding).get(toLastDayOfMonth(LocalDate.now().plusMonths(1)))));
    assertEquals(0, BigDecimal.valueOf(308).compareTo(
        actual.get(etfHolding).get(toLastDayOfMonth(LocalDate.now().plusMonths(2)))));
    assertTrue(notification.getExceptions().isEmpty());
  }

  @Test
  void shouldConvert_whenCheckResultConvertCadToUsd() {
    var fxRates = Map.of(
        new CurrencyExchangePair(Currency.CAD, Currency.USD),
        (NavigableMap<LocalDate, BigDecimal>) getCadToUsdRates());

    PortfolioHolding etfHolding = new PortfolioHolding(null, null, new SecurityIdentifier("Ticker",
        FiIdentifierType.TICKER));
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns = getReturns(etfHolding);
    Map<PortfolioHolding, Currency> holdingCurrencies = Map.of(etfHolding, Currency.CAD);
    PceExceptionCollector notification = new PceExceptionCollector();

    var actual = service.convertReturns(returns, holdingCurrencies, fxRates, Currency.USD, notification);

    assertEquals(0, BigDecimal.valueOf(102).compareTo(
        actual.get(etfHolding).get(toLastDayOfMonth(LocalDate.now().plusMonths(1)))));
    assertEquals(0, BigDecimal.valueOf(410).compareTo(
        actual.get(etfHolding).get(toLastDayOfMonth(LocalDate.now().plusMonths(2)))));
    assertTrue(notification.getExceptions().isEmpty());
  }

  @Test
  void shouldReturnOriginalReturnsAndAddWarning_whenAnyMonthlyRateIsMissing() {
    var fxRates = Map.of(
        new CurrencyExchangePair(Currency.CAD, Currency.USD),
        (NavigableMap<LocalDate, BigDecimal>) getIncompleteRates());

    PortfolioHolding etfHolding = new PortfolioHolding(null, null, new SecurityIdentifier("Ticker",
        FiIdentifierType.TICKER));
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns = getReturns(etfHolding);
    Map<PortfolioHolding, Currency> holdingCurrencies = Map.of(etfHolding, Currency.CAD);
    PceExceptionCollector notification = new PceExceptionCollector();

    var actual = service.convertReturns(returns, holdingCurrencies, fxRates, Currency.USD, notification);

    assertEquals(returns.get(etfHolding), actual.get(etfHolding));
    assertEquals(1, notification.getExceptions().size());
    assertEquals(FX_RATES_UNAVAILABLE, notification.getExceptions().getFirst().getErrorCode());
  }

  @Test
  void shouldReturnOriginalReturnsAndAddWarning_whenRatesMapIsEmpty() {
    var fxRates = Map.of(
        new CurrencyExchangePair(Currency.CAD, Currency.USD),
        (NavigableMap<LocalDate, BigDecimal>) new TreeMap<LocalDate, BigDecimal>());

    PortfolioHolding etfHolding = new PortfolioHolding(null, null, new SecurityIdentifier("Ticker",
        FiIdentifierType.TICKER));
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns = getReturns(etfHolding);
    Map<PortfolioHolding, Currency> holdingCurrencies = Map.of(etfHolding, Currency.CAD);
    PceExceptionCollector notification = new PceExceptionCollector();

    var actual = service.convertReturns(returns, holdingCurrencies, fxRates, Currency.USD, notification);

    assertEquals(returns.get(etfHolding), actual.get(etfHolding));
    assertEquals(1, notification.getExceptions().size());
    assertEquals(FX_RATES_UNAVAILABLE, notification.getExceptions().getFirst().getErrorCode());
  }

  @Test
  void shouldReturnOriginalReturnsAndAddWarning_whenPairIsNotInRatesMap() {
    Map<CurrencyExchangePair, NavigableMap<LocalDate, BigDecimal>> fxRates = Map.of();

    PortfolioHolding etfHolding = new PortfolioHolding(null, null, new SecurityIdentifier("Ticker",
        FiIdentifierType.TICKER));
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns = getReturns(etfHolding);
    Map<PortfolioHolding, Currency> holdingCurrencies = Map.of(etfHolding, Currency.CAD);
    PceExceptionCollector notification = new PceExceptionCollector();

    var actual = service.convertReturns(returns, holdingCurrencies, fxRates, Currency.USD, notification);

    assertEquals(returns.get(etfHolding), actual.get(etfHolding));
    assertEquals(1, notification.getExceptions().size());
    assertEquals(FX_RATES_UNAVAILABLE, notification.getExceptions().getFirst().getErrorCode());
  }

  @Test
  void shouldFetchEachDistinctSourceCurrency_whenRatesIsCalled() {
    PortfolioHolding usdHolding = new PortfolioHolding(null, null, new SecurityIdentifier("USD-ETF",
        FiIdentifierType.TICKER));
    PortfolioHolding eurHolding = new PortfolioHolding(null, null, new SecurityIdentifier("EUR-ETF",
        FiIdentifierType.TICKER));
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
    PortfolioHolding firstUsdHolding = new PortfolioHolding(null, null, new SecurityIdentifier("USD-1",
        FiIdentifierType.TICKER));
    PortfolioHolding secondUsdHolding = new PortfolioHolding(null, null, new SecurityIdentifier("USD-2",
        FiIdentifierType.TICKER));
    Map<PortfolioHolding, Currency> holdingCurrencies = Map.of(firstUsdHolding, Currency.USD, secondUsdHolding,
        Currency.USD);
    when(fxRatesFetcher.fetch(any(), any())).thenReturn(new TreeMap<>());

    service.rates(holdingCurrencies, Currency.CAD, RANGE);

    verify(fxRatesFetcher, times(1)).fetch(new CurrencyExchangePair(Currency.USD, Currency.CAD), RANGE);
  }

  @Test
  void shouldSkipSelfCurrencyPairs_whenSourceCurrencyEqualsTargetCurrency() {
    PortfolioHolding cadHolding = new PortfolioHolding(null, null, new SecurityIdentifier("CAD-ETF",
        FiIdentifierType.TICKER));
    Map<PortfolioHolding, Currency> holdingCurrencies = Map.of(cadHolding, Currency.CAD);

    var actual = service.rates(holdingCurrencies, Currency.CAD, RANGE);

    assertThat(actual).isEmpty();
    verify(fxRatesFetcher, never()).fetch(any(), any());
  }

  @Test
  void shouldReturnEmptyTreeMapForFailedPair_whenFetcherThrowsBasePceException() {
    PortfolioHolding usdHolding = new PortfolioHolding(null, null, new SecurityIdentifier("USD-ETF",
        FiIdentifierType.TICKER));
    PortfolioHolding eurHolding = new PortfolioHolding(null, null, new SecurityIdentifier("EUR-ETF",
        FiIdentifierType.TICKER));
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
    PortfolioHolding usdHolding = new PortfolioHolding(null, null, new SecurityIdentifier("USD-ETF",
        FiIdentifierType.TICKER));
    Map<PortfolioHolding, Currency> holdingCurrencies = Map.of(usdHolding, Currency.USD);
    when(fxRatesFetcher.fetch(new CurrencyExchangePair(Currency.USD, Currency.CAD), RANGE)).thenReturn(null);

    var actual = service.rates(holdingCurrencies, Currency.CAD, RANGE);

    assertThat(actual.get(new CurrencyExchangePair(Currency.USD, Currency.CAD))).isEmpty();
  }

  private Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> getReturns(PortfolioHolding etfHolding) {
    HashMap<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns = new HashMap<>();
    TreeMap<LocalDate, BigDecimal> returnsPerHolding = new TreeMap<>();
    returnsPerHolding.put(toLastDayOfMonth(LocalDate.now().plusMonths(1)), BigDecimal.valueOf(0.01));
    returnsPerHolding.put(toLastDayOfMonth(LocalDate.now().plusMonths(2)), BigDecimal.valueOf(0.02));
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

  private TreeMap<LocalDate, BigDecimal> getIncompleteRates() {
    TreeMap<LocalDate, BigDecimal> rates = new TreeMap<>();
    rates.put(toLastDayOfMonth(LocalDate.now().plusMonths(2)), BigDecimal.valueOf(10));
    return rates;
  }
}
