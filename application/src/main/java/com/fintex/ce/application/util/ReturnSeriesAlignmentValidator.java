package com.fintex.ce.application.util;

import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.util.DateTimeUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.stream.IntStream;
import lombok.experimental.UtilityClass;

import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;

@UtilityClass
public final class ReturnSeriesAlignmentValidator {

  public static void requirePortfolioBenchmarkCoverage(
      NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      NavigableMap<LocalDate, BigDecimal> benchmarkReturns,
      int numberOfMonths) {
    LocalDate firstExpectedDate = toLastDayOfMonth(portfolioReturns.lastKey().minusMonths(numberOfMonths - 1L));
    IntStream.range(0, numberOfMonths)
        .mapToObj(firstExpectedDate::plusMonths)
        .map(DateTimeUtils::toLastDayOfMonth)
        .forEach(date -> requirePortfolioBenchmarkDate(portfolioReturns, benchmarkReturns, date));
  }

  private static void requirePortfolioBenchmarkDate(
      NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      NavigableMap<LocalDate, BigDecimal> benchmarkReturns,
      LocalDate date) {
    if (!portfolioReturns.containsKey(date)) {
      throw ErrorCode.MISSING_PORTFOLIO_RETURN_FOR_DATE.toException(date);
    }
    if (!benchmarkReturns.containsKey(date)) {
      throw ErrorCode.MISSING_BENCHMARK_RETURN_FOR_DATE.toException(date);
    }
  }
}
