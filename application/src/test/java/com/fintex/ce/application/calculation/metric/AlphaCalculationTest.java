package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.AlphaResult;
import com.fintex.ce.model.util.BigDecimalConstants;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlphaCalculationTest {

  final int TWELVE = 12;

  @Test
  void shouldDelegateToFormTimeIntervalResult_whenDefiningResponseType() {
    final AlphaCalculation alpha = mock(AlphaCalculation.class);

    final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05",
        BigDecimal.ONE));

    final Set<TimeIntervalResult> timeIntervals = Set.of(new TimeIntervalResult("2000-01-12", ONE));
    when(alpha.formTimeIntervalResult(anySet())).thenReturn(timeIntervals);

    doCallRealMethod().when(alpha).defineResponseType(anySet());
    alpha.defineResponseType(pairs);

    verify(alpha).formTimeIntervalResult(pairs);
  }

  @Test
  void shouldReturnAlphaResultWithMappedIntervals_whenDefiningResponseType() {
    final AlphaCalculation alpha = mock(AlphaCalculation.class);

    final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2020-01-05", BigDecimal.ONE), Pair.of("2000-01-12",
        ZERO));

    final Set<TimeIntervalResult> expected = Set.of(
        new TimeIntervalResult("2000-01-12", ZERO),
        new TimeIntervalResult("2020-01-05", BigDecimal.ONE));
    when(alpha.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(alpha).defineResponseType(anySet());
    final AlphaResult actual = alpha.defineResponseType(pairs);

    assertEquals(expected, actual.getAlpha());
  }

  @Test
  void shouldResolvePeriodStartDate_whenCalculatingPeriodForNumberOfMonths() {
    final AlphaCalculation alpha = mock(AlphaCalculation.class);

    final TreeMap treeMap = mock(TreeMap.class);

    when(alpha.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(alpha.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(treeMap.size()).thenReturn(TWELVE);
    when(alpha.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);

    doCallRealMethod().when(alpha).calculatePeriodForNumberOfMonths(anyInt());
    alpha.calculatePeriodForNumberOfMonths(TWELVE);

    verify(alpha).getPeriodStartDate(12, treeMap);
  }

  @SuppressWarnings("unchecked")
  @Test
  void shouldBuildSubMapsFromResolvedStartDate_whenCalculatingPeriodForNumberOfMonths() {
    final AlphaCalculation alpha = mock(AlphaCalculation.class);

    final TreeMap<LocalDate, BigDecimal> portfolioBenchmarkPortfolioReturns = mock(TreeMap.class);
    when(alpha.getPortfolioTotalReturns()).thenReturn(portfolioBenchmarkPortfolioReturns);
    when(alpha.getBenchmarkTotalReturns()).thenReturn(portfolioBenchmarkPortfolioReturns);
    when(portfolioBenchmarkPortfolioReturns.size()).thenReturn(100);

    final TreeMap<LocalDate, BigDecimal> portfolioBenchmarkExcessReturns = mock(TreeMap.class);
    when(alpha.getPortfolioExcessReturn()).thenReturn(portfolioBenchmarkExcessReturns);
    when(alpha.getBenchmarkExcessReturn()).thenReturn(portfolioBenchmarkExcessReturns);
    when(portfolioBenchmarkExcessReturns.size()).thenReturn(100);

    when(alpha.getSubMapByPeriodStartDate(any(), any())).thenReturn(portfolioBenchmarkExcessReturns);

    final LocalDate periodStartDate = LocalDate.now();
    when(alpha.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);

    when(alpha.getSubMapByPeriodStartDate(any(), any())).thenReturn(portfolioBenchmarkExcessReturns);

    doCallRealMethod().when(alpha).calculatePeriodForNumberOfMonths(anyInt());
    alpha.calculatePeriodForNumberOfMonths(24);

    verify(alpha, times(2)).getSubMapByPeriodStartDate(periodStartDate, portfolioBenchmarkExcessReturns);
  }

  @Test
  void shouldCallCalculateAlpha_whenCalculatingPeriodForNumberOfMonths() {
    final AlphaCalculation alpha = mock(AlphaCalculation.class);

    final TreeMap treeMap = mock(TreeMap.class);

    when(alpha.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(alpha.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(alpha.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    doCallRealMethod().when(alpha).calculatePeriodForNumberOfMonths(anyInt());
    alpha.calculatePeriodForNumberOfMonths(TWELVE);

    verify(alpha).calculateAlpha(eq(TWELVE), any(), any());
  }

  @Test
  void shouldReturnCalculatedAlpha_whenPeriodAndDataSizeAreValid() {
    final AlphaCalculation alpha = mock(AlphaCalculation.class);
    final TreeMap treeMap = mock(TreeMap.class);
    when(alpha.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(alpha.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    when(alpha.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(alpha.calculateAlpha(anyInt(), any(), any())).thenReturn(TEN);

    doCallRealMethod().when(alpha).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal result = alpha.calculatePeriodForNumberOfMonths(TWELVE);

    assertEquals(TEN, result);
  }

  @Test
  void shouldReturnNull_whenBenchmarkSizeIsLessThanRequestedPeriod() {
    final AlphaCalculation alpha = mock(AlphaCalculation.class);
    final TreeMap treeMap = mock(TreeMap.class);

    when(alpha.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(alpha.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    when(alpha.calculateAlpha(anyInt(), any(), any())).thenReturn(BigDecimal.TEN);

    doCallRealMethod().when(alpha).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal result = alpha.calculatePeriodForNumberOfMonths(24);

    assertNull(result);
  }

  @Test
  void shouldReturnNull_whenRequestedPeriodIsLessThanTwelveMonths() {
    final AlphaCalculation alpha = mock(AlphaCalculation.class);

    final TreeMap treeMap = mock(TreeMap.class);

    when(alpha.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(alpha.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(alpha.calculateAlpha(anyInt(), any(), any())).thenReturn(BigDecimal.TEN);
    when(treeMap.size()).thenReturn(TWELVE);

    doCallRealMethod().when(alpha).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal result = alpha.calculatePeriodForNumberOfMonths(6);

    assertNull(result);
  }

  @Test
  void shouldCallCalculateBeta_whenCalculatingAlpha() {
    final AlphaCalculation alpha = mock(AlphaCalculation.class);

    when(alpha.calculateBeta(anyInt())).thenReturn(TEN);

    doCallRealMethod().when(alpha).calculateAlpha(anyInt(), any(), any());
    alpha.calculateAlpha(10, TEN, BigDecimalConstants.TWELVE);

    verify(alpha).calculateBeta(10);
  }

  @Test
  void shouldReturnExpectedAlphaValue_whenCalculatingAlphaWithBetaTen() {
    final AlphaCalculation alpha = mock(AlphaCalculation.class);

    when(alpha.calculateBeta(anyInt())).thenReturn(TEN);

    doCallRealMethod().when(alpha).calculateAlpha(anyInt(), any(), any());
    final BigDecimal actual = alpha.calculateAlpha(10, TEN, BigDecimalConstants.TWELVE);

    assertEquals(BigDecimal.valueOf(-1320), actual);
  }

  @Test
  void shouldReturnNull_whenBetaIsNull() {
    final AlphaCalculation alpha = mock(AlphaCalculation.class);

    when(alpha.calculateBeta(anyInt())).thenReturn(null);

    doCallRealMethod().when(alpha).calculateAlpha(anyInt(), any(), any());
    final BigDecimal actual = alpha.calculateAlpha(10, TEN, BigDecimalConstants.TWELVE);

    assertNull(actual);
  }

  @Test
  void shouldReturnExpectedAlphaValue_whenCalculatingAlphaWithBetaOne() {
    final AlphaCalculation alpha = mock(AlphaCalculation.class);

    when(alpha.calculateBeta(anyInt())).thenReturn(ONE);

    doCallRealMethod().when(alpha).calculateAlpha(anyInt(), any(), any());
    final BigDecimal actual = alpha.calculateAlpha(10, HUNDRED, BigDecimalConstants.TWELVE);

    assertEquals(BigDecimal.valueOf(1056), actual);
  }

}
