package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.returns.ExcessReturnsResult;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExcessReturnsCalculationTest {

  final int TWELVE = 12;

  @Test
  void shouldDefineResponseType_whenVerifyFormTimeIntervalResult() {
    ExcessReturnsCalculation alpha = mock(ExcessReturnsCalculation.class);

    Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05",
        BigDecimal.ONE));

    doCallRealMethod().when(alpha).defineResponseType(anySet());
    alpha.defineResponseType(pairs);

    verify(alpha).formTimeIntervalResult(pairs);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculateAnnualizedReturnsByPeriod() {
    ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
    TreeMap treeMap = mock(TreeMap.class);
    when(excessReturnsCalculation.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(excessReturnsCalculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(12);
    doCallRealMethod().when(excessReturnsCalculation).calculatePeriodForNumberOfMonths(anyInt());
    excessReturnsCalculation.calculatePeriodForNumberOfMonths(12);

    verify(excessReturnsCalculation, times(2)).calculateAnnualizedReturnsByPeriod(12, treeMap);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResult() {
    ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
    TreeMap portfolioTreeMap = mock(TreeMap.class);
    TreeMap benchmarkTreeMap = mock(TreeMap.class);
    when(portfolioTreeMap.size()).thenReturn(12);
    when(benchmarkTreeMap.size()).thenReturn(12);
    when(excessReturnsCalculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTreeMap);
    when(excessReturnsCalculation.getPortfolioTotalReturns()).thenReturn(portfolioTreeMap);
    when(excessReturnsCalculation.calculateAnnualizedReturnsByPeriod(TWELVE, portfolioTreeMap)).thenReturn(
        BigDecimal.TEN);
    when(excessReturnsCalculation.calculateAnnualizedReturnsByPeriod(TWELVE, benchmarkTreeMap)).thenReturn(
        BigDecimal.ONE);
    doCallRealMethod().when(excessReturnsCalculation).calculatePeriodForNumberOfMonths(anyInt());
    BigDecimal returnValue = excessReturnsCalculation.calculatePeriodForNumberOfMonths(TWELVE);

    assertEquals(DecimalUtils.toUserScale(BigDecimal.valueOf(9)), returnValue);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResultWhenPeriodIsLargerThenTotalReturnsSize() {
    ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
    TreeMap portfolioTreeMap = mock(TreeMap.class);
    TreeMap benchmarkTreeMap = mock(TreeMap.class);
    when(portfolioTreeMap.size()).thenReturn(12);
    when(benchmarkTreeMap.size()).thenReturn(9);
    when(excessReturnsCalculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTreeMap);
    when(excessReturnsCalculation.getPortfolioTotalReturns()).thenReturn(portfolioTreeMap);
    when(excessReturnsCalculation.calculateAnnualizedReturnsByPeriod(TWELVE, portfolioTreeMap)).thenReturn(
        BigDecimal.TEN);
    when(excessReturnsCalculation.calculateAnnualizedReturnsByPeriod(TWELVE, benchmarkTreeMap)).thenReturn(
        BigDecimal.ONE);
    doCallRealMethod().when(excessReturnsCalculation).calculatePeriodForNumberOfMonths(anyInt());
    BigDecimal returnValue = excessReturnsCalculation.calculatePeriodForNumberOfMonths(TWELVE);

    assertNull(returnValue);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResultWhenAnnualizedReturnsAreNull() {
    ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
    TreeMap portfolioTreeMap = mock(TreeMap.class);
    TreeMap benchmarkTreeMap = mock(TreeMap.class);
    when(excessReturnsCalculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTreeMap);
    when(excessReturnsCalculation.getPortfolioTotalReturns()).thenReturn(portfolioTreeMap);
    when(excessReturnsCalculation.calculateAnnualizedReturnsByPeriod(TWELVE, portfolioTreeMap)).thenReturn(null);
    when(excessReturnsCalculation.calculateAnnualizedReturnsByPeriod(TWELVE, benchmarkTreeMap)).thenReturn(null);
    doCallRealMethod().when(excessReturnsCalculation).calculatePeriodForNumberOfMonths(anyInt());
    BigDecimal returnValue = excessReturnsCalculation.calculatePeriodForNumberOfMonths(TWELVE);

    assertNull(returnValue);
  }

  @Test
  void shouldCalculateAnnualizedReturnsByPeriod_whenVerifyGetPeriodStartDate() {
    ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
    TreeMap portfolioTreeMap = mock(TreeMap.class);
    doCallRealMethod().when(excessReturnsCalculation).calculateAnnualizedReturnsByPeriod(anyInt(), any());
    when(portfolioTreeMap.firstKey()).thenReturn(LocalDate.now());
    when(excessReturnsCalculation.getPeriodStartDate(anyInt(), any())).thenReturn(LocalDate.now().minusMonths(TWELVE));
    when(excessReturnsCalculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(portfolioTreeMap);
    when(excessReturnsCalculation.getPower(anyInt())).thenReturn(ONE);
    excessReturnsCalculation.calculateAnnualizedReturnsByPeriod(TWELVE, portfolioTreeMap);

    verify(excessReturnsCalculation).getPeriodStartDate(TWELVE, portfolioTreeMap);
  }

  @Test
  void shouldCalculateAnnualizedReturnsByPeriod_whenVerifyGetSubMap() {
    ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
    TreeMap portfolioTreeMap = mock(TreeMap.class);
    doCallRealMethod().when(excessReturnsCalculation).calculateAnnualizedReturnsByPeriod(anyInt(), any());
    LocalDate periodStartDate = LocalDate.now();
    when(portfolioTreeMap.firstKey()).thenReturn(LocalDate.now().minusMonths(TWELVE));
    when(excessReturnsCalculation.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);
    when(excessReturnsCalculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(portfolioTreeMap);
    when(excessReturnsCalculation.getPower(anyInt())).thenReturn(ONE);
    excessReturnsCalculation.calculateAnnualizedReturnsByPeriod(TWELVE, portfolioTreeMap);

    verify(excessReturnsCalculation).getSubMapByPeriodStartDate(periodStartDate, portfolioTreeMap);
  }

  @Test
  void shouldCalculateAnnualizedReturnsByPeriod_whenVerifyGetPower() {
    ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
    TreeMap portfolioTreeMap = mock(TreeMap.class);
    doCallRealMethod().when(excessReturnsCalculation).calculateAnnualizedReturnsByPeriod(anyInt(), any());
    LocalDate periodStartDate = LocalDate.now();
    when(portfolioTreeMap.firstKey()).thenReturn(LocalDate.now().minusMonths(TWELVE));
    when(excessReturnsCalculation.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);
    when(excessReturnsCalculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(portfolioTreeMap);
    when(excessReturnsCalculation.getPower(anyInt())).thenReturn(ONE);
    excessReturnsCalculation.calculateAnnualizedReturnsByPeriod(TWELVE, portfolioTreeMap);

    verify(excessReturnsCalculation).getPower(TWELVE);
  }

  @Test
  void shouldCalculateAnnualizedReturnsByPeriod_whenCheckResult() {
    ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
    doCallRealMethod().when(excessReturnsCalculation).calculateAnnualizedReturnsByPeriod(anyInt(), any());
    LocalDate periodStartDate = LocalDate.now();
    when(excessReturnsCalculation.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);
    when(excessReturnsCalculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(getPortfolioReturns());
    when(excessReturnsCalculation.getPower(anyInt())).thenReturn(ONE);
    BigDecimal returnValue = excessReturnsCalculation.calculateAnnualizedReturnsByPeriod(TWELVE,
        getPortfolioReturns());

    assertEquals(new BigDecimal("0.1594496804421743"), returnValue);
  }

  @Test
  void shouldGetPower_whenCheckResult() {
    ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
    doCallRealMethod().when(excessReturnsCalculation).getPower(anyInt());
    BigDecimal power = excessReturnsCalculation.getPower(23);

    assertEquals(BigDecimal.valueOf(0.521739130434783), power);
  }

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);

    Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05", ONE));

    Set<TimeIntervalResult> expected = Set.of(
        new TimeIntervalResult("2000-01-12", ZERO),
        new TimeIntervalResult("2020-01-05", BigDecimal.ONE));
    when(excessReturnsCalculation.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(excessReturnsCalculation).defineResponseType(anySet());
    ExcessReturnsResult actual = excessReturnsCalculation.defineResponseType(pairs);

    assertEquals(expected, actual.getExcessReturns());
  }

  private TreeMap<LocalDate, BigDecimal> getPortfolioReturns() {
    LocalDate date = LocalDate.of(2020, 12, 1);
    Map<LocalDate, BigDecimal> map = new HashMap<>();
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
