package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.TrackingErrorResult;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;
import static com.fintex.ce.model.util.BigDecimalConstants.TWO;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrackingErrorCalculationTest {

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    final var calculation = mock(TrackingErrorCalculation.class);
    final var pairs = Set.of(
        Pair.of("2010-01-01", ONE),
        Pair.of("2020-01-01", TEN));

    final var interval1 = new TimeIntervalResult("2010-01-01", ONE);
    final var interval2 = new TimeIntervalResult("2020-01-01", TEN);
    final var expected = Set.of(interval2, interval1);

    doCallRealMethod().when(calculation).defineResponseType(anySet());
    final TrackingErrorResult actual = calculation.defineResponseType(pairs);

    assertEquals(expected, actual.getTrackingError());
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResult() {
    final var calculation = mock(TrackingErrorCalculation.class);
    final var benchmarkTotalReturns = mock(TreeMap.class);
    final var portfolioTotalReturns = mock(TreeMap.class);

    when(benchmarkTotalReturns.size()).thenReturn(1);
    when(portfolioTotalReturns.size()).thenReturn(1);

    when(calculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturns);
    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);
    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());

    BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(2);

    assertNull(actual);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResult1() {
    final var calculation = mock(TrackingErrorCalculation.class);
    final var benchmarkTotalReturns = mock(TreeMap.class);
    final var portfolioTotalReturns = mock(TreeMap.class);
    when(benchmarkTotalReturns.size()).thenReturn(25);
    when(portfolioTotalReturns.size()).thenReturn(25);

    when(calculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturns);
    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);
    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(-1);

    assertNull(actual);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetPeriodStartDate() {
    final var calculation = mock(TrackingErrorCalculation.class);

    final var benchmarkTotalReturns = mock(TreeMap.class);
    final var portfolioTotalReturns = mock(TreeMap.class);
    when(benchmarkTotalReturns.size()).thenReturn(25);
    when(portfolioTotalReturns.size()).thenReturn(25);

    calculation.portfolioReturnOverBenchmark = new TreeMap<>();

    when(calculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturns);
    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(24);

    verify(calculation).getPeriodStartDate(eq(24), argThat(
        argument -> argument == calculation.portfolioReturnOverBenchmark));
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetSubMap() {
    final var calculation = mock(TrackingErrorCalculation.class);
    final var benchmarkTotalReturns = mock(TreeMap.class);
    final var portfolioTotalReturns = mock(TreeMap.class);
    final var periodStartDate = LocalDate.of(2020, 4, 10);

    when(benchmarkTotalReturns.size()).thenReturn(25);
    when(portfolioTotalReturns.size()).thenReturn(25);

    when(calculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturns);
    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);

    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(24);

    verify(calculation).getSubMapByPeriodStartDate(eq(periodStartDate), argThat(
        argument -> argument == calculation.portfolioReturnOverBenchmark));
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculateAverageExcessPortfolioReturnsByPeriod() {
    final var calculation = mock(TrackingErrorCalculation.class);
    final var benchmarkTotalReturns = mock(TreeMap.class);
    final var portfolioTotalReturns = mock(TreeMap.class);
    final TreeMap<LocalDate, BigDecimal> subMapByPeriodStartDate = new TreeMap<>();
    final LocalDate periodStartDate = LocalDate.of(2020, 4, 10);

    when(benchmarkTotalReturns.size()).thenReturn(25);
    when(portfolioTotalReturns.size()).thenReturn(25);

    when(calculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturns);
    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);

    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(subMapByPeriodStartDate);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(24);

    verify(calculation).calculateAverageByPeriod(subMapByPeriodStartDate);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculateDiffExcessPortfolioAndAVGExcessPortfolio() {
    final var calculation = mock(TrackingErrorCalculation.class);

    final var benchmarkTotalReturns = mock(TreeMap.class);
    final var portfolioTotalReturns = mock(TreeMap.class);
    when(benchmarkTotalReturns.size()).thenReturn(25);
    when(portfolioTotalReturns.size()).thenReturn(25);

    final TreeMap<LocalDate, BigDecimal> subMapByPeriodStartDate = new TreeMap<>();
    final var periodStartDate = LocalDate.of(2020, 4, 10);
    final var averageExcessPortfolioReturns = mock(BigDecimal.class);

    when(calculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturns);
    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);

    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(subMapByPeriodStartDate);
    when(calculation.calculateAverageByPeriod(any())).thenReturn(averageExcessPortfolioReturns);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(24);

    verify(calculation).calculateAverageByPeriod(subMapByPeriodStartDate);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculateTrackingError() {
    final var var = mock(TrackingErrorCalculation.class);

    final var benchmarkTotalReturns = mock(TreeMap.class);
    final var portfolioTotalReturns = mock(TreeMap.class);
    when(benchmarkTotalReturns.size()).thenReturn(25);
    when(portfolioTotalReturns.size()).thenReturn(25);

    final TreeMap<LocalDate, BigDecimal> subMapByPeriodStartDate = new TreeMap<>();
    final var periodStartDate = LocalDate.of(2020, 4, 10);
    final var averageExcessPortfolioReturns = mock(BigDecimal.class);
    final var diff = mock(TreeMap.class);

    when(var.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturns);
    when(var.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);

    when(var.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);
    when(var.getSubMapByPeriodStartDate(any(), any())).thenReturn(subMapByPeriodStartDate);
    when(var.calculateAverageByPeriod(any())).thenReturn(averageExcessPortfolioReturns);
    when(var.calculateDiffPortfolioAndAVGPortfolio(any(), any())).thenReturn(diff);

    doCallRealMethod().when(var).calculatePeriodForNumberOfMonths(anyInt());
    var.calculatePeriodForNumberOfMonths(24);

    verify(var).calculateTrackingError(24, diff);
  }

  @Test
  void shouldCalculateAverageExcessPortfolioReturnsByPeriod_whenCheckResult() {
    final var calculation = mock(TrackingErrorCalculation.class);
    final var subMapByPeriodStartDate = new TreeMap();

    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(2), TWO);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(3), TEN);

    doCallRealMethod().when(calculation).calculateAverageByPeriod(subMapByPeriodStartDate);
    final BigDecimal actual = calculation.calculateAverageByPeriod(subMapByPeriodStartDate);

    assertEquals(6.0, actual.doubleValue());
  }

  @Test
  void shouldCalculateAverageExcessPortfolioReturnsByPeriod_whenCheckResult1() {
    final var calculation = mock(TrackingErrorCalculation.class);
    final var subMapByPeriodStartDate = new TreeMap();

    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(10), TWELVE);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(1), TEN);

    doCallRealMethod().when(calculation)
        .calculateAverageByPeriod(subMapByPeriodStartDate);
    final BigDecimal actual = calculation
        .calculateAverageByPeriod(subMapByPeriodStartDate);

    assertEquals(11.0, actual.doubleValue());
  }

  @Test
  void shouldCalculateAverageExcessPortfolioReturnsByPeriod_whenCheckResult3() {
    final var calculation = mock(TrackingErrorCalculation.class);
    final var subMapByPeriodStartDate = new TreeMap();

    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(3), ZERO);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(5), TWO);

    doCallRealMethod().when(calculation)
        .calculateAverageByPeriod(subMapByPeriodStartDate);
    final BigDecimal actual = calculation
        .calculateAverageByPeriod(subMapByPeriodStartDate);

    assertEquals(1.000000000000000, actual.doubleValue());
  }

  @Test
  void shouldCalculateTrackingError_whenCheckResult() {
    final var calculation = mock(TrackingErrorCalculation.class);
    final TreeMap<LocalDate, BigDecimal> subMapByPeriodStartDate = new TreeMap<>();

    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(3), ZERO);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(4), TWELVE);

    doCallRealMethod().when(calculation)
        .calculateTrackingError(25, subMapByPeriodStartDate);
    final BigDecimal actual = calculation
        .calculateTrackingError(25, subMapByPeriodStartDate);

    assertEquals(BigDecimal.valueOf(2.4494897428), actual);
  }

  @Test
  void shouldCalculateTrackingError_whenCheckResult1() {
    final var calculation = mock(TrackingErrorCalculation.class);
    final var subMapByPeriodStartDate = new TreeMap();

    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(11), TEN);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(4), TWELVE);

    doCallRealMethod().when(calculation).calculateTrackingError(60, subMapByPeriodStartDate);
    final BigDecimal actual = calculation.calculateTrackingError(60, subMapByPeriodStartDate);

    assertEquals(BigDecimal.valueOf(2.1153194253), actual);
  }

  @Test
  void shouldCalculateDiffExcessPortfolioAndAVGExcessPortfolio_whenCheckResult() {
    final var calculation = mock(TrackingErrorCalculation.class);

    final var subMapByPeriodStartDate = new TreeMap();
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(5), TEN);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(5), TWELVE);

    final var expected = new TreeMap();
    expected.put(LocalDate.now().minusMonths(5), BigDecimal.valueOf(64.0));
    expected.put(LocalDate.now().minusMonths(5), BigDecimal.valueOf(100.0));

    doCallRealMethod().when(calculation).calculateDiffPortfolioAndAVGPortfolio(subMapByPeriodStartDate, TWO);
    final TreeMap<LocalDate, BigDecimal> actual = calculation.calculateDiffPortfolioAndAVGPortfolio(
        subMapByPeriodStartDate,
        TWO);

    assertEquals(expected, actual);
  }

  @Test
  void shouldCalculateDiffExcessPortfolioAndAVGExcessPortfolio_whenCheckResult2() {
    final var calculation = mock(TrackingErrorCalculation.class);

    final var subMapByPeriodStartDate = new TreeMap();
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(15), ZERO);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(11), TEN);

    final var expected = new TreeMap();
    expected.put(LocalDate.now().minusMonths(15), BigDecimal.valueOf(1.0));
    expected.put(LocalDate.now().minusMonths(11), BigDecimal.valueOf(81.0));

    doCallRealMethod().when(calculation).calculateDiffPortfolioAndAVGPortfolio(subMapByPeriodStartDate, ONE);
    final var actual = calculation.calculateDiffPortfolioAndAVGPortfolio(subMapByPeriodStartDate, ONE);

    assertEquals(expected, actual);
  }

  @Test
  void shouldCalculateDiffExcessPortfolioAndAVGExcessPortfolio_whenCheckResult3() {
    final var calculation = mock(TrackingErrorCalculation.class);

    final var subMapByPeriodStartDate = new TreeMap();
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(1), TWO);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(2), ZERO);

    final var expected = new TreeMap();
    expected.put(LocalDate.now().minusMonths(1), BigDecimal.valueOf(64.0));
    expected.put(LocalDate.now().minusMonths(2), BigDecimal.valueOf(100.0));

    doCallRealMethod().when(calculation).calculateDiffPortfolioAndAVGPortfolio(subMapByPeriodStartDate, TEN);
    final var actual = calculation.calculateDiffPortfolioAndAVGPortfolio(subMapByPeriodStartDate, TEN);

    assertEquals(expected, actual);
  }

  @Test
  void shouldCalculateExcessPortfolioReturnOverBenchmark_whenCheckResult() {
    final var calculation = mock(TrackingErrorCalculation.class);

    final TreeMap<LocalDate, BigDecimal> portfolioTotalReturns = new TreeMap<>();
    portfolioTotalReturns.put(LocalDate.now().minusMonths(2), TEN);
    portfolioTotalReturns.put(LocalDate.now().minusMonths(6), TEN);

    final TreeMap<LocalDate, BigDecimal> benchmarkTotalReturns = new TreeMap<>();
    benchmarkTotalReturns.put(LocalDate.now().minusMonths(2), ONE);
    benchmarkTotalReturns.put(LocalDate.now().minusMonths(6), ZERO);

    final TreeMap<LocalDate, BigDecimal> expected = new TreeMap<>();
    expected.put(LocalDate.now().minusMonths(2), BigDecimal.valueOf(9));
    expected.put(LocalDate.now().minusMonths(6), TEN);

    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);
    when(calculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturns);

    doCallRealMethod().when(calculation).calculateExcessPortfolioReturnOverBenchmark();
    NavigableMap<LocalDate, BigDecimal> actual = calculation.calculateExcessPortfolioReturnOverBenchmark();

    assertEquals(expected, actual);
  }

  @Test
  void shouldCalculateExcessPortfolioReturnOverBenchmark_whenCheckResult1() {
    final var calculation = mock(TrackingErrorCalculation.class);

    final TreeMap<LocalDate, BigDecimal> portfolioTotalReturns = new TreeMap<>();
    portfolioTotalReturns.put(LocalDate.now().minusMonths(6), ZERO);
    portfolioTotalReturns.put(LocalDate.now().minusMonths(8), TWO);

    final TreeMap<LocalDate, BigDecimal> benchmarkTotalReturns = new TreeMap<>();
    benchmarkTotalReturns.put(LocalDate.now().minusMonths(6), TEN);
    benchmarkTotalReturns.put(LocalDate.now().minusMonths(8), ZERO);

    final TreeMap<LocalDate, BigDecimal> expected = new TreeMap<>();
    expected.put(LocalDate.now().minusMonths(6), BigDecimal.valueOf(-10));
    expected.put(LocalDate.now().minusMonths(8), TWO);

    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);
    when(calculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturns);

    doCallRealMethod().when(calculation).calculateExcessPortfolioReturnOverBenchmark();
    final NavigableMap<LocalDate, BigDecimal> actual = calculation.calculateExcessPortfolioReturnOverBenchmark();

    assertEquals(expected, actual);
  }

}