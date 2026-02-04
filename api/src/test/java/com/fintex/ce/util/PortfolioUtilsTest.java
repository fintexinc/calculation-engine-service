package com.fintex.ce.util;

import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.enumeration.calculation.EquityMarketCapType;
import com.fintex.ce.domain.exception.SystemException;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.smclient.dto.FxRatesDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static com.fintex.ce.domain.enumeration.Currency.CAD;
import static com.fintex.ce.domain.enumeration.calculation.CreditQualityRating.AAA;
import static com.fintex.ce.util.TestConstants.LOCAL_DATE_NOW;
import static java.math.BigDecimal.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PortfolioUtilsTest {

  @Test
  void calculateInitialPortfolioWeight_test() {
    // SETUP
    final Holding h1 = new Holding().setType(HoldingType.US_ETF).setValue(TEN);
    final Holding h2 = new Holding().setValue(TEN);
    final Set<Holding> holdings = Set.of(h1, h2);

    // ACT
    final Map<Holding, BigDecimal> actual = PortfolioUtils.calculateInitialPortfolioWeight(holdings);

    // VERIFY
    Assertions.assertNotNull(actual);
    ComparisonUtils.compareMaps(Map.of(h1, new BigDecimal("0.500000000000000"), h2, new BigDecimal(
        "0.500000000000000")), actual);
  }

  private void mapFxRatesBasedOnCurrencyTest(Currency from, Currency to, int actualReturn) {
    // SETUP
    final FxRatesDTO fx = mock(FxRatesDTO.class);
    when(fx.getUsdCad()).thenReturn(BigDecimal.valueOf(3));
    when(fx.getCadUsd()).thenReturn(BigDecimal.valueOf(2));

    final Map<LocalDate, FxRatesDTO> fxRates = Map.of(LOCAL_DATE_NOW, fx);

    // ACT
    final Map<LocalDate, BigDecimal> actual = PortfolioUtils.fxRatesForHolding(fxRates, from, to);

    // VERIFY
    ComparisonUtils.compareMaps(Map.of(LOCAL_DATE_NOW, BigDecimal.valueOf(actualReturn)), actual);
  }

  @Test
  void mapFxRatesBasedOnCurrency_fromCadToUsd() {
    mapFxRatesBasedOnCurrencyTest(CAD, Currency.USD, 2);
  }

  @Test
  void mapFxRatesBasedOnCurrency_fromUsdToCad() {
    // SETUP
    mapFxRatesBasedOnCurrencyTest(Currency.USD, CAD, 3);
  }

  @Test
  void mapFxRatesBasedOnCurrency_fromUsdToUsd() {
    // SETUP
    mapFxRatesBasedOnCurrencyTest(Currency.USD, Currency.USD, 1);
  }

  @Test
  void mapFxRatesBasedOnCurrency_fromCadToCad() {
    // SETUP
    mapFxRatesBasedOnCurrencyTest(Currency.USD, Currency.USD, 1);
  }

  @Test
  void mapFxRatesBasedOnCurrency_fromCadToNull() {
    // SETUP
    assertThrows(SystemException.class, () -> mapFxRatesBasedOnCurrencyTest(CAD, null, 1));
  }

  @Test
  void mapFxRatesBasedOnCurrency_fromNullToUsd() {
    // SETUP
    assertThrows(SystemException.class, () -> mapFxRatesBasedOnCurrencyTest(null, Currency.USD, 1));
  }

  @Test
  void areAllValuesInMapEmpty_checkResultWhenAllValuesInMapAreEmpty() {
    // SETUP
    final Map map = Map.of(new Holding(), Map.of());
    // ACT
    final boolean actual = PortfolioUtils.areAllValuesInMapEmpty(map);

    // VERIFY
    assertTrue(actual);
  }

  @Test
  void areAllValuesInMapEmpty_checkResultWhenNotAllValuesInMapAreEmpty() {
    // SETUP
    final Map map = Map.of(new Holding(), Map.of(), new Holding(ONE, HoldingType.CANADA_MUTUAL_FUNDS), Map.of(AAA,
        ONE));
    // ACT
    final boolean actual = PortfolioUtils.areAllValuesInMapEmpty(map);

    // VERIFY
    assertFalse(actual);
  }

  @Test
  void areAllValuesZerosInMap_checkResultWhenAllValuesInMapAreZeros() {
    // SETUP
    final Map map = Map.of(new Holding(), Map.of(EquityMarketCapType.GIANT, ZERO, EquityMarketCapType.SMALL, ZERO));
    // ACT
    final boolean actual = PortfolioUtils.areAllValuesZerosInMap(map);

    // VERIFY
    assertTrue(actual);
  }

  @Test
  void areAllValuesZerosInMap_checkResultWhenNotAllValuesInMapAreZeros() {
    // SETUP
    final Map map = Map.of(new Holding(), Map.of(EquityMarketCapType.GIANT, ZERO, EquityMarketCapType.SMALL, ZERO,
        EquityMarketCapType.LARGE, ONE));
    // ACT
    final boolean actual = PortfolioUtils.areAllValuesZerosInMap(map);

    // VERIFY
    assertFalse(actual);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeCash() {
    // ACT
    final String actual = PortfolioUtils.createKey(new CashHolding().setCurrency(CAD).setType(HoldingType.CASH));

    // VERIFY
    assertEquals("CASH_CAD", actual);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeUsEtf() {
    // SETUP
    // ACT
    final String result = PortfolioUtils.createKey(new EtfHolding().setTicker("TICKER").setType(HoldingType.US_ETF));

    // VERIFY
    assertEquals(HoldingType.US_ETF.name() + "_" + "TICKER", result);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeCanadaEtf() {
    // SETUP
    // ACT
    final String result = PortfolioUtils.createKey(new EtfHolding().setTicker("TICKER").setType(
        HoldingType.CANADA_ETF));

    // VERIFY
    assertEquals(HoldingType.CANADA_ETF.name() + "_" + "TICKER", result);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeUsStock() {
    // SETUP
    // ACT
    final String result = PortfolioUtils.createKey(new StockHolding()
        .setExchangeCode("EXCHANGE_CODE")
        .setTicker("TICKER")
        .setType(HoldingType.US_STOCKS));

    // VERIFY
    assertEquals(HoldingType.US_STOCKS.name() + "_" + "TICKER" + "_" + "EXCHANGE_CODE", result);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeCadStock() {
    // SETUP
    // ACT
    final String result = PortfolioUtils.createKey(new StockHolding()
        .setExchangeCode("EXCHANGE_CODE")
        .setTicker("TICKER")
        .setType(HoldingType.CANADA_STOCKS));

    // VERIFY
    assertEquals(HoldingType.CANADA_STOCKS.name() + "_" + "TICKER" + "_" + "EXCHANGE_CODE", result);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeMutualFund() {
    // SETUP
    // ACT
    final String result = PortfolioUtils.createKey(new FundSeriesHolding()
        .setFundServCode("FUND_SERVE_CODE")
        .setType(HoldingType.CANADA_MUTUAL_FUNDS));

    // VERIFY
    assertEquals(HoldingType.CANADA_MUTUAL_FUNDS.name() + "_" + "FUND_SERVE_CODE", result);
  }

}