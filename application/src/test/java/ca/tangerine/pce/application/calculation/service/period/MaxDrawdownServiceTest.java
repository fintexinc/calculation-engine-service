package ca.tangerine.pce.application.calculation.service.period;

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

import static ca.tangerine.pce.application.util.DecimalUtils.toUserScale;
import static ca.tangerine.pce.model.util.BigDecimalConstants.ONE;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.TEN_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.TWENTY_YR;
import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.tangerine.pce.application.config.PeriodProperties;
import ca.tangerine.pce.application.returns.MonthlyReturnsContext;
import ca.tangerine.pce.application.returns.PortfolioMonthlyReturnsContextProvider;
import ca.tangerine.pce.application.returns.ReturnsSnapshot;
import ca.tangerine.pce.application.returns.WeightedAverageResult;
import ca.tangerine.pce.application.returns.pipeline.CpedScaleParams;
import ca.tangerine.pce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import ca.tangerine.pce.application.util.Growth10KHelper;
import ca.tangerine.pce.application.util.ReturnFactorScale;
import ca.tangerine.pce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import ca.tangerine.pce.model.domain.result.MaxDrawdownEntry;
import ca.tangerine.pce.model.domain.result.risk.MaxDrawdownResult;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.exceptions.CalculationException;
import ca.tangerine.pce.model.util.BigDecimalConstants;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;

class MaxDrawdownServiceTest {

  final int twelve = 12;

  @Test
  void shouldGetPeriodStartDateWithOneMonthOffset_whenCheckResult() {
    var service = mock(MaxDrawdownService.class);
    var growth10K = mock(TreeMap.class);
    var numberOfMonths = 12;
    var nowDate = LocalDate.now();
    var expected = nowDate.minusMonths(1);

    when(service.getPeriodStartDate(anyInt(), any())).thenReturn(nowDate);

    doCallRealMethod().when(service).getPeriodStartDateWithOneMonthOffset(anyInt(), any());
    var actual = service.getPeriodStartDateWithOneMonthOffset(numberOfMonths, growth10K);

    assertEquals(expected, actual);
  }

  @Test
  void shouldGetDrawDownStartDateWithOneMonthOffset_whenCheckResult() {
    var service = mock(MaxDrawdownService.class);
    var nowDate = LocalDate.now();
    Map.Entry<LocalDate, BigDecimal> argument = new AbstractMap.SimpleEntry<>(nowDate, TEN);

    doCallRealMethod().when(service).getDrawDownStartDateWithOneMonthOffset(any());
    var actual = service.getDrawDownStartDateWithOneMonthOffset(argument);

    assertEquals(nowDate.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth()), actual);
  }

  @Test
  void shouldCalculateEntry_whenHappyPath() {
    var service = mock(MaxDrawdownService.class);
    var portfolioReturns = mock(TreeMap.class);
    var growth10K = mock(TreeMap.class);
    var treeMap = mock(TreeMap.class);
    var sortedMap = mock(SortedMap.class);
    var entry = mock(Map.Entry.class);
    var date = LocalDate.now();

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
    MaxDrawdownEntry result = service.calculateEntry(twelve, portfolioReturns, growth10K);

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
    var service = mock(MaxDrawdownService.class);
    var portfolioReturns = mock(TreeMap.class);
    var growth10K = mock(TreeMap.class);

    when(portfolioReturns.size()).thenReturn(ONE.intValue());
    doCallRealMethod().when(service).calculateEntry(anyInt(), any(), any());

    assertNull(service.calculateEntry(twelve, portfolioReturns, growth10K));
  }

  @Test
  void shouldCalculateEntry_whenReturnsNullWhenMaximumDrawdownMapIsEmpty() {
    var service = mock(MaxDrawdownService.class);
    var portfolioReturns = mock(TreeMap.class);
    var growth10K = mock(TreeMap.class);
    var sortedMap = mock(SortedMap.class);
    var date = LocalDate.now();

    when(portfolioReturns.size()).thenReturn(twelve);
    when(service.getPeriodStartDateWithOneMonthOffset(anyInt(), any())).thenReturn(date);
    when(service.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
    when(service.calculateMaxDrawdownValues(any())).thenReturn(new TreeMap<>());

    doCallRealMethod().when(service).calculateEntry(anyInt(), any(), any());

    assertNull(service.calculateEntry(twelve, portfolioReturns, growth10K));
  }

  @Test
  void shouldCalculateEntry_whenReturnsZeroEntryWhenMaxDrawdownValueIsZero() {
    var service = mock(MaxDrawdownService.class);
    var portfolioReturns = mock(TreeMap.class);
    var growth10K = mock(TreeMap.class);
    var treeMap = mock(TreeMap.class);
    var sortedMap = mock(SortedMap.class);
    var date = LocalDate.now();

    when(portfolioReturns.size()).thenReturn(twelve);
    when(service.getPeriodStartDateWithOneMonthOffset(anyInt(), any())).thenReturn(date);
    when(service.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
    when(service.calculateMaxDrawdownValues(any())).thenReturn(treeMap);
    when(service.getMaxDrawdownValue(any()))
        .thenReturn(new TreeMap<>(Map.of(date, BigDecimal.ZERO)).firstEntry());
    when(treeMap.isEmpty()).thenReturn(false);

    doCallRealMethod().when(service).calculateEntry(anyInt(), any(), any());

    MaxDrawdownEntry maxDrawdown = service.calculateEntry(twelve, portfolioReturns, growth10K);

    assertEquals(String.valueOf(twelve), maxDrawdown.period());
    assertEquals(0, BigDecimal.ZERO.compareTo(maxDrawdown.value()));
    assertNull(maxDrawdown.drawdownTroughDate());
    assertNull(maxDrawdown.drawdownStartDate());
    assertNull(maxDrawdown.recoveryTime());
  }

  @Test
  void shouldBuildResult_whenCheckResult() {
    var service = mock(MaxDrawdownService.class);
    var pairs = Set.of(
        Pair.of("12", new MaxDrawdownEntry(null, null, null, null, null)),
        Pair.of("22", new MaxDrawdownEntry(null, null, null, null, null)));
    var expected = pairs.stream()
        .map(p -> new MaxDrawdownEntry(p.getKey(), null, null, null, null))
        .toList();

    doCallRealMethod().when(service).buildResult(anySet());
    MaxDrawdownResult actual = service.buildResult(pairs);

    assertEquals(new HashSet<>(expected), new HashSet<>(actual.getMaxDrawdown()));
  }

  @Test
  void shouldCalculateMaxDrawdownValues_whenCheckResult() {
    var service = mock(MaxDrawdownService.class);
    var date = LocalDate.now();
    var growth10KByPeriod = new TreeMap<>(Map.of(date, ONE, date.minusMonths(1), TEN, date.minusMonths(2),
        BigDecimalConstants.TWELVE));
    when(service.getSubMapFromFirstKeyToCustomDate(any(), any())).thenReturn(growth10KByPeriod);

    doCallRealMethod().when(service).calculateMaxDrawdownValues(any());
    NavigableMap<LocalDate, BigDecimal> results = service.calculateMaxDrawdownValues(growth10KByPeriod);

    assertEquals(3, results.size());
    assertEquals(date.minusMonths(2), results.firstKey());
    assertEquals(toUserScale(BigDecimal.valueOf(0)), toUserScale(results.firstEntry().getValue()));
    assertEquals(date, results.lastKey());
    assertEquals(toUserScale(BigDecimal.valueOf(-0.916666666666667)), toUserScale(results.lastEntry().getValue()));
  }

  @Test
  void shouldCalculateMaxDrawdownValues_whenSkipsPointsWithZeroPeak() {
    var service = mock(MaxDrawdownService.class);
    var date = LocalDate.now();
    var growth10KByPeriod = new TreeMap<>(Map.of(date, BigDecimal.ZERO, date.minusMonths(1), BigDecimal.ZERO,
        date.minusMonths(2), BigDecimal.ZERO));
    when(service.getSubMapFromFirstKeyToCustomDate(any(), any())).thenReturn(growth10KByPeriod);

    doCallRealMethod().when(service).calculateMaxDrawdownValues(any());
    NavigableMap<LocalDate, BigDecimal> results = service.calculateMaxDrawdownValues(growth10KByPeriod);

    assertTrue(results.isEmpty());
  }

  @Test
  void shouldCalculateMaxDrawdownValues_whenSkipsOnlyZeroPeakMonths_andKeepsRecoveredMonths() {
    var service = mock(MaxDrawdownService.class);
    var date = LocalDate.now();
    var growth10KByPeriod = new TreeMap<>(Map.of(
        date.minusMonths(3), BigDecimal.ZERO,
        date.minusMonths(2), BigDecimal.ZERO,
        date.minusMonths(1), BigDecimal.valueOf(100),
        date, BigDecimal.valueOf(50)));
    doCallRealMethod().when(service).getSubMapFromFirstKeyToCustomDate(any(), any());
    doCallRealMethod().when(service).calculateMaxDrawdownValues(any());

    NavigableMap<LocalDate, BigDecimal> results = service.calculateMaxDrawdownValues(growth10KByPeriod);

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
    var service = mock(MaxDrawdownService.class);
    var date = LocalDate.now();
    var maximumDrawdownMap = new TreeMap<>(Map.of(date, ONE, date.minusMonths(1), TEN, date.minusMonths(2),
        BigDecimalConstants.TWELVE, date.plusMonths(3), TEN));

    doCallRealMethod().when(service).getMaxDrawdownValue(any());
    Map.Entry<LocalDate, BigDecimal> maxDrawdownValue = service.getMaxDrawdownValue(maximumDrawdownMap);

    assertEquals(date, maxDrawdownValue.getKey());
    assertEquals(BigDecimal.ONE, maxDrawdownValue.getValue());
  }

  @Test
  void shouldGetPeakValue_whenCheckResult() {
    var service = mock(MaxDrawdownService.class);
    var date = LocalDate.now();
    var maximumDrawdownMap = new TreeMap<>(Map.of(date, ONE, date.minusMonths(1), TEN, date.minusMonths(2),
        BigDecimalConstants.TWELVE, date.plusMonths(3), ONE));
    when(service.getSubMapFromFirstKeyToCustomDate(any(), any())).thenReturn(maximumDrawdownMap);

    doCallRealMethod().when(service).getPeakValue(any(), any());
    Map.Entry<LocalDate, BigDecimal> peakValue = service.getPeakValue(maximumDrawdownMap,
        maximumDrawdownMap.lastEntry());

    assertEquals(date.minusMonths(2), peakValue.getKey());
    assertEquals(BigDecimalConstants.TWELVE, peakValue.getValue());
  }

  @Test
  void shouldGetRecoveryTimeValue_whenCheckResult() {
    var service = mock(MaxDrawdownService.class);
    var date = LocalDate.now();
    var maximumDrawdownMap = new TreeMap<>(
        Map.of(date, BigDecimalConstants.TWELVE, date.minusMonths(1), TEN, date.minusMonths(2), ONE));
    when(service.getSubMapByPeriodStartDate(any(), any())).thenReturn(maximumDrawdownMap);

    doCallRealMethod().when(service).getRecoveryTimeValue(any(), any(), any());
    Integer recoveryTimeValue = service.getRecoveryTimeValue(maximumDrawdownMap,
        maximumDrawdownMap.firstEntry(), maximumDrawdownMap.lastEntry());

    assertEquals(2, recoveryTimeValue);
  }

  @Test
  void shouldGetSubMapFromFirstKeyToCustomDate_whenCheckResult() {
    var service = mock(MaxDrawdownService.class);
    var date = LocalDate.now();
    var maximumDrawdownMap = new TreeMap<>(
        Map.of(date, BigDecimalConstants.TWELVE, date.minusMonths(1), TEN, date.minusMonths(2), ONE));
    doCallRealMethod().when(service).getSubMapFromFirstKeyToCustomDate(any(), any());

    NavigableMap<LocalDate, BigDecimal> result = service.getSubMapFromFirstKeyToCustomDate(maximumDrawdownMap,
        date.minusMonths(1));

    assertEquals(2, result.size());
  }

  @Test
  void shouldPerform_whenVerifyBuildPeriodCalculationInput() {
    var contextProvider = mock(PortfolioMonthlyReturnsContextProvider.class);
    var pipeline = mock(PortfolioWeightedAverageWithCpedPipeline.class);
    var service = new MaxDrawdownService(contextProvider, pipeline, new PeriodProperties());

    var req = mock(PeriodCommand.class);
    var monthlyReturnsContext = mock(MonthlyReturnsContext.class);
    when(contextProvider.get(req.getHoldings(), req.getCurrency(), Map.of()))
        .thenReturn(monthlyReturnsContext);

    TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>(
        Map.of(LocalDate.now(), BigDecimal.TEN));
    var result = new WeightedAverageResult<>(weightedAverageReturns, ReturnsSnapshot.empty());
    when(pipeline.run(monthlyReturnsContext, new CpedScaleParams(req.getCustomPed(), ReturnFactorScale.SCALE_OF_TWO)))
        .thenReturn(result);

    service.perform(req, PortfolioBenchmarkReturns.EMPTY);

    verify(pipeline).run(monthlyReturnsContext,
        new CpedScaleParams(req.getCustomPed(), ReturnFactorScale.SCALE_OF_TWO));
  }

  @Test
  void shouldAddInsufficientDataWarnings_whenGrowthCollapses_thenEmitsDegenerateGrowthDataWarning() {
    var endDate = LocalDate.of(2024, 12, 31);

    NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    for (int i = 11; i >= 0; i--) {
      returns.put(endDate.minusMonths(i), ONE);
    }

    // All-zero growth10K: seed month + 12 months, simulating a -100% return before the window
    NavigableMap<LocalDate, BigDecimal> growth10K = new TreeMap<>();
    for (int i = 12; i >= 0; i--) {
      growth10K.put(endDate.minusMonths(i), BigDecimal.ZERO);
    }

    var contextProvider = mock(PortfolioMonthlyReturnsContextProvider.class);
    var pipeline = mock(PortfolioWeightedAverageWithCpedPipeline.class);
    var service = new MaxDrawdownService(contextProvider, pipeline, new PeriodProperties());

    var command = mock(PeriodCommand.class);
    when(command.getPeriods()).thenReturn(Set.of(ONE_YR));

    var monthlyReturnsContext = mock(MonthlyReturnsContext.class);
    when(contextProvider.get(any(), any(), any())).thenReturn(monthlyReturnsContext);
    var weightedAverageResult = new WeightedAverageResult<>(returns, ReturnsSnapshot.empty());
    doReturn(weightedAverageResult).when(pipeline).run(any(), any());

    try (var mockedGrowth = Mockito.mockStatic(Growth10KHelper.class)) {
      mockedGrowth.when(() -> Growth10KHelper.compoundGrowth10K(any(), any()))
          .thenReturn(growth10K);

      MaxDrawdownResult result = service.perform(command, PortfolioBenchmarkReturns.EMPTY);

      assertEquals(1, result.getWarnings().size());
      assertEquals(ErrorCode.Codes.DEGENERATE_GROWTH_DATA, result.getWarnings().get(0).getCode());
    }
  }

  @Test
  void shouldPerform_whenCipsdBeforeAvailableRange_thenThrowsCipsdOutsideDataRangeError() {
    var firstKey = LocalDate.of(2024, 1, 31);
    NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    for (int i = 0; i < twelve; i++) {
      returns.put(firstKey.plusMonths(i).with(TemporalAdjusters.lastDayOfMonth()), ONE);
    }

    var contextProvider = mock(PortfolioMonthlyReturnsContextProvider.class);
    var pipeline = mock(PortfolioWeightedAverageWithCpedPipeline.class);
    var service = new MaxDrawdownService(contextProvider, pipeline, new PeriodProperties());

    var command = mock(PeriodCommand.class);
    // CIPSD is earlier than the earliest available month-end, so it is out of range.
    when(command.getCustomIntervalPsd()).thenReturn(LocalDate.of(2023, 1, 31));

    var monthlyReturnsContext = mock(MonthlyReturnsContext.class);
    when(contextProvider.get(any(), any(), any())).thenReturn(monthlyReturnsContext);
    doReturn(new WeightedAverageResult<>(returns, ReturnsSnapshot.empty())).when(pipeline).run(any(), any());

    CalculationException exception = assertThrows(CalculationException.class,
        () -> service.perform(command, PortfolioBenchmarkReturns.EMPTY));
    assertEquals(ErrorCode.CIPSD_OUTSIDE_DATA_RANGE_ERROR, exception.getErrorCode());
    // Assert the formatted message so a regression that drops a substituted CIPSD/range value is caught.
    assertEquals("CIPSD 2023-01-31 is outside the available monthly returns range [2024-01-31, 2024-12-31]",
        exception.getMessage());
  }

  @Test
  void shouldPerform_whenCipsdOnEarliestMonthEnd_thenComputesSinceCipsdWithoutError() {
    var firstKey = LocalDate.of(2024, 1, 31);
    NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    for (int i = 0; i < twelve; i++) {
      returns.put(firstKey.plusMonths(i).with(TemporalAdjusters.lastDayOfMonth()), ONE);
    }

    var contextProvider = mock(PortfolioMonthlyReturnsContextProvider.class);
    var pipeline = mock(PortfolioWeightedAverageWithCpedPipeline.class);
    var service = new MaxDrawdownService(contextProvider, pipeline, new PeriodProperties());

    var command = mock(PeriodCommand.class);

    // CIPSD exactly on the earliest available month-end is in range (inclusive), so no error is raised.
    when(command.getCustomIntervalPsd()).thenReturn(firstKey);

    var monthlyReturnsContext = mock(MonthlyReturnsContext.class);
    when(contextProvider.get(any(), any(), any())).thenReturn(monthlyReturnsContext);
    doReturn(new WeightedAverageResult<>(returns, ReturnsSnapshot.empty())).when(pipeline).run(any(), any());

    MaxDrawdownResult result = service.perform(command, PortfolioBenchmarkReturns.EMPTY);

    assertNotNull(result);
    assertEquals(firstKey, result.getCustomIntervalPerformanceStartDate());
    assertEquals(firstKey, result.getPerformanceStartDate());
    assertEquals(returns.lastKey(), result.getPerformanceEndDate());
    // Only SINCE_CIPSD was requested (via a supplied CIPSD), so exactly that one entry is produced and no
    // out-of-range error/warning is raised for an in-range CIPSD.
    assertEquals(1, result.getMaxDrawdown().size());
    assertEquals(TimePeriod.CIPSD.name(), result.getMaxDrawdown().get(0).period());
    assertTrue(result.getWarnings().isEmpty());
  }

  @Test
  void shouldReturnTwentyYearMaxDrawdown_whenPeriodIs240AndHistoryExceeds240Months() {
    NavigableMap<LocalDate, BigDecimal> returns = patternedFactorReturns(TWENTY_YR.getMonths() + ONE_YR.getMonths());
    var contextProvider = mock(PortfolioMonthlyReturnsContextProvider.class);
    var pipeline = mock(PortfolioWeightedAverageWithCpedPipeline.class);
    var service = new MaxDrawdownService(contextProvider, pipeline, new PeriodProperties());

    var command = mock(PeriodCommand.class);
    when(command.getPeriods()).thenReturn(Set.of(TWENTY_YR));
    when(contextProvider.get(any(), any(), any())).thenReturn(mock(MonthlyReturnsContext.class));
    doReturn(new WeightedAverageResult<>(returns, ReturnsSnapshot.empty())).when(pipeline).run(any(), any());

    MaxDrawdownResult result = service.perform(command, PortfolioBenchmarkReturns.EMPTY);

    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getPerformanceEndDate()).isEqualTo(returns.lastKey());
    // The recurring one-month decline gives a -0.6% peak-to-trough drawdown that recovers the following month.
    assertThat(result.getMaxDrawdown()).singleElement().satisfies(entry -> {
      assertThat(entry.period()).isEqualTo(TWENTY_YR.name());
      assertThat(entry.value()).isEqualByComparingTo("-0.006");
      assertThat(entry.recoveryTime()).isEqualTo(1);
    });
  }

  @Test
  void shouldIncludeTwentyYearPeriodByDefault_whenNoPeriodsRequested() {
    NavigableMap<LocalDate, BigDecimal> returns = patternedFactorReturns(TWENTY_YR.getMonths() + ONE_YR.getMonths());
    var contextProvider = mock(PortfolioMonthlyReturnsContextProvider.class);
    var pipeline = mock(PortfolioWeightedAverageWithCpedPipeline.class);
    PeriodProperties periods = new PeriodProperties();
    periods.setRiskCalculations(new HashSet<>(Set.of(ONE_YR, TEN_YR, TWENTY_YR)));
    var service = new MaxDrawdownService(contextProvider, pipeline, periods);

    var command = mock(PeriodCommand.class);
    when(command.getPeriods()).thenReturn(null);
    when(contextProvider.get(any(), any(), any())).thenReturn(mock(MonthlyReturnsContext.class));
    doReturn(new WeightedAverageResult<>(returns, ReturnsSnapshot.empty())).when(pipeline).run(any(), any());

    MaxDrawdownResult result = service.perform(command, PortfolioBenchmarkReturns.EMPTY);

    assertThat(result.getMaxDrawdown())
        .extracting(MaxDrawdownEntry::period)
        .contains(TEN_YR.name(), TWENTY_YR.name());
    assertThat(findEntry(result, TWENTY_YR).value()).isEqualByComparingTo("-0.006");
  }

  private static MaxDrawdownEntry findEntry(MaxDrawdownResult result, TimePeriod period) {
    return result.getMaxDrawdown().stream()
        .filter(entry -> period.name().equals(entry.period()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Missing period " + period));
  }

  // Percent-return cycle (base 1.0% + recurring deltas) already scaled to factor form (SCALE_OF_TWO), the shape the
  // pipeline hands MaxDrawdownService. The single -1.6pp month per cycle drives the -0.6% drawdown.
  private static final double[] DELTA_CYCLE = {0.0, -0.7, 0.9, -1.6, 1.4, -0.9, 0.3, 0.8, -1.2, 1.1, -0.5, 0.6};

  private static NavigableMap<LocalDate, BigDecimal> patternedFactorReturns(int count) {
    NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    LocalDate end = LocalDate.of(2024, 12, 31);
    for (int i = 0; i < count; i++) {
      LocalDate month = end.minusMonths((long) count - 1 - i).with(TemporalAdjusters.lastDayOfMonth());
      returns.put(month, BigDecimal.valueOf((101.0 + DELTA_CYCLE[i % DELTA_CYCLE.length]) / 100.0));
    }
    return returns;
  }
}
