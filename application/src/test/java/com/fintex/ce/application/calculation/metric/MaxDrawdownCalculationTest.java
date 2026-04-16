package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.result.MaxDrawdownEntry;
import com.fintex.ce.model.domain.result.risk.MaxDrawdownResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
import com.fintex.ce.model.util.BigDecimalConstants;
import com.fintex.ce.util.DecimalUtils;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.AbstractMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
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
    final var input = mock(CalculationDTO.class);
    final var sut = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K, null));

    final var numberOfMonths = 12;
    final var nowDate = LocalDate.now();

    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(nowDate);

    doCallRealMethod().when(sut).getPeriodStartDateWithOneMonthOffset(anyInt());
    sut.getPeriodStartDateWithOneMonthOffset(numberOfMonths);

    verify(sut).getPeriodStartDate(numberOfMonths, growth10K);
  }

  @Test
  void shouldGetPeriodStartDateWithOneMonthOffset_whenCheckResult() {
    final var sut = mock(MaxDrawdownCalculation.class);

    final var numberOfMonths = 12;
    final var nowDate = LocalDate.now();
    final var expected = nowDate.minusMonths(1);

    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(nowDate);

    doCallRealMethod().when(sut).getPeriodStartDateWithOneMonthOffset(anyInt());
    final var actual = sut.getPeriodStartDateWithOneMonthOffset(numberOfMonths);

    assertEquals(expected, actual);
  }

  @Test
  void shouldGetDrawDownStartDateWithOneMonthOffset_whenCheckResult() {
    final var sut = mock(MaxDrawdownCalculation.class);

    final var nowDate = LocalDate.now();
    final Map.Entry<LocalDate, BigDecimal> argument = new AbstractMap.SimpleEntry<>(nowDate, TEN);

    doCallRealMethod().when(sut).getDrawDownStartDateWithOneMonthOffset(any());
    final var actual = sut.getDrawDownStartDateWithOneMonthOffset(argument);

    assertEquals(nowDate.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth()), actual);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetPeriodStartDateWithOneMonthOffset() {
    final var growth10K = mock(TreeMap.class);
    final var input = mock(CalculationDTO.class);
    final Function<BigDecimal, BigDecimal> scaleFunction = DecimalUtils::toUserScale;
    final var sut = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K,
        scaleFunction));

    final var treeMap = mock(TreeMap.class);
    final var entry = mock(Map.Entry.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(mock(SortedMap.class));
    when(sut.getMaxDrawdownValue(any())).thenReturn(entry);
    when(sut.getPeakValue(any(), any())).thenReturn(entry);
    when(treeMap.size()).thenReturn(TWELVE);
    when(entry.getValue()).thenReturn(ONE);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sut).getPeriodStartDateWithOneMonthOffset(12);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetSubMapByPeriodStartDate() {
    final var growth10K = mock(TreeMap.class);
    final var input = mock(CalculationDTO.class);
    final Function<BigDecimal, BigDecimal> scaleFunction = DecimalUtils::toUserScale;
    final var sut = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K,
        scaleFunction));

    final var treeMap = mock(TreeMap.class);
    final var entry = mock(Map.Entry.class);
    final var date = LocalDate.now();

    when(entry.getValue()).thenReturn(ONE);
    when(sut.getPeriodStartDateWithOneMonthOffset(anyInt())).thenReturn(date);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(mock(SortedMap.class));
    when(sut.getMaxDrawdownValue(any())).thenReturn(entry);
    when(sut.getPeakValue(any(), any())).thenReturn(entry);
    when(treeMap.size()).thenReturn(TWELVE);
    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

    sut.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sut).getSubMapByPeriodStartDate(date, growth10K);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculateMaxDrawdownValues() {
    final var growth10K = mock(TreeMap.class);
    final var input = mock(CalculationDTO.class);
    final Function<BigDecimal, BigDecimal> scaleFunction = DecimalUtils::toUserScale;
    final var sut = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K,
        scaleFunction));

    final var treeMap = mock(TreeMap.class);
    final var sortedMap = mock(SortedMap.class);
    final var entry = mock(Map.Entry.class);
    final LocalDate date = LocalDate.now();

    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
    when(sut.getMaxDrawdownValue(any())).thenReturn(entry);
    when(sut.getPeakValue(any(), any())).thenReturn(entry);
    when(treeMap.size()).thenReturn(TWELVE);
    when(entry.getValue()).thenReturn(ONE);
    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

    sut.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sut).calculateMaxDrawdownValues(new TreeMap<>(sortedMap));
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetMaxDrawdownValue() {
    final var growth10K = mock(TreeMap.class);
    final var input = mock(CalculationDTO.class);
    final Function<BigDecimal, BigDecimal> scaleFunction = DecimalUtils::toUserScale;
    final var sut = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K,
        scaleFunction));

    final var treeMap = mock(TreeMap.class);
    final var sortedMap = mock(SortedMap.class);
    final var entry = mock(Map.Entry.class);
    final var date = LocalDate.now();

    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
    when(sut.getMaxDrawdownValue(any())).thenReturn(entry);
    when(sut.calculateMaxDrawdownValues(any())).thenReturn(treeMap);
    when(sut.getPeakValue(any(), any())).thenReturn(entry);
    when(treeMap.size()).thenReturn(TWELVE);
    when(entry.getValue()).thenReturn(ONE);
    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

    sut.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sut).getMaxDrawdownValue(treeMap);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetPeakValue() {
    final var growth10K = mock(TreeMap.class);
    final var input = mock(CalculationDTO.class);
    final Function<BigDecimal, BigDecimal> scaleFunction = DecimalUtils::toUserScale;
    final var sut = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K,
        scaleFunction));

    final var treeMap = mock(TreeMap.class);
    final var sortedMap = mock(SortedMap.class);
    final var entry = mock(Map.Entry.class);
    final var date = LocalDate.now();

    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
    when(sut.getMaxDrawdownValue(any())).thenReturn(entry);
    when(sut.calculateMaxDrawdownValues(any())).thenReturn(treeMap);
    when(sut.getPeakValue(any(), any())).thenReturn(entry);
    when(treeMap.size()).thenReturn(TWELVE);
    when(entry.getValue()).thenReturn(ONE);
    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

    sut.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sut).getPeakValue(treeMap, entry);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetRecoveryTimeValue() {
    final var growth10K = mock(TreeMap.class);
    final var input = mock(CalculationDTO.class);
    final Function<BigDecimal, BigDecimal> scaleFunction = DecimalUtils::toUserScale;
    final var sut = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K,
        scaleFunction));

    final var treeMap = mock(TreeMap.class);
    final var sortedMap = mock(SortedMap.class);
    final var entry = mock(Map.Entry.class);
    final LocalDate date = LocalDate.now();

    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
    when(sut.getMaxDrawdownValue(any())).thenReturn(entry);
    when(sut.calculateMaxDrawdownValues(any())).thenReturn(treeMap);
    when(sut.getPeakValue(any(), any())).thenReturn(entry);
    when(treeMap.size()).thenReturn(TWELVE);
    when(entry.getValue()).thenReturn(ONE);
    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

    sut.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sut).getRecoveryTimeValue(treeMap, entry, entry);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResultWhenPortfolioTotalReturnsSizeLessThanPeriod() {
    final var growth10K = mock(TreeMap.class);
    final var input = mock(CalculationDTO.class);
    final var sut = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K, null));

    final var treeMap = mock(TreeMap.class);
    final var sortedMap = mock(SortedMap.class);
    final var entry = mock(Map.Entry.class);
    final var date = LocalDate.now();

    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
    when(sut.getMaxDrawdownValue(any())).thenReturn(entry);
    when(sut.calculateMaxDrawdownValues(any())).thenReturn(treeMap);
    when(sut.getPeakValue(any(), any())).thenReturn(entry);
    when(treeMap.size()).thenReturn(ONE.intValue());
    when(entry.getValue()).thenReturn(ONE);
    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

    final MaxDrawdownEntry maxDrawDownDTO = sut.calculatePeriodForNumberOfMonths(TWELVE);

    assertEquals(String.valueOf(TWELVE), maxDrawDownDTO.getTimeIntervalPeriod());
    assertNull(maxDrawDownDTO.getDrawdownTroughDate());
    assertNull(maxDrawDownDTO.getDrawdownStartDate());
    assertNull(maxDrawDownDTO.getRecoveryTime());
    assertNull(maxDrawDownDTO.getValue());
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResultWhengetMaxDrawdownValueReturnsZero() {
    final var growth10K = mock(TreeMap.class);
    final var input = mock(CalculationDTO.class);
    final var sut = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K, null));

    final var treeMap = mock(TreeMap.class);
    final var sortedMap = mock(SortedMap.class);
    final var entry = mock(Map.Entry.class);
    final var date = LocalDate.now();

    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
    when(sut.getMaxDrawdownValue(any()))
        .thenReturn(new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.valueOf(0, 00000000000))).firstEntry());
    when(sut.calculateMaxDrawdownValues(any())).thenReturn(treeMap);
    when(sut.getPeakValue(any(), any())).thenReturn(entry);
    when(treeMap.size()).thenReturn(ONE.intValue());
    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

    final MaxDrawdownEntry maxDrawDownDTO = sut.calculatePeriodForNumberOfMonths(TWELVE);

    assertEquals(String.valueOf(TWELVE), maxDrawDownDTO.getTimeIntervalPeriod());
    assertNull(maxDrawDownDTO.getDrawdownTroughDate());
    assertNull(maxDrawDownDTO.getDrawdownStartDate());
    assertNull(maxDrawDownDTO.getRecoveryTime());
    assertNull(maxDrawDownDTO.getValue());
  }

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    final var sut = mock(MaxDrawdownCalculation.class);
    final var pairs = Set.of(Pair.of("12", new MaxDrawdownEntry()), Pair.of("22", new MaxDrawdownEntry()));
    final var expected = pairs.stream().map(Pair::getValue).collect(Collectors.toList());

    doCallRealMethod().when(sut).defineResponseType(anySet());
    final MaxDrawdownResult actual = sut.defineResponseType(pairs);

    assertEquals(expected, actual.getMaxDrawdown());
  }

  @Test
  void shouldCalculateMaxDrawdownValues_whenVerifyGetSubMapFromFirstKeyToCustomDate() {
    final var sut = mock(MaxDrawdownCalculation.class);
    final var date = LocalDate.now();
    final var growth10KByPeriod = new TreeMap<>(Map.of(date, ONE));
    when(sut.getSubMapFromFirstKeyToCustomDate(any(), any())).thenReturn(growth10KByPeriod);

    doCallRealMethod().when(sut).calculateMaxDrawdownValues(any());
    sut.calculateMaxDrawdownValues(growth10KByPeriod);

    verify(sut, times(1)).getSubMapFromFirstKeyToCustomDate(growth10KByPeriod, date);
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
    final var sut = mock(MaxDrawdownCalculation.class);
    final var date = LocalDate.now();
    final var maximumDrawdownMap = Map.of(date, ONE, date.minusMonths(1), TEN, date.minusMonths(2),
        BigDecimalConstants.TWELVE, date.plusMonths(3), ONE);
    final var growth10KByPeriod = new TreeMap<>(maximumDrawdownMap);

    doCallRealMethod().when(sut).getMaxDrawdownValue(any());
    final Map.Entry<LocalDate, BigDecimal> maxDrawdownValue = sut.getMaxDrawdownValue(growth10KByPeriod);

    assertEquals(date, maxDrawdownValue.getKey());
    assertEquals(BigDecimal.ONE, maxDrawdownValue.getValue());
  }

  @Test
  void shouldGetPeakValue_whenVerifyGetSubMapFromFirstKeyToCustomDate() {
    final var sut = mock(MaxDrawdownCalculation.class);
    final var date = LocalDate.now();
    final var map = Map.of(date, ONE, date.minusMonths(1), TEN, date.minusMonths(2), BigDecimalConstants.TWELVE, date
        .plusMonths(3), ONE);
    final var maximumDrawdownMap = new TreeMap<>(map);
    when(sut.getSubMapFromFirstKeyToCustomDate(any(), any())).thenReturn(maximumDrawdownMap);

    doCallRealMethod().when(sut).getPeakValue(any(), any());
    sut.getPeakValue(maximumDrawdownMap, maximumDrawdownMap.lastEntry());

    verify(sut).getSubMapFromFirstKeyToCustomDate(maximumDrawdownMap, maximumDrawdownMap.lastKey());
  }

  @Test
  void shouldGetPeakValue_whenCheckResult() {
    final var sut = mock(MaxDrawdownCalculation.class);
    final var date = LocalDate.now();
    final var map = Map.of(date, ONE, date.minusMonths(1), TEN, date.minusMonths(2), BigDecimalConstants.TWELVE, date
        .plusMonths(3), ONE);
    final var maximumDrawdownMap = new TreeMap<>(map);
    when(sut.getSubMapFromFirstKeyToCustomDate(any(), any())).thenReturn(maximumDrawdownMap);

    doCallRealMethod().when(sut).getPeakValue(any(), any());
    final Map.Entry<LocalDate, BigDecimal> peakValue = sut.getPeakValue(maximumDrawdownMap, maximumDrawdownMap
        .lastEntry());

    assertEquals(date.minusMonths(2), peakValue.getKey());
    assertEquals(BigDecimalConstants.TWELVE, peakValue.getValue());
  }

  @Test
  void shouldGetRecoveryTimeValue_whenVerifyGetSubMapByPeriodStartDate() {
    final var sut = mock(MaxDrawdownCalculation.class);
    final var date = LocalDate.now();
    final var map = Map.of(date, BigDecimalConstants.TWELVE, date.minusMonths(1), TEN, date.minusMonths(2), ONE);
    final var maximumDrawdownMap = new TreeMap<>(map);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(maximumDrawdownMap);

    doCallRealMethod().when(sut).getRecoveryTimeValue(any(), any(), any());
    sut.getRecoveryTimeValue(maximumDrawdownMap, maximumDrawdownMap.firstEntry(), maximumDrawdownMap.lastEntry());

    verify(sut).getSubMapByPeriodStartDate(maximumDrawdownMap.firstEntry().getKey(), maximumDrawdownMap);
  }

  @Test
  void shouldGetRecoveryTimeValue_whenCheckResult() {
    final var sut = mock(MaxDrawdownCalculation.class);
    final var date = LocalDate.now();
    final var map = Map.of(date, BigDecimalConstants.TWELVE, date.minusMonths(1), TEN, date.minusMonths(2), ONE);
    final var maximumDrawdownMap = new TreeMap<>(map);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(maximumDrawdownMap);

    doCallRealMethod().when(sut).getRecoveryTimeValue(any(), any(), any());
    final Integer recoveryTimeValue = sut.getRecoveryTimeValue(maximumDrawdownMap, maximumDrawdownMap.firstEntry(),
        maximumDrawdownMap.lastEntry());

    assertEquals(2, recoveryTimeValue);
  }

  @Test
  void shouldGetSubMapFromFirstKeyToCustomDate_whenCheckResult() {
    final var sut = mock(MaxDrawdownCalculation.class);
    final var date = LocalDate.now();
    final var map = Map.of(date, BigDecimalConstants.TWELVE, date.minusMonths(1), TEN, date.minusMonths(2), ONE);
    final var maximumDrawdownMap = new TreeMap<>(map);
    doCallRealMethod().when(sut).getSubMapFromFirstKeyToCustomDate(any(), any());

    final NavigableMap<LocalDate, BigDecimal> result = sut.getSubMapFromFirstKeyToCustomDate(maximumDrawdownMap, date
        .minusMonths(1));

    assertEquals(2, result.size());
  }

}
