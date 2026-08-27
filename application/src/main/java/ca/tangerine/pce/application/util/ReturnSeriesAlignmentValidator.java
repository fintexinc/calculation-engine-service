package ca.tangerine.pce.application.util;

import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NavigableMap;
import java.util.stream.IntStream;
import lombok.experimental.UtilityClass;

import static ca.tangerine.pce.util.DateTimeUtils.toLastDayOfMonth;
import static java.util.stream.Collectors.joining;

import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.util.DateTimeUtils;

@UtilityClass
public class ReturnSeriesAlignmentValidator {

  public static List<LocalDate> findMissingCalendarMonthEnds(NavigableMap<LocalDate, BigDecimal> returns) {
    if (CollectionUtils.isEmpty(returns)) {
      return List.of();
    }
    int numberOfMonths = Math.toIntExact(YearMonth.from(returns.firstKey())
        .until(YearMonth.from(returns.lastKey()), ChronoUnit.MONTHS)) + 1;
    return expectedDates(returns.firstKey(), numberOfMonths).stream()
        .filter(date -> !returns.containsKey(date))
        .toList();
  }

  public static void requirePortfolioBenchmarkCoverage(
      NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      NavigableMap<LocalDate, BigDecimal> benchmarkReturns,
      int numberOfMonths) {
    if (CollectionUtils.isEmpty(portfolioReturns) || numberOfMonths <= 0) {
      return;
    }
    List<LocalDate> missingBenchmarkDates = expectedDatesEndingAt(portfolioReturns.lastKey(), numberOfMonths).stream()
        .filter(date -> !benchmarkReturns.containsKey(date))
        .toList();
    if (!missingBenchmarkDates.isEmpty()) {
      throw ErrorCode.MISSING_BENCHMARK_RETURN_FOR_DATE.toException(formatDates(missingBenchmarkDates));
    }
  }

  private static List<LocalDate> expectedDatesEndingAt(LocalDate endDate, int numberOfMonths) {
    return expectedDates(toLastDayOfMonth(endDate.minusMonths(numberOfMonths - 1L)), numberOfMonths);
  }

  private static List<LocalDate> expectedDates(LocalDate firstExpectedDate, int numberOfMonths) {
    return IntStream.range(0, numberOfMonths)
        .mapToObj(firstExpectedDate::plusMonths)
        .map(DateTimeUtils::toLastDayOfMonth)
        .toList();
  }

  private static String formatDates(List<LocalDate> dates) {
    return dates.stream()
        .map(LocalDate::toString)
        .collect(joining(", "));
  }
}
