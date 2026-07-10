package com.fintex.ce.application.calculation.metric.core;

import com.fintex.ce.application.returns.ReturnsRole;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class RSquaredCalculationAbstractTest {

  final int TWELVE = 12;
  private final LocalDate today = LocalDate.now();

  @Test
  void shouldThrowMissingTBillRate_whenPortfolioExcessReturnDoesNotCoverPortfolioWindow() {
    final var calculation = buildCalculationMissingCoverageOn(ReturnsRole.PORTFOLIO);

    final CalculationException ex = assertThrows(CalculationException.class,
        () -> calculation.calculatePeriodForNumberOfMonths(TWELVE));
    assertEquals(ErrorCode.MISSING_TBILL_RATE, ex.getErrorCode());
    assertEquals("Missing T-Bill rate for date " + today, ex.getMessage());
    assertEquals(Map.of("param-1", today), ex.getMetadata());
  }

  @Test
  void shouldThrowMissingTBillRate_whenBenchmarkExcessReturnDoesNotCoverPortfolioWindow() {
    final var calculation = buildCalculationMissingCoverageOn(ReturnsRole.BENCHMARK);

    final CalculationException ex = assertThrows(CalculationException.class,
        () -> calculation.calculatePeriodForNumberOfMonths(TWELVE));
    assertEquals(ErrorCode.MISSING_TBILL_RATE, ex.getErrorCode());
    assertEquals("Missing T-Bill rate for date " + today, ex.getMessage());
    assertEquals(Map.of("param-1", today), ex.getMetadata());
  }

  /**
   * Builds a real (non-mocked-construction) {@link RSquaredCalculationAbstract} whose portfolio and benchmark total
   * return series both fully cover a 12-month window, but whose excess-return series on the given dimension is missing
   * the most recent month inside that window — the per-date coverage precondition enforced by
   * {@link com.fintex.ce.application.util.RiskFreeWindowValidator} must throw {@code MISSING_TBILL_RATE}.
   */
  private RSquaredCalculationAbstract buildCalculationMissingCoverageOn(final ReturnsRole dimension) {
    final NavigableMap<LocalDate, BigDecimal> portfolioReturns = new TreeMap<>();
    final NavigableMap<LocalDate, BigDecimal> benchmarkReturns = new TreeMap<>();
    for (int i = 0; i < TWELVE; i++) {
      portfolioReturns.put(today.minusMonths(i), ONE);
      benchmarkReturns.put(today.minusMonths(i), ONE);
    }
    final NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn = new TreeMap<>();
    final NavigableMap<LocalDate, BigDecimal> benchmarkExcessReturn = new TreeMap<>();
    for (int i = 1; i < TWELVE; i++) {
      portfolioExcessReturn.put(today.minusMonths(i), ONE);
      benchmarkExcessReturn.put(today.minusMonths(i), ONE);
    }
    if (dimension == ReturnsRole.PORTFOLIO) {
      benchmarkExcessReturn.put(today, ONE);
    } else {
      portfolioExcessReturn.put(today, ONE);
    }

    final var input = mock(BenchmarkPeriodCalculationInput.class);
    when(input.getWeightedAveragePortfolioReturns()).thenReturn(portfolioReturns);
    when(input.getWeightedAverageBenchmarkReturns()).thenReturn(benchmarkReturns);

    final var calculation = mock(RSquaredCalculationAbstract.class,
        withSettings().useConstructor(input, Set.of(), portfolioExcessReturn, benchmarkExcessReturn));

    doCallRealMethod().when(calculation).getPortfolioTotalReturns();
    doCallRealMethod().when(calculation).getBenchmarkTotalReturns();
    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    doCallRealMethod().when(calculation).getPeriodStartDate(anyInt(), any());
    doCallRealMethod().when(calculation).getSubMapByPeriodStartDate(any(), any());
    return calculation;
  }

  @Test
  void shouldResolvePeriodStartDate_whenCalculatingPeriod() {
    final var calculation = mock(RSquaredCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    calculation.portfolioExcessReturn = treeMap;
    calculation.benchmarkExcessReturn = treeMap;

    when(calculation.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(treeMap.size()).thenReturn(TWELVE);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(calculation).getPeriodStartDate(12, treeMap);
  }

  @Test
  void shouldGetExcessReturnSubMaps_whenCalculatingPeriod() {
    final var calculation = mock(RSquaredCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    final var periodStartDate = today;

    calculation.benchmarkExcessReturn = treeMap;
    calculation.portfolioExcessReturn = treeMap;

    when(calculation.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    // Three calls: portfolio window (validation), portfolio excess return submap, benchmark excess return submap.
    verify(calculation, times(3)).getSubMapByPeriodStartDate(periodStartDate, treeMap);
  }

  @Test
  void shouldCalculateRSquared_whenCalculatingPeriod() {
    final var calculation = mock(RSquaredCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    calculation.portfolioExcessReturn = treeMap;
    calculation.benchmarkExcessReturn = treeMap;

    when(calculation.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    doCallRealMethod().when(calculation).calculatePeriod(any(), any(), any());
    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(calculation).calculateRSquared(eq(treeMap), eq(treeMap), any());
  }

  @Test
  void shouldReturnCalculatedRSquared_whenInputDataIsValid() {
    final var calculation = mock(RSquaredCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    calculation.portfolioExcessReturn = treeMap;
    calculation.benchmarkExcessReturn = treeMap;

    when(calculation.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(calculation.calculateRSquared(any(), any(), any())).thenReturn(TEN);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    doCallRealMethod().when(calculation).calculatePeriod(any(), any(), any());
    final BigDecimal result = calculation.calculatePeriodForNumberOfMonths(TWELVE);

    assertEquals(TEN, result);
  }

  @Test
  void shouldReturnNull_whenBenchmarkReturnsSizeIsLessThanPeriod() {
    final var calculation = mock(RSquaredCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);

    when(calculation.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    when(calculation.calculateRSquared(any(), any(), any())).thenReturn(TEN);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal result = calculation.calculatePeriodForNumberOfMonths(24);

    assertNull(result);
  }

  @Test
  void shouldReturnNull_whenPeriodIsLessThanTwelve() {
    final var calculation = mock(RSquaredCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    calculation.portfolioExcessReturn = treeMap;
    calculation.benchmarkExcessReturn = treeMap;

    when(calculation.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(calculation.calculateRSquared(any(), any(), any())).thenReturn(TEN);
    when(treeMap.size()).thenReturn(TWELVE);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal result = calculation.calculatePeriodForNumberOfMonths(6);

    assertNull(result);
  }

  @Test
  void shouldUseSumSquaredRegression_whenCalculatingRSquared() {
    final var calculation = mock(RSquaredCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    final var bigDecimal = mock(BigDecimal.class);

    when(calculation.calculateSumSquaredRegression(any(), any())).thenReturn(ONE);
    when(calculation.calculateTotalSumOfSquares(any(), any())).thenReturn(ONE);

    doCallRealMethod().when(calculation).calculateRSquared(any(), any(), any());
    calculation.calculateRSquared(treeMap, treeMap, bigDecimal);

    verify(calculation).calculateSumSquaredRegression(treeMap, treeMap);
  }

  @Test
  void shouldUseTotalSumOfSquares_whenCalculatingRSquared() {
    final var calculation = mock(RSquaredCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    final var bigDecimal = mock(BigDecimal.class);

    when(calculation.calculateSumSquaredRegression(any(), any())).thenReturn(ONE);
    when(calculation.calculateTotalSumOfSquares(any(), any())).thenReturn(ONE);

    doCallRealMethod().when(calculation).calculateRSquared(any(), any(), any());
    calculation.calculateRSquared(treeMap, treeMap, bigDecimal);

    verify(calculation).calculateTotalSumOfSquares(treeMap, bigDecimal);
  }

  @Test
  void shouldReturnRSquaredValue_whenRegressionAndTotalSumProvided() {
    final var calculation = mock(RSquaredCalculationAbstract.class);
    final var treeMap = mock(TreeMap.class);
    final var bigDecimal = mock(BigDecimal.class);

    when(calculation.calculateSumSquaredRegression(any(), any())).thenReturn(BigDecimal.valueOf(1.01094319080371));
    when(calculation.calculateTotalSumOfSquares(any(), any())).thenReturn(BigDecimal.valueOf(0.994895485347306));

    doCallRealMethod().when(calculation).calculateRSquared(any(), any(), any());
    final BigDecimal result = calculation.calculateRSquared(treeMap, treeMap, bigDecimal);

    assertEquals(toUserScale(BigDecimal.valueOf(-0.0161300415)), result);
  }

  @Test
  void shouldCalculateSumSquaredRegression_whenReturnsProvided() {
    final var calculation = mock(RSquaredCalculationAbstract.class);

    final var portfolioExcessReturnByPeriod = new TreeMap<>(Map.of(today, BigDecimal.valueOf(
        1.01222986673534)));
    final var benchmarkExcessReturnByPeriod = new TreeMap<>(Map.of(today, BigDecimal.valueOf(
        0.994895485347306)));

    final var portfolioExcessAverage = BigDecimal.valueOf(0.004475946208333);
    final var benchmarkExcessAverage = BigDecimal.valueOf(0.007504533222917);

    doCallRealMethod().when(calculation).calculateSumSquaredRegression(any(), any());
    final BigDecimal result = calculation.calculateSumSquaredRegression(portfolioExcessReturnByPeriod,
        benchmarkExcessReturnByPeriod);

    assertEquals(toUserScale(BigDecimal.valueOf(0.0003004808)), toUserScale(result));
  }

  @Test
  void shouldCalculateTotalSumOfSquares_whenReturnsProvided() {
    final var calculation = mock(RSquaredCalculationAbstract.class);

    final var benchmarkExcessReturnByPeriod = new TreeMap<>(Map.of(today, BigDecimal.valueOf(
        0.994895485347306)));
    final var benchmarkExcessAverage = BigDecimal.valueOf(0.007504533222917);

    doCallRealMethod().when(calculation).calculateTotalSumOfSquares(any(), any());
    final BigDecimal result = calculation.calculateTotalSumOfSquares(benchmarkExcessReturnByPeriod,
        benchmarkExcessAverage);

    assertEquals(toUserScale(BigDecimal.valueOf(0.974940892337108)), toUserScale(result));
  }

  @Test
  void shouldOverrideTotalReturnsToMonthlyChange_whenTotalReturnsProvided() {
    final var calculation = mock(RSquaredCalculationAbstract.class);
    final var date = LocalDate.of(2020, 12, 1);
    final var portfolioTotalReturns = new TreeMap<>(Map.of(toLastDayOfMonth(date), BigDecimal.valueOf(1.01094319080371),
        toLastDayOfMonth(date.minusMonths(1)), BigDecimal.valueOf(1.02297440154456)));

    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);

    doCallRealMethod().when(calculation).overrideTotalReturns(any());
    final NavigableMap<LocalDate, BigDecimal> totalReturns = calculation.overrideTotalReturns(portfolioTotalReturns);

    assertEquals(2, totalReturns.size());
    assertEquals(toUserScale(BigDecimal.valueOf(0.02297440154456)), toUserScale(totalReturns.firstEntry().getValue()));
    assertEquals(toUserScale(BigDecimal.valueOf(0.01094319080371)), toUserScale(totalReturns.lastEntry().getValue()));
  }

  private TreeMap<LocalDate, BigDecimal> getReturns() {
    final var date = LocalDate.of(2020, 12, 1);
    final Map<LocalDate, BigDecimal> map = new HashMap<>();
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
