package com.fintex.ce.model.domain.calculation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateRangeTest {

  private static final LocalDate NOW = LocalDate.of(2024, 6, 15);

  private static Stream<Arguments> contains_provider() {
    return Stream.of(
        Arguments.of(DateRange.UNBOUNDED, NOW, true),
        Arguments.of(new DateRange(null, null), NOW, true),

        Arguments.of(new DateRange(NOW.plusMonths(1), null), NOW, false),
        Arguments.of(new DateRange(NOW.minusMonths(1), null), NOW, true),
        Arguments.of(new DateRange(NOW, null), NOW, true),

        Arguments.of(new DateRange(null, NOW), NOW, true),
        Arguments.of(new DateRange(null, NOW.minusMonths(1)), NOW, false),
        Arguments.of(new DateRange(null, NOW.plusMonths(1)), NOW, true),

        Arguments.of(new DateRange(NOW.minusMonths(1), NOW.plusMonths(1)), NOW, true),
        Arguments.of(new DateRange(NOW.plusMonths(1), NOW.plusMonths(1)), NOW, false),
        Arguments.of(new DateRange(NOW.plusMonths(2), NOW.plusMonths(3)), NOW, false),
        Arguments.of(new DateRange(NOW.minusMonths(2), NOW.minusMonths(1)), NOW, false));
  }

  @ParameterizedTest
  @MethodSource("contains_provider")
  void shouldContains_whenCheckResult(DateRange dateRange, LocalDate date, boolean expected) {
    assertEquals(expected, dateRange.contains(date));
  }

  @Test
  void shouldBeUnbounded_whenBothBoundsAreNull() {
    assertTrue(new DateRange(null, null).isUnbounded());
    assertTrue(DateRange.UNBOUNDED.isUnbounded());
  }

  @Test
  void shouldNotBeUnbounded_whenAnyBoundIsSet() {
    assertFalse(new DateRange(NOW, null).isUnbounded());
    assertFalse(new DateRange(null, NOW).isUnbounded());
    assertFalse(new DateRange(NOW, NOW).isUnbounded());
  }
}
