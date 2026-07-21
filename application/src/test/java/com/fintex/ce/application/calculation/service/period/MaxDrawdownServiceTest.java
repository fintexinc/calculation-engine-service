package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.returns.pipeline.CpedScaleParams;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.Growth10KHelper;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.result.MaxDrawdownEntry;
import com.fintex.ce.model.domain.result.risk.MaxDrawdownResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.util.BigDecimalConstants;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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

import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MaxDrawdownServiceTest {

  final int twelve = 12;

  @Test
  void shouldGetPeriodStartDateWithOneMonthOffset_whenCheckResult() {
    final var service = mock(MaxDrawdownService.class);
    final var growth10K = mock(TreeMap.class);
    final var numberOfMonths = 12;
    final var nowDate = LocalDate.now();
    final var expected = nowDate.minusMonths(1);

    when(service.getPeriodStartDate(anyInt(), any())).thenReturn(nowDate);

    doCallRealMethod().when(service).getPeriodStartDateWithOneMonthOffset(anyInt(), any());
    final var actual = service.getPeriodStartDateWithOneMonthOffset(numberOfMonths, growth10K);

    assertEquals(expected, actual);
  }

  @Test
  void shouldGetDrawDownStartDateWithOneMonthOffset_whenCheckResult() {
    final var service = mock(MaxDrawdownService.class);
    final var nowDate = LocalDate.now();
    final Map.Entry<LocalDate, BigDecimal> argument = new AbstractMap.SimpleEntry<>(nowDate, TEN);

    doCallRealMethod().when(service).getDrawDownStartDateWithOneMonthOffset(any());
    final var actual = service.getDrawDownStartDateWithOneMonthOffset(argument);

    assertEquals(nowDate.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth()), actual);
  }

  @Test
  void shouldCalculateEntry_whenHappyPath() {
    final var service = mock(MaxDrawdownService.class);
    final var portfolioReturns = mock(TreeMap.class);
    final var growth10K = mock(TreeMap.class);
    final var treeMap = mock(TreeMap.class);
    final var sortedMap = mock(SortedMap.class);
    final var entry = mock(Map.Entry.class);
    final var date = LocalDate.now();

    when(portfolioReturns.size()).thenReturn(twelve);
    when(service.getPeriodStartDateWithOneMonthOffset(anyInt(), any())).thenReturn(date);
    when(service.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
    when(service.calculateMaxDrawdownValues(any())).thenReturn(treeMap);
    when(service.getMaxDrawdownValue(any())).thenReturn(entry);
    when(service.getPeakValue(any(), any())).thenReturn(entry);
    when(service.getRecoveryTimeValue(any(), any(), any())).thenReturn(6);
    when(treeMap.isEmpty()).thenReturn(false);
    when(entry.getValue()).thenReturn(ONE);

    doCallRealMethod().when(service).calculateEntry(anyInt(), any(), any());
    final MaxDrawdownEntry result = service.calculateEntry(twelve, portfolioReturns, growth10K);

    verify(service).getPeriodStartDateWithOneMonthOffset(twelve, growth10K);
    verify(service).getSubMapByPeriodStartDate(date, growth10K);
    verify(service).calculateMaxDrawdownValues(new TreeMap<>(sortedMap));
    verify(service).getMaxDrawdownValue(treeMap);
    verify(service).getPeakValue(treeMap, entry);
    verify(service).getRecoveryTimeValue(treeMap, entry, entry);
    assertEquals(String.valueOf(twelve), result.period());
    assertEquals(ONE, result.value());
    assertNull(result.drawdownStartDate());
    assertNull(result.drawdownTroughDate());
    assertEquals(6, result.recoveryTime());
  }

  @Test
  void shouldCalculateEntry_whenReturnsNullWhenPortfolioReturnsSizeLessThanPeriod() {
    final var service = mock(MaxDrawdownService.class);
    final var portfolioReturns = mock(TreeMap.class);
    final var growth10K = mock(TreeMap.class);

    when(portfolioReturns.size()).thenReturn(ONE.intValue());
    doCallRealMethod().when(service).calculateEntry(anyInt(), any(), any());

    assertNull(service.calculateEntry(twelve, portfolioReturns, growth10K));
  }

  @Test
  void shouldCalculateEntry_whenReturnsNullWhenMaximumDrawdownMapIsEmpty() {
    final var service = mock(MaxDrawdownService.class);
    final var portfolioReturns = mock(TreeMap.class);
    final var growth10K = mock(TreeMap.class);
    final var sortedMap = mock(SortedMap.class);
    final var date = LocalDate.now();

    when(portfolioReturns.size()).thenReturn(twelve);
    when(service.getPeriodStartDateWithOneMonthOffset(anyInt(), any())).thenReturn(date);
    when(service.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
    when(service.calculateMaxDrawdownValues(any())).thenReturn(new TreeMap<>());

    doCallRealMethod().when(service).calculateEntry(anyInt(), any(), any());

    assertNull(service.calculateEntry(twelve, portfolioReturns, growth10K));
  }

  @Test
  void shouldCalculateEntry_whenReturnsZeroEntryWhenMaxDrawdownValueIsZero() {
    final var service = mock(MaxDrawdownService.class);
    final var portfolioReturns = mock(TreeMap.class);
    final var growth10K = mock(TreeMap.class);
    final var treeMap = mock(TreeMap.class);
    final var sortedMap = mock(SortedMap.class);
    final var date = LocalDate.now();

    when(portfolioReturns.size()).thenReturn(twelve);
    when(service.getPeriodStartDateWithOneMonthOffset(anyInt(), any())).thenReturn(date);
    when(service.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
    when(service.calculateMaxDrawdownValues(any())).thenReturn(treeMap);
    when(service.getMaxDrawdownValue(any()))
        .thenReturn(new TreeMap<>(Map.of(date, BigDecimal.ZERO)).firstEntry());
    when(treeMap.isEmpty()).thenReturn(false);

    doCallRealMethod().when(service).calculateEntry(anyInt(), any(), any());

    final MaxDrawdownEntry maxDrawdown = service.calculateEntry(twelve, portfolioReturns, growth10K);

    assertEquals(String.valueOf(twelve), maxDrawdown.period());
    assertEquals(0, BigDecimal.ZERO.compareTo(maxDrawdown.value()));
    assertNull(maxDrawdown.drawdownTroughDate());
    assertNull(maxDrawdown.drawdownStartDate());
    assertNull(maxDrawdown.recoveryTime());
  }

  @Test
  void shouldBuildResult_whenCheckResult() {
    final var service = mock(MaxDrawdownService.class);
    final var pairs = Set.of(
        Pair.of("12", new MaxDrawdownEntry(null, null, null, null, null)),
        Pair.of("22", new MaxDrawdownEntry(null, null, null, null, null)));
    final var expected = pairs.stream()
        .map(p -> new MaxDrawdownEntry(p.getKey(), null, null, null, null))
        .toList();

    doCallRealMethod().when(service).buildResult(anySet());
    final MaxDrawdownResult actual = service.buildResult(pairs);

    assertEquals(new HashSet<>(expected), new HashSet<>(actual.getMaxDrawdown()));
  }

  @Test
  void shouldCalculateMaxDrawdownValues_whenCheckResult() {
    final var service = mock(MaxDrawdownService.class);
    final var date = LocalDate.now();
    final var growth10KByPeriod = new TreeMap<>(Map.of(date, ONE, date.minusMonths(1), TEN, date.minusMonths(2),
        BigDecimalConstants.TWELVE));
    when(service.getSubMapFromFirstKeyToCustomDate(any(), any())).thenReturn(growth10KByPeriod);

    doCallRealMethod().when(service).calculateMaxDrawdownValues(any());
    final NavigableMap<LocalDate, BigDecimal> results = service.calculateMaxDrawdownValues(growth10KByPeriod);

    assertEquals(3, results.size());
    assertEquals(date.minusMonths(2), results.firstKey());
    assertEquals(toUserScale(BigDecimal.valueOf(0)), toUserScale(results.firstEntry().getValue()));
    assertEquals(date, results.lastKey());
    assertEquals(toUserScale(BigDecimal.valueOf(-0.916666666666667)), toUserScale(results.lastEntry().getValue()));
  }

  @Test
  void shouldCalculateMaxDrawdownValues_whenSkipsPointsWithZeroPeak() {
    final var service = mock(MaxDrawdownService.class);
    final var date = LocalDate.now();
    final var growth10KByPeriod = new TreeMap<>(Map.of(date, BigDecimal.ZERO, date.minusMonths(1), BigDecimal.ZERO,
        date.minusMonths(2), BigDecimal.ZERO));
    when(service.getSubMapFromFirstKeyToCustomDate(any(), any())).thenReturn(growth10KByPeriod);

    doCallRealMethod().when(service).calculateMaxDrawdownValues(any());
    final NavigableMap<LocalDate, BigDecimal> results = service.calculateMaxDrawdownValues(growth10KByPeriod);

    assertTrue(results.isEmpty());
  }

  @Test
  void shouldCalculateMaxDrawdownValues_whenSkipsOnlyZeroPeakMonths_andKeepsRecoveredMonths() {
    final var service = mock(MaxDrawdownService.class);
    final var date = LocalDate.now();
    final var growth10KByPeriod = new TreeMap<>(Map.of(
        date.minusMonths(3), BigDecimal.ZERO,
        date.minusMonths(2), BigDecimal.ZERO,
        date.minusMonths(1), BigDecimal.valueOf(100),
        date, BigDecimal.valueOf(50)));
    doCallRealMethod().when(service).getSubMapFromFirstKeyToCustomDate(any(), any());
    doCallRealMethod().when(service).calculateMaxDrawdownValues(any());

    final NavigableMap<LocalDate, BigDecimal> results = service.calculateMaxDrawdownValues(growth10KByPeriod);

    assertEquals(2, results.size());
    assertFalse(results.containsKey(date.minusMonths(3)));
    assertFalse(results.containsKey(date.minusMonths(2)));
    assertEquals(date.minusMonths(1), results.firstKey());
    assertEquals(toUserScale(BigDecimal.valueOf(0)), toUserScale(results.firstEntry().getValue()));
    assertEquals(date, results.lastKey());
    assertEquals(toUserScale(BigDecimal.valueOf(-0.5)), toUserScale(results.lastEntry().getValue()));
  }

  @Test
  void shouldGetMaxDrawdownValue_whenCheckResult() {
    final var service = mock(MaxDrawdownService.class);
    final var date = LocalDate.now();
    final var maximumDrawdownMap = new TreeMap<>(Map.of(date, ONE, date.minusMonths(1), TEN, date.minusMonths(2),
        BigDecimalConstants.TWELVE, date.plusMonths(3), TEN));

    doCallRealMethod().when(service).getMaxDrawdownValue(any());
    final Map.Entry<LocalDate, BigDecimal> maxDrawdownValue = service.getMaxDrawdownValue(maximumDrawdownMap);

    assertEquals(date, maxDrawdownValue.getKey());
    assertEquals(BigDecimal.ONE, maxDrawdownValue.getValue());
  }

  @Test
  void shouldGetPeakValue_whenCheckResult() {
    final var service = mock(MaxDrawdownService.class);
    final var date = LocalDate.now();
    final var maximumDrawdownMap = new TreeMap<>(Map.of(date, ONE, date.minusMonths(1), TEN, date.minusMonths(2),
        BigDecimalConstants.TWELVE, date.plusMonths(3), ONE));
    when(service.getSubMapFromFirstKeyToCustomDate(any(), any())).thenReturn(maximumDrawdownMap);

    doCallRealMethod().when(service).getPeakValue(any(), any());
    final Map.Entry<LocalDate, BigDecimal> peakValue = service.getPeakValue(maximumDrawdownMap,
        maximumDrawdownMap.lastEntry());

    assertEquals(date.minusMonths(2), peakValue.getKey());
    assertEquals(BigDecimalConstants.TWELVE, peakValue.getValue());
  }

  @Test
  void shouldGetRecoveryTimeValue_whenCheckResult() {
    final var service = mock(MaxDrawdownService.class);
    final var date = LocalDate.now();
    final var maximumDrawdownMap = new TreeMap<>(
        Map.of(date, BigDecimalConstants.TWELVE, date.minusMonths(1), TEN, date.minusMonths(2), ONE));
    when(service.getSubMapByPeriodStartDate(any(), any())).thenReturn(maximumDrawdownMap);

    doCallRealMethod().when(service).getRecoveryTimeValue(any(), any(), any());
    final Integer recoveryTimeValue = service.getRecoveryTimeValue(maximumDrawdownMap,
        maximumDrawdownMap.firstEntry(), maximumDrawdownMap.lastEntry());

    assertEquals(2, recoveryTimeValue);
  }

  @Test
  void shouldGetSubMapFromFirstKeyToCustomDate_whenCheckResult() {
    final var service = mock(MaxDrawdownService.class);
    final var date = LocalDate.now();
    final var maximumDrawdownMap = new TreeMap<>(
        Map.of(date, BigDecimalConstants.TWELVE, date.minusMonths(1), TEN, date.minusMonths(2), ONE));
    doCallRealMethod().when(service).getSubMapFromFirstKeyToCustomDate(any(), any());

    final NavigableMap<LocalDate, BigDecimal> result = service.getSubMapFromFirstKeyToCustomDate(maximumDrawdownMap,
        date.minusMonths(1));

    assertEquals(2, result.size());
  }

  @Test
  void shouldPerform_whenVerifyBuildPeriodCalculationInput() {
    final var contextProvider = mock(PortfolioMonthlyReturnsContextProvider.class);
    final var pipeline = mock(PortfolioWeightedAverageWithCpedPipeline.class);
    final var service = new MaxDrawdownService(contextProvider, pipeline, Set.of());

    final var req = mock(PeriodCommand.class);
    final var monthlyReturnsContext = mock(MonthlyReturnsContext.class);
    when(contextProvider.get(req.getHoldings(), req.getCurrency(), Map.of()))
        .thenReturn(monthlyReturnsContext);

    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>(
        Map.of(LocalDate.now(), BigDecimal.TEN));
    final var result = new WeightedAverageResult<>(weightedAverageReturns, ReturnsSnapshot.empty());
    when(pipeline.run(monthlyReturnsContext, new CpedScaleParams(req.getCustomPed(), ReturnFactorScale.SCALE_OF_TWO)))
        .thenReturn(result);

    service.perform(req, PortfolioBenchmarkReturns.EMPTY);

    verify(pipeline).run(monthlyReturnsContext,
        new CpedScaleParams(req.getCustomPed(), ReturnFactorScale.SCALE_OF_TWO));
  }

  @Test
  void shouldAddInsufficientDataWarnings_whenGrowthCollapses_thenEmitsDegenerateGrowthDataWarning() {
    final var endDate = LocalDate.of(2024, 12, 31);

    final NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    for (int i = 11; i >= 0; i--) {
      returns.put(endDate.minusMonths(i), ONE);
    }

    // All-zero growth10K: seed month + 12 months, simulating a -100% return before the window
    final NavigableMap<LocalDate, BigDecimal> growth10K = new TreeMap<>();
    for (int i = 12; i >= 0; i--) {
      growth10K.put(endDate.minusMonths(i), BigDecimal.ZERO);
    }

    final var contextProvider = mock(PortfolioMonthlyReturnsContextProvider.class);
    final var pipeline = mock(PortfolioWeightedAverageWithCpedPipeline.class);
    final var service = new MaxDrawdownService(contextProvider, pipeline, Set.of());

    final var command = mock(PeriodCommand.class);
    when(command.getPeriods()).thenReturn(Set.of("12"));

    final var monthlyReturnsContext = mock(MonthlyReturnsContext.class);
    when(contextProvider.get(any(), any(), any())).thenReturn(monthlyReturnsContext);
    final var weightedAverageResult = new WeightedAverageResult<>(returns, ReturnsSnapshot.empty());
    doReturn(weightedAverageResult).when(pipeline).run(any(), any());

    try (var mockedGrowth = Mockito.mockStatic(Growth10KHelper.class)) {
      mockedGrowth.when(() -> Growth10KHelper.compoundGrowth10K(any(), any()))
          .thenReturn(growth10K);

      final MaxDrawdownResult result = service.perform(command, PortfolioBenchmarkReturns.EMPTY);

      assertEquals(1, result.getWarnings().size());
      assertEquals(ErrorCode.Codes.DEGENERATE_GROWTH_DATA, result.getWarnings().get(0).getCode());
    }
  }
}
