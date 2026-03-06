package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.ExcessReturnsCalculation;
import com.fintex.ce.port.input.result.ExcessReturnsResult;
import com.fintex.ce.port.input.result.core.TimeIntervalResult;
import com.fintex.ce.util.DecimalUtils;
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
  void defineResponseType_verifyFormTimeIntervalResult() {
    // SETUP
    final ExcessReturnsCalculation alpha = mock(ExcessReturnsCalculation.class);

    final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05",
        BigDecimal.ONE));

    doCallRealMethod().when(alpha).defineResponseType(anySet());
    // ACT
    alpha.defineResponseType(pairs);

    // VERIFY
    verify(alpha).formTimeIntervalResult(pairs);
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyCalculateAnnualizedReturnsByPeriod() {
    // SETUP
    final ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
    final TreeMap treeMap = mock(TreeMap.class);
    when(excessReturnsCalculation.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(excessReturnsCalculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(12);
    doCallRealMethod().when(excessReturnsCalculation).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    excessReturnsCalculation.calculatePeriodForNumberOfMonths(12);

    // VERIFY
    verify(excessReturnsCalculation, times(2)).calculateAnnualizedReturnsByPeriod(12, treeMap);
  }

  @Test
  void calculatePeriodForNumberOfMonths_checkResult() {
    // SETUP
    final ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
    final TreeMap portfolioTreeMap = mock(TreeMap.class);
    final TreeMap benchmarkTreeMap = mock(TreeMap.class);
    when(portfolioTreeMap.size()).thenReturn(12);
    when(benchmarkTreeMap.size()).thenReturn(12);
    when(excessReturnsCalculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTreeMap);
    when(excessReturnsCalculation.getPortfolioTotalReturns()).thenReturn(portfolioTreeMap);
    when(excessReturnsCalculation.calculateAnnualizedReturnsByPeriod(TWELVE, portfolioTreeMap)).thenReturn(
        BigDecimal.TEN);
    when(excessReturnsCalculation.calculateAnnualizedReturnsByPeriod(TWELVE, benchmarkTreeMap)).thenReturn(
        BigDecimal.ONE);
    doCallRealMethod().when(excessReturnsCalculation).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final BigDecimal returnValue = excessReturnsCalculation.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    assertEquals(DecimalUtils.toUserScale(BigDecimal.valueOf(9)), returnValue);
  }

  @Test
  void calculatePeriodForNumberOfMonths_checkResultWhenPeriodIsLargerThenTotalReturnsSize() {
    // SETUP
    final ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
    final TreeMap portfolioTreeMap = mock(TreeMap.class);
    final TreeMap benchmarkTreeMap = mock(TreeMap.class);
    when(portfolioTreeMap.size()).thenReturn(12);
    when(benchmarkTreeMap.size()).thenReturn(9);
    when(excessReturnsCalculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTreeMap);
    when(excessReturnsCalculation.getPortfolioTotalReturns()).thenReturn(portfolioTreeMap);
    when(excessReturnsCalculation.calculateAnnualizedReturnsByPeriod(TWELVE, portfolioTreeMap)).thenReturn(
        BigDecimal.TEN);
    when(excessReturnsCalculation.calculateAnnualizedReturnsByPeriod(TWELVE, benchmarkTreeMap)).thenReturn(
        BigDecimal.ONE);
    doCallRealMethod().when(excessReturnsCalculation).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final BigDecimal returnValue = excessReturnsCalculation.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    assertNull(returnValue);
  }

  @Test
  void calculatePeriodForNumberOfMonths_checkResultWhenAnnualizedReturnsAreNull() {
    // SETUP
    final ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
    final TreeMap portfolioTreeMap = mock(TreeMap.class);
    final TreeMap benchmarkTreeMap = mock(TreeMap.class);
    when(excessReturnsCalculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTreeMap);
    when(excessReturnsCalculation.getPortfolioTotalReturns()).thenReturn(portfolioTreeMap);
    when(excessReturnsCalculation.calculateAnnualizedReturnsByPeriod(TWELVE, portfolioTreeMap)).thenReturn(null);
    when(excessReturnsCalculation.calculateAnnualizedReturnsByPeriod(TWELVE, benchmarkTreeMap)).thenReturn(null);
    doCallRealMethod().when(excessReturnsCalculation).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final BigDecimal returnValue = excessReturnsCalculation.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    assertNull(returnValue);
  }

  @Test
  void calculateAnnualizedReturnsByPeriod_verifyGetPeriodStartDate() {
    // SETUP
    final ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
    final TreeMap portfolioTreeMap = mock(TreeMap.class);
    doCallRealMethod().when(excessReturnsCalculation).calculateAnnualizedReturnsByPeriod(anyInt(), any());
    when(portfolioTreeMap.firstKey()).thenReturn(LocalDate.now());
    when(excessReturnsCalculation.getPeriodStartDate(anyInt(), any())).thenReturn(LocalDate.now().minusMonths(TWELVE));
    when(excessReturnsCalculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(portfolioTreeMap);
    when(excessReturnsCalculation.getPower(anyInt())).thenReturn(ONE);
    // ACT
    final BigDecimal returnValue = excessReturnsCalculation.calculateAnnualizedReturnsByPeriod(TWELVE,
        portfolioTreeMap);

    // VERIFY
    verify(excessReturnsCalculation).getPeriodStartDate(TWELVE, portfolioTreeMap);
  }

  @Test
  void calculateAnnualizedReturnsByPeriod_verifyGetSubMap() {
    // SETUP
    final ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
    final TreeMap portfolioTreeMap = mock(TreeMap.class);
    doCallRealMethod().when(excessReturnsCalculation).calculateAnnualizedReturnsByPeriod(anyInt(), any());
    final LocalDate periodStartDate = LocalDate.now();
    when(portfolioTreeMap.firstKey()).thenReturn(LocalDate.now().minusMonths(TWELVE));
    when(excessReturnsCalculation.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);
    when(excessReturnsCalculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(portfolioTreeMap);
    when(excessReturnsCalculation.getPower(anyInt())).thenReturn(ONE);
    // ACT
    final BigDecimal returnValue = excessReturnsCalculation.calculateAnnualizedReturnsByPeriod(TWELVE,
        portfolioTreeMap);

    // VERIFY
    verify(excessReturnsCalculation).getSubMapByPeriodStartDate(periodStartDate, portfolioTreeMap);
  }

  @Test
  void calculateAnnualizedReturnsByPeriod_verifyGetPower() {
    // SETUP
    final ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
    final TreeMap portfolioTreeMap = mock(TreeMap.class);
    doCallRealMethod().when(excessReturnsCalculation).calculateAnnualizedReturnsByPeriod(anyInt(), any());
    final LocalDate periodStartDate = LocalDate.now();
    when(portfolioTreeMap.firstKey()).thenReturn(LocalDate.now().minusMonths(TWELVE));
    when(excessReturnsCalculation.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);
    when(excessReturnsCalculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(portfolioTreeMap);
    when(excessReturnsCalculation.getPower(anyInt())).thenReturn(ONE);
    // ACT
    final BigDecimal returnValue = excessReturnsCalculation.calculateAnnualizedReturnsByPeriod(TWELVE,
        portfolioTreeMap);

    // VERIFY
    verify(excessReturnsCalculation).getPower(TWELVE);
  }

  @Test
  void calculateAnnualizedReturnsByPeriod_checkResult() {
    // SETUP
    final ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
    doCallRealMethod().when(excessReturnsCalculation).calculateAnnualizedReturnsByPeriod(anyInt(), any());
    final LocalDate periodStartDate = LocalDate.now();
    when(excessReturnsCalculation.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);
    when(excessReturnsCalculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(getPortfolioReturns());
    when(excessReturnsCalculation.getPower(anyInt())).thenReturn(ONE);
    // ACT
    final BigDecimal returnValue = excessReturnsCalculation.calculateAnnualizedReturnsByPeriod(TWELVE,
        getPortfolioReturns());

    // VERIFY
    assertEquals(DecimalUtils.toUserScale(BigDecimal.valueOf(0.15944968044217)), returnValue);
  }

  @Test
  void getPower_checkResult() {
    // SETUP
    final ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
    doCallRealMethod().when(excessReturnsCalculation).getPower(anyInt());
    // ACT
    final BigDecimal power = excessReturnsCalculation.getPower(23);

    // VERIFY
    assertEquals(BigDecimal.valueOf(0.521739130434783), power);
  }

  @Test
  void defineResponseType_checkResult() {
    // SETUP
    final ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);

    final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05", ONE));

    final Set<TimeIntervalResult> expected = Set.of(
        new TimeIntervalResult("2000-01-12", ZERO),
        new TimeIntervalResult("2020-01-05", BigDecimal.ONE));
    when(excessReturnsCalculation.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(excessReturnsCalculation).defineResponseType(anySet());
    // ACT
    final ExcessReturnsResult actual = excessReturnsCalculation.defineResponseType(pairs);

    // VERIFY
    assertEquals(expected, actual.getExcessReturns());
  }

  private TreeMap<LocalDate, BigDecimal> getPortfolioReturns() {
    final LocalDate date = LocalDate.of(2020, 12, 1);
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
