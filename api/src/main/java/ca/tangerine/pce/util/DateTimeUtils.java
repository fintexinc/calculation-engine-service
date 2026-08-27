package ca.tangerine.pce.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DateTimeUtils {
  public static final int QUINCENTENARY = 500;
  private static final long ONE = 1;
  public static final DateTimeFormatter PATTERN_1 = DateTimeFormatter.ofPattern("yyyy-MM");

  private DateTimeUtils() {
  }

  public static LocalDate toLastDayOfMonth(final LocalDate date) {
    return Objects.requireNonNull(date).withDayOfMonth(date.getMonth().length(date.isLeapYear()));
  }

  public static LocalDate toFirstDayOfMonth(final LocalDate date) {
    return LocalDate.of(date.getYear(), date.getMonth(), 1);
  }

  public static boolean isLastDayOfMonth(final LocalDate date) {
    return Objects.requireNonNull(date).withDayOfMonth(date.getMonth().length(date.isLeapYear())).equals(date);
  }

  public static int getMonthsBetweenDates(final LocalDate startDate, final LocalDate endDate,
      final TemporalAdjuster temporalAdjuster) {
    return (int) ChronoUnit.MONTHS.between(startDate.with(temporalAdjuster), endDate) + 1;
  }

  public static LocalDate addOneMonth(final LocalDate date) {
    return date.plusMonths(ONE);
  }

  public static LocalDate minusOneMonth(final LocalDate date) {
    return date.minusMonths(ONE);
  }

  public static List<LocalDate> rangeWithLastDayOfMonth(final LocalDate startDate, final LocalDate endDate) {
    if (Objects.isNull(startDate) || Objects.isNull(endDate) || !startDate.isBefore(endDate)) {
      return List.of();
    }
    LocalDate startFrom = limitStartDate(startDate, endDate);
    final int monthsBetween = Math.max(1, (int) ChronoUnit.MONTHS.between(startFrom.withDayOfMonth(1), endDate
        .withDayOfMonth(1)));
    return IntStream.range(0, monthsBetween)
        .mapToObj(startFrom::plusMonths)
        .map(DateTimeUtils::toLastDayOfMonth)
        .toList();
  }

  public static boolean isDateOlderQuincentenaryFromNow(LocalDate localDate) {
    LocalDate now = LocalDate.now();
    LocalDate quincentenaryBeforeNow = now.minusYears(QUINCENTENARY);
    return localDate.isBefore(quincentenaryBeforeNow);
  }

  // Value of startDate is already verified in HoldingReqValidator::validateGicHolding and can't be quincentenary
  // before now
  // But checkmarx is not able to see this and forces to add double check
  private static LocalDate limitStartDate(final LocalDate startDate, final LocalDate endDate) {
    if (startDate.isAfter(endDate)) {
      log.warn("Start date {} is after end date {}. Setting start date to end date.", startDate, endDate);
      return endDate;
    }
    LocalDate quincentenaryBeforeEnd = endDate.minusYears(QUINCENTENARY);
    if (startDate.isBefore(quincentenaryBeforeEnd)) {
      log.warn(
          "Start date {} is more than quincentenary before end date {}. Setting start date to quincentenary before end date {}.",
          startDate, endDate, quincentenaryBeforeEnd);
      return quincentenaryBeforeEnd;
    }
    return startDate;
  }
}
