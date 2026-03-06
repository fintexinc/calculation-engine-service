package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.TrackingErrorCalculation;
import com.fintex.ce.port.input.result.TrackingErrorResult;
import com.fintex.ce.port.input.result.core.TimeIntervalResult;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.domain.constant.BigDecimalConstants.ONE;
import static com.fintex.ce.domain.constant.BigDecimalConstants.TWELVE;
import static com.fintex.ce.domain.constant.BigDecimalConstants.TWO;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

class TrackingErrorCalculationTest {

  @Test
  void defineResponseType_checkResult() {
    final var sut = mock(TrackingErrorCalculation.class);
    final var pairs = Set.of(
        Pair.of("2010-01-01", ONE),
        Pair.of("2020-01-01", TEN));

    final var intervalResDto = new TimeIntervalResult("2010-01-01", ONE);
    final var intervalResDto1 = new TimeIntervalResult("2020-01-01", TEN);
    final var expected = Set.of(intervalResDto1, intervalResDto);

    doCallRealMethod().when(sut).defineResponseType(anySet());
    // ACT
    final TrackingErrorResult actual = sut.defineResponseType(pairs);

    // VERIFY
    assertEquals(expected, actual.getTrackingError());
  }

  @Test
  void calculatePeriodForNumberOfMonths_checkResult() {
    // SETUP
    final var sut = mock(TrackingErrorCalculation.class);
    final var benchmarkTotalReturns = mock(TreeMap.class);
    final var portfolioTotalReturns = mock(TreeMap.class);

    when(benchmarkTotalReturns.size()).thenReturn(1);
    when(portfolioTotalReturns.size()).thenReturn(1);

    when(sut.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturns);
    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);
    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

    // ACT
    BigDecimal actual = sut.calculatePeriodForNumberOfMonths(2);

    // VERIFY
    assertNull(actual);
  }

  @Test
  void calculatePeriodForNumberOfMonths_checkResult1() {
    // SETUP
    final var sut = mock(TrackingErrorCalculation.class);
    final var benchmarkTotalReturns = mock(TreeMap.class);
    final var portfolioTotalReturns = mock(TreeMap.class);
    when(benchmarkTotalReturns.size()).thenReturn(25);
    when(portfolioTotalReturns.size()).thenReturn(25);

    when(sut.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturns);
    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);
    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(-1);

    // VERIFY
    assertNull(actual);
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyGetPeriodStartDate() {
    // SETUP
    final var sut = mock(TrackingErrorCalculation.class);

    final var benchmarkTotalReturns = mock(TreeMap.class);
    final var portfolioTotalReturns = mock(TreeMap.class);
    when(benchmarkTotalReturns.size()).thenReturn(25);
    when(portfolioTotalReturns.size()).thenReturn(25);

    sut.portfolioReturnOverBenchmark = new TreeMap<>();

    when(sut.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturns);
    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    sut.calculatePeriodForNumberOfMonths(24);

    // VERIFY
    verify(sut).getPeriodStartDate(eq(24), argThat(argument -> argument == sut.portfolioReturnOverBenchmark));
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyGetSubMap() {
    // SETUP
    final var sut = mock(TrackingErrorCalculation.class);
    final var benchmarkTotalReturns = mock(TreeMap.class);
    final var portfolioTotalReturns = mock(TreeMap.class);
    final var periodStartDate = LocalDate.of(2020, 4, 10);

    when(benchmarkTotalReturns.size()).thenReturn(25);
    when(portfolioTotalReturns.size()).thenReturn(25);

    when(sut.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturns);
    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);

    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    BigDecimal actual = sut.calculatePeriodForNumberOfMonths(24);

    // VERIFY
    verify(sut).getSubMapByPeriodStartDate(eq(periodStartDate), argThat(
        argument -> argument == sut.portfolioReturnOverBenchmark));
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyCalculateAverageExcessPortfolioReturnsByPeriod() {
    // SETUP
    final var sut = mock(TrackingErrorCalculation.class);
    final var benchmarkTotalReturns = mock(TreeMap.class);
    final var portfolioTotalReturns = mock(TreeMap.class);
    final TreeMap<LocalDate, BigDecimal> subMapByPeriodStartDate = new TreeMap<>();
    final LocalDate periodStartDate = LocalDate.of(2020, 4, 10);

    when(benchmarkTotalReturns.size()).thenReturn(25);
    when(portfolioTotalReturns.size()).thenReturn(25);

    when(sut.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturns);
    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);

    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(subMapByPeriodStartDate);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    sut.calculatePeriodForNumberOfMonths(24);

    // VERIFY
    verify(sut).calculateAverageByPeriod(subMapByPeriodStartDate);
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyCalculateDiffExcessPortfolioAndAVGExcessPortfolio() {
    // SETUP
    final var sut = mock(TrackingErrorCalculation.class);

    final var benchmarkTotalReturns = mock(TreeMap.class);
    final var portfolioTotalReturns = mock(TreeMap.class);
    when(benchmarkTotalReturns.size()).thenReturn(25);
    when(portfolioTotalReturns.size()).thenReturn(25);

    final TreeMap<LocalDate, BigDecimal> subMapByPeriodStartDate = new TreeMap<>();
    final var periodStartDate = LocalDate.of(2020, 4, 10);
    final var averageExcessPortfolioReturns = mock(BigDecimal.class);

    when(sut.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturns);
    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);

    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(subMapByPeriodStartDate);
    when(sut.calculateAverageByPeriod(any())).thenReturn(averageExcessPortfolioReturns);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    sut.calculatePeriodForNumberOfMonths(24);

    // VERIFY
    verify(sut).calculateAverageByPeriod(subMapByPeriodStartDate);
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyCalculateTrackingError() {
    // SETUP
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
    // ACT
    var.calculatePeriodForNumberOfMonths(24);

    // VERIFY
    verify(var).calculateTrackingError(24, diff);
  }

  @Test
  void calculateAverageExcessPortfolioReturnsByPeriod_checkResult() {
    // SETUP
    final var sut = mock(TrackingErrorCalculation.class);
    final var subMapByPeriodStartDate = new TreeMap();

    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(2), TWO);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(3), TEN);

    doCallRealMethod().when(sut).calculateAverageByPeriod(subMapByPeriodStartDate);
    // ACT
    final BigDecimal actual = sut.calculateAverageByPeriod(subMapByPeriodStartDate);

    // VERIFY
    assertEquals(6.0, actual.doubleValue());
  }

  @Test
  void calculateAverageExcessPortfolioReturnsByPeriod_checkResult1() {
    // SETUP
    final var sut = mock(TrackingErrorCalculation.class);
    final var subMapByPeriodStartDate = new TreeMap();

    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(10), TWELVE);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(1), TEN);

    doCallRealMethod().when(sut)
        .calculateAverageByPeriod(subMapByPeriodStartDate);
    // ACT
    final BigDecimal actual = sut
        .calculateAverageByPeriod(subMapByPeriodStartDate);

    // VERIFY
    assertEquals(11.0, actual.doubleValue());
  }

  @Test
  void calculateAverageExcessPortfolioReturnsByPeriod_checkResult3() {
    // SETUP
    final var sut = mock(TrackingErrorCalculation.class);
    final var subMapByPeriodStartDate = new TreeMap();

    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(3), ZERO);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(5), TWO);

    doCallRealMethod().when(sut)
        .calculateAverageByPeriod(subMapByPeriodStartDate);
    // ACT
    final BigDecimal actual = sut
        .calculateAverageByPeriod(subMapByPeriodStartDate);

    // VERIFY
    assertEquals(1.000000000000000, actual.doubleValue());
  }

  @Test
  void calculateTrackingError_checkResult() {
    // SETUP
    final var sut = mock(TrackingErrorCalculation.class);
    final TreeMap<LocalDate, BigDecimal> subMapByPeriodStartDate = new TreeMap<>();

    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(3), ZERO);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(4), TWELVE);

    doCallRealMethod().when(sut)
        .calculateTrackingError(25, subMapByPeriodStartDate);
    // ACT
    final BigDecimal actual = sut
        .calculateTrackingError(25, subMapByPeriodStartDate);

    // VERIFY
    assertEquals(BigDecimal.valueOf(2.4494897428), actual);
  }

  @Test
  void calculateTrackingError_checkResult1() {
    // SETUP
    final var sut = mock(TrackingErrorCalculation.class);
    final var subMapByPeriodStartDate = new TreeMap();

    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(11), TEN);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(4), TWELVE);

    doCallRealMethod().when(sut).calculateTrackingError(60, subMapByPeriodStartDate);
    // ACT
    final BigDecimal actual = sut.calculateTrackingError(60, subMapByPeriodStartDate);

    // VERIFY
    assertEquals(BigDecimal.valueOf(2.1153194253), actual);
  }

  @Test
  void calculateDiffExcessPortfolioAndAVGExcessPortfolio_checkResult() {
    // SETUP
    final var sut = mock(TrackingErrorCalculation.class);

    final var subMapByPeriodStartDate = new TreeMap();
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(5), TEN);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(5), TWELVE);

    final var expected = new TreeMap();
    expected.put(LocalDate.now().minusMonths(5), BigDecimal.valueOf(64.0));
    expected.put(LocalDate.now().minusMonths(5), BigDecimal.valueOf(100.0));

    doCallRealMethod().when(sut).calculateDiffPortfolioAndAVGPortfolio(subMapByPeriodStartDate, TWO);
    // ACT
    final TreeMap<LocalDate, BigDecimal> actual = sut.calculateDiffPortfolioAndAVGPortfolio(subMapByPeriodStartDate,
        TWO);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void calculateDiffExcessPortfolioAndAVGExcessPortfolio_checkResult2() {
    // SETUP
    final var sut = mock(TrackingErrorCalculation.class);

    final var subMapByPeriodStartDate = new TreeMap();
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(15), ZERO);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(11), TEN);

    final var expected = new TreeMap();
    expected.put(LocalDate.now().minusMonths(15), BigDecimal.valueOf(1.0));
    expected.put(LocalDate.now().minusMonths(11), BigDecimal.valueOf(81.0));

    doCallRealMethod().when(sut).calculateDiffPortfolioAndAVGPortfolio(subMapByPeriodStartDate, ONE);
    // ACT
    final var actual = sut.calculateDiffPortfolioAndAVGPortfolio(subMapByPeriodStartDate, ONE);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void calculateDiffExcessPortfolioAndAVGExcessPortfolio_checkResult3() {
    // SETUP
    final var sut = mock(TrackingErrorCalculation.class);

    final var subMapByPeriodStartDate = new TreeMap();
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(1), TWO);
    subMapByPeriodStartDate.put(LocalDate.now().minusMonths(2), ZERO);

    final var expected = new TreeMap();
    expected.put(LocalDate.now().minusMonths(1), BigDecimal.valueOf(64.0));
    expected.put(LocalDate.now().minusMonths(2), BigDecimal.valueOf(100.0));

    doCallRealMethod().when(sut).calculateDiffPortfolioAndAVGPortfolio(subMapByPeriodStartDate, TEN);
    // ACT
    final var actual = sut.calculateDiffPortfolioAndAVGPortfolio(subMapByPeriodStartDate, TEN);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void calculateExcessPortfolioReturnOverBenchmark_checkResult() {
    // SETUP
    final var sut = mock(TrackingErrorCalculation.class);

    final TreeMap<LocalDate, BigDecimal> portfolioTotalReturns = new TreeMap<>();
    portfolioTotalReturns.put(LocalDate.now().minusMonths(2), TEN);
    portfolioTotalReturns.put(LocalDate.now().minusMonths(6), TEN);

    final TreeMap<LocalDate, BigDecimal> benchmarkTotalReturns = new TreeMap<>();
    benchmarkTotalReturns.put(LocalDate.now().minusMonths(2), ONE);
    benchmarkTotalReturns.put(LocalDate.now().minusMonths(6), ZERO);

    final TreeMap<LocalDate, BigDecimal> expected = new TreeMap<>();
    expected.put(LocalDate.now().minusMonths(2), BigDecimal.valueOf(9));
    expected.put(LocalDate.now().minusMonths(6), TEN);

    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);
    when(sut.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturns);

    doCallRealMethod().when(sut).calculateExcessPortfolioReturnOverBenchmark();
    // ACT
    NavigableMap<LocalDate, BigDecimal> actual = sut.calculateExcessPortfolioReturnOverBenchmark();

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void calculateExcessPortfolioReturnOverBenchmark_checkResult1() {
    // SETUP
    final var sut = mock(TrackingErrorCalculation.class);

    final TreeMap<LocalDate, BigDecimal> portfolioTotalReturns = new TreeMap<>();
    portfolioTotalReturns.put(LocalDate.now().minusMonths(6), ZERO);
    portfolioTotalReturns.put(LocalDate.now().minusMonths(8), TWO);

    final TreeMap<LocalDate, BigDecimal> benchmarkTotalReturns = new TreeMap<>();
    benchmarkTotalReturns.put(LocalDate.now().minusMonths(6), TEN);
    benchmarkTotalReturns.put(LocalDate.now().minusMonths(8), ZERO);

    final TreeMap<LocalDate, BigDecimal> expected = new TreeMap<>();
    expected.put(LocalDate.now().minusMonths(6), BigDecimal.valueOf(-10));
    expected.put(LocalDate.now().minusMonths(8), TWO);

    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);
    when(sut.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturns);

    doCallRealMethod().when(sut).calculateExcessPortfolioReturnOverBenchmark();
    // ACT
    final NavigableMap<LocalDate, BigDecimal> actual = sut.calculateExcessPortfolioReturnOverBenchmark();

    // VERIFY
    assertEquals(expected, actual);
  }

}