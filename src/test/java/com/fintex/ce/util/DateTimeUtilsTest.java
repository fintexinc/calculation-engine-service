package com.fintex.ce.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateTimeUtilsTest {

    @Test
    void toLastDayOfMonth_checkResult() {
        //SETUP
        final Map<Integer, Integer> monthsLastDaysMap = new HashMap<>();
        monthsLastDaysMap.putAll(Map.of(1, 31, 2, 29, 3, 31, 4, 30, 5, 31,
                6, 30, 7, 31, 8, 31, 9, 30, 10, 31));
        monthsLastDaysMap.putAll(Map.of(11, 30, 12, 31));

        //VERIFY
        monthsLastDaysMap.forEach((month, expectedLastDay) -> {
            final LocalDate date = LocalDate.of(2020, month, 1);
            final LocalDate lastDayOfMonth = DateTimeUtils.toLastDayOfMonth(date);

            assertEquals(date.withDayOfMonth(expectedLastDay), lastDayOfMonth);
        });
    }

    @Test
    void toLastDayOfMonth_whenDateIsNull() {
        assertThrows(NullPointerException.class, () -> DateTimeUtils.toLastDayOfMonth(null));
    }

    @Test
    void getMonthsBetweenDates_checkResult() {
        //SETUP
        final LocalDate startDate = LocalDate.of(2020, 3, 10);
        final LocalDate endDate = LocalDate.of(2020, 10, 10);
        final int expectedMonthDifference = endDate.getMonthValue() - startDate.getMonthValue() + 1;

        //ACT
        final int givenMonthDifference = DateTimeUtils.getMonthsBetweenDates(startDate, endDate, firstDayOfMonth());

        //VERFY
        assertEquals(expectedMonthDifference, givenMonthDifference);
    }

    @Test
    void addOneMonth_checkResult() {
        //SETUP
        final LocalDate date = LocalDate.of(2020, 10, 10);

        //ACT
        final LocalDate givenNextMonthDate = DateTimeUtils.addOneMonth(date);

        //VERIFY
        assertEquals(date.plusMonths(1), givenNextMonthDate);
    }

    @Test
    void removeOneMonth_checkResult() {
        //SETUP
        final LocalDate date = LocalDate.of(2020, 10, 10);

        //ACT
        final LocalDate givenNextMonthDate = DateTimeUtils.minusOneMonth(date);

        //VERIFY
        assertEquals(date.minusMonths(1), givenNextMonthDate);
    }

    @ParameterizedTest
    @MethodSource("dataForRangeWithLastDayOfMonth")
    void testRangeWithLastDayOfMonth(final LocalDate start, final LocalDate end, final List<LocalDate> expected) {
        final List<LocalDate> actual = DateTimeUtils.rangeWithLastDayOfMonth(start, end);
        assertEquals(expected, actual);
    }

    private static Stream<Arguments> dataForRangeWithLastDayOfMonth() {
        return Stream.of(
                Arguments.of(null, null, List.of()),
                Arguments.of(null, LocalDate.of(2024, 10, 10), List.of()),
                Arguments.of(LocalDate.of(2024, 8, 10), null, List.of()),
                Arguments.of(LocalDate.of(2024, 8, 10), LocalDate.of(2024, 8, 10), List.of()),
                Arguments.of(LocalDate.of(2024, 8, 10), LocalDate.of(2024, 8, 11), List.of(LocalDate.of(2024, 8, 31))),
                Arguments.of(LocalDate.of(2024, 8, 10), LocalDate.of(2024, 10, 10), List.of(LocalDate.of(2024, 8, 31), LocalDate.of(2024, 9, 30))),
                Arguments.of(LocalDate.of(2024, 8, 1), LocalDate.of(2024, 10, 31), List.of(LocalDate.of(2024, 8, 31), LocalDate.of(2024, 9, 30))),
                Arguments.of(LocalDate.of(2024, 8, 31), LocalDate.of(2024, 10, 1), List.of(LocalDate.of(2024, 8, 31), LocalDate.of(2024, 9, 30)))
        );
    }

    @ParameterizedTest
    @MethodSource("dataForIsDateOlderQuincentenaryFromNow")
    void testisDateOlderQuincentenaryFromNow(LocalDate startDate, boolean expected) {
        boolean actual = DateTimeUtils.isDateOlderQuincentenaryFromNow(startDate);

        assertEquals(expected, actual);
    }

    private static Stream<Arguments> dataForIsDateOlderQuincentenaryFromNow() {
        return Stream.of(
                Arguments.of(LocalDate.of(3024, 6, 1), false),
                Arguments.of(LocalDate.now(), false),
                Arguments.of(LocalDate.of(2024, 6, 1), false),
                Arguments.of(LocalDate.of(1523, 6, 1), true)
        );
    }

    @ParameterizedTest
    @MethodSource("dataForLimitStatDate")
    void testLimitStartDate(LocalDate startDate, LocalDate endDate, LocalDate expected) throws Exception {
        Method limitStartDateMethod = ReflectionUtils.findMethod(DateTimeUtils.class, "limitStartDate", LocalDate.class, LocalDate.class);
        ReflectionUtils.makeAccessible(limitStartDateMethod);
        LocalDate result = (LocalDate) limitStartDateMethod.invoke(null, startDate, endDate);

        assertEquals(expected, result);
    }

    private static Stream<Arguments> dataForLimitStatDate() {
        LocalDate endDate = LocalDate.of(2024, 6, 1);
        return Stream.of(
                Arguments.of(LocalDate.of(2024, 7, 1), endDate, endDate),
                Arguments.of(endDate, endDate, endDate),
                Arguments.of(LocalDate.of(2023, 6, 1), endDate, LocalDate.of(2023, 6, 1)),
                Arguments.of(LocalDate.of(1524, 6, 1), endDate, LocalDate.of(1524, 6, 1)),
                Arguments.of(LocalDate.of(1523, 6, 1), endDate, LocalDate.of(1524, 6, 1))
        );
    }
}