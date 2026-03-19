package com.fintex.ce.monthlyreturns;

import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.exception.DataErrorException;
import com.fintex.ce.domain.model.FxRates;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_RRC_MFR_001;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FxRatesConversionComponentTest {

  @Test
  void shouldConvert_whenCheckResultConvertUsdToCad() {
    // SETUP
    Map<LocalDate, FxRates.FxRate> fxRates = getFxRates();
    final FxRatesConversionComponent sut = new FxRatesConversionComponent(fxRates, Currency.CAD);

    final Holding etfHolding = new Holding();
    etfHolding.setSecurityIdentifier(new SecurityIdentifier("Ticker", FiIdentifierType.TICKER));

    final BigDecimal expectedFirst = BigDecimal.valueOf(102);
    final BigDecimal expectedSecond = BigDecimal.valueOf(308);
    final Map<Holding, TreeMap<LocalDate, BigDecimal>> returns = getReturns(etfHolding);
    final Map<Holding, Currency> holdingCurrencies = Map.of(etfHolding, Currency.USD);

    // ACT
    final Map<Holding, TreeMap<LocalDate, BigDecimal>> actual = sut.convert(returns, holdingCurrencies);

    // VERIFY
    final BigDecimal actualFirst = actual.get(etfHolding).get(toLastDayOfMonth(LocalDate.now().plusMonths(1)));
    final BigDecimal actualSecond = actual.get(etfHolding).get(toLastDayOfMonth(LocalDate.now().plusMonths(2)));
    assertEquals(0, expectedFirst.compareTo(actualFirst));
    assertEquals(0, expectedSecond.compareTo(actualSecond));
  }

  @Test
  void shouldConvert_whenCheckResultConvertCadToUsd() {
    // SETUP
    Map<LocalDate, FxRates.FxRate> fxRates = getFxRates();
    final FxRatesConversionComponent sut = new FxRatesConversionComponent(fxRates, Currency.USD);

    final Holding etfHolding = new Holding();
    etfHolding.setSecurityIdentifier(new SecurityIdentifier("Ticker", FiIdentifierType.TICKER));

    final BigDecimal expectedFirst = BigDecimal.valueOf(102);
    final BigDecimal expectedSecond = BigDecimal.valueOf(410);
    final Map<Holding, TreeMap<LocalDate, BigDecimal>> returns = getReturns(etfHolding);
    final Map<Holding, Currency> holdingCurrencies = Map.of(etfHolding, Currency.CAD);

    // ACT
    final Map<Holding, TreeMap<LocalDate, BigDecimal>> actual = sut.convert(returns, holdingCurrencies);

    // VERIFY
    final BigDecimal actualFirst = actual.get(etfHolding).get(toLastDayOfMonth(LocalDate.now().plusMonths(1)));
    final BigDecimal actualSecond = actual.get(etfHolding).get(toLastDayOfMonth(LocalDate.now().plusMonths(2)));
    assertEquals(0, expectedFirst.compareTo(actualFirst));
    assertEquals(0, expectedSecond.compareTo(actualSecond));
  }

  @Test
  void shouldConvert_whenFxRateIsNullThrowError() {
    // SETUP
    Map<LocalDate, FxRates.FxRate> fxRates = getNotCompleteFxRates();
    final FxRatesConversionComponent sut = new FxRatesConversionComponent(fxRates, Currency.USD);

    final Holding etfHolding = new Holding();
    etfHolding.setSecurityIdentifier(new SecurityIdentifier("Ticker", FiIdentifierType.TICKER));

    final Map<Holding, TreeMap<LocalDate, BigDecimal>> returns = getReturns(etfHolding);
    final Map<Holding, Currency> holdingCurrencies = Map.of(etfHolding, Currency.CAD);

    final LocalDate date = toLastDayOfMonth(LocalDate.now().plusMonths(1));
    final DataErrorException expected = ERR_RRC_MFR_001.error(date);

    // ACT
    final DataErrorException actual = assertThrows(DataErrorException.class, () -> sut.convert(returns,
        holdingCurrencies));

    // VERIFY
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

  private Map<LocalDate, FxRates.FxRate> getFxRates() {
    final Map<LocalDate, FxRates.FxRate> fxRates = new HashMap<>();
    fxRates.put(toLastDayOfMonth(LocalDate.now()), new FxRates.FxRate(BigDecimal.valueOf(2), BigDecimal.valueOf(1)));
    fxRates.put(toLastDayOfMonth(LocalDate.now().plusMonths(1)), new FxRates.FxRate(BigDecimal.valueOf(4), BigDecimal
        .valueOf(2)));
    fxRates.put(toLastDayOfMonth(LocalDate.now().plusMonths(2)), new FxRates.FxRate(BigDecimal.valueOf(16), BigDecimal
        .valueOf(10)));
    return fxRates;
  }

  private Map<LocalDate, FxRates.FxRate> getNotCompleteFxRates() {
    final Map<LocalDate, FxRates.FxRate> fxRates = new HashMap<>();
    fxRates.put(toLastDayOfMonth(LocalDate.now()), new FxRates.FxRate(BigDecimal.valueOf(2), BigDecimal.valueOf(1)));
    fxRates.put(toLastDayOfMonth(LocalDate.now().plusMonths(2)), new FxRates.FxRate(BigDecimal.valueOf(16), BigDecimal
        .valueOf(10)));
    return fxRates;
  }

}
