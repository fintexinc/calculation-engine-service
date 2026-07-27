package com.fintex.ce.application.util;

import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.ce.util.DateTimeUtils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReturnSeriesAlignmentValidatorTest {

  private static final int TWELVE_MONTHS = 12;

  @Test
  void shouldNotThrowException_whenPortfolioReturnsAreEmpty() {
    TreeMap<LocalDate, BigDecimal> portfolioReturns = new TreeMap<>();

    assertThatCode(() -> ReturnSeriesAlignmentValidator.requirePortfolioCoverage(portfolioReturns, TWELVE_MONTHS))
        .doesNotThrowAnyException();
  }

  @Test
  void shouldReportAllMissingPortfolioDates_whenPortfolioCoverageHasMultipleGaps() {
    TreeMap<LocalDate, BigDecimal> portfolioReturns = monthlyReturns(LocalDate.parse("2024-01-31"));
    TreeMap<LocalDate, BigDecimal> benchmarkReturns = monthlyReturns(LocalDate.parse("2024-01-31"));
    portfolioReturns.remove(LocalDate.parse("2024-01-31"));
    portfolioReturns.remove(LocalDate.parse("2024-06-30"));

    assertThatThrownBy(() -> ReturnSeriesAlignmentValidator.requirePortfolioBenchmarkCoverage(portfolioReturns,
        benchmarkReturns, 12))
        .isInstanceOfSatisfying(CalculationException.class, exception -> {
          assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSING_PORTFOLIO_RETURN_FOR_DATE);
          assertThat(exception).hasMessageContaining("2024-01-31")
              .hasMessageContaining("2024-06-30");
        });
  }

  @Test
  void shouldReportAllMissingBenchmarkDates_whenBenchmarkCoverageHasMultipleGaps() {
    TreeMap<LocalDate, BigDecimal> portfolioReturns = monthlyReturns(LocalDate.parse("2024-01-31"));
    TreeMap<LocalDate, BigDecimal> benchmarkReturns = monthlyReturns(LocalDate.parse("2024-01-31"));
    benchmarkReturns.remove(LocalDate.parse("2024-01-31"));
    benchmarkReturns.remove(LocalDate.parse("2024-06-30"));

    assertThatThrownBy(() -> ReturnSeriesAlignmentValidator.requirePortfolioBenchmarkCoverage(portfolioReturns,
        benchmarkReturns, 12))
        .isInstanceOfSatisfying(CalculationException.class, exception -> {
          assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSING_BENCHMARK_RETURN_FOR_DATE);
          assertThat(exception).hasMessageContaining("2024-01-31")
              .hasMessageContaining("2024-06-30");
        });
  }

  private static TreeMap<LocalDate, BigDecimal> monthlyReturns(LocalDate startDate) {
    return IntStream.range(0, TWELVE_MONTHS)
        .mapToObj(startDate::plusMonths)
        .collect(Collectors.toMap(DateTimeUtils::toLastDayOfMonth, date -> ONE, (left, right) -> right, TreeMap::new));
  }
}
