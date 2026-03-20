package com.fintex.ce.application.calculation;

import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.domain.model.result.InformationRatioResult;
import com.fintex.ce.domain.model.result.core.TimeIntervalResult;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class InformationRatioCalculationTest {

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResult() {
    final var input = mock(BenchmarkCalculationDTO.class);
    final var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var trackingErrorCalculation = mock(TrackingErrorCalculation.class);
    final var sut = mock(InformationRatioCalculation.class, withSettings().useConstructor(input, Set.of("12", "24"),
        trailingTotalReturnsCalculation, trackingErrorCalculation));

    final var portfolioTotalReturn = mock(TreeMap.class);
    final var benchmarkTotalReturn = mock(TreeMap.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(sut.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(100);
    when(benchmarkTotalReturn.size()).thenReturn(100);

    when(trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(12, portfolioTotalReturn)).thenReturn(
        BigDecimal.TEN);
    when(trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(12, benchmarkTotalReturn)).thenReturn(
        BigDecimal.valueOf(4));
    when(trackingErrorCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(BigDecimal.valueOf(2));

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    final var actual = sut.calculatePeriodForNumberOfMonths(12);

    assertEquals(0, BigDecimal.valueOf(3).compareTo(actual));
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGettingPortfolioReturnBenchmarkReturnTrackingError() {
    final var input = mock(BenchmarkCalculationDTO.class);
    final var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var trackingErrorCalculation = mock(TrackingErrorCalculation.class);
    final var sut = mock(InformationRatioCalculation.class, withSettings().useConstructor(input, Set.of("12", "24"),
        trailingTotalReturnsCalculation, trackingErrorCalculation));

    final var portfolioTotalReturn = mock(TreeMap.class);
    final var benchmarkTotalReturn = mock(TreeMap.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(sut.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(100);
    when(benchmarkTotalReturn.size()).thenReturn(100);

    when(trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(anyInt(), any())).thenReturn(BigDecimal.TEN);
    when(trackingErrorCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(BigDecimal.TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    sut.calculatePeriodForNumberOfMonths(12);

    verify(trailingTotalReturnsCalculation).calculatePeriodForNumberOfMonths(12, portfolioTotalReturn);
    verify(trailingTotalReturnsCalculation).calculatePeriodForNumberOfMonths(12, benchmarkTotalReturn);
    verify(trackingErrorCalculation).calculatePeriodForNumberOfMonths(12);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyNumberOfMonthsGreaterThanBenchmarkTotalReturns() {
    final var input = mock(BenchmarkCalculationDTO.class);
    final var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var trackingErrorCalculation = mock(TrackingErrorCalculation.class);
    final var sut = mock(InformationRatioCalculation.class, withSettings().useConstructor(input, Set.of("12", "24"),
        trailingTotalReturnsCalculation, trackingErrorCalculation));

    final var portfolioTotalReturn = mock(TreeMap.class);
    final var benchmarkTotalReturn = mock(TreeMap.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(sut.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(140);
    when(benchmarkTotalReturn.size()).thenReturn(100);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    final var actual = sut.calculatePeriodForNumberOfMonths(120);

    assertNull(actual);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyNumberOfMonthsGreaterThanPortfolioTotalReturns() {
    final var input = mock(BenchmarkCalculationDTO.class);
    final var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var trackingErrorCalculation = mock(TrackingErrorCalculation.class);
    final var sut = mock(InformationRatioCalculation.class, withSettings().useConstructor(input, Set.of("12", "24"),
        trailingTotalReturnsCalculation, trackingErrorCalculation));

    final var portfolioTotalReturn = mock(TreeMap.class);
    final var benchmarkTotalReturn = mock(TreeMap.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(sut.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(100);
    when(benchmarkTotalReturn.size()).thenReturn(140);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    final var actual = sut.calculatePeriodForNumberOfMonths(120);

    assertNull(actual);
  }

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    final var input = mock(BenchmarkCalculationDTO.class);
    final var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var trackingErrorCalculation = mock(TrackingErrorCalculation.class);
    final var sut = mock(InformationRatioCalculation.class, withSettings().useConstructor(input, Set.of("12", "24"),
        trailingTotalReturnsCalculation, trackingErrorCalculation));

    final Set<Pair<String, BigDecimal>> periodAndInformationRatio = mock(Set.class);
    Set<TimeIntervalResult> timeIntervalResultS = mock(Set.class);
    final var expected = new InformationRatioResult();
    expected.setTimeIntervalResultS(timeIntervalResultS);

    when(sut.formTimeIntervalResult(periodAndInformationRatio)).thenReturn(timeIntervalResultS);

    doCallRealMethod().when(sut).defineResponseType(any());
    final var actual = sut.defineResponseType(periodAndInformationRatio);

    assertEquals(expected, actual);
  }

}