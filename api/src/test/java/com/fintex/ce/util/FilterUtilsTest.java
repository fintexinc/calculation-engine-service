package com.fintex.ce.util;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FilterUtilsTest {

  @Test
  void filterHoldings_cashCheckResult() {
    // SETUP
    final PortfolioHolding h1 = mock(PortfolioHolding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.CASH);

    final PortfolioHolding h2 = mock(PortfolioHolding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.ETF);

    final PortfolioHolding h3 = mock(PortfolioHolding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.ETF);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final List<PortfolioHolding> holdings = List.of(h1, h2, h3);

    // ACT
    final List<PortfolioHolding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.CASH_PREDICATE);

    // VERIFY
    assertEquals(List.of(h1), actual);
  }

  @Test
  void filterHoldings_canadaETFCheckResult() {
    // SETUP
    final PortfolioHolding h1 = mock(PortfolioHolding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.ETF);
    when(h1.getCountry()).thenReturn(Country.CANADA);

    final PortfolioHolding h2 = mock(PortfolioHolding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.CASH);

    final PortfolioHolding h3 = mock(PortfolioHolding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK);
    when(h3.getCountry()).thenReturn(Country.USA);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final List<PortfolioHolding> holdings = List.of(h1, h2, h3);

    // ACT
    final List<PortfolioHolding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.CANADA_ETF_PREDICATE);

    // VERIFY
    assertEquals(List.of(h1), actual);
  }

  @Test
  void filterHoldings_usETFCheckResult() {
    // SETUP
    final PortfolioHolding h1 = mock(PortfolioHolding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.ETF);
    when(h1.getCountry()).thenReturn(Country.USA);

    final PortfolioHolding h2 = mock(PortfolioHolding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.ETF);
    when(h2.getCountry()).thenReturn(Country.CANADA);

    final PortfolioHolding h3 = mock(PortfolioHolding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.ETF);
    when(h3.getCountry()).thenReturn(Country.CANADA);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final List<PortfolioHolding> holdings = List.of(h1, h2, h3);

    // ACT
    final List<PortfolioHolding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.US_ETF_PREDICATE);

    // VERIFY
    assertEquals(List.of(h1), actual);
  }

  @Test
  void filterHoldings_stockCheckResult() {
    // SETUP
    final PortfolioHolding h1 = mock(PortfolioHolding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.ETF);

    final PortfolioHolding h2 = mock(PortfolioHolding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK);

    final PortfolioHolding h3 = mock(PortfolioHolding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final List<PortfolioHolding> holdings = List.of(h1, h2, h3);

    // ACT
    final List<PortfolioHolding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.STOCK_PREDICATE);

    // VERIFY
    assertEquals(List.of(h2, h3), actual);
  }

  @Test
  void stockPredicate_matchesGenericStockType() {
    PortfolioHolding genericStock = mock(PortfolioHolding.class);
    when(genericStock.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK);

    PortfolioHolding etf = mock(PortfolioHolding.class);
    when(etf.getHoldingType()).thenReturn(FinancialInstrumentType.ETF);

    List<PortfolioHolding> result = FilterUtils.filterHoldings(List.of(genericStock, etf),
        FilterUtils.STOCK_PREDICATE);

    assertEquals(List.of(genericStock), result);
  }

  @Test
  void etfPredicate_matchesGenericEtfType() {
    PortfolioHolding genericEtf = mock(PortfolioHolding.class);
    when(genericEtf.getHoldingType()).thenReturn(FinancialInstrumentType.ETF);

    PortfolioHolding usEtf = mock(PortfolioHolding.class);
    when(usEtf.getHoldingType()).thenReturn(FinancialInstrumentType.ETF);

    PortfolioHolding stock = mock(PortfolioHolding.class);
    when(stock.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK);

    List<PortfolioHolding> result = FilterUtils.filterHoldings(List.of(genericEtf, usEtf, stock),
        FilterUtils.ETF_PREDICATE);

    assertEquals(List.of(genericEtf, usEtf), result);
  }

  @Test
  void gicPredicate_matchesGenericAndCountrySpecific() {
    PortfolioHolding genericGic = mock(PortfolioHolding.class);
    when(genericGic.getHoldingType()).thenReturn(FinancialInstrumentType.GIC);

    PortfolioHolding canadaGic = mock(PortfolioHolding.class);
    when(canadaGic.getHoldingType()).thenReturn(FinancialInstrumentType.GIC);
    when(canadaGic.getCountry()).thenReturn(Country.CANADA);

    PortfolioHolding cash = mock(PortfolioHolding.class);
    when(cash.getHoldingType()).thenReturn(FinancialInstrumentType.CASH);

    List<PortfolioHolding> result = FilterUtils.filterHoldings(List.of(genericGic, canadaGic, cash),
        FilterUtils.GIC_PREDICATE);

    assertEquals(List.of(genericGic, canadaGic), result);
  }

  @Test
  void isOfType_walksParentChain() {
    Assertions.assertTrue(FilterUtils.isOfType(FinancialInstrumentType.STOCK, FinancialInstrumentType.STOCK));
    Assertions.assertTrue(FilterUtils.isOfType(FinancialInstrumentType.MUTUAL_FUND,
        FinancialInstrumentType.FUND));
    Assertions.assertFalse(FilterUtils.isOfType(FinancialInstrumentType.ETF, FinancialInstrumentType.STOCK));
    Assertions.assertFalse(FilterUtils.isOfType(null, FinancialInstrumentType.STOCK));
  }

  @Test
  void filterHoldings_canadaMutualFundCheckResult() {
    // SETUP
    final PortfolioHolding h1 = mock(PortfolioHolding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.ETF);

    final PortfolioHolding h2 = mock(PortfolioHolding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK);

    final PortfolioHolding h3 = mock(PortfolioHolding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND);
    when(h3.getCountry()).thenReturn(Country.CANADA);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final List<PortfolioHolding> holdings = List.of(h1, h2, h3);

    // ACT
    final List<PortfolioHolding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.CANADA_MUTUAL_PREDICATE);

    // VERIFY
    assertEquals(List.of(h3), actual);
  }

  @Test
  void filterHoldings_usStockFundCheckResult() {
    // SETUP
    final PortfolioHolding h1 = mock(PortfolioHolding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.ETF);

    final PortfolioHolding h2 = mock(PortfolioHolding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK);
    when(h2.getCountry()).thenReturn(Country.USA);

    final PortfolioHolding h3 = mock(PortfolioHolding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final List<PortfolioHolding> holdings = List.of(h1, h2, h3);

    // ACT
    final List<PortfolioHolding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.US_STOCKS_PREDICATE);

    // VERIFY
    assertEquals(List.of(h2), actual);
  }

  @Test
  void filterHoldings_fixedIncomeCheckResult() {
    // SETUP
    final PortfolioHolding h1 = mock(PortfolioHolding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.ETF);

    final PortfolioHolding h2 = mock(PortfolioHolding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK);

    final PortfolioHolding h3 = mock(PortfolioHolding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final PortfolioHolding h4 = mock(PortfolioHolding.class);
    when(h4.getHoldingType()).thenReturn(FinancialInstrumentType.FIXED_INCOME);

    final List<PortfolioHolding> holdings = List.of(h1, h2, h3, h4);

    // ACT
    final List<PortfolioHolding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.FIXED_INCOME_PREDICATE);

    // VERIFY
    assertEquals(List.of(h4), actual);
  }

  @Test
  void filterHoldings_separatelyManagedAccountCheckResult() {
    // SETUP
    final PortfolioHolding h1 = mock(PortfolioHolding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.ETF);

    final PortfolioHolding h2 = mock(PortfolioHolding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK);

    final PortfolioHolding h3 = mock(PortfolioHolding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final PortfolioHolding h4 = mock(PortfolioHolding.class);
    when(h4.getHoldingType()).thenReturn(FinancialInstrumentType.FIXED_INCOME);

    final PortfolioHolding h5 = mock(PortfolioHolding.class);
    when(h5.getHoldingType()).thenReturn(FinancialInstrumentType.SEPARATELY_MANAGED_ACCOUNT);

    final List<PortfolioHolding> holdings = List.of(h1, h2, h3, h4, h5);

    // ACT
    final List<PortfolioHolding> actual = FilterUtils.filterHoldings(holdings,
        FilterUtils.SEPARATELY_MANAGED_ACCOUNT_PREDICATE);

    // VERIFY
    assertEquals(List.of(h5), actual);
  }

  @Test
  void filterHoldings_canadaStockFundCheckResult() {
    // SETUP
    final PortfolioHolding h1 = mock(PortfolioHolding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.ETF);

    final PortfolioHolding h2 = mock(PortfolioHolding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK);

    final PortfolioHolding h3 = mock(PortfolioHolding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final PortfolioHolding h4 = mock(PortfolioHolding.class);
    when(h4.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK);
    when(h4.getCountry()).thenReturn(Country.CANADA);
    when(h4.getValue()).thenReturn(BigDecimal.TEN);

    final List<PortfolioHolding> holdings = List.of(h1, h2, h3, h4);

    // ACT
    final List<PortfolioHolding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.CANADA_STOCKS_PREDICATE);

    // VERIFY
    assertEquals(List.of(h4), actual);
  }

  @Test
  void filterHoldings_canadaEtfCheckResult() {
    // SETUP
    final PortfolioHolding h1 = mock(PortfolioHolding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.ETF);

    final PortfolioHolding h2 = mock(PortfolioHolding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.ETF);

    final PortfolioHolding h3 = mock(PortfolioHolding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final PortfolioHolding h4 = mock(PortfolioHolding.class);
    when(h4.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK);
    when(h4.getValue()).thenReturn(BigDecimal.TEN);

    final List<PortfolioHolding> holdings = List.of(h1, h2, h3, h4);

    // ACT
    final List<PortfolioHolding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.ETF_PREDICATE);

    // VERIFY
    assertEquals(List.of(h1, h2), actual);
  }

  @Test
  void filterHoldings_benchmarksCheckResult() {
    // SETUP
    final PortfolioHolding h1 = mock(PortfolioHolding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.ETF);

    final PortfolioHolding h2 = mock(PortfolioHolding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.ETF);

    final PortfolioHolding h3 = mock(PortfolioHolding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final PortfolioHolding h4 = mock(PortfolioHolding.class);
    when(h4.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK);
    when(h4.getValue()).thenReturn(BigDecimal.TEN);

    final PortfolioHolding h5 = mock(PortfolioHolding.class);
    when(h5.getHoldingType()).thenReturn(FinancialInstrumentType.BENCHMARK_INDEX);
    when(h5.getValue()).thenReturn(BigDecimal.TEN);

    final List<PortfolioHolding> holdings = List.of(h1, h2, h3, h4, h5);

    // ACT
    final List<PortfolioHolding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.BENCHMARKS_PREDICATE);

    // VERIFY
    assertEquals(List.of(h5), actual);
  }

  // TODO: PAG_GUIDED_PORTFOLIO is not present in FinancialInstrumentType. Re-enable when business requirements are
  // clarified.
  // @Test
  // void filterHoldings_pagGuidedPortfolioCheckResult() {
  // // PAG_GUIDED_PORTFOLIO not in FinancialInstrumentType - test disabled
  // }

  @Test
  void getSpecifiedIfEmpty_checkResultWhenDataProvidersIsEmpty() {
    // SETUP
    final List<DataProvider> dataProviders = List.of();
    final var expected = List.of(DataProvider.MORNINGSTAR);

    // ACT
    final var actual = FilterUtils.getSpecifiedIfEmpty(dataProviders, DataProvider.MORNINGSTAR);

    // VERIFY
    Assertions.assertNotNull(actual);
    ComparisonUtils.compareCollections(expected, actual);
  }

  @Test
  void getSpecifiedIfEmpty_checkResultWhenDataProvidersIsNotEmpty() {
    // SETUP
    final var dataProviders = List.of(DataProvider.MORNINGSTAR);

    // ACT
    final var actual = FilterUtils.getSpecifiedIfEmpty(dataProviders);

    // VERIFY
    Assertions.assertNotNull(actual);
    ComparisonUtils.compareCollections(dataProviders, actual);
  }

  @Test
  void getSpecifiedIfEmpty_checkResultWhenDataProvidersIsNull() {
    // SETUP
    final List<DataProvider> dataProviders = null;
    final var expected = List.of(DataProvider.MORNINGSTAR);

    // ACT
    final var actual = FilterUtils.getSpecifiedIfEmpty(dataProviders, DataProvider.MORNINGSTAR);

    // VERIFY
    Assertions.assertNotNull(actual);
    ComparisonUtils.compareCollections(expected, actual);
  }

}