package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.MaxDrawdownEntry;
import com.fintex.ce.model.domain.result.risk.MaxDrawdownResult;
import com.fintex.ce.model.util.BigDecimalConstants;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class MaxDrawdownCalculationTest {

  final int TWELVE = 12;

  @Test
  void shouldGetPeriodStartDateWithOneMonthOffset_whenVerifyGetPeriodStartDate() {
    final var growth10K = mock(TreeMap.class);
    final var input = mock(PeriodCalculationInput.class);
    final var calculation = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K,
        null));

    final var numberOfMonths = 12;
    final var nowDate = LocalDate.now();

    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(nowDate);

    doCallRealMethod().when(calculation).getPeriodStartDateWithOneMonthOffset(anyInt());
    calculation.getPeriodStartDateWithOneMonthOffset(numberOfMonths);

    verify(calculation).getPeriodStartDate(numberOfMonths, growth10K);
  }

  @Test
  void shouldGetPeriodStartDateWithOneMonthOffset_whenCheckResult() {
    final var calculation = mock(MaxDrawdownCalculation.class);

    final var numberOfMonths = 12;
    final var nowDate = LocalDate.now();
    final var expected = nowDate.minusMonths(1);

    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(nowDate);

    doCallRealMethod().when(calculation).getPeriodStartDateWithOneMonthOffset(anyInt());
    final var actual = calculation.getPeriodStartDateWithOneMonthOffset(numberOfMonths);

    assertEquals(expected, actual);
  }

  @Test
  void shouldGetDrawDownStartDateWithOneMonthOffset_whenCheckResult() {
    final var calculation = mock(MaxDrawdownCalculation.class);

    final var nowDate = LocalDate.now();
    final Map.Entry<LocalDate, BigDecimal> argument = new AbstractMap.SimpleEntry<>(nowDate, TEN);

    doCallRealMethod().when(calculation).getDrawDownStartDateWithOneMonthOffset(any());
    final var actual = calculation.getDrawDownStartDateWithOneMonthOffset(argument);

    assertEquals(nowDate.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth()), actual);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetPeriodStartDateWithOneMonthOffset() {
    final var growth10K = mock(TreeMap.class);
    final var input = mock(PeriodCalculationInput.class);
    final Function<BigDecimal, BigDecimal> scaleFunction = DecimalUtils::toUserScale;
    final var calculation = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K,
        scaleFunction));

    final var treeMap = mock(TreeMap.class);
    final var entry = mock(Map.Entry.class);

    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(mock(SortedMap.class));
    when(calculation.getMaxDrawdownValue(any())).thenReturn(entry);
    when(calculation.getPeakValue(any(), any())).thenReturn(entry);
    when(treeMap.size()).thenReturn(TWELVE);
    when(entry.getValue()).thenReturn(ONE);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(calculation).getPeriodStartDateWithOneMonthOffset(12);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetSubMapByPeriodStartDate() {
    final var growth10K = mock(TreeMap.class);
    final var input = mock(PeriodCalculationInput.class);
    final Function<BigDecimal, BigDecimal> scaleFunction = DecimalUtils::toUserScale;
    final var calculation = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K,
        scaleFunction));

    final var treeMap = mock(TreeMap.class);
    final var entry = mock(Map.Entry.class);
    final var date = LocalDate.now();

    when(entry.getValue()).thenReturn(ONE);
    when(calculation.getPeriodStartDateWithOneMonthOffset(anyInt())).thenReturn(date);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(mock(SortedMap.class));
    when(calculation.getMaxDrawdownValue(any())).thenReturn(entry);
    when(calculation.getPeakValue(any(), any())).thenReturn(entry);
    when(treeMap.size()).thenReturn(TWELVE);
    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());

    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(calculation).getSubMapByPeriodStartDate(date, growth10K);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculateMaxDrawdownValues() {
    final var growth10K = mock(TreeMap.class);
    final var input = mock(PeriodCalculationInput.class);
    final Function<BigDecimal, BigDecimal> scaleFunction = DecimalUtils::toUserScale;
    final var calculation = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K,
        scaleFunction));

    final var treeMap = mock(TreeMap.class);
    final var sortedMap = mock(SortedMap.class);
    final var entry = mock(Map.Entry.class);
    final LocalDate date = LocalDate.now();

    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
    when(calculation.getMaxDrawdownValue(any())).thenReturn(entry);
    when(calculation.getPeakValue(any(), any())).thenReturn(entry);
    when(treeMap.size()).thenReturn(TWELVE);
    when(entry.getValue()).thenReturn(ONE);
    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());

    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(calculation).calculateMaxDrawdownValues(new TreeMap<>(sortedMap));
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetMaxDrawdownValue() {
    final var growth10K = mock(TreeMap.class);
    final var input = mock(PeriodCalculationInput.class);
    final Function<BigDecimal, BigDecimal> scaleFunction = DecimalUtils::toUserScale;
    final var calculation = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K,
        scaleFunction));

    final var treeMap = mock(TreeMap.class);
    final var sortedMap = mock(SortedMap.class);
    final var entry = mock(Map.Entry.class);
    final var date = LocalDate.now();

    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
    when(calculation.getMaxDrawdownValue(any())).thenReturn(entry);
    when(calculation.calculateMaxDrawdownValues(any())).thenReturn(treeMap);
    when(calculation.getPeakValue(any(), any())).thenReturn(entry);
    when(treeMap.size()).thenReturn(TWELVE);
    when(entry.getValue()).thenReturn(ONE);
    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());

    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(calculation).getMaxDrawdownValue(treeMap);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetPeakValue() {
    final var growth10K = mock(TreeMap.class);
    final var input = mock(PeriodCalculationInput.class);
    final Function<BigDecimal, BigDecimal> scaleFunction = DecimalUtils::toUserScale;
    final var calculation = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K,
        scaleFunction));

    final var treeMap = mock(TreeMap.class);
    final var sortedMap = mock(SortedMap.class);
    final var entry = mock(Map.Entry.class);
    final var date = LocalDate.now();

    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
    when(calculation.getMaxDrawdownValue(any())).thenReturn(entry);
    when(calculation.calculateMaxDrawdownValues(any())).thenReturn(treeMap);
    when(calculation.getPeakValue(any(), any())).thenReturn(entry);
    when(treeMap.size()).thenReturn(TWELVE);
    when(entry.getValue()).thenReturn(ONE);
    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());

    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(calculation).getPeakValue(treeMap, entry);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetRecoveryTimeValue() {
    final var growth10K = mock(TreeMap.class);
    final var input = mock(PeriodCalculationInput.class);
    final Function<BigDecimal, BigDecimal> scaleFunction = DecimalUtils::toUserScale;
    final var calculation = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K,
        scaleFunction));

    final var treeMap = mock(TreeMap.class);
    final var sortedMap = mock(SortedMap.class);
    final var entry = mock(Map.Entry.class);
    final LocalDate date = LocalDate.now();

    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
    when(calculation.getMaxDrawdownValue(any())).thenReturn(entry);
    when(calculation.calculateMaxDrawdownValues(any())).thenReturn(treeMap);
    when(calculation.getPeakValue(any(), any())).thenReturn(entry);
    when(treeMap.size()).thenReturn(TWELVE);
    when(entry.getValue()).thenReturn(ONE);
    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());

    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(calculation).getRecoveryTimeValue(treeMap, entry, entry);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResultWhenPortfolioTotalReturnsSizeLessThanPeriod() {
    final var growth10K = mock(TreeMap.class);
    final var input = mock(PeriodCalculationInput.class);
    final var calculation = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K,
        null));

    final var treeMap = mock(TreeMap.class);
    final var sortedMap = mock(SortedMap.class);
    final var entry = mock(Map.Entry.class);
    final var date = LocalDate.now();

    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
    when(calculation.getMaxDrawdownValue(any())).thenReturn(entry);
    when(calculation.calculateMaxDrawdownValues(any())).thenReturn(treeMap);
    when(calculation.getPeakValue(any(), any())).thenReturn(entry);
    when(treeMap.size()).thenReturn(ONE.intValue());
    when(entry.getValue()).thenReturn(ONE);
    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());

    final MaxDrawdownEntry maxDrawDown = calculation.calculatePeriodForNumberOfMonths(TWELVE);

    assertEquals(String.valueOf(TWELVE), maxDrawDown.period());
    assertNull(maxDrawDown.drawdownTroughDate());
    assertNull(maxDrawDown.drawdownStartDate());
    assertNull(maxDrawDown.recoveryTime());
    assertNull(maxDrawDown.value());
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResultWhengetMaxDrawdownValueReturnsZero() {
    final var growth10K = mock(TreeMap.class);
    final var input = mock(PeriodCalculationInput.class);
    final var calculation = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K,
        null));

    final var treeMap = mock(TreeMap.class);
    final var sortedMap = mock(SortedMap.class);
    final var entry = mock(Map.Entry.class);
    final var date = LocalDate.now();

    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
    when(calculation.getMaxDrawdownValue(any()))
        .thenReturn(new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.valueOf(0, 00000000000))).firstEntry());
    when(calculation.calculateMaxDrawdownValues(any())).thenReturn(treeMap);
    when(calculation.getPeakValue(any(), any())).thenReturn(entry);
    when(treeMap.size()).thenReturn(ONE.intValue());
    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());

    final MaxDrawdownEntry maxDrawDown = calculation.calculatePeriodForNumberOfMonths(TWELVE);

    assertEquals(String.valueOf(TWELVE), maxDrawDown.period());
    assertNull(maxDrawDown.drawdownTroughDate());
    assertNull(maxDrawDown.drawdownStartDate());
    assertNull(maxDrawDown.recoveryTime());
    assertNull(maxDrawDown.value());
  }

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    final var calculation = mock(MaxDrawdownCalculation.class);
    final var pairs = Set.of(
        Pair.of("12", new MaxDrawdownEntry(null, null, null, null, null)),
        Pair.of("22", new MaxDrawdownEntry(null, null, null, null, null)));
    final var expected = pairs.stream()
        .map(p -> new MaxDrawdownEntry(p.getKey(), null, null, null, null))
        .toList();

    doCallRealMethod().when(calculation).defineResponseType(anySet());
    final MaxDrawdownResult actual = calculation.defineResponseType(pairs);

    assertEquals(new HashSet<>(expected), new HashSet<>(actual.getMaxDrawdown()));
  }

  @Test
  void shouldCalculateMaxDrawdownValues_whenVerifyGetSubMapFromFirstKeyToCustomDate() {
    final var calculation = mock(MaxDrawdownCalculation.class);
    final var date = LocalDate.now();
    final var growth10KByPeriod = new TreeMap<>(Map.of(date, ONE));
    when(calculation.getSubMapFromFirstKeyToCustomDate(any(), any())).thenReturn(growth10KByPeriod);

    doCallRealMethod().when(calculation).calculateMaxDrawdownValues(any());
    calculation.calculateMaxDrawdownValues(growth10KByPeriod);

    verify(calculation, times(1)).getSubMapFromFirstKeyToCustomDate(growth10KByPeriod, date);
  }
  @Test
  void shouldCalculateMaxDrawdownValues_whenCheckResult() {
    final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);
    final var date = LocalDate.now();
    final var growth10KByPeriod = new TreeMap<>(Map.of(date, ONE, date.minusMonths(1), TEN, date.minusMonths(2),
        BigDecimalConstants.TWELVE));
    when(maxDrawdownCalculation.getSubMapFromFirstKeyToCustomDate(any(), any())).thenReturn(growth10KByPeriod);

    doCallRealMethod().when(maxDrawdownCalculation).calculateMaxDrawdownValues(any());
    final NavigableMap<LocalDate, BigDecimal> results = maxDrawdownCalculation.calculateMaxDrawdownValues(
        growth10KByPeriod);

    assertEquals(3, results.size());
    assertEquals(date.minusMonths(2), results.firstKey());
    assertEquals(toUserScale(BigDecimal.valueOf(0)), toUserScale(results.firstEntry().getValue()));
    assertEquals(date, results.lastKey());
    assertEquals(toUserScale(BigDecimal.valueOf(-0.916666666666667)), toUserScale(results.lastEntry().getValue()));
  }

  @Test
  void shouldGetMaxDrawdownValue_whenCheckResult() {
    final var calculation = mock(MaxDrawdownCalculation.class);
    final var date = LocalDate.now();
    final var maximumDrawdownMap = Map.of(date, ONE, date.minusMonths(1), TEN, date.minusMonths(2),
        BigDecimalConstants.TWELVE, date.plusMonths(3), ONE);
    final var growth10KByPeriod = new TreeMap<>(maximumDrawdownMap);

    doCallRealMethod().when(calculation).getMaxDrawdownValue(any());
    final Map.Entry<LocalDate, BigDecimal> maxDrawdownValue = calculation.getMaxDrawdownValue(growth10KByPeriod);

    assertEquals(date, maxDrawdownValue.getKey());
    assertEquals(BigDecimal.ONE, maxDrawdownValue.getValue());
  }

  @Test
  void shouldGetPeakValue_whenVerifyGetSubMapFromFirstKeyToCustomDate() {
    final var calculation = mock(MaxDrawdownCalculation.class);
    final var date = LocalDate.now();
    final var map = Map.of(date, ONE, date.minusMonths(1), TEN, date.minusMonths(2), BigDecimalConstants.TWELVE, date
        .plusMonths(3), ONE);
    final var maximumDrawdownMap = new TreeMap<>(map);
    when(calculation.getSubMapFromFirstKeyToCustomDate(any(), any())).thenReturn(maximumDrawdownMap);

    doCallRealMethod().when(calculation).getPeakValue(any(), any());
    calculation.getPeakValue(maximumDrawdownMap, maximumDrawdownMap.lastEntry());

    verify(calculation).getSubMapFromFirstKeyToCustomDate(maximumDrawdownMap, maximumDrawdownMap.lastKey());
  }

  @Test
  void shouldGetPeakValue_whenCheckResult() {
    final var calculation = mock(MaxDrawdownCalculation.class);
    final var date = LocalDate.now();
    final var map = Map.of(date, ONE, date.minusMonths(1), TEN, date.minusMonths(2), BigDecimalConstants.TWELVE, date
        .plusMonths(3), ONE);
    final var maximumDrawdownMap = new TreeMap<>(map);
    when(calculation.getSubMapFromFirstKeyToCustomDate(any(), any())).thenReturn(maximumDrawdownMap);

    doCallRealMethod().when(calculation).getPeakValue(any(), any());
    final Map.Entry<LocalDate, BigDecimal> peakValue = calculation.getPeakValue(maximumDrawdownMap, maximumDrawdownMap
        .lastEntry());

    assertEquals(date.minusMonths(2), peakValue.getKey());
    assertEquals(BigDecimalConstants.TWELVE, peakValue.getValue());
  }

  @Test
  void shouldGetRecoveryTimeValue_whenVerifyGetSubMapByPeriodStartDate() {
    final var calculation = mock(MaxDrawdownCalculation.class);
    final var date = LocalDate.now();
    final var map = Map.of(date, BigDecimalConstants.TWELVE, date.minusMonths(1), TEN, date.minusMonths(2), ONE);
    final var maximumDrawdownMap = new TreeMap<>(map);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(maximumDrawdownMap);

    doCallRealMethod().when(calculation).getRecoveryTimeValue(any(), any(), any());
    calculation.getRecoveryTimeValue(maximumDrawdownMap, maximumDrawdownMap.firstEntry(), maximumDrawdownMap
        .lastEntry());

    verify(calculation).getSubMapByPeriodStartDate(maximumDrawdownMap.firstEntry().getKey(), maximumDrawdownMap);
  }

  @Test
  void shouldGetRecoveryTimeValue_whenCheckResult() {
    final var calculation = mock(MaxDrawdownCalculation.class);
    final var date = LocalDate.now();
    final var map = Map.of(date, BigDecimalConstants.TWELVE, date.minusMonths(1), TEN, date.minusMonths(2), ONE);
    final var maximumDrawdownMap = new TreeMap<>(map);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(maximumDrawdownMap);

    doCallRealMethod().when(calculation).getRecoveryTimeValue(any(), any(), any());
    final Integer recoveryTimeValue = calculation.getRecoveryTimeValue(maximumDrawdownMap, maximumDrawdownMap
        .firstEntry(),
        maximumDrawdownMap.lastEntry());

    assertEquals(2, recoveryTimeValue);
  }

  @Test
  void shouldGetSubMapFromFirstKeyToCustomDate_whenCheckResult() {
    final var calculation = mock(MaxDrawdownCalculation.class);
    final var date = LocalDate.now();
    final var map = Map.of(date, BigDecimalConstants.TWELVE, date.minusMonths(1), TEN, date.minusMonths(2), ONE);
    final var maximumDrawdownMap = new TreeMap<>(map);
    doCallRealMethod().when(calculation).getSubMapFromFirstKeyToCustomDate(any(), any());

    final NavigableMap<LocalDate, BigDecimal> result = calculation.getSubMapFromFirstKeyToCustomDate(maximumDrawdownMap,
        date
            .minusMonths(1));

    assertEquals(2, result.size());
  }

}
