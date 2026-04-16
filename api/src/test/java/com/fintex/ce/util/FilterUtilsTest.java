package com.fintex.ce.util;

import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.wm.commons.domain.DataProvider;
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
    final Holding h1 = mock(Holding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.CASH);

    final Holding h2 = mock(Holding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.ETF_CANADA);

    final Holding h3 = mock(Holding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.ETF_CANADA);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final List<Holding> holdings = List.of(h1, h2, h3);

    // ACT
    final List<Holding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.CASH_PREDICATE);

    // VERIFY
    assertEquals(List.of(h1), actual);
  }

  @Test
  void filterHoldings_canadaETFCheckResult() {
    // SETUP
    final Holding h1 = mock(Holding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.ETF_CANADA);

    final Holding h2 = mock(Holding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.CASH);

    final Holding h3 = mock(Holding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK_US);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final List<Holding> holdings = List.of(h1, h2, h3);

    // ACT
    final List<Holding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.CANADA_ETF_PREDICATE);

    // VERIFY
    assertEquals(List.of(h1), actual);
  }

  @Test
  void filterHoldings_usETFCheckResult() {
    // SETUP
    final Holding h1 = mock(Holding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.ETF_US);

    final Holding h2 = mock(Holding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.ETF_CANADA);

    final Holding h3 = mock(Holding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.ETF_CANADA);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final List<Holding> holdings = List.of(h1, h2, h3);

    // ACT
    final List<Holding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.US_ETF_PREDICATE);

    // VERIFY
    assertEquals(List.of(h1), actual);
  }

  @Test
  void filterHoldings_stockCheckResult() {
    // SETUP
    final Holding h1 = mock(Holding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.ETF_US);

    final Holding h2 = mock(Holding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK_US);

    final Holding h3 = mock(Holding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK_CANADA);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final List<Holding> holdings = List.of(h1, h2, h3);

    // ACT
    final List<Holding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.STOCK_PREDICATE);

    // VERIFY
    assertEquals(List.of(h2, h3), actual);
  }

  @Test
  void filterHoldings_canadaMutualFundCheckResult() {
    // SETUP
    final Holding h1 = mock(Holding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.ETF_US);

    final Holding h2 = mock(Holding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK_US);

    final Holding h3 = mock(Holding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND_CANADA);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final List<Holding> holdings = List.of(h1, h2, h3);

    // ACT
    final List<Holding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.CANADA_MUTUAL_PREDICATE);

    // VERIFY
    assertEquals(List.of(h3), actual);
  }

  @Test
  void filterHoldings_usStockFundCheckResult() {
    // SETUP
    final Holding h1 = mock(Holding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.ETF_US);

    final Holding h2 = mock(Holding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK_US);

    final Holding h3 = mock(Holding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND_CANADA);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final List<Holding> holdings = List.of(h1, h2, h3);

    // ACT
    final List<Holding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.US_STOCKS_PREDICATE);

    // VERIFY
    assertEquals(List.of(h2), actual);
  }

  @Test
  void filterHoldings_fixedIncomeCheckResult() {
    // SETUP
    final Holding h1 = mock(Holding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.ETF_US);

    final Holding h2 = mock(Holding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK_US);

    final Holding h3 = mock(Holding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND_CANADA);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final Holding h4 = mock(Holding.class);
    when(h4.getHoldingType()).thenReturn(FinancialInstrumentType.FIXED_INCOME);

    final List<Holding> holdings = List.of(h1, h2, h3, h4);

    // ACT
    final List<Holding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.FIXED_INCOME_PREDICATE);

    // VERIFY
    assertEquals(List.of(h4), actual);
  }

  @Test
  void filterHoldings_separatelyManagedAccountCheckResult() {
    // SETUP
    final Holding h1 = mock(Holding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.ETF_US);

    final Holding h2 = mock(Holding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK_US);

    final Holding h3 = mock(Holding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND_CANADA);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final Holding h4 = mock(Holding.class);
    when(h4.getHoldingType()).thenReturn(FinancialInstrumentType.FIXED_INCOME);

    final Holding h5 = mock(Holding.class);
    when(h5.getHoldingType()).thenReturn(FinancialInstrumentType.SEPARATELY_MANAGED_ACCOUNT);

    final List<Holding> holdings = List.of(h1, h2, h3, h4, h5);

    // ACT
    final List<Holding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.SEPARATELY_MANAGED_ACCOUNT_PREDICATE);

    // VERIFY
    assertEquals(List.of(h5), actual);
  }

  @Test
  void filterHoldings_canadaStockFundCheckResult() {
    // SETUP
    final Holding h1 = mock(Holding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.ETF_US);

    final Holding h2 = mock(Holding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK_US);

    final Holding h3 = mock(Holding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND_CANADA);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final Holding h4 = mock(Holding.class);
    when(h4.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK_CANADA);
    when(h4.getValue()).thenReturn(BigDecimal.TEN);

    final List<Holding> holdings = List.of(h1, h2, h3, h4);

    // ACT
    final List<Holding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.CANADA_STOCKS_PREDICATE);

    // VERIFY
    assertEquals(List.of(h4), actual);
  }

  @Test
  void filterHoldings_canadaEtfCheckResult() {
    // SETUP
    final Holding h1 = mock(Holding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.ETF_US);

    final Holding h2 = mock(Holding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.ETF_CANADA);

    final Holding h3 = mock(Holding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND_CANADA);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final Holding h4 = mock(Holding.class);
    when(h4.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK_CANADA);
    when(h4.getValue()).thenReturn(BigDecimal.TEN);

    final List<Holding> holdings = List.of(h1, h2, h3, h4);

    // ACT
    final List<Holding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.ETF_PREDICATE);

    // VERIFY
    assertEquals(List.of(h1, h2), actual);
  }

  @Test
  void filterHoldings_benchmarksCheckResult() {
    // SETUP
    final Holding h1 = mock(Holding.class);
    when(h1.getHoldingType()).thenReturn(FinancialInstrumentType.ETF_US);

    final Holding h2 = mock(Holding.class);
    when(h2.getHoldingType()).thenReturn(FinancialInstrumentType.ETF_CANADA);

    final Holding h3 = mock(Holding.class);
    when(h3.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND_CANADA);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final Holding h4 = mock(Holding.class);
    when(h4.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK_CANADA);
    when(h4.getValue()).thenReturn(BigDecimal.TEN);

    final Holding h5 = mock(Holding.class);
    when(h5.getHoldingType()).thenReturn(FinancialInstrumentType.BENCHMARK_INDEX);
    when(h5.getValue()).thenReturn(BigDecimal.TEN);

    final List<Holding> holdings = List.of(h1, h2, h3, h4, h5);

    // ACT
    final List<Holding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.BENCHMARKS_PREDICATE);

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