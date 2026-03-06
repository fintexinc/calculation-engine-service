package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.InformationRatioCalculation;
import com.fintex.ce.application.calculation.TrackingErrorCalculation;
import com.fintex.ce.application.calculation.TrailingTotalReturnsCalculation;
import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.port.input.result.InformationRatioResult;
import com.fintex.ce.port.input.result.core.TimeIntervalResult;
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
  void calculatePeriodForNumberOfMonths_checkResult() {
    // SETUP
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
    // ACT
    final var actual = sut.calculatePeriodForNumberOfMonths(12);

    // VERIFY
    assertEquals(0, BigDecimal.valueOf(3).compareTo(actual));
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyGettingPortfolioReturn_benchmarkReturn_trackingError() {
    // SETUP
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
    // ACT
    sut.calculatePeriodForNumberOfMonths(12);

    // VERIFY
    verify(trailingTotalReturnsCalculation).calculatePeriodForNumberOfMonths(12, portfolioTotalReturn);
    verify(trailingTotalReturnsCalculation).calculatePeriodForNumberOfMonths(12, benchmarkTotalReturn);
    verify(trackingErrorCalculation).calculatePeriodForNumberOfMonths(12);
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyNumberOfMonthsGreaterThanBenchmarkTotalReturns() {
    // SETUP
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
    // ACT
    final var actual = sut.calculatePeriodForNumberOfMonths(120);

    // VERIFY
    assertNull(actual);
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyNumberOfMonthsGreaterThanPortfolioTotalReturns() {
    // SETUP
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
    // ACT
    final var actual = sut.calculatePeriodForNumberOfMonths(120);

    // VERIFY
    assertNull(actual);
  }

  @Test
  void defineResponseType_checkResult() {
    // SETUP
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
    // ACT
    final var actual = sut.defineResponseType(periodAndInformationRatio);

    // VERIFY
    assertEquals(expected, actual);
  }

}