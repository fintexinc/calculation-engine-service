package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.InformationRatioResult;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.ce.util.DateTimeUtils;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

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
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@Disabled("metric unsupported")
class InformationRatioCalculationTest {

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResult() {
    var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    var trackingErrorCalculation = mock(TrackingErrorCalculation.class);
    var portfolioTotalReturn = monthlyReturns(LocalDate.parse("2024-01-31"), 100, BigDecimal.TEN);
    var benchmarkTotalReturn = monthlyReturns(LocalDate.parse("2024-01-31"), 100, BigDecimal.ONE);
    var calculation = new InformationRatioCalculation(benchmarkInput(portfolioTotalReturn, benchmarkTotalReturn),
        Set.of("12", "24"), trailingTotalReturnsCalculation, trackingErrorCalculation);

    when(trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(eq(12), any())).thenReturn(BigDecimal.TEN,
        BigDecimal.valueOf(4));
    when(trackingErrorCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(BigDecimal.valueOf(2));

    var actual = calculation.calculatePeriodForNumberOfMonths(12);

    assertEquals(0, BigDecimal.valueOf(3).compareTo(actual));
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGettingPortfolioReturnBenchmarkReturnTrackingError() {
    var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    var trackingErrorCalculation = mock(TrackingErrorCalculation.class);
    var portfolioTotalReturn = monthlyReturns(LocalDate.parse("2024-01-31"), 12, BigDecimal.TEN);
    var benchmarkTotalReturn = monthlyReturns(LocalDate.parse("2024-01-31"), 13, BigDecimal.ONE);
    var calculation = new InformationRatioCalculation(benchmarkInput(portfolioTotalReturn, benchmarkTotalReturn),
        Set.of("12", "24"), trailingTotalReturnsCalculation, trackingErrorCalculation);

    when(trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(eq(12), any())).thenReturn(BigDecimal.TEN);
    when(trackingErrorCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(BigDecimal.TEN);

    calculation.calculatePeriodForNumberOfMonths(12);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<NavigableMap<LocalDate, BigDecimal>> returnsCaptor = ArgumentCaptor.forClass(NavigableMap.class);
    verify(trailingTotalReturnsCalculation, times(2)).calculatePeriodForNumberOfMonths(eq(12), returnsCaptor.capture());
    assertThat(returnsCaptor.getAllValues().getFirst()).isSameAs(portfolioTotalReturn);
    assertThat(returnsCaptor.getAllValues().get(1)).containsOnlyKeys(portfolioTotalReturn.keySet())
        .doesNotContainKey(LocalDate.parse("2025-01-31"));
    verify(trackingErrorCalculation).calculatePeriodForNumberOfMonths(12);
  }

  @ParameterizedTest(name = "[{index}] portfolioReturn={0}, benchmarkReturn={1}, trackingError={2}")
  @MethodSource("invalidDependentCalculationResults")
  void shouldReturnNull_whenDependentCalculationResultIsMissingOrZero(
      BigDecimal portfolioReturn,
      BigDecimal benchmarkReturn,
      BigDecimal trackingError) {
    var input = mock(BenchmarkPeriodCalculationInput.class);
    var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    var trackingErrorCalculation = mock(TrackingErrorCalculation.class);
    var calculation = mock(InformationRatioCalculation.class, withSettings().useConstructor(input, Set.of("12"),
        trailingTotalReturnsCalculation, trackingErrorCalculation));

    var portfolioTotalReturn = mock(TreeMap.class);
    var benchmarkTotalReturn = mock(TreeMap.class);

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
    BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(12);

    assertNull(actual);
  }

  @Test
  void shouldThrowMissingBenchmarkReturn_whenPortfolioAndBenchmarkDatesAreShifted() {
    var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    var trackingErrorCalculation = mock(TrackingErrorCalculation.class);
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
    var input = mock(BenchmarkPeriodCalculationInput.class);
    var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    var trackingErrorCalculation = mock(TrackingErrorCalculation.class);
    var calculation = mock(InformationRatioCalculation.class, withSettings().useConstructor(input, Set.of("12",
        "24"),
        trailingTotalReturnsCalculation, trackingErrorCalculation));

    var portfolioTotalReturn = mock(TreeMap.class);
    var benchmarkTotalReturn = mock(TreeMap.class);

    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(calculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(140);
    when(benchmarkTotalReturn.size()).thenReturn(100);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    var actual = calculation.calculatePeriodForNumberOfMonths(120);

    assertNull(actual);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyNumberOfMonthsGreaterThanPortfolioTotalReturns() {
    var input = mock(BenchmarkPeriodCalculationInput.class);
    var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    var trackingErrorCalculation = mock(TrackingErrorCalculation.class);
    var calculation = mock(InformationRatioCalculation.class, withSettings().useConstructor(input, Set.of("12",
        "24"),
        trailingTotalReturnsCalculation, trackingErrorCalculation));

    var portfolioTotalReturn = mock(TreeMap.class);
    var benchmarkTotalReturn = mock(TreeMap.class);

    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(calculation.getBenchmarkTotalReturns()).thenReturn(benchmarkTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(100);
    when(benchmarkTotalReturn.size()).thenReturn(140);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    var actual = calculation.calculatePeriodForNumberOfMonths(120);

    assertNull(actual);
  }

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    var input = mock(BenchmarkPeriodCalculationInput.class);
    var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    var trackingErrorCalculation = mock(TrackingErrorCalculation.class);
    var calculation = mock(InformationRatioCalculation.class, withSettings().useConstructor(input, Set.of("12",
        "24"),
        trailingTotalReturnsCalculation, trackingErrorCalculation));

    Set<Pair<String, BigDecimal>> periodAndInformationRatio = mock(Set.class);
    Set<TimeIntervalResult> informationRatio = mock(Set.class);
    var expected = new InformationRatioResult(informationRatio);
    when(calculation.formTimeIntervalResult(periodAndInformationRatio)).thenReturn(informationRatio);

    doCallRealMethod().when(calculation).defineResponseType(any());
    var actual = calculation.defineResponseType(periodAndInformationRatio);

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
