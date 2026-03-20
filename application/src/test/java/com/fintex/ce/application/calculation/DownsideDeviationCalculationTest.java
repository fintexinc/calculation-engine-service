package com.fintex.ce.application.calculation;

import com.fintex.ce.domain.model.result.DownsideDeviationResult;
import com.fintex.ce.domain.model.result.core.TimeIntervalResult;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.domain.constant.BigDecimalConstants.ONE;
import static com.fintex.ce.domain.constant.BigDecimalConstants.TEN_THOUSAND;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DownsideDeviationCalculationTest {
  final int TWELVE = 12;

  @Test
  void shouldReturnNull_whenPeriodExceedsExcessReturnsSize() {
    final var sut = mock(DownsideDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var excessReturns = mock(TreeMap.class);
    sut.portfolioExcessReturn = excessReturns;

    when(treeMap.size()).thenReturn(100);
    when(excessReturns.size()).thenReturn(20);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(sut.calculateDownsideDeviation(anyInt(), any())).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(100);

    assertNull(actual);
  }

  @Test
  void shouldResolvePeriodStartDate_whenCalculatingPeriod() {
    final var sut = mock(DownsideDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    sut.portfolioExcessReturn = treeMap;

    when(treeMap.size()).thenReturn(TWELVE);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    sut.calculatePeriodForNumberOfMonths(TWELVE);
    verify(sut).getPeriodStartDate(12, treeMap);
  }

  @Test
  void shouldGetSubMapByStartDate_whenCalculatingPeriod() {
    final var sut = mock(DownsideDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var date = LocalDate.now();
    sut.portfolioExcessReturn = treeMap;
    when(treeMap.size()).thenReturn(TWELVE);
    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    sut.calculatePeriodForNumberOfMonths(TWELVE);
    verify(sut).getSubMapByPeriodStartDate(date, treeMap);
  }

  @Test
  void shouldCalculateDownsideReturnSquared_whenCalculatingPeriod() {
    final var sut = mock(DownsideDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var date = LocalDate.now();
    sut.portfolioExcessReturn = treeMap;
    when(treeMap.size()).thenReturn(TWELVE);
    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    sut.calculatePeriodForNumberOfMonths(TWELVE);
    verify(sut).calculateDownsideReturnSquared(treeMap);
  }

  @Test
  void shouldCalculateDownsideDeviation_whenCalculatingPeriod() {
    final var sut = mock(DownsideDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final LocalDate date = LocalDate.now();
    sut.portfolioExcessReturn = treeMap;
    when(treeMap.size()).thenReturn(TWELVE);
    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(sut.calculateDownsideReturnSquared(any())).thenReturn(treeMap);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    sut.calculatePeriodForNumberOfMonths(TWELVE);
    verify(sut).calculateDownsideDeviation(TWELVE, treeMap);
  }

  @Test
  void shouldReturnNull_whenPortfolioReturnsSizeIsLessThanPeriod() {
    final var sut = mock(DownsideDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    sut.portfolioExcessReturn = treeMap;
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(BigDecimal.ONE.intValue());

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal result = sut.calculatePeriodForNumberOfMonths(TWELVE);
    assertNull(result);
  }

  @Test
  void shouldKeepOnlyNegativeExcessReturnsSquared_whenCalculatingDownsideReturnSquared() {
    final var sut = mock(DownsideDeviationCalculation.class);
    final var date = LocalDate.now();
    final var portfolioExcessReturnsInPeriod = new TreeMap(Map.of(date, ONE, date.plusMonths(1), BigDecimal.valueOf(
        -5.2222), date.plusMonths(2), TEN_THOUSAND));

    doCallRealMethod().when(sut).calculateDownsideReturnSquared(any());
    final TreeMap downsideReturnSquared = sut.calculateDownsideReturnSquared(portfolioExcessReturnsInPeriod);
    assertEquals(1, downsideReturnSquared.size());
    assertEquals(BigDecimal.valueOf(27.271372839999998), downsideReturnSquared.firstEntry().getValue());
    assertEquals(date.plusMonths(1), downsideReturnSquared.firstKey());
  }

  @Test
  void shouldCalculateDownsideDeviationValue_whenSquaredReturnsProvided() {
    final var sut = mock(DownsideDeviationCalculation.class);
    final var date = LocalDate.now();
    final var downsideReturnSquared = new TreeMap(Map.of(date, BigDecimal.valueOf(2), date.plusMonths(1),
        BigDecimal.valueOf(3), date.plusMonths(2), BigDecimal.valueOf(4)));

    doCallRealMethod().when(sut).calculateDownsideDeviation(anyInt(), any());
    final BigDecimal downsideDeviation = sut.calculateDownsideDeviation(TWELVE, downsideReturnSquared);
    assertEquals(toUserScale(BigDecimal.valueOf(2.99999999999999725114029147225)), toUserScale(downsideDeviation));
  }

  @Test
  void shouldMapIntervalResults_whenDefiningResponseType() {
    final var sut = mock(DownsideDeviationCalculation.class);
    final var pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05", BigDecimal.ONE));
    final var intervalResDto = new TimeIntervalResult("2000-01-12", ZERO);
    final var intervalResDto1 = new TimeIntervalResult("2020-01-05", BigDecimal.ONE);
    final var expected = Set.of(intervalResDto, intervalResDto1);
    when(sut.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(sut).defineResponseType(anySet());
    final DownsideDeviationResult actual = (DownsideDeviationResult) sut.defineResponseType(pairs);
    assertEquals(expected, actual.getDownsideDeviation());
  }
}
