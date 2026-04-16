package com.fintex.ce.application.calculation.metric.core;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.fintex.ce.application.util.TestConstants.LOCAL_DATE_NOW;
import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static com.fintex.ce.util.DecimalUtils.pow;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpDownSideCalculationAbstractTest {

  @Test
  void shouldReturnPortfolioDetermination_whenCaptureConditionIsMet() {
    final var sut = mock(UpDownSideCalculationAbstract.class);

    final var benchmark = Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE.subtract(TEN));
    when(sut.getBenchmarkTotalReturns()).thenReturn(new TreeMap<>(benchmark));

    final var portfolio = Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE.subtract(TEN));
    when(sut.getPortfolioTotalReturns()).thenReturn(new TreeMap<>(portfolio));

    when(sut.filterCaptureExpression(any())).thenReturn(true);
    doCallRealMethod().when(sut).getPortfolioDetermination();
    final TreeMap<LocalDate, BigDecimal> actual = sut.getPortfolioDetermination();

    assertEquals(Map.of(LOCAL_DATE_NOW.minusMonths(1), new BigDecimal("0.910000000000000")), actual);
  }

  @Test
  void shouldReturnBenchmarkDeterminationForPortfolioDates_whenBenchmarkDataExists() {
    final var sut = mock(UpDownSideCalculationAbstract.class);

    final var benchmark = Map.of(
        LOCAL_DATE_NOW.minusMonths(1), ONE.subtract(TEN),
        LOCAL_DATE_NOW, ZERO, LOCAL_DATE_NOW.plusMonths(1), ONE);
    when(sut.getBenchmarkTotalReturns()).thenReturn(new TreeMap<>(benchmark));

    final var portfolio = new TreeMap<>(
        Map.of(LOCAL_DATE_NOW.plusMonths(1), HUNDRED));

    doCallRealMethod().when(sut).getBenchmarkDetermination(any());
    final NavigableMap<LocalDate, BigDecimal> actual = sut.getBenchmarkDetermination(portfolio);

    assertEquals(Map.of(LOCAL_DATE_NOW.plusMonths(1), new BigDecimal("1.010000000000000")), actual);
  }

  @Test
  void shouldReturnCaptureRatio_whenDeviationsAreCalculated() {
    final var sut = mock(UpDownSideCalculationAbstract.class);

    sut.portfolioDetermination = new TreeMap<>(Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE));
    sut.benchmarkDetermination = new TreeMap<>(Map.of(LOCAL_DATE_NOW.minusMonths(2), ONE));

    final var treeMap = mock(TreeMap.class);
    when(treeMap.size()).thenReturn(12);

    when(sut.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(sut.calculateDeviationFor(12, sut.portfolioDetermination)).thenReturn(ONE);
    when(sut.calculateDeviationFor(12, sut.benchmarkDetermination)).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(12);
    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(12);

    assertEquals(0, TEN.compareTo(actual));
  }

  @Test
  void shouldReturnNull_whenPeriodIsLessThanTwelve() {
    final var sut = mock(UpDownSideCalculationAbstract.class);

    sut.portfolioDetermination = new TreeMap<>(Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE));
    sut.benchmarkDetermination = new TreeMap<>(Map.of(LOCAL_DATE_NOW.minusMonths(2), ONE));

    final var treeMap = mock(TreeMap.class);
    final int months = 11;
    when(treeMap.size()).thenReturn(months);

    when(sut.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(sut.calculateDeviationFor(months, sut.portfolioDetermination)).thenReturn(ONE);
    when(sut.calculateDeviationFor(months, sut.benchmarkDetermination)).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(months);
    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(months);

    assertNull(actual);
  }

  @Test
  void shouldReturnNull_whenPeriodExceedsPortfolioSize() {
    final var sut = mock(UpDownSideCalculationAbstract.class);

    final var treeMap = mock(TreeMap.class);
    when(treeMap.size()).thenReturn(1);

    when(sut.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(60);
    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(60);

    assertNull(actual);
  }

  @Test
  void shouldReturnZero_whenBenchmarkDeviationIsZero() {
    final var sut = mock(UpDownSideCalculationAbstract.class);

    sut.portfolioDetermination = new TreeMap<>(Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE));
    sut.benchmarkDetermination = new TreeMap<>(Map.of(LOCAL_DATE_NOW.minusMonths(2), ONE));

    final var treeMap = mock(TreeMap.class);
    final int months = 12;
    when(treeMap.size()).thenReturn(months);

    when(sut.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(sut.calculateDeviationFor(months, sut.portfolioDetermination)).thenReturn(ONE);
    when(sut.calculateDeviationFor(months, sut.benchmarkDetermination)).thenReturn(ZERO);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(months);
    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(months);

    assertEquals(ZERO, actual);
  }

  @Test
  void shouldCalculateDeviation_whenRequiredMonthsExist() {
    final var sut = mock(UpDownSideCalculationAbstract.class);

    final int numberOfMonths = 2;
    final TreeMap<LocalDate, BigDecimal> determination = new TreeMap<>();
    determination.put(toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), TEN);
    determination.put(toLastDayOfMonth(LOCAL_DATE_NOW), ONE);

    when(sut.getPortfolioTotalReturns()).thenReturn(determination);

    doCallRealMethod().when(sut).getBenchmarkValues(numberOfMonths, determination);
    doCallRealMethod().when(sut).filterRequiredMonthsForPeriod(numberOfMonths, determination);

    doCallRealMethod().when(sut).calculateDeviationFor(eq(numberOfMonths), any());
    final BigDecimal actual = sut.calculateDeviationFor(numberOfMonths, determination);

    assertEquals(pow(TEN, BigDecimal.valueOf(0.5)).subtract(ONE), actual);
  }

  @Test
  void shouldReturnZeroDeviation_whenBenchmarkValuesAreMissing() {
    final var sut = mock(UpDownSideCalculationAbstract.class);
    final var periodCalculationAbstract = mock(PeriodCalculationAbstract.class);

    final TreeMap<LocalDate, BigDecimal> determination = new TreeMap<>(
        Map.of(toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), TEN, toLastDayOfMonth(LOCAL_DATE_NOW), ONE));

    when(periodCalculationAbstract.getBenchmarkValues(60, determination)).thenReturn(List.of());

    doCallRealMethod().when(sut).calculateDeviationFor(eq(60), any());
    final BigDecimal actual = sut.calculateDeviationFor(60, determination);

    assertEquals(ZERO, actual);
  }
}
