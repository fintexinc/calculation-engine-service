package com.fintex.ce.application.returns;

import com.fintex.ce.domain.exception.DataErrorException;
import com.fintex.ce.domain.model.CurrencyExchangePair;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.fintex.ce.domain.exception.code.ErrorCode.ERR_RRC_MFR_001;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FxRatesConversionComponentTest {

  private final FxRatesConversionComponent component = new FxRatesConversionComponent();

  @Test
  void shouldConvert_whenCheckResultConvertUsdToCad() {
    var fxRates = Map.of(
        new CurrencyExchangePair(CurrencyType.USD, CurrencyType.CAD),
        (NavigableMap<LocalDate, BigDecimal>) getUsdToCadRates());

    final Holding etfHolding = new Holding(null, null, new SecurityIdentifier("Ticker", FiIdentifierType.TICKER));
    final Map<Holding, TreeMap<LocalDate, BigDecimal>> returns = getReturns(etfHolding);
    final Map<Holding, CurrencyType> holdingCurrencies = Map.of(etfHolding, CurrencyType.USD);

    final var actual = component.convert(returns, holdingCurrencies, fxRates, CurrencyType.CAD);

    assertEquals(0, BigDecimal.valueOf(102).compareTo(
        actual.get(etfHolding).get(toLastDayOfMonth(LocalDate.now().plusMonths(1)))));
    assertEquals(0, BigDecimal.valueOf(308).compareTo(
        actual.get(etfHolding).get(toLastDayOfMonth(LocalDate.now().plusMonths(2)))));
  }

  @Test
  void shouldConvert_whenCheckResultConvertCadToUsd() {
    var fxRates = Map.of(
        new CurrencyExchangePair(CurrencyType.CAD, CurrencyType.USD),
        (NavigableMap<LocalDate, BigDecimal>) getCadToUsdRates());

    final Holding etfHolding = new Holding(null, null, new SecurityIdentifier("Ticker", FiIdentifierType.TICKER));
    final Map<Holding, TreeMap<LocalDate, BigDecimal>> returns = getReturns(etfHolding);
    final Map<Holding, CurrencyType> holdingCurrencies = Map.of(etfHolding, CurrencyType.CAD);

    final var actual = component.convert(returns, holdingCurrencies, fxRates, CurrencyType.USD);

    assertEquals(0, BigDecimal.valueOf(102).compareTo(
        actual.get(etfHolding).get(toLastDayOfMonth(LocalDate.now().plusMonths(1)))));
    assertEquals(0, BigDecimal.valueOf(410).compareTo(
        actual.get(etfHolding).get(toLastDayOfMonth(LocalDate.now().plusMonths(2)))));
  }

  @Test
  void shouldConvert_whenFxRateIsNullThrowError() {
    var fxRates = Map.of(
        new CurrencyExchangePair(CurrencyType.CAD, CurrencyType.USD),
        (NavigableMap<LocalDate, BigDecimal>) getIncompleteRates());

    final Holding etfHolding = new Holding(null, null, new SecurityIdentifier("Ticker", FiIdentifierType.TICKER));
    final Map<Holding, TreeMap<LocalDate, BigDecimal>> returns = getReturns(etfHolding);
    final Map<Holding, CurrencyType> holdingCurrencies = Map.of(etfHolding, CurrencyType.CAD);

    final LocalDate date = toLastDayOfMonth(LocalDate.now().plusMonths(1));
    final DataErrorException expected = ERR_RRC_MFR_001.error(date);

    final DataErrorException actual = assertThrows(DataErrorException.class,
        () -> component.convert(returns, holdingCurrencies, fxRates, CurrencyType.USD));

    assertEquals(expected, actual);
  }

  private Map<Holding, TreeMap<LocalDate, BigDecimal>> getReturns(final Holding etfHolding) {
    final HashMap<Holding, TreeMap<LocalDate, BigDecimal>> returns = new HashMap<>();
    final TreeMap<LocalDate, BigDecimal> returnsPerHolding = new TreeMap<>();
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