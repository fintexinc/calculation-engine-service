package com.fintex.ce.util;

import com.fintex.ce.domain.model.CommonDates;
import com.fintex.ce.domain.model.holding.Holding;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.util.TestConstants.LOCAL_DATE_NOW;
import static java.math.BigDecimal.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class MapUtilsTest {

  private static Stream<Arguments> isWithinTheRange_provider() {
    return Stream.of(
        Arguments.of(mock(CommonDates.class), LOCAL_DATE_NOW, true),

        Arguments.of(new CommonDates(LOCAL_DATE_NOW.plusMonths(1), null), LOCAL_DATE_NOW, false),
        Arguments.of(new CommonDates(LOCAL_DATE_NOW.minusMonths(1), null), LOCAL_DATE_NOW, true),
        Arguments.of(new CommonDates(LOCAL_DATE_NOW, null), LOCAL_DATE_NOW, true),

        Arguments.of(new CommonDates(null, LOCAL_DATE_NOW), LOCAL_DATE_NOW, true),
        Arguments.of(new CommonDates(null, LOCAL_DATE_NOW.minusMonths(1)), LOCAL_DATE_NOW, false),
        Arguments.of(new CommonDates(null, LOCAL_DATE_NOW.plusMonths(1)), LOCAL_DATE_NOW, true),

        Arguments.of(new CommonDates(LOCAL_DATE_NOW.minusMonths(1), LOCAL_DATE_NOW.plusMonths(1)), LOCAL_DATE_NOW,
            true),
        Arguments.of(new CommonDates(LOCAL_DATE_NOW.plusMonths(1), LOCAL_DATE_NOW.plusMonths(1)), LOCAL_DATE_NOW,
            false),
        Arguments.of(new CommonDates(LOCAL_DATE_NOW.plusMonths(2), LOCAL_DATE_NOW.plusMonths(3)), LOCAL_DATE_NOW,
            false),
        Arguments.of(new CommonDates(LOCAL_DATE_NOW.minusMonths(2), LOCAL_DATE_NOW.minusMonths(1)), LOCAL_DATE_NOW,
            false));
  }

  @ParameterizedTest
  @MethodSource("isWithinTheRange_provider")
  void isWithinTheRange_consumer(final CommonDates dates, final LocalDate date, boolean result) {
    // SETUP

    // ACT
    final boolean actual = MapUtils.isWithinTheRange(dates, date);

    // VERIFY
    assertEquals(result, actual);
  }

  @Test
  void filterWithinRange_isWorking() {
    // SETUP
    final CommonDates commonDates = new CommonDates(LOCAL_DATE_NOW.minusMonths(1), LOCAL_DATE_NOW);
    final Holding holding = mock(Holding.class);
    final Map<Holding, Map<LocalDate, BigDecimal>> mock = Map.of(holding, Map.of(
        LOCAL_DATE_NOW.minusMonths(1), ONE,
        LOCAL_DATE_NOW.minusMonths(2), ONE,
        LOCAL_DATE_NOW.plusMonths(3), ONE,
        LOCAL_DATE_NOW.plusMonths(1), ONE));

    // ACT
    final Map<Holding, Map<LocalDate, BigDecimal>> actual = MapUtils.filterWithinRange(commonDates, mock);

    // VERIFY
    Assertions.assertNotNull(actual);
    ComparisonUtils.compareMaps(actual, Map.of(holding, Map.of(
        LOCAL_DATE_NOW.minusMonths(1), ONE)));
  }

  @Test
  void overrideDefaultValues_checkResult() {
    // SETUP
    final Map<String, BigDecimal> defaultMap = Map.of("1", ZERO, "2", ZERO);
    final Map<String, BigDecimal> userMap = Map.of("1", TEN);

    // ACT
    final Map<String, BigDecimal> actual = MapUtils.overrideDefaultValues(defaultMap, userMap);

    // VERIFY
    assertEquals(Map.of("1", TEN, "2", ZERO), actual);
  }

}