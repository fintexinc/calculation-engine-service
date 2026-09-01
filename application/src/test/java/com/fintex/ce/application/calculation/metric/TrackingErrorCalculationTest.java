package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.result.risk.TrackingErrorResult;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.ce.util.DateTimeUtils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;
import static com.fintex.ce.model.util.BigDecimalConstants.TWO;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled("metric unsupported")
class TrackingErrorCalculationTest {

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    var calculation = mock(TrackingErrorCalculation.class);
    Map<String, BigDecimal> periods = Map.of("2010-01-01", ONE, "2020-01-01", TEN);

    Map<String, BigDecimal> expected = Map.of("2010-01-01", ONE, "2020-01-01", TEN);

    doCallRealMethod().when(calculation).defineResponseType(anyMap());
    TrackingErrorResult actual = calculation.defineResponseType(periods);

    assertEquals(expected, actual.getTrackingError());
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResult() {
    var calculation = mock(TrackingErrorCalculation.class);
    var benchmarkTotalReturns = mock(TreeMap.class);
    var portfolioTotalReturns = mock(TreeMap.class);

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
    var calculation = mock(TrackingErrorCalculation.class);
    var benchmarkTotalReturns = mock(TreeMap.class);
    var portfolioTotalReturns = mock(TreeMap.class);
    when(benchmarkTotalReturns.size()).thenReturn(25);
    when(portfolioTotalReturns.size()).thenReturn(25);

    when(calculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturns);
    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);
    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(-1);

    assertNull(actual);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetPeriodStartDate() {
    var calculation = mock(TrackingErrorCalculation.class);

    var benchmarkTotalReturns = mock(TreeMap.class);
    var portfolioTotalReturns = mock(TreeMap.class);
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
    var calculation = mock(TrackingErrorCalculation.class);
    var benchmarkTotalReturns = mock(TreeMap.class);
    var portfolioTotalReturns = mock(TreeMap.class);
    var periodStartDate = LocalDate.of(2020, 4, 10);

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
    var calculation = mock(TrackingErrorCalculation.class);
    var benchmarkTotalReturns = mock(TreeMap.class);
    var portfolioTotalReturns = mock(TreeMap.class);
    TreeMap<LocalDate, BigDecimal> subMapByPeriodStartDate = new TreeMap<>();
    LocalDate periodStartDate = LocalDate.of(2020, 4, 10);

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
    var calculation = mock(TrackingErrorCalculation.class);

    var benchmarkTotalReturns = mock(TreeMap.class);
    var portfolioTotalReturns = mock(TreeMap.class);
    when(benchmarkTotalReturns.size()).thenReturn(25);
    when(portfolioTotalReturns.size()).thenReturn(25);

    TreeMap<LocalDate, BigDecimal> subMapByPeriodStartDate = new TreeMap<>();
    var periodStartDate = LocalDate.of(2020, 4, 10);
    var averageExcessPortfolioReturns = mock(BigDecimal.class);

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
    var var = mock(TrackingErrorCalculation.class);

    var benchmarkTotalReturns = mock(TreeMap.class);
    var portfolioTotalReturns = mock(TreeMap.class);
    when(benchmarkTotalReturns.size()).thenReturn(25);
    when(portfolioTotalReturns.size()).thenReturn(25);

    TreeMap<LocalDate, BigDecimal> subMapByPeriodStartDate = new TreeMap<>();
    var periodStartDate = LocalDate.of(2020, 4, 10);
    var averageExcessPortfolioReturns = mock(BigDecimal.class);
    var diff = mock(TreeMap.class);

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
    var calculation = mock(TrackingErrorCalculation.class);
    var subMapByPeriodStartDate = new TreeMap();

    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(2), TWO);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(3), TEN);

    doCallRealMethod().when(calculation).calculateAverageByPeriod(subMapByPeriodStartDate);
    BigDecimal actual = calculation.calculateAverageByPeriod(subMapByPeriodStartDate);

    assertEquals(6.0, actual.doubleValue());
  }

  @Test
  void shouldCalculateAverageExcessPortfolioReturnsByPeriod_whenCheckResult1() {
    var calculation = mock(TrackingErrorCalculation.class);
    var subMapByPeriodStartDate = new TreeMap();

    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(10), TWELVE);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(1), TEN);

    doCallRealMethod().when(calculation)
        .calculateAverageByPeriod(subMapByPeriodStartDate);
    BigDecimal actual = calculation
        .calculateAverageByPeriod(subMapByPeriodStartDate);

    assertEquals(11.0, actual.doubleValue());
  }

  @Test
  void shouldCalculateAverageExcessPortfolioReturnsByPeriod_whenCheckResult3() {
    var calculation = mock(TrackingErrorCalculation.class);
    var subMapByPeriodStartDate = new TreeMap();

    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(3), ZERO);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(5), TWO);

    doCallRealMethod().when(calculation)
        .calculateAverageByPeriod(subMapByPeriodStartDate);
    BigDecimal actual = calculation
        .calculateAverageByPeriod(subMapByPeriodStartDate);

    assertEquals(1.000000000000000, actual.doubleValue());
  }

  @Test
  void shouldCalculateTrackingError_whenCheckResult() {
    var calculation = mock(TrackingErrorCalculation.class);
    TreeMap<LocalDate, BigDecimal> subMapByPeriodStartDate = new TreeMap<>();

    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(3), ZERO);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(4), TWELVE);

    doCallRealMethod().when(calculation)
        .calculateTrackingError(25, subMapByPeriodStartDate);
    BigDecimal actual = calculation
        .calculateTrackingError(25, subMapByPeriodStartDate);

    assertEquals(BigDecimal.valueOf(2.4494897428), actual);
  }

  @Test
  void shouldCalculateTrackingError_whenCheckResult1() {
    var calculation = mock(TrackingErrorCalculation.class);
    var subMapByPeriodStartDate = new TreeMap();

    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(11), TEN);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(4), TWELVE);

    doCallRealMethod().when(calculation).calculateTrackingError(60, subMapByPeriodStartDate);
    BigDecimal actual = calculation.calculateTrackingError(60, subMapByPeriodStartDate);

    assertEquals(BigDecimal.valueOf(2.1153194253), actual);
  }

  @Test
  void shouldCalculateDiffExcessPortfolioAndAVGExcessPortfolio_whenCheckResult() {
    var calculation = mock(TrackingErrorCalculation.class);

    var subMapByPeriodStartDate = new TreeMap();
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(5), TEN);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(5), TWELVE);

    var expected = new TreeMap();
    expected.put(LocalDate.now().minusMonths(5), BigDecimal.valueOf(64.0));
    expected.put(LocalDate.now().minusMonths(5), BigDecimal.valueOf(100.0));

    doCallRealMethod().when(calculation).calculateDiffPortfolioAndAVGPortfolio(subMapByPeriodStartDate, TWO);
    TreeMap<LocalDate, BigDecimal> actual = calculation.calculateDiffPortfolioAndAVGPortfolio(
        subMapByPeriodStartDate,
        TWO);

    assertEquals(expected, actual);
  }

  @Test
  void shouldCalculateDiffExcessPortfolioAndAVGExcessPortfolio_whenCheckResult2() {
    var calculation = mock(TrackingErrorCalculation.class);

    var subMapByPeriodStartDate = new TreeMap();
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(15), ZERO);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(11), TEN);

    var expected = new TreeMap();
    expected.put(LocalDate.now().minusMonths(15), BigDecimal.valueOf(1.0));
    expected.put(LocalDate.now().minusMonths(11), BigDecimal.valueOf(81.0));

    doCallRealMethod().when(calculation).calculateDiffPortfolioAndAVGPortfolio(subMapByPeriodStartDate, ONE);
    var actual = calculation.calculateDiffPortfolioAndAVGPortfolio(subMapByPeriodStartDate, ONE);

    assertEquals(expected, actual);
  }

  @Test
  void shouldCalculateDiffExcessPortfolioAndAVGExcessPortfolio_whenCheckResult3() {
    var calculation = mock(TrackingErrorCalculation.class);

    var subMapByPeriodStartDate = new TreeMap();
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(1), TWO);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(2), ZERO);

    var expected = new TreeMap();
    expected.put(LocalDate.now().minusMonths(1), BigDecimal.valueOf(64.0));
    expected.put(LocalDate.now().minusMonths(2), BigDecimal.valueOf(100.0));

    doCallRealMethod().when(calculation).calculateDiffPortfolioAndAVGPortfolio(subMapByPeriodStartDate, TEN);
    var actual = calculation.calculateDiffPortfolioAndAVGPortfolio(subMapByPeriodStartDate, TEN);

    assertEquals(expected, actual);
  }

  @Test
  void shouldCalculateExcessPortfolioReturnOverBenchmark_whenCheckResult() {
    var calculation = mock(TrackingErrorCalculation.class);

    TreeMap<LocalDate, BigDecimal> portfolioTotalReturns = new TreeMap<>();
    portfolioTotalReturns.put(LocalDate.now().minusMonths(2), TEN);
    portfolioTotalReturns.put(LocalDate.now().minusMonths(6), TEN);

    TreeMap<LocalDate, BigDecimal> benchmarkTotalReturns = new TreeMap<>();
    benchmarkTotalReturns.put(LocalDate.now().minusMonths(2), ONE);
    benchmarkTotalReturns.put(LocalDate.now().minusMonths(6), ZERO);

    TreeMap<LocalDate, BigDecimal> expected = new TreeMap<>();
    expected.put(LocalDate.now().minusMonths(2), BigDecimal.valueOf(9));
    expected.put(LocalDate.now().minusMonths(6), TEN);

    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);
    when(calculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturns);

    doCallRealMethod().when(calculation).calculateExcessPortfolioReturnOverBenchmark();
    NavigableMap<LocalDate, BigDecimal> actual = calculation.calculateExcessPortfolioReturnOverBenchmark();

    assertEquals(expected, actual);
  }

  @Test
  void shouldThrowMissingBenchmarkReturn_whenPortfolioAndBenchmarkDatesAreShifted() {
    LocalDate missingDate = LocalDate.parse("2024-01-31");
    TreeMap<LocalDate, BigDecimal> portfolioReturns = monthlyReturns(missingDate, 12, TEN);
    TreeMap<LocalDate, BigDecimal> benchmarkReturns = monthlyReturns(LocalDate.parse("2024-02-29"), 12, ONE);

    TrackingErrorCalculation calculation = new TrackingErrorCalculation(benchmarkInput(portfolioReturns,
        benchmarkReturns), Set.of(ONE_YR));

    assertThat(calculation.portfolioReturnOverBenchmark).doesNotContainKey(missingDate);
    assertThatThrownBy(() -> calculation.calculatePeriodForNumberOfMonths(12))
        .isInstanceOfSatisfying(CalculationException.class,
            exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSING_BENCHMARK_RETURN_FOR_DATE));
  }

  @Test
  void shouldThrowMissingBenchmarkReturn_whenBenchmarkWindowHasDateGap() {
    LocalDate periodStartDate = LocalDate.parse("2024-01-31");
    LocalDate gapDate = LocalDate.parse("2024-06-30");
    TreeMap<LocalDate, BigDecimal> portfolioReturns = monthlyReturns(periodStartDate, 12, TEN);
    TreeMap<LocalDate, BigDecimal> benchmarkReturns = monthlyReturns(periodStartDate, 12, ONE);
    benchmarkReturns.remove(gapDate);
    benchmarkReturns.put(LocalDate.parse("2023-12-31"), ONE);

    TrackingErrorCalculation calculation = new TrackingErrorCalculation(benchmarkInput(portfolioReturns,
        benchmarkReturns), Set.of(ONE_YR));

    assertThat(calculation.portfolioReturnOverBenchmark).doesNotContainKey(gapDate);
    assertThatThrownBy(() -> calculation.calculatePeriodForNumberOfMonths(12))
        .isInstanceOfSatisfying(CalculationException.class,
            exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSING_BENCHMARK_RETURN_FOR_DATE));
  }

  @Test
  void shouldCalculateExcessPortfolioReturnOverBenchmark_whenCheckResult1() {
    var calculation = mock(TrackingErrorCalculation.class);

    TreeMap<LocalDate, BigDecimal> portfolioTotalReturns = new TreeMap<>();
    portfolioTotalReturns.put(LocalDate.now().minusMonths(6), ZERO);
    portfolioTotalReturns.put(LocalDate.now().minusMonths(8), TWO);

    TreeMap<LocalDate, BigDecimal> benchmarkTotalReturns = new TreeMap<>();
    benchmarkTotalReturns.put(LocalDate.now().minusMonths(6), TEN);
    benchmarkTotalReturns.put(LocalDate.now().minusMonths(8), ZERO);

    TreeMap<LocalDate, BigDecimal> expected = new TreeMap<>();
    expected.put(LocalDate.now().minusMonths(6), BigDecimal.valueOf(-10));
    expected.put(LocalDate.now().minusMonths(8), TWO);

    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);
    when(calculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturns);

    doCallRealMethod().when(calculation).calculateExcessPortfolioReturnOverBenchmark();
    NavigableMap<LocalDate, BigDecimal> actual = calculation.calculateExcessPortfolioReturnOverBenchmark();

    assertEquals(expected, actual);
  }

  private static BenchmarkPeriodCalculationInput benchmarkInput(
      NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      NavigableMap<LocalDate, BigDecimal> benchmarkReturns) {
    BenchmarkPeriodCalculationInput input = new BenchmarkPeriodCalculationInput();
    input.setWeightedAveragePortfolioReturns(portfolioReturns);
    input.setWeightedAverageBenchmarkReturns(benchmarkReturns);
    return input;
  }

  private static TreeMap<LocalDate, BigDecimal> monthlyReturns(LocalDate startDate, int months, BigDecimal value) {
    return IntStream.range(0, months)
        .mapToObj(startDate::plusMonths)
        .collect(Collectors.toMap(DateTimeUtils::toLastDayOfMonth, date -> value, (left, right) -> right,
            TreeMap::new));
  }

}
