package com.fintex.ce.util;

import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.calculation.EquityMarketCapType;
import com.fintex.ce.domain.exception.SystemException;
import com.fintex.ce.domain.model.FxRates;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.EquitySecurityIdentifier;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.domain.model.enumeration.Currency.CAD;
import static com.fintex.ce.domain.model.calculation.CreditQualityRating.AAA;
import static com.fintex.ce.util.TestConstants.LOCAL_DATE_NOW;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PortfolioUtilsTest {

  @Test
  void calculateInitialPortfolioWeight_test() {
    // SETUP
    final Holding h1 = new Holding().setHoldingType(FinancialInstrumentType.ETF_US).setValue(TEN);
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
    final FxRates.FxRate fx = mock(FxRates.FxRate.class);
    when(fx.getUsdCad()).thenReturn(BigDecimal.valueOf(3));
    when(fx.getCadUsd()).thenReturn(BigDecimal.valueOf(2));

    final Map<LocalDate, FxRates.FxRate> fxRates = Map.of(LOCAL_DATE_NOW, fx);

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
    final Map map = Map.of(new Holding(), Map.of(), new Holding(ONE, FinancialInstrumentType.MUTUAL_FUND_CANADA), Map.of(AAA,
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
    final String actual = PortfolioUtils.createKey(new CashHolding().setCurrency(CAD).setHoldingType(FinancialInstrumentType.CASH));

    // VERIFY
    assertEquals("CASH_CAD", actual);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeUsEtf() {
    // SETUP
    // ACT
    final String result = PortfolioUtils.createKey(new Holding().setSecurityIdentifier(new SecurityIdentifier("TICKER", FiIdentifierType.TICKER)).setHoldingType(FinancialInstrumentType.ETF_US));

    // VERIFY
    assertEquals(FinancialInstrumentType.ETF_US.name() + "_" + "TICKER", result);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeCanadaEtf() {
    // SETUP
    // ACT
    final String result = PortfolioUtils.createKey(new Holding().setSecurityIdentifier(new SecurityIdentifier("TICKER", FiIdentifierType.TICKER)).setHoldingType(
        FinancialInstrumentType.ETF_CANADA));

    // VERIFY
    assertEquals(FinancialInstrumentType.ETF_CANADA.name() + "_" + "TICKER", result);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeUsStock() {
    // SETUP
    final EquitySecurityIdentifier securityIdentifier = mock(EquitySecurityIdentifier.class);
    when(securityIdentifier.getId()).thenReturn("TICKER");
    when(securityIdentifier.getExchangeId()).thenReturn("EXCHANGE_CODE");

    // ACT
    final String result = PortfolioUtils.createKey(new Holding()
        .setSecurityIdentifier(securityIdentifier)
        .setHoldingType(FinancialInstrumentType.STOCK_US));

    // VERIFY
    assertEquals(FinancialInstrumentType.STOCK_US.name() + "_" + "TICKER" + "_" + "EXCHANGE_CODE", result);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeCadStock() {
    // SETUP
    final EquitySecurityIdentifier securityIdentifier = mock(EquitySecurityIdentifier.class);
    when(securityIdentifier.getId()).thenReturn("TICKER");
    when(securityIdentifier.getExchangeId()).thenReturn("EXCHANGE_CODE");

    // ACT
    final String result = PortfolioUtils.createKey(new Holding()
        .setSecurityIdentifier(securityIdentifier)
        .setHoldingType(FinancialInstrumentType.STOCK_CANADA));

    // VERIFY
    assertEquals(FinancialInstrumentType.STOCK_CANADA.name() + "_" + "TICKER" + "_" + "EXCHANGE_CODE", result);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeMutualFund() {
    // SETUP
    // ACT
    final String result = PortfolioUtils.createKey(new Holding()
        .setSecurityIdentifier(new SecurityIdentifier("FUND_SERVE_CODE", FiIdentifierType.FUNDSERV))
        .setHoldingType(FinancialInstrumentType.MUTUAL_FUND_CANADA));

    // VERIFY
    assertEquals(FinancialInstrumentType.MUTUAL_FUND_CANADA.name() + "_" + "FUND_SERVE_CODE", result);
  }

}
