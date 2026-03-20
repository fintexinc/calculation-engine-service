package com.fintex.ce.util;

import com.fintex.ce.domain.model.enumeration.DataProvider;
import com.fintex.ce.domain.model.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.Holding;
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
    when(h1.getType()).thenReturn(HoldingType.CASH);

    final Holding h2 = mock(Holding.class);
    when(h2.getType()).thenReturn(HoldingType.CANADA_ETF);

    final Holding h3 = mock(Holding.class);
    when(h3.getType()).thenReturn(HoldingType.CANADA_ETF);
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
    when(h1.getType()).thenReturn(HoldingType.CANADA_ETF);

    final Holding h2 = mock(Holding.class);
    when(h2.getType()).thenReturn(HoldingType.CASH);

    final Holding h3 = mock(Holding.class);
    when(h3.getType()).thenReturn(HoldingType.US_STOCKS);
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
    when(h1.getType()).thenReturn(HoldingType.US_ETF);

    final Holding h2 = mock(Holding.class);
    when(h2.getType()).thenReturn(HoldingType.CANADA_ETF);

    final Holding h3 = mock(Holding.class);
    when(h3.getType()).thenReturn(HoldingType.CANADA_ETF);
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
    when(h1.getType()).thenReturn(HoldingType.US_ETF);

    final Holding h2 = mock(Holding.class);
    when(h2.getType()).thenReturn(HoldingType.US_STOCKS);

    final Holding h3 = mock(Holding.class);
    when(h3.getType()).thenReturn(HoldingType.CANADA_STOCKS);
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
    when(h1.getType()).thenReturn(HoldingType.US_ETF);

    final Holding h2 = mock(Holding.class);
    when(h2.getType()).thenReturn(HoldingType.US_STOCKS);

    final Holding h3 = mock(Holding.class);
    when(h3.getType()).thenReturn(HoldingType.CANADA_MUTUAL_FUNDS);
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
    when(h1.getType()).thenReturn(HoldingType.US_ETF);

    final Holding h2 = mock(Holding.class);
    when(h2.getType()).thenReturn(HoldingType.US_STOCKS);

    final Holding h3 = mock(Holding.class);
    when(h3.getType()).thenReturn(HoldingType.CANADA_MUTUAL_FUNDS);
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
    when(h1.getType()).thenReturn(HoldingType.US_ETF);

    final Holding h2 = mock(Holding.class);
    when(h2.getType()).thenReturn(HoldingType.US_STOCKS);

    final Holding h3 = mock(Holding.class);
    when(h3.getType()).thenReturn(HoldingType.CANADA_MUTUAL_FUNDS);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final Holding h4 = mock(Holding.class);
    when(h4.getType()).thenReturn(HoldingType.FIXED_INCOME);

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
    when(h1.getType()).thenReturn(HoldingType.US_ETF);

    final Holding h2 = mock(Holding.class);
    when(h2.getType()).thenReturn(HoldingType.US_STOCKS);

    final Holding h3 = mock(Holding.class);
    when(h3.getType()).thenReturn(HoldingType.CANADA_MUTUAL_FUNDS);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final Holding h4 = mock(Holding.class);
    when(h4.getType()).thenReturn(HoldingType.FIXED_INCOME);

    final Holding h5 = mock(Holding.class);
    when(h5.getType()).thenReturn(HoldingType.SEPARATELY_MANAGED_ACCOUNT);

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
    when(h1.getType()).thenReturn(HoldingType.US_ETF);

    final Holding h2 = mock(Holding.class);
    when(h2.getType()).thenReturn(HoldingType.US_STOCKS);

    final Holding h3 = mock(Holding.class);
    when(h3.getType()).thenReturn(HoldingType.CANADA_MUTUAL_FUNDS);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final Holding h4 = mock(Holding.class);
    when(h4.getType()).thenReturn(HoldingType.CANADA_STOCKS);
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
    when(h1.getType()).thenReturn(HoldingType.US_ETF);

    final Holding h2 = mock(Holding.class);
    when(h2.getType()).thenReturn(HoldingType.CANADA_ETF);

    final Holding h3 = mock(Holding.class);
    when(h3.getType()).thenReturn(HoldingType.CANADA_MUTUAL_FUNDS);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final Holding h4 = mock(Holding.class);
    when(h4.getType()).thenReturn(HoldingType.CANADA_STOCKS);
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
    when(h1.getType()).thenReturn(HoldingType.US_ETF);

    final Holding h2 = mock(Holding.class);
    when(h2.getType()).thenReturn(HoldingType.CANADA_ETF);

    final Holding h3 = mock(Holding.class);
    when(h3.getType()).thenReturn(HoldingType.CANADA_MUTUAL_FUNDS);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final Holding h4 = mock(Holding.class);
    when(h4.getType()).thenReturn(HoldingType.CANADA_STOCKS);
    when(h4.getValue()).thenReturn(BigDecimal.TEN);

    final Holding h5 = mock(Holding.class);
    when(h5.getType()).thenReturn(HoldingType.BENCHMARK_INDEX);
    when(h5.getValue()).thenReturn(BigDecimal.TEN);

    final List<Holding> holdings = List.of(h1, h2, h3, h4, h5);

    // ACT
    final List<Holding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.BENCHMARKS_PREDICATE);

    // VERIFY
    assertEquals(List.of(h5), actual);
  }

  @Test
  void filterHoldings_pagGuidedPortfolioCheckResult() {
    // SETUP
    final Holding h1 = mock(Holding.class);
    when(h1.getType()).thenReturn(HoldingType.PAG_GUIDED_PORTFOLIO);

    final Holding h2 = mock(Holding.class);
    when(h2.getType()).thenReturn(HoldingType.SEPARATELY_MANAGED_ACCOUNT);

    final Holding h3 = mock(Holding.class);
    when(h3.getType()).thenReturn(HoldingType.PAG_GUIDED_PORTFOLIO);
    when(h3.getValue()).thenReturn(BigDecimal.ONE);

    final Holding h4 = mock(Holding.class);
    when(h4.getType()).thenReturn(HoldingType.CANADA_STOCKS);
    when(h4.getValue()).thenReturn(BigDecimal.TEN);

    final Holding h5 = mock(Holding.class);
    when(h4.getType()).thenReturn(HoldingType.SEPARATELY_MANAGED_ACCOUNT);
    when(h4.getValue()).thenReturn(BigDecimal.TEN);

    final List<Holding> holdings = List.of(h1, h2, h3, h4, h5);

    // ACT
    final List<Holding> actual = FilterUtils.filterHoldings(holdings, FilterUtils.PAG_GUIDED_PORTFOLIO_PREDICATE);

    // VERIFY
    assertEquals(List.of(h1, h3), actual);
  }

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
    final var dataProviders = List.of(DataProvider.EAGLE, DataProvider.MORNINGSTAR);

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