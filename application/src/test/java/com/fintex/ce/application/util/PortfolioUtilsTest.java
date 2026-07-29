package com.fintex.ce.application.util;

import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.allocation.EquityMarketCapitalizationType;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.EquitySecurityIdentifier;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.rating.CreditQualityRatingType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static com.fintex.wm.commons.domain.currency.Currency.CAD;
import static com.fintex.wm.commons.domain.rating.CreditQualityRatingType.AAA;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PortfolioUtilsTest {

  @Test
  void calculateInitialPortfolioWeight_test() {
    final PortfolioHolding h1 = new PortfolioHolding(TEN, FinancialInstrumentType.ETF, Country.USA, null);
    final PortfolioHolding h2 = new PortfolioHolding(TEN, null, null);
    final Set<PortfolioHolding> holdings = Set.of(h1, h2);

    final Map<PortfolioHolding, BigDecimal> actual = PortfolioUtils.calculateInitialPortfolioWeight(holdings);

    Assertions.assertNotNull(actual);
    ComparisonUtils.compareMaps(Map.of(h1, new BigDecimal("0.500000000000000"), h2, new BigDecimal(
        "0.500000000000000")), actual);
  }

  @Test
  void areAllValuesInMapEmpty_checkResultWhenAllValuesInMapAreEmpty() {
    final Map<PortfolioHolding, Map<CreditQualityRatingType, BigDecimal>> map = Map.of(new PortfolioHolding(null, null,
        null), Map.of());
    final boolean actual = PortfolioUtils.areAllValuesInMapEmpty(map);

    assertTrue(actual);
  }

  @Test
  void areAllValuesInMapEmpty_checkResultWhenNotAllValuesInMapAreEmpty() {
    final Map<PortfolioHolding, Map<CreditQualityRatingType, BigDecimal>> map = Map.of(new PortfolioHolding(null, null,
        null), Map.of(),
        new PortfolioHolding(ONE, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, null), Map.of(AAA,
            ONE));
    final boolean actual = PortfolioUtils.areAllValuesInMapEmpty(map);

    assertFalse(actual);
  }

  @Test
  void areAllValuesZerosInMap_checkResultWhenAllValuesInMapAreZeros() {
    final Map<PortfolioHolding, Map<EquityMarketCapitalizationType, BigDecimal>> map = Map.of(new PortfolioHolding(null,
        null, null), Map
            .of(EquityMarketCapitalizationType.GIANT, ZERO, EquityMarketCapitalizationType.SMALL, ZERO));
    final boolean actual = PortfolioUtils.areAllValuesZerosInMap(map);

    assertTrue(actual);
  }

  @Test
  void areAllValuesZerosInMap_checkResultWhenNotAllValuesInMapAreZeros() {
    final Map<PortfolioHolding, Map<EquityMarketCapitalizationType, BigDecimal>> map = Map.of(new PortfolioHolding(null,
        null, null), Map
            .of(EquityMarketCapitalizationType.GIANT, ZERO, EquityMarketCapitalizationType.SMALL, ZERO,
                EquityMarketCapitalizationType.LARGE, ONE));
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
    final String result = PortfolioUtils.createKey(new PortfolioHolding(null, FinancialInstrumentType.ETF, Country.USA,
        new SecurityIdentifier("TICKER", FiIdentifierType.TICKER)));

    assertEquals(FinancialInstrumentType.ETF.name() + "_" + "TICKER", result);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeCanadaEtf() {
    final String result = PortfolioUtils.createKey(new PortfolioHolding(null, FinancialInstrumentType.ETF,
        Country.CANADA,
        new SecurityIdentifier("TICKER", FiIdentifierType.TICKER)));

    assertEquals(FinancialInstrumentType.ETF.name() + "_" + "TICKER", result);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeUsStock() {
    final EquitySecurityIdentifier securityIdentifier = mock(EquitySecurityIdentifier.class);
    when(securityIdentifier.getId()).thenReturn("TICKER");
    when(securityIdentifier.getExchangeId()).thenReturn("EXCHANGE_CODE");

    final String result = PortfolioUtils.createKey(new PortfolioHolding(null, FinancialInstrumentType.STOCK,
        Country.USA,
        securityIdentifier));

    assertEquals(FinancialInstrumentType.STOCK.name() + "_" + "TICKER" + "_" + "EXCHANGE_CODE", result);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeCadStock() {
    final EquitySecurityIdentifier securityIdentifier = mock(EquitySecurityIdentifier.class);
    when(securityIdentifier.getId()).thenReturn("TICKER");
    when(securityIdentifier.getExchangeId()).thenReturn("EXCHANGE_CODE");

    final String result = PortfolioUtils.createKey(new PortfolioHolding(null, FinancialInstrumentType.STOCK,
        Country.CANADA,
        securityIdentifier));

    assertEquals(FinancialInstrumentType.STOCK.name() + "_" + "TICKER" + "_" + "EXCHANGE_CODE", result);
  }

  @Test
  void createKey_checkResultWhenHoldingTypeMutualFund() {
    final String result = PortfolioUtils.createKey(new PortfolioHolding(null,
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA,
        new SecurityIdentifier("FUND_SERVE_CODE", FiIdentifierType.FUNDSERV)));

    assertEquals(FinancialInstrumentType.MUTUAL_FUND.name() + "_" + "FUND_SERVE_CODE", result);
  }

}
