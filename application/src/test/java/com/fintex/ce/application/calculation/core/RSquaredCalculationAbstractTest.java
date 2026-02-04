package com.fintex.ce.application.calculation.core;

import com.fintex.ce.application.calculation.core.RSquaredCalculationAbstract;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.fintex.ce.domain.constant.BigDecimalConstants.ONE;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class RSquaredCalculationAbstractTest {

  final int TWELVE = 12;

  @Test
  void calculatePeriodForNumberOfMonths_numberOfMonthGreaterThanPortfolioExcessReturnsResultNull() {
    // SETUP
    final var sut = mock(RSquaredCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    final var portfolioExcessReturns = mock(TreeMap.class);
    final var benchmarkExcessReturns = mock(TreeMap.class);
    sut.portfolioExcessReturn = portfolioExcessReturns;
    sut.benchmarkExcessReturn = benchmarkExcessReturns;

    when(portfolioExcessReturns.size()).thenReturn(20);
    when(benchmarkExcessReturns.size()).thenReturn(100);
    when(sut.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(treeMap.size()).thenReturn(100);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(sut.calculateRSquared(any(), any(), any())).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(100);

    // VERIFY
    assertNull(actual);
  }

  @Test
  void calculatePeriodForNumberOfMonths_numberOfMonthGreaterThanBenchmarkExcessReturnsResultNull() {
    // SETUP
    final var sut = mock(RSquaredCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    final var portfolioExcessReturns = mock(TreeMap.class);
    final var benchmarkExcessReturns = mock(TreeMap.class);
    sut.portfolioExcessReturn = portfolioExcessReturns;
    sut.benchmarkExcessReturn = benchmarkExcessReturns;

    when(portfolioExcessReturns.size()).thenReturn(100);
    when(benchmarkExcessReturns.size()).thenReturn(20);
    when(sut.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(treeMap.size()).thenReturn(100);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(sut.calculateRSquared(any(), any(), any())).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(100);

    // VERIFY
    assertNull(actual);
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyGetPeriodStartDate() {
    // SETUP
    final var sut = mock(RSquaredCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    sut.portfolioExcessReturn = treeMap;
    sut.benchmarkExcessReturn = treeMap;

    when(sut.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(treeMap.size()).thenReturn(TWELVE);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    verify(sut).getPeriodStartDate(12, treeMap);
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyGetSubMapByPeriodStartDate() {
    // SETUP
    final var sut = mock(RSquaredCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    final var periodStartDate = LocalDate.now();

    sut.benchmarkExcessReturn = treeMap;
    sut.portfolioExcessReturn = treeMap;

    when(sut.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    verify(sut, times(2)).getSubMapByPeriodStartDate(periodStartDate, treeMap);
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyCalculateRSquared() {
    // SETUP
    final var sut = mock(RSquaredCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    sut.portfolioExcessReturn = treeMap;
    sut.benchmarkExcessReturn = treeMap;

    when(sut.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    doCallRealMethod().when(sut).calculatePeriod(any(), any(), any());
    // ACT
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    verify(sut).calculateRSquared(eq(treeMap), eq(treeMap), any());
  }

  @Test
  void calculatePeriodForNumberOfMonths_checkResult() {
    // SETUP
    final var sut = mock(RSquaredCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    sut.portfolioExcessReturn = treeMap;
    sut.benchmarkExcessReturn = treeMap;

    when(sut.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(sut.calculateRSquared(any(), any(), any())).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    doCallRealMethod().when(sut).calculatePeriod(any(), any(), any());
    // ACT
    final BigDecimal result = sut.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    assertEquals(TEN, result);
  }

  @Test
  void calculatePeriodForNumberOfMonths_checkResultWhenBenchmarkTotalReturnsSizeLessThenPeriod() {
    // SETUP
    final var sut = mock(RSquaredCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);

    when(sut.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    when(sut.calculateRSquared(any(), any(), any())).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final BigDecimal result = sut.calculatePeriodForNumberOfMonths(24);

    // VERIFY
    assertNull(result);
  }

  @Test
  void calculatePeriodForNumberOfMonths_checkResultWhenPeriodIsLessThanTwelve() {
    // SETUP
    final var sut = mock(RSquaredCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    sut.portfolioExcessReturn = treeMap;
    sut.benchmarkExcessReturn = treeMap;

    when(sut.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(sut.calculateRSquared(any(), any(), any())).thenReturn(TEN);
    when(treeMap.size()).thenReturn(TWELVE);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final BigDecimal result = sut.calculatePeriodForNumberOfMonths(6);

    // VERIFY
    assertNull(result);
  }

  @Test
  void calculateRSquared_verifyCalculateNumerator() {
    // SETUP
    final var sut = mock(RSquaredCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    final var bigDecimal = mock(BigDecimal.class);

    when(sut.calculateSumSquaredRegression(any(), any())).thenReturn(ONE);
    when(sut.calculateTotalSumOfSquares(any(), any())).thenReturn(ONE);

    doCallRealMethod().when(sut).calculateRSquared(any(), any(), any());
    // ACT
    sut.calculateRSquared(treeMap, treeMap, bigDecimal);

    // VERIFY
    verify(sut).calculateSumSquaredRegression(treeMap, treeMap);
  }

  @Test
  void calculateRSquared_verifyCalculateTotalSumOfSquares() {
    // SETUP
    final var sut = mock(RSquaredCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    final var bigDecimal = mock(BigDecimal.class);

    when(sut.calculateSumSquaredRegression(any(), any())).thenReturn(ONE);
    when(sut.calculateTotalSumOfSquares(any(), any())).thenReturn(ONE);

    doCallRealMethod().when(sut).calculateRSquared(any(), any(), any());
    // ACT
    sut.calculateRSquared(treeMap, treeMap, bigDecimal);

    // VERIFY
    verify(sut).calculateTotalSumOfSquares(treeMap, bigDecimal);
  }

  @Test
  void calculateRSquared_checkResult() {
    // SETUP
    final var sut = mock(RSquaredCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    final var bigDecimal = mock(BigDecimal.class);

    when(sut.calculateSumSquaredRegression(any(), any())).thenReturn(BigDecimal.valueOf(1.01094319080371));
    when(sut.calculateTotalSumOfSquares(any(), any())).thenReturn(BigDecimal.valueOf(0.994895485347306));

    doCallRealMethod().when(sut).calculateRSquared(any(), any(), any());
    // ACT
    final BigDecimal result = sut.calculateRSquared(treeMap, treeMap, bigDecimal);

    // VERIFY
    assertEquals(toUserScale(BigDecimal.valueOf(-0.0161300415)), result);
  }

  @Test
  void calculateNumerator_checkResult() {
    // SETUP
    final var sut = mock(RSquaredCalculationAbstract.class);

    final var portfolioExcessReturnByPeriod = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.valueOf(
        1.01222986673534)));
    final var benchmarkExcessReturnByPeriod = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.valueOf(
        0.994895485347306)));

    final var portfolioExcessAverage = BigDecimal.valueOf(0.004475946208333);
    final var benchmarkExcessAverage = BigDecimal.valueOf(0.007504533222917);

    doCallRealMethod().when(sut).calculateSumSquaredRegression(any(), any());
    // ACT
    final BigDecimal result = sut.calculateSumSquaredRegression(portfolioExcessReturnByPeriod,
        benchmarkExcessReturnByPeriod);

    // VERIFY
    assertEquals(toUserScale(BigDecimal.valueOf(0.0003004808)), toUserScale(result));
  }

  @Test
  void calculateTotalSumofSquares_checkResult() {
    // SETUP
    final var sut = mock(RSquaredCalculationAbstract.class);

    final var benchmarkExcessReturnByPeriod = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.valueOf(
        0.994895485347306)));
    final var benchmarkExcessAverage = BigDecimal.valueOf(0.007504533222917);

    doCallRealMethod().when(sut).calculateTotalSumOfSquares(any(), any());
    // ACT
    final BigDecimal result = sut.calculateTotalSumOfSquares(benchmarkExcessReturnByPeriod, benchmarkExcessAverage);

    // VERIFY
    assertEquals(toUserScale(BigDecimal.valueOf(0.974940892337108)), toUserScale(result));
  }

  @Test
  void overrideTotalReturns_checkResult() {
    // SETUP
    final var sut = mock(RSquaredCalculationAbstract.class);
    final var date = LocalDate.of(2020, 12, 1);
    final var portfolioTotalReturns = new TreeMap<>(Map.of(toLastDayOfMonth(date), BigDecimal.valueOf(1.01094319080371),
        toLastDayOfMonth(date.minusMonths(1)), BigDecimal.valueOf(1.02297440154456)));

    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);

    doCallRealMethod().when(sut).overrideTotalReturns(any());
    // ACT
    final NavigableMap<LocalDate, BigDecimal> totalReturns = sut.overrideTotalReturns(portfolioTotalReturns);

    // VERIFY
    assertEquals(2, totalReturns.size());
    assertEquals(toUserScale(BigDecimal.valueOf(0.02297440154456)), toUserScale(totalReturns.firstEntry().getValue()));
    assertEquals(toUserScale(BigDecimal.valueOf(0.01094319080371)), toUserScale(totalReturns.lastEntry().getValue()));
  }

  private TreeMap<LocalDate, BigDecimal> getReturns() {
    final var date = LocalDate.of(2020, 12, 1);
    final Map<LocalDate, BigDecimal> map = new HashMap<>();
    map.put(toLastDayOfMonth(date), new BigDecimal("1.01222986673534"));
    map.put(toLastDayOfMonth(date.minusMonths(12)), new BigDecimal("1.01094319080371"));
    map.put(toLastDayOfMonth(date.minusMonths(11)), new BigDecimal("0.994895485347306"));
    map.put(toLastDayOfMonth(date.minusMonths(10)), new BigDecimal("1.02297440154456"));
    map.put(toLastDayOfMonth(date.minusMonths(9)), new BigDecimal("1.03431353421321"));
    map.put(toLastDayOfMonth(date.minusMonths(8)), new BigDecimal("1.01111160279157"));
    map.put(toLastDayOfMonth(date.minusMonths(7)), new BigDecimal("0.998508625796384"));
    map.put(toLastDayOfMonth(date.minusMonths(6)), new BigDecimal("0.996781991187829"));
    map.put(toLastDayOfMonth(date.minusMonths(5)), new BigDecimal("1.01213800595451"));
    map.put(toLastDayOfMonth(date.minusMonths(4)), new BigDecimal("1.02031184300726"));
    map.put(toLastDayOfMonth(date.minusMonths(3)), new BigDecimal("1.01074832088959"));
    map.put(toLastDayOfMonth(date.minusMonths(2)), new BigDecimal("1.01608812281602"));
    map.put(toLastDayOfMonth(date.minusMonths(1)), new BigDecimal("1.00844777099365"));
    return new TreeMap<>(map);
  }

}
