package ca.tangerine.pce.model.domain.calculation;

import com.google.common.collect.Range;

import java.time.LocalDate;

/**
 * Immutable inclusive date range with optional bounds. A {@code null} bound means the range is open on that side. Use
 * {@link #UNBOUNDED} to represent a range that is open on both sides.
 */
public record DateRange(LocalDate start, LocalDate end) {

  public static final DateRange UNBOUNDED = new DateRange(null, null);

  public boolean isUnbounded() {
    return start == null && end == null;
  }

  public boolean isBounded() {
    return start != null && end != null;
  }

  public boolean contains(LocalDate date) {
    return (start == null || !date.isBefore(start))
        && (end == null || !date.isAfter(end));
  }

  public Range<LocalDate> toRange() {
    return Range.closed(start, end);
  }

}
