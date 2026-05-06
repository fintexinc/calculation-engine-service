package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.InformationRatioResult;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class InformationRatioCalculationTest {

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResult() {
    final var input = mock(BenchmarkPeriodCalculationInput.class);
    final var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var trackingErrorCalculation = mock(TrackingErrorCalculation.class);
    final var calculation = mock(InformationRatioCalculation.class, withSettings().useConstructor(input, Set.of("12",
        "24"),
        trailingTotalReturnsCalculation, trackingErrorCalculation));

    final var portfolioTotalReturn = mock(TreeMap.class);
    final var benchmarkTotalReturn = mock(TreeMap.class);

    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(calculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(100);
    when(benchmarkTotalReturn.size()).thenReturn(100);

    when(trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(12, portfolioTotalReturn)).thenReturn(
        BigDecimal.TEN);
    when(trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(12, benchmarkTotalReturn)).thenReturn(
        BigDecimal.valueOf(4));
    when(trackingErrorCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(BigDecimal.valueOf(2));

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final var actual = calculation.calculatePeriodForNumberOfMonths(12);

    assertEquals(0, BigDecimal.valueOf(3).compareTo(actual));
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGettingPortfolioReturnBenchmarkReturnTrackingError() {
    final var input = mock(BenchmarkPeriodCalculationInput.class);
    final var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var trackingErrorCalculation = mock(TrackingErrorCalculation.class);
    final var calculation = mock(InformationRatioCalculation.class, withSettings().useConstructor(input, Set.of("12",
        "24"),
        trailingTotalReturnsCalculation, trackingErrorCalculation));

    final var portfolioTotalReturn = mock(TreeMap.class);
    final var benchmarkTotalReturn = mock(TreeMap.class);

    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(calculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(100);
    when(benchmarkTotalReturn.size()).thenReturn(100);

    when(trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(anyInt(), any())).thenReturn(BigDecimal.TEN);
    when(trackingErrorCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(BigDecimal.TEN);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(12);

    verify(trailingTotalReturnsCalculation).calculatePeriodForNumberOfMonths(12, portfolioTotalReturn);
    verify(trailingTotalReturnsCalculation).calculatePeriodForNumberOfMonths(12, benchmarkTotalReturn);
    verify(trackingErrorCalculation).calculatePeriodForNumberOfMonths(12);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyNumberOfMonthsGreaterThanBenchmarkTotalReturns() {
    final var input = mock(BenchmarkPeriodCalculationInput.class);
    final var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var trackingErrorCalculation = mock(TrackingErrorCalculation.class);
    final var calculation = mock(InformationRatioCalculation.class, withSettings().useConstructor(input, Set.of("12",
        "24"),
        trailingTotalReturnsCalculation, trackingErrorCalculation));

    final var portfolioTotalReturn = mock(TreeMap.class);
    final var benchmarkTotalReturn = mock(TreeMap.class);

    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(calculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(140);
    when(benchmarkTotalReturn.size()).thenReturn(100);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final var actual = calculation.calculatePeriodForNumberOfMonths(120);

    assertNull(actual);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyNumberOfMonthsGreaterThanPortfolioTotalReturns() {
    final var input = mock(BenchmarkPeriodCalculationInput.class);
    final var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var trackingErrorCalculation = mock(TrackingErrorCalculation.class);
    final var calculation = mock(InformationRatioCalculation.class, withSettings().useConstructor(input, Set.of("12",
        "24"),
        trailingTotalReturnsCalculation, trackingErrorCalculation));

    final var portfolioTotalReturn = mock(TreeMap.class);
    final var benchmarkTotalReturn = mock(TreeMap.class);

    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(calculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(100);
    when(benchmarkTotalReturn.size()).thenReturn(140);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final var actual = calculation.calculatePeriodForNumberOfMonths(120);

    assertNull(actual);
  }

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    final var input = mock(BenchmarkPeriodCalculationInput.class);
    final var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var trackingErrorCalculation = mock(TrackingErrorCalculation.class);
    final var calculation = mock(InformationRatioCalculation.class, withSettings().useConstructor(input, Set.of("12",
        "24"),
        trailingTotalReturnsCalculation, trackingErrorCalculation));

    final Set<Pair<String, BigDecimal>> periodAndInformationRatio = mock(Set.class);
    Set<TimeIntervalResult> informationRatio = mock(Set.class);
    final var expected = new InformationRatioResult(informationRatio);
    when(calculation.formTimeIntervalResult(periodAndInformationRatio)).thenReturn(informationRatio);

    doCallRealMethod().when(calculation).defineResponseType(any());
    final var actual = calculation.defineResponseType(periodAndInformationRatio);

    assertEquals(expected, actual);
  }

}