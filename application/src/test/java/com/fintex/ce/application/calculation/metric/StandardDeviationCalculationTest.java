package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.util.CalculationUtils;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.StandardDeviationResult;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.application.util.DecimalUtils.OUTPUT_SCALE;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
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
import static org.mockito.Mockito.withSettings;

class StandardDeviationCalculationTest {

  private final int TWELVE = 12;

  private static TreeMap<LocalDate, BigDecimal> map;

  @BeforeAll
  static void setUp() {
    final LocalDate date = LocalDate.of(2020, 12, 1);
    map = new TreeMap<>();
    map.put(toLastDayOfMonth(date.minusMonths(3)), new BigDecimal("1.01074832088959"));
    map.put(toLastDayOfMonth(date.minusMonths(2)), new BigDecimal("1.01608812281602"));
    map.put(toLastDayOfMonth(date.minusMonths(1)), new BigDecimal("1.00844777099365"));
    map.put(toLastDayOfMonth(date), new BigDecimal("1.01222986673534"));
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetPeriodStartDate() {
    final var sut = mock(StandardDeviationCalculation.class, withSettings().useConstructor(mock(
        PeriodCalculationInput.class),
        Set.of()));
    final var treeMap = mock(TreeMap.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt(), any());
    sut.calculatePeriodForNumberOfMonths(TWELVE, treeMap);

    verify(sut).getPeriodStartDate(12, treeMap);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculatePeriodForNumberOfMonths() {
    final var sut = mock(StandardDeviationCalculation.class);
    final var returns = mock(TreeMap.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(returns);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sut).calculatePeriodForNumberOfMonths(TWELVE, returns);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetSubMapByPeriodStartDate() {
    final var sut = mock(StandardDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    final var periodStartDate = LocalDate.now();
    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);
    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt(), any());

    sut.calculatePeriodForNumberOfMonths(TWELVE, treeMap);

    verify(sut).getSubMapByPeriodStartDate(periodStartDate, treeMap);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResult() {
    final var sut = mock(StandardDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);

    when(treeMap.size()).thenReturn(1);

    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(TWELVE, treeMap);

    assertNull(actual);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResult2() {
    final var sut = mock(StandardDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);

    when(treeMap.size()).thenReturn(20);

    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(ONE.intValue(), treeMap);

    assertNull(actual);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculateStandardDeviation() {
    final var sut = mock(StandardDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    final LocalDate periodStartDate = LocalDate.now();
    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt(), any());
    sut.calculatePeriodForNumberOfMonths(TWELVE, treeMap);

    verify(sut).calculateStandardDeviation(treeMap, TWELVE);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResultWhenNumberOfMonthsBiggerThanReturnsSize() {
    final var sut = mock(StandardDeviationCalculation.class);
    final var returns = mock(NavigableMap.class);

    when(returns.size()).thenReturn(11);
    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt(), any(NavigableMap.class));

    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(12, returns);

    assertNull(actual);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResultWhenNumberOfMonthsBiggerLessThan12() {
    final var sut = mock(StandardDeviationCalculation.class);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt(), any(NavigableMap.class));

    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(11, mock(NavigableMap.class));

    assertNull(actual);
  }

  @Test
  void shouldCalculateStandardDeviation_whenVerifyCalculateNumerator() {
    final var sut = mock(StandardDeviationCalculation.class);
    final var treeMap = new TreeMap();
    treeMap.put(LocalDate.now(), TEN);
    treeMap.put(LocalDate.now().minusMonths(1), TEN);
    treeMap.put(LocalDate.now().minusMonths(5), TEN);

    when(sut.calculateNumerator(any(), any())).thenReturn(BigDecimal.ONE);
    doCallRealMethod().when(sut).calculateStandardDeviation(any(), anyInt());

    sut.calculateStandardDeviation(treeMap, TWELVE);

    verify(sut).calculateNumerator(treeMap, TEN.setScale(15, RoundingMode.UNNECESSARY));
  }

  @Test
  void shouldCalculateStandardDeviation_whenCheckResult() {
    final var sut = mock(StandardDeviationCalculation.class);
    doCallRealMethod().when(sut).setScale(anyInt());
    sut.setScale(OUTPUT_SCALE);
    final var treeMap = new TreeMap();
    treeMap.put(LocalDate.now(), ONE);
    treeMap.put(LocalDate.now().minusMonths(1), TEN);
    treeMap.put(LocalDate.now().minusMonths(5), TEN);

    when(sut.calculateNumerator(any(), any())).thenReturn(BigDecimal.TEN);
    doCallRealMethod().when(sut).calculateStandardDeviation(any(), anyInt());
    final BigDecimal actual = sut.calculateStandardDeviation(treeMap, TWELVE);

    assertEquals(toUserScale(BigDecimal.valueOf(3.30289129537908)), actual);
  }

  @Test
  void shouldCalculateNumerator_whenCheckResult() {
    final var sut = mock(StandardDeviationCalculation.class);

    doCallRealMethod().when(sut).calculateNumerator(any(), any());
    doCallRealMethod().when(sut).overrideTotalReturns(any());

    final NavigableMap<LocalDate, BigDecimal> totalReturns = sut.overrideTotalReturns(map);
    final BigDecimal actual = sut.calculateNumerator(totalReturns, CalculationUtils.average(totalReturns));

    assertEquals(toUserScale(BigDecimal.valueOf(3.08915891708201E-05)), toUserScale(actual));
  }

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    final var sut = mock(StandardDeviationCalculation.class);
    final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05", ONE));
    final var interval1 = new TimeIntervalResult("2000-01-12", ZERO);
    final var interval2 = new TimeIntervalResult("2020-01-05", ONE);
    final var expected = Set.of(interval1, interval2);

    when(sut.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(sut).defineResponseType(anySet());
    final StandardDeviationResult actual = (StandardDeviationResult) sut.defineResponseType(pairs);

    assertEquals(expected, actual.getStandardDeviation());
  }

  @AfterAll
  static void tearDown() {
    map.clear();
  }

}
