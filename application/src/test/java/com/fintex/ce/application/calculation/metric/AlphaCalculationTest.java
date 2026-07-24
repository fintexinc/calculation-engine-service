package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.AlphaResult;
import com.fintex.ce.model.util.BigDecimalConstants;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Disabled;
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

@Disabled("metric unsupported")
class AlphaCalculationTest {

  final int TWELVE = 12;

  @Test
  void shouldDelegateToFormTimeIntervalResult_whenDefiningResponseType() {
    AlphaCalculation alpha = mock(AlphaCalculation.class);

    Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05",
        BigDecimal.ONE));

    Set<TimeIntervalResult> timeIntervals = Set.of(new TimeIntervalResult("2000-01-12", ONE));
    when(alpha.formTimeIntervalResult(anySet())).thenReturn(timeIntervals);

    doCallRealMethod().when(alpha).defineResponseType(anySet());
    alpha.defineResponseType(pairs);

    verify(alpha).formTimeIntervalResult(pairs);
  }

  @Test
  void shouldReturnAlphaResultWithMappedIntervals_whenDefiningResponseType() {
    AlphaCalculation alpha = mock(AlphaCalculation.class);

    Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2020-01-05", BigDecimal.ONE), Pair.of("2000-01-12",
        ZERO));

    Set<TimeIntervalResult> expected = Set.of(
        new TimeIntervalResult("2000-01-12", ZERO),
        new TimeIntervalResult("2020-01-05", BigDecimal.ONE));
    when(alpha.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(alpha).defineResponseType(anySet());
    AlphaResult actual = alpha.defineResponseType(pairs);

    assertEquals(expected, actual.getAlpha());
  }

  @Test
  void shouldResolvePeriodStartDate_whenCalculatingPeriodForNumberOfMonths() {
    AlphaCalculation alpha = mock(AlphaCalculation.class);

    TreeMap treeMap = mock(TreeMap.class);

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
    AlphaCalculation alpha = mock(AlphaCalculation.class);

    TreeMap<LocalDate, BigDecimal> portfolioBenchmarkPortfolioReturns = mock(TreeMap.class);
    when(alpha.getPortfolioTotalReturns()).thenReturn(portfolioBenchmarkPortfolioReturns);
    when(alpha.getBenchmarkTotalReturns()).thenReturn(portfolioBenchmarkPortfolioReturns);
    when(portfolioBenchmarkPortfolioReturns.size()).thenReturn(100);

    TreeMap<LocalDate, BigDecimal> portfolioBenchmarkExcessReturns = mock(TreeMap.class);
    when(alpha.getPortfolioExcessReturn()).thenReturn(portfolioBenchmarkExcessReturns);
    when(alpha.getBenchmarkExcessReturn()).thenReturn(portfolioBenchmarkExcessReturns);
    when(portfolioBenchmarkExcessReturns.size()).thenReturn(100);

    when(alpha.getSubMapByPeriodStartDate(any(), any())).thenReturn(portfolioBenchmarkExcessReturns);

    LocalDate periodStartDate = LocalDate.now();
    when(alpha.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);

    when(alpha.getSubMapByPeriodStartDate(any(), any())).thenReturn(portfolioBenchmarkExcessReturns);

    doCallRealMethod().when(alpha).calculatePeriodForNumberOfMonths(anyInt());
    alpha.calculatePeriodForNumberOfMonths(24);

    verify(alpha, times(2)).getSubMapByPeriodStartDate(periodStartDate, portfolioBenchmarkExcessReturns);
  }

  @Test
  void shouldCallCalculateAlpha_whenCalculatingPeriodForNumberOfMonths() {
    AlphaCalculation alpha = mock(AlphaCalculation.class);

    TreeMap treeMap = mock(TreeMap.class);

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
    AlphaCalculation alpha = mock(AlphaCalculation.class);
    TreeMap treeMap = mock(TreeMap.class);
    when(alpha.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(alpha.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    when(alpha.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(alpha.calculateAlpha(anyInt(), any(), any())).thenReturn(TEN);

    doCallRealMethod().when(alpha).calculatePeriodForNumberOfMonths(anyInt());
    BigDecimal result = alpha.calculatePeriodForNumberOfMonths(TWELVE);

    assertEquals(TEN, result);
  }

  @Test
  void shouldReturnNull_whenBenchmarkSizeIsLessThanRequestedPeriod() {
    AlphaCalculation alpha = mock(AlphaCalculation.class);
    TreeMap treeMap = mock(TreeMap.class);

    when(alpha.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(alpha.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    when(alpha.calculateAlpha(anyInt(), any(), any())).thenReturn(BigDecimal.TEN);

    doCallRealMethod().when(alpha).calculatePeriodForNumberOfMonths(anyInt());
    BigDecimal result = alpha.calculatePeriodForNumberOfMonths(24);

    assertNull(result);
  }

  @Test
  void shouldReturnNull_whenRequestedPeriodIsLessThanTwelveMonths() {
    AlphaCalculation alpha = mock(AlphaCalculation.class);

    TreeMap treeMap = mock(TreeMap.class);

    when(alpha.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(alpha.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(alpha.calculateAlpha(anyInt(), any(), any())).thenReturn(BigDecimal.TEN);
    when(treeMap.size()).thenReturn(TWELVE);

    doCallRealMethod().when(alpha).calculatePeriodForNumberOfMonths(anyInt());
    BigDecimal result = alpha.calculatePeriodForNumberOfMonths(6);

    assertNull(result);
  }

  @Test
  void shouldCallCalculateBeta_whenCalculatingAlpha() {
    AlphaCalculation alpha = mock(AlphaCalculation.class);

    when(alpha.calculateBeta(anyInt())).thenReturn(TEN);

    doCallRealMethod().when(alpha).calculateAlpha(anyInt(), any(), any());
    alpha.calculateAlpha(10, TEN, BigDecimalConstants.TWELVE);

    verify(alpha).calculateBeta(10);
  }

  @Test
  void shouldReturnExpectedAlphaValue_whenCalculatingAlphaWithBetaTen() {
    AlphaCalculation alpha = mock(AlphaCalculation.class);

    when(alpha.calculateBeta(anyInt())).thenReturn(TEN);

    doCallRealMethod().when(alpha).calculateAlpha(anyInt(), any(), any());
    BigDecimal actual = alpha.calculateAlpha(10, TEN, BigDecimalConstants.TWELVE);

    assertEquals(BigDecimal.valueOf(-1320), actual);
  }

  @Test
  void shouldReturnNull_whenBetaIsNull() {
    AlphaCalculation alpha = mock(AlphaCalculation.class);

    when(alpha.calculateBeta(anyInt())).thenReturn(null);

    doCallRealMethod().when(alpha).calculateAlpha(anyInt(), any(), any());
    BigDecimal actual = alpha.calculateAlpha(10, TEN, BigDecimalConstants.TWELVE);

    assertNull(actual);
  }

  @Test
  void shouldReturnExpectedAlphaValue_whenCalculatingAlphaWithBetaOne() {
    AlphaCalculation alpha = mock(AlphaCalculation.class);

    when(alpha.calculateBeta(anyInt())).thenReturn(ONE);

    doCallRealMethod().when(alpha).calculateAlpha(anyInt(), any(), any());
    BigDecimal actual = alpha.calculateAlpha(10, HUNDRED, BigDecimalConstants.TWELVE);

    assertEquals(BigDecimal.valueOf(1056), actual);
  }

}
