package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.InformationRatioResult;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.ce.util.DateTimeUtils;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

  @ParameterizedTest(name = "[{index}] portfolioReturn={0}, benchmarkReturn={1}, trackingError={2}")
  @MethodSource("invalidDependentCalculationResults")
  void shouldReturnNull_whenDependentCalculationResultIsMissingOrZero(
      BigDecimal portfolioReturn,
      BigDecimal benchmarkReturn,
      BigDecimal trackingError) {
    final var input = mock(BenchmarkPeriodCalculationInput.class);
    final var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var trackingErrorCalculation = mock(TrackingErrorCalculation.class);
    final var calculation = mock(InformationRatioCalculation.class, withSettings().useConstructor(input, Set.of("12"),
        trailingTotalReturnsCalculation, trackingErrorCalculation));

    final var portfolioTotalReturn = mock(TreeMap.class);
    final var benchmarkTotalReturn = mock(TreeMap.class);

    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(calculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(100);
    when(benchmarkTotalReturn.size()).thenReturn(100);
    when(trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(12, portfolioTotalReturn)).thenReturn(
        portfolioReturn);
    when(trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(12, benchmarkTotalReturn)).thenReturn(
        benchmarkReturn);
    when(trackingErrorCalculation.calculatePeriodForNumberOfMonths(12)).thenReturn(trackingError);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(12);

    assertNull(actual);
  }

  @Test
  void shouldThrowMissingBenchmarkReturn_whenPortfolioAndBenchmarkDatesAreShifted() {
    final var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var trackingErrorCalculation = mock(TrackingErrorCalculation.class);
    LocalDate missingDate = LocalDate.parse("2024-01-31");
    TreeMap<LocalDate, BigDecimal> portfolioReturns = monthlyReturns(missingDate, 12, BigDecimal.TEN);
    TreeMap<LocalDate, BigDecimal> benchmarkReturns = monthlyReturns(LocalDate.parse("2024-02-29"), 12,
        BigDecimal.ONE);
    InformationRatioCalculation calculation = new InformationRatioCalculation(benchmarkInput(portfolioReturns,
        benchmarkReturns), Set.of("12"), trailingTotalReturnsCalculation, trackingErrorCalculation);

    assertThatThrownBy(() -> calculation.calculatePeriodForNumberOfMonths(12))
        .isInstanceOfSatisfying(CalculationException.class,
            exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSING_BENCHMARK_RETURN_FOR_DATE));
    verifyNoInteractions(trailingTotalReturnsCalculation, trackingErrorCalculation);
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

  private static Stream<Arguments> invalidDependentCalculationResults() {
    return Stream.of(
        Arguments.of(BigDecimal.TEN, BigDecimal.ONE, null),
        Arguments.of(BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ZERO),
        Arguments.of(null, BigDecimal.ONE, BigDecimal.TEN),
        Arguments.of(BigDecimal.TEN, null, BigDecimal.TEN));
  }
}
