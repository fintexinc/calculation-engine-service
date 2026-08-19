package com.fintex.ce.application.util;

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
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.fintex.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.TWENTY_YR;
import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ReturnSeriesAlignmentValidatorTest {

  private static final int TWELVE_MONTHS = ONE_YR.getMonths();
  private static final int TWENTY_YEAR_MONTHS = TWENTY_YR.getMonths();

  @Test
  void shouldNotThrowException_whenPortfolioReturnsAreEmpty() {
    TreeMap<LocalDate, BigDecimal> portfolioReturns = new TreeMap<>();

    assertThatCode(() -> ReturnSeriesAlignmentValidator.requirePortfolioBenchmarkCoverage(portfolioReturns,
        new TreeMap<>(), TWELVE_MONTHS))
        .doesNotThrowAnyException();
  }

  @Test
  void shouldPassCoverage_whenPortfolioAndBenchmarkAlignedOverTwoHundredFortyMonthWindow() {
    TreeMap<LocalDate, BigDecimal> portfolioReturns = monthlyReturns(LocalDate.parse("2005-01-31"), TWENTY_YEAR_MONTHS);
    TreeMap<LocalDate, BigDecimal> benchmarkReturns = monthlyReturns(LocalDate.parse("2005-01-31"), TWENTY_YEAR_MONTHS);

    assertThatCode(() -> ReturnSeriesAlignmentValidator.requirePortfolioBenchmarkCoverage(portfolioReturns,
        benchmarkReturns, TWENTY_YEAR_MONTHS))
        .doesNotThrowAnyException();
  }

  @Test
  void shouldOnlyValidateBenchmarkCoverage_whenPortfolioReturnsHaveCalendarGaps() {
    TreeMap<LocalDate, BigDecimal> portfolioReturns = monthlyReturns(LocalDate.parse("2024-01-31"), TWELVE_MONTHS);
    TreeMap<LocalDate, BigDecimal> benchmarkReturns = monthlyReturns(LocalDate.parse("2024-01-31"), TWELVE_MONTHS);
    portfolioReturns.remove(LocalDate.parse("2024-01-31"));
    portfolioReturns.remove(LocalDate.parse("2024-06-30"));

    assertThatCode(() -> ReturnSeriesAlignmentValidator.requirePortfolioBenchmarkCoverage(portfolioReturns,
        benchmarkReturns, TWELVE_MONTHS))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("coverageGapCases")
  void shouldReportMissingBenchmarkDates_whenCoverageHasGaps(String description, LocalDate startDate,
      int windowMonths, List<String> missingDates) {
    TreeMap<LocalDate, BigDecimal> portfolioReturns = monthlyReturns(startDate, windowMonths);
    TreeMap<LocalDate, BigDecimal> benchmarkReturns = monthlyReturns(startDate, windowMonths);
    missingDates.forEach(date -> benchmarkReturns.remove(LocalDate.parse(date)));
    String formattedDates = String.join(", ", missingDates);

    assertThatThrownBy(() -> ReturnSeriesAlignmentValidator.requirePortfolioBenchmarkCoverage(portfolioReturns,
        benchmarkReturns, windowMonths))
        .isInstanceOfSatisfying(CalculationException.class, exception -> {
          assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSING_BENCHMARK_RETURN_FOR_DATE);
          assertThat(exception).hasMessage("The benchmark's monthly return data does not cover every month in "
              + "the requested date range. Missing months: " + formattedDates);
          assertThat(exception.getMetadata()).containsExactlyEntriesOf(Map.of("param-1", formattedDates));
        });
  }

  private static Stream<Arguments> coverageGapCases() {
    return Stream.of(
        arguments("all missing benchmark dates over twelve-month window", LocalDate.parse("2024-01-31"),
            TWELVE_MONTHS, List.of("2024-01-31", "2024-06-30")),
        arguments("missing benchmark date inside two-hundred-forty-month window", LocalDate.parse("2005-01-31"),
            TWENTY_YEAR_MONTHS, List.of("2015-06-30")));
  }

  private static TreeMap<LocalDate, BigDecimal> monthlyReturns(LocalDate startDate, int count) {
    return IntStream.range(0, count)
        .mapToObj(startDate::plusMonths)
        .collect(Collectors.toMap(DateTimeUtils::toLastDayOfMonth, date -> ONE, (left, right) -> right, TreeMap::new));
  }
}
