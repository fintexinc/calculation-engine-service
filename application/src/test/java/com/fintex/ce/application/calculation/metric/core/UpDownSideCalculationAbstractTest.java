package com.fintex.ce.application.calculation.metric.core;

import com.fintex.ce.application.calculation.metric.DownsideCaptureCalculation;
import com.fintex.ce.application.calculation.metric.UpsideCaptureCalculation;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.ce.util.DateTimeUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.fintex.ce.application.util.DecimalUtils.pow;
import static com.fintex.ce.application.util.TestConstants.LOCAL_DATE_NOW;
import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpDownSideCalculationAbstractTest {

  @Test
  void shouldReturnPortfolioDetermination_whenCaptureConditionIsMet() {
    final var calculation = mock(UpDownSideCalculationAbstract.class);

    final var benchmark = Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE.subtract(TEN));
    when(calculation.getBenchmarkTotalReturns()).thenReturn(new TreeMap<>(benchmark));

    final var portfolio = Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE.subtract(TEN));
    when(calculation.getPortfolioTotalReturns()).thenReturn(new TreeMap<>(portfolio));

    when(calculation.filterCaptureExpression(any())).thenReturn(true);
    doCallRealMethod().when(calculation).getPortfolioDetermination();
    final TreeMap<LocalDate, BigDecimal> actual = calculation.getPortfolioDetermination();

    assertEquals(Map.of(LOCAL_DATE_NOW.minusMonths(1), new BigDecimal("0.910000000000000")), actual);
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("captureCalculationFactories")
  void shouldThrowMissingPortfolioReturn_whenCaptureWindowContainsQualifyingBenchmarkDateWithoutPortfolioReturn(
      String calculationName,
      Function<BenchmarkPeriodCalculationInput, UpDownSideCalculationAbstract<?>> calculationFactory,
      BigDecimal benchmarkReturn) {
    LocalDate missingDate = LocalDate.parse("2024-01-31");
    TreeMap<LocalDate, BigDecimal> benchmarkReturns = monthlyReturns(missingDate, 12, benchmarkReturn);
    TreeMap<LocalDate, BigDecimal> portfolioReturns = monthlyReturns(LocalDate.parse("2024-02-29"), 11, ONE);
    portfolioReturns.put(LocalDate.parse("2023-12-31"), ONE);

    UpDownSideCalculationAbstract<?> calculation = calculationFactory.apply(benchmarkInput(portfolioReturns,
        benchmarkReturns));

    assertThat(calculation.getPortfolioDetermination()).doesNotContainKey(missingDate);
    assertThatThrownBy(() -> calculation.calculatePeriodForNumberOfMonths(12))
        .as(calculationName)
        .isInstanceOfSatisfying(CalculationException.class,
            exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSING_PORTFOLIO_RETURN_FOR_DATE));
  }

  @Test
  void shouldReturnBenchmarkDeterminationForPortfolioDates_whenBenchmarkDataExists() {
    final var calculation = mock(UpDownSideCalculationAbstract.class);

    final var benchmark = Map.of(
        LOCAL_DATE_NOW.minusMonths(1), ONE.subtract(TEN),
        LOCAL_DATE_NOW, ZERO, LOCAL_DATE_NOW.plusMonths(1), ONE);
    when(calculation.getBenchmarkTotalReturns()).thenReturn(new TreeMap<>(benchmark));

    final var portfolio = new TreeMap<>(
        Map.of(LOCAL_DATE_NOW.plusMonths(1), HUNDRED));

    doCallRealMethod().when(calculation).getBenchmarkDetermination(any());
    final NavigableMap<LocalDate, BigDecimal> actual = calculation.getBenchmarkDetermination(portfolio);

    assertEquals(Map.of(LOCAL_DATE_NOW.plusMonths(1), new BigDecimal("1.010000000000000")), actual);
  }

  @Test
  void shouldReturnCaptureRatio_whenDeviationsAreCalculated() {
    final var calculation = mock(UpDownSideCalculationAbstract.class);

    calculation.portfolioDetermination = new TreeMap<>(Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE));
    calculation.benchmarkDetermination = new TreeMap<>(Map.of(LOCAL_DATE_NOW.minusMonths(2), ONE));

    final var treeMap = mock(TreeMap.class);
    when(treeMap.size()).thenReturn(12);

    when(calculation.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(calculation.calculateDeviationFor(12, calculation.portfolioDetermination)).thenReturn(ONE);
    when(calculation.calculateDeviationFor(12, calculation.benchmarkDetermination)).thenReturn(TEN);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(12);
    final BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(12);

    assertEquals(0, TEN.compareTo(actual));
  }

  @Test
  void shouldReturnNull_whenPeriodIsLessThanTwelve() {
    final var calculation = mock(UpDownSideCalculationAbstract.class);

    calculation.portfolioDetermination = new TreeMap<>(Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE));
    calculation.benchmarkDetermination = new TreeMap<>(Map.of(LOCAL_DATE_NOW.minusMonths(2), ONE));

    final var treeMap = mock(TreeMap.class);
    final int months = 11;
    when(treeMap.size()).thenReturn(months);

    when(calculation.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(calculation.calculateDeviationFor(months, calculation.portfolioDetermination)).thenReturn(ONE);
    when(calculation.calculateDeviationFor(months, calculation.benchmarkDetermination)).thenReturn(TEN);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(months);
    final BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(months);

    assertNull(actual);
  }

  @Test
  void shouldReturnNull_whenPeriodExceedsPortfolioSize() {
    final var calculation = mock(UpDownSideCalculationAbstract.class);

    final var treeMap = mock(TreeMap.class);
    when(treeMap.size()).thenReturn(1);

    when(calculation.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(60);
    final BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(60);

    assertNull(actual);
  }

  @Test
  void shouldReturnZero_whenBenchmarkDeviationIsZero() {
    final var calculation = mock(UpDownSideCalculationAbstract.class);

    calculation.portfolioDetermination = new TreeMap<>(Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE));
    calculation.benchmarkDetermination = new TreeMap<>(Map.of(LOCAL_DATE_NOW.minusMonths(2), ONE));

    final var treeMap = mock(TreeMap.class);
    final int months = 12;
    when(treeMap.size()).thenReturn(months);

    when(calculation.getBenchmarkTotalReturns()).thenReturn(treeMap);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);

    when(calculation.calculateDeviationFor(months, calculation.portfolioDetermination)).thenReturn(ONE);
    when(calculation.calculateDeviationFor(months, calculation.benchmarkDetermination)).thenReturn(ZERO);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(months);
    final BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(months);

    assertEquals(ZERO, actual);
  }

  @Test
  void shouldCalculateDeviation_whenRequiredMonthsExist() {
    final var calculation = mock(UpDownSideCalculationAbstract.class);

    final int numberOfMonths = 2;
    final TreeMap<LocalDate, BigDecimal> determination = new TreeMap<>();
    determination.put(toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), TEN);
    determination.put(toLastDayOfMonth(LOCAL_DATE_NOW), ONE);

    when(calculation.getPortfolioTotalReturns()).thenReturn(determination);

    doCallRealMethod().when(calculation).getBenchmarkValues(numberOfMonths, determination);
    doCallRealMethod().when(calculation).filterRequiredMonthsForPeriod(numberOfMonths, determination);

    doCallRealMethod().when(calculation).calculateDeviationFor(eq(numberOfMonths), any());
    final BigDecimal actual = calculation.calculateDeviationFor(numberOfMonths, determination);

    assertEquals(pow(TEN, BigDecimal.valueOf(0.5)).subtract(ONE), actual);
  }

  @Test
  void shouldReturnZeroDeviation_whenBenchmarkValuesAreMissing() {
    final var calculation = mock(UpDownSideCalculationAbstract.class);
    final var periodCalculationAbstract = mock(PeriodCalculationAbstract.class);

    final TreeMap<LocalDate, BigDecimal> determination = new TreeMap<>(
        Map.of(toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), TEN, toLastDayOfMonth(LOCAL_DATE_NOW), ONE));

    when(periodCalculationAbstract.getBenchmarkValues(60, determination)).thenReturn(List.of());

    doCallRealMethod().when(calculation).calculateDeviationFor(eq(60), any());
    final BigDecimal actual = calculation.calculateDeviationFor(60, determination);

    assertEquals(ZERO, actual);
  }

  private static Stream<Arguments> captureCalculationFactories() {
    return Stream.of(
        Arguments.of("upside",
            (Function<BenchmarkPeriodCalculationInput, UpDownSideCalculationAbstract<?>>) input -> new UpsideCaptureCalculation(
                input, Set.of("12")), ONE),
        Arguments.of("downside",
            (Function<BenchmarkPeriodCalculationInput, UpDownSideCalculationAbstract<?>>) input -> new DownsideCaptureCalculation(
                input, Set.of("12")), ONE.negate()));
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
