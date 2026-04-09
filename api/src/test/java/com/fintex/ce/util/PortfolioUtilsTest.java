package com.fintex.ce.util;

import com.fintex.ce.domain.exception.SystemException;
import com.fintex.ce.domain.model.FxRates;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.EquitySecurityIdentifier;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.CreditQualityRatingType;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import com.fintex.sm.model.domain.enumeration.EquityMarketCapitalizationType;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static com.fintex.ce.util.TestConstants.LOCAL_DATE_NOW;
import static com.fintex.sm.model.domain.enumeration.CreditQualityRatingType.AAA;
import static com.fintex.sm.model.domain.enumeration.CurrencyType.CAD;
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
    final Holding h1 = new Holding(TEN, FinancialInstrumentType.ETF_US, null);
    final Holding h2 = new Holding(TEN, null, null);
    final Set<Holding> holdings = Set.of(h1, h2);

    final Map<Holding, BigDecimal> actual = PortfolioUtils.calculateInitialPortfolioWeight(holdings);

    Assertions.assertNotNull(actual);
    ComparisonUtils.compareMaps(Map.of(h1, new BigDecimal("0.500000000000000"), h2, new BigDecimal(
        "0.500000000000000")), actual);
  }

  private void mapFxRatesBasedOnCurrencyTest(CurrencyType from, CurrencyType to, int actualReturn) {
    final FxRates.FxRate fx = mock(FxRates.FxRate.class);
    when(fx.getUsdCad()).thenReturn(BigDecimal.valueOf(3));
    when(fx.getCadUsd()).thenReturn(BigDecimal.valueOf(2));

    final Map<LocalDate, FxRates.FxRate> fxRates = Map.of(LOCAL_DATE_NOW, fx);

    final Map<LocalDate, BigDecimal> actual = PortfolioUtils.fxRatesForHolding(fxRates, from, to);

    ComparisonUtils.compareMaps(Map.of(LOCAL_DATE_NOW, BigDecimal.valueOf(actualReturn)), actual);
  }

  @Test
  void mapFxRatesBasedOnCurrency_fromCadToUsd() {
    mapFxRatesBasedOnCurrencyTest(CAD, CurrencyType.USD, 2);
  }

  @Test
  void mapFxRatesBasedOnCurrency_fromUsdToCad() {
    mapFxRatesBasedOnCurrencyTest(CurrencyType.USD, CAD, 3);
  }

  @Test
  void mapFxRatesBasedOnCurrency_fromUsdToUsd() {
    mapFxRatesBasedOnCurrencyTest(CurrencyType.USD, CurrencyType.USD, 1);
  }

  @Test
  void mapFxRatesBasedOnCurrency_fromCadToCad() {
    mapFxRatesBasedOnCurrencyTest(CurrencyType.USD, CurrencyType.USD, 1);
  }

  @Test
  void mapFxRatesBasedOnCurrency_fromCadToNull() {
    assertThrows(SystemException.class, () -> mapFxRatesBasedOnCurrencyTest(CAD, null, 1));
  }

  @Test
  void mapFxRatesBasedOnCurrency_fromNullToUsd() {
    assertThrows(SystemException.class, () -> mapFxRatesBasedOnCurrencyTest(null, CurrencyType.USD, 1));
  }

  @Test
  void areAllValuesInMapEmpty_checkResultWhenAllValuesInMapAreEmpty() {
    final Map<Holding, Map<CreditQualityRatingType, BigDecimal>> map = Map.of(new Holding(null, null, null), Map.of());
    final boolean actual = PortfolioUtils.areAllValuesInMapEmpty(map);

    assertTrue(actual);
  }

  @Test
  void areAllValuesInMapEmpty_checkResultWhenNotAllValuesInMapAreEmpty() {
    final Map<Holding, Map<CreditQualityRatingType, BigDecimal>> map = Map.of(new Holding(null, null, null), Map.of(),
        new Holding(ONE, FinancialInstrumentType.MUTUAL_FUND_CANADA, null), Map.of(AAA,
            ONE));
    final boolean actual = PortfolioUtils.areAllValuesInMapEmpty(map);

    assertFalse(actual);
  }

  @Test
  void areAllValuesZerosInMap_checkResultWhenAllValuesInMapAreZeros() {
    // SETUP
    final Map<Holding, Map<EquityMarketCapitalizationType, BigDecimal>> map = Map.of(new Holding(null, null, null), Map
        .of(EquityMarketCapitalizationType.GIANT, ZERO, EquityMarketCapitalizationType.SMALL, ZERO));
    // ACT
    final boolean actual = PortfolioUtils.areAllValuesZerosInMap(map);

    assertTrue(actual);
  }

  @Test
  void areAllValuesZerosInMap_checkResultWhenNotAllValuesInMapAreZeros() {
    // SETUP
    final Map<Holding, Map<EquityMarketCapitalizationType, BigDecimal>> map = Map.of(new Holding(null, null, null), Map
        .of(EquityMarketCapitalizationType.GIANT, ZERO, EquityMarketCapitalizationType.SMALL, ZERO,
            EquityMarketCapitalizationType.LARGE, ONE));
    // ACT
    final boolean actual = PortfolioUtils.areAllValuesZerosInMap(map);

    assertFalse(actual);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeCash() {
    final String actual = PortfolioUtils.createKey(CashHolding.builder().currency(CAD).holdingType(
        FinancialInstrumentType.CASH).build());

    assertEquals("CASH_CAD", actual);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeUsEtf() {
    final String result = PortfolioUtils.createKey(new Holding(null, FinancialInstrumentType.ETF_US,
        new SecurityIdentifier("TICKER", FiIdentifierType.TICKER)));

    assertEquals(FinancialInstrumentType.ETF_US.name() + "_" + "TICKER", result);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeCanadaEtf() {
    final String result = PortfolioUtils.createKey(new Holding(null, FinancialInstrumentType.ETF_CANADA,
        new SecurityIdentifier("TICKER", FiIdentifierType.TICKER)));

    assertEquals(FinancialInstrumentType.ETF_CANADA.name() + "_" + "TICKER", result);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeUsStock() {
    final EquitySecurityIdentifier securityIdentifier = mock(EquitySecurityIdentifier.class);
    when(securityIdentifier.getId()).thenReturn("TICKER");
    when(securityIdentifier.getExchangeId()).thenReturn("EXCHANGE_CODE");

    final String result = PortfolioUtils.createKey(new Holding(null, FinancialInstrumentType.STOCK_US,
        securityIdentifier));

    assertEquals(FinancialInstrumentType.STOCK_US.name() + "_" + "TICKER" + "_" + "EXCHANGE_CODE", result);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeCadStock() {
    final EquitySecurityIdentifier securityIdentifier = mock(EquitySecurityIdentifier.class);
    when(securityIdentifier.getId()).thenReturn("TICKER");
    when(securityIdentifier.getExchangeId()).thenReturn("EXCHANGE_CODE");

    final String result = PortfolioUtils.createKey(new Holding(null, FinancialInstrumentType.STOCK_CANADA,
        securityIdentifier));

    assertEquals(FinancialInstrumentType.STOCK_CANADA.name() + "_" + "TICKER" + "_" + "EXCHANGE_CODE", result);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeMutualFund() {
    final String result = PortfolioUtils.createKey(new Holding(null, FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("FUND_SERVE_CODE", FiIdentifierType.FUNDSERV)));

    assertEquals(FinancialInstrumentType.MUTUAL_FUND_CANADA.name() + "_" + "FUND_SERVE_CODE", result);
  }

}
