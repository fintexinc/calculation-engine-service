package com.fintex.ce.application.util;

import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.util.DateTimeUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NavigableMap;
import java.util.stream.IntStream;
import lombok.experimental.UtilityClass;

import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.util.stream.Collectors.joining;

@UtilityClass
public class ReturnSeriesAlignmentValidator {

  public static void requirePortfolioBenchmarkCoverage(
      NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      NavigableMap<LocalDate, BigDecimal> benchmarkReturns,
      int numberOfMonths) {
    LocalDate firstExpectedDate = toLastDayOfMonth(portfolioReturns.lastKey().minusMonths(numberOfMonths - 1L));
    List<LocalDate> expectedDates = IntStream.range(0, numberOfMonths)
        .mapToObj(firstExpectedDate::plusMonths)
        .map(DateTimeUtils::toLastDayOfMonth)
        .toList();

    List<LocalDate> missingPortfolioDates = expectedDates.stream()
        .filter(date -> !portfolioReturns.containsKey(date))
        .toList();
    if (!missingPortfolioDates.isEmpty()) {
      throw ErrorCode.MISSING_PORTFOLIO_RETURN_FOR_DATE.toException(formatDates(missingPortfolioDates));
    }

    List<LocalDate> missingBenchmarkDates = expectedDates.stream()
        .filter(date -> !benchmarkReturns.containsKey(date))
        .toList();
    if (!missingBenchmarkDates.isEmpty()) {
      throw ErrorCode.MISSING_BENCHMARK_RETURN_FOR_DATE.toException(formatDates(missingBenchmarkDates));
    }
  }

  private static String formatDates(List<LocalDate> dates) {
    return dates.stream()
        .map(LocalDate::toString)
        .collect(joining(", "));
  }
}
