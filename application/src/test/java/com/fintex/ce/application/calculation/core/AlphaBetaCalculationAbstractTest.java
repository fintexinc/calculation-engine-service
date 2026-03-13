package com.fintex.ce.application.calculation.core;

import com.fintex.ce.application.calculation.core.AlphaBetaCalculationAbstract;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlphaBetaCalculationAbstractTest {

  final int TWELVE = 12;

  @Test
  void shouldReturnNull_whenPeriodExceedsPortfolioExcessReturnsSize() {
    final var sut = mock(AlphaBetaCalculationAbstract.class);
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
    when(sut.calculateBeta(any(), any(), any(), any())).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(100);

    assertNull(actual);
  }

  @Test
  void shouldReturnNull_whenPeriodExceedsBenchmarkExcessReturnsSize() {
    final var sut = mock(AlphaBetaCalculationAbstract.class);
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
    when(sut.calculateBeta(any(), any(), any(), any())).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(100);

    assertNull(actual);
  }

  @Test
  void shouldResolvePeriodStartDate_whenCalculatingPeriod() {
    final var sut = mock(AlphaBetaCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    sut.portfolioExcessReturn = treeMap;
    sut.benchmarkExcessReturn = treeMap;

    when(sut.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(treeMap.size()).thenReturn(TWELVE);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sut).getPeriodStartDate(12, treeMap);
  }

  @Test
  void shouldGetExcessReturnSubMaps_whenCalculatingPeriod() {
    final var sut = mock(AlphaBetaCalculationAbstract.class);
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
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sut, times(2)).getSubMapByPeriodStartDate(periodStartDate, treeMap);
  }

  @Test
  void shouldCalculateBeta_whenCalculatingPeriod() {
    final var sut = mock(AlphaBetaCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    sut.portfolioExcessReturn = treeMap;
    sut.benchmarkExcessReturn = treeMap;

    when(sut.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    doCallRealMethod().when(sut).calculatePeriod(any(), any(), any());
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sut).calculateBeta(eq(treeMap), eq(treeMap), any(), any());
  }

  @Test
  void shouldReturnCalculatedBeta_whenInputDataIsValid() {
    final var sut = mock(AlphaBetaCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    sut.portfolioExcessReturn = treeMap;
    sut.benchmarkExcessReturn = treeMap;

    when(sut.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(sut.calculateBeta(any(), any(), any(), any())).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    doCallRealMethod().when(sut).calculatePeriod(any(), any(), any());
    final BigDecimal result = sut.calculatePeriodForNumberOfMonths(TWELVE);

    assertEquals(TEN, result);
  }

  @Test
  void shouldReturnNull_whenBenchmarkReturnsSizeIsLessThanPeriod() {
    final var sut = mock(AlphaBetaCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);

    when(sut.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    when(sut.calculateBeta(any(), any(), any(), any())).thenReturn(BigDecimal.TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal result = sut.calculatePeriodForNumberOfMonths(24);

    assertNull(result);
  }

  @Test
  void shouldReturnNull_whenPeriodIsLessThanTwelve() {
    final var sut = mock(AlphaBetaCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    sut.portfolioExcessReturn = treeMap;
    sut.benchmarkExcessReturn = treeMap;

    when(sut.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(sut.calculateBeta(any(), any(), any(), any())).thenReturn(BigDecimal.TEN);
    when(treeMap.size()).thenReturn(TWELVE);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal result = sut.calculatePeriodForNumberOfMonths(6);

    assertNull(result);
  }

  @Test
  void shouldUseNumerator_whenCalculatingBeta() {
    final var sut = mock(AlphaBetaCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    final var bigDecimal = mock(BigDecimal.class);

    when(sut.calculateNumerator(any(), any(), any(), any())).thenReturn(ONE);
    when(sut.calculateDenominator(any(), any())).thenReturn(ONE);

    doCallRealMethod().when(sut).calculateBeta(any(), any(), any(), any());
    sut.calculateBeta(treeMap, treeMap, bigDecimal, bigDecimal);

    verify(sut).calculateNumerator(treeMap, treeMap, bigDecimal, bigDecimal);
  }

  @Test
  void shouldUseDenominator_whenCalculatingBeta() {
    final var sut = mock(AlphaBetaCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    final var bigDecimal = mock(BigDecimal.class);

    when(sut.calculateNumerator(any(), any(), any(), any())).thenReturn(ONE);
    when(sut.calculateDenominator(any(), any())).thenReturn(ONE);

    doCallRealMethod().when(sut).calculateBeta(any(), any(), any(), any());
    sut.calculateBeta(treeMap, treeMap, bigDecimal, bigDecimal);

    verify(sut).calculateDenominator(treeMap, bigDecimal);
  }

  @Test
  void shouldReturnBetaValue_whenNumeratorAndDenominatorProvided() {
    final var sut = mock(AlphaBetaCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    final var bigDecimal = mock(BigDecimal.class);

    when(sut.calculateNumerator(any(), any(), any(), any())).thenReturn(BigDecimal.valueOf(1.01094319080371));
    when(sut.calculateDenominator(any(), any())).thenReturn(BigDecimal.valueOf(0.994895485347306));

    doCallRealMethod().when(sut).calculateBeta(any(), any(), any(), any());
    final BigDecimal result = sut.calculateBeta(treeMap, treeMap, bigDecimal, bigDecimal);

    assertEquals(toUserScale(BigDecimal.valueOf(1.01613004148954)), result);
  }

  @Test
  void shouldCalculateNumeratorValue_whenExcessReturnsProvided() {
    final var sut = mock(AlphaBetaCalculationAbstract.class);

    final var portfolioExcessReturnByPeriod = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.valueOf(
        1.01222986673534)));
    final var benchmarkExcessReturnByPeriod = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.valueOf(
        0.994895485347306)));

    final var portfolioExcessAverage = BigDecimal.valueOf(0.004475946208333);
    final var benchmarkExcessAverage = BigDecimal.valueOf(0.007504533222917);

    doCallRealMethod().when(sut).calculateNumerator(any(), any(), any(), any());
    final BigDecimal result = sut.calculateNumerator(portfolioExcessReturnByPeriod, benchmarkExcessReturnByPeriod,
        portfolioExcessAverage, benchmarkExcessAverage);

    assertEquals(toUserScale(BigDecimal.valueOf(0.995047103096247)), toUserScale(result));
  }

  @Test
  void shouldCalculateDenominatorValue_whenExcessReturnsProvided() {
    final var sut = mock(AlphaBetaCalculationAbstract.class);

    final var benchmarkExcessReturnByPeriod = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.valueOf(
        0.994895485347306)));
    final var benchmarkExcessAverage = BigDecimal.valueOf(0.007504533222917);

    doCallRealMethod().when(sut).calculateDenominator(any(), any());
    final BigDecimal result = sut.calculateDenominator(benchmarkExcessReturnByPeriod, benchmarkExcessAverage);

    assertEquals(toUserScale(BigDecimal.valueOf(0.974940892337108)), toUserScale(result));
  }

  @Test
  void shouldOverrideTotalReturnsToMonthlyChange_whenTotalReturnsProvided() {
    final var sut = mock(AlphaBetaCalculationAbstract.class);
    final var date = LocalDate.of(2020, 12, 1);
    final var portfolioTotalReturns = new TreeMap<>(Map.of(toLastDayOfMonth(date), BigDecimal.valueOf(1.01094319080371),
        toLastDayOfMonth(date.minusMonths(1)), BigDecimal.valueOf(1.02297440154456)));

    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);

    doCallRealMethod().when(sut).overrideTotalReturns(any());
    final NavigableMap<LocalDate, BigDecimal> totalReturns = sut.overrideTotalReturns(portfolioTotalReturns);

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
