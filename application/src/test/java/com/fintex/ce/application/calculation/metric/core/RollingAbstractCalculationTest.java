package com.fintex.ce.application.calculation.metric.core;

import com.fintex.ce.application.util.ComparisonUtils;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.RollingIntervalResult;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class RollingAbstractCalculationTest {
  private static final LocalDate NOW = LocalDate.now();

  private static NavigableMap<LocalDate, BigDecimal> totalPortfolioReturns;

  @BeforeAll
  static void setUp() {
    totalPortfolioReturns = new TreeMap<>();
    totalPortfolioReturns.put(NOW.plusMonths(1), ONE);
    totalPortfolioReturns.put(NOW.plusMonths(2), ONE);
    totalPortfolioReturns.put(NOW.plusMonths(3), ONE);
    totalPortfolioReturns.put(NOW.plusMonths(4), ONE);
    totalPortfolioReturns.put(NOW.plusMonths(5), ONE);
    totalPortfolioReturns.put(NOW.plusMonths(6), ONE);
    totalPortfolioReturns.put(NOW.plusMonths(7), ONE);
    totalPortfolioReturns.put(NOW.plusMonths(8), ONE);
    totalPortfolioReturns.put(NOW.plusMonths(9), ONE);
    totalPortfolioReturns.put(NOW.plusMonths(10), ONE);
    totalPortfolioReturns.put(NOW.plusMonths(11), ONE);
    totalPortfolioReturns.put(NOW.plusMonths(12), ONE);
    totalPortfolioReturns.put(NOW.plusMonths(13), ONE);
  }

  @Test
  void shouldReturnNull_whenMonthsExceedAvailablePeriod() {
    final var calculation = mock(RollingAbstractCalculation.class);
    final var numberOfMonths = 120;
    final var portfolioReturns = totalPortfolioReturns;

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt(), any());
    final NavigableMap actual = calculation.calculatePeriodForNumberOfMonths(numberOfMonths, portfolioReturns);

    Assertions.assertNull(actual);
  }

  @Test
  void shouldCheckEachEntryRange_whenCalculatingRollingPeriod() {
    final var calculation = mock(RollingAbstractCalculation.class);
    final var numberOfMonths = 12;
    final var portfolioReturns = totalPortfolioReturns;
    final LocalDate startDateOfRollingReturn = NOW.plusMonths(1).plusMonths(11);
    final LocalDate ped = NOW.plusMonths(13);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt(), any());
    calculation.calculatePeriodForNumberOfMonths(numberOfMonths, portfolioReturns);

    verify(calculation).isInRange(buildEntry(1), startDateOfRollingReturn, ped);
    verify(calculation).isInRange(buildEntry(2), startDateOfRollingReturn, ped);
    verify(calculation).isInRange(buildEntry(3), startDateOfRollingReturn, ped);
    verify(calculation).isInRange(buildEntry(4), startDateOfRollingReturn, ped);
    verify(calculation).isInRange(buildEntry(5), startDateOfRollingReturn, ped);
    verify(calculation).isInRange(buildEntry(6), startDateOfRollingReturn, ped);
    verify(calculation).isInRange(buildEntry(7), startDateOfRollingReturn, ped);
    verify(calculation).isInRange(buildEntry(8), startDateOfRollingReturn, ped);
    verify(calculation).isInRange(buildEntry(9), startDateOfRollingReturn, ped);
    verify(calculation).isInRange(buildEntry(10), startDateOfRollingReturn, ped);
    verify(calculation).isInRange(buildEntry(11), startDateOfRollingReturn, ped);
    verify(calculation).isInRange(buildEntry(12), startDateOfRollingReturn, ped);
    verify(calculation).isInRange(buildEntry(13), startDateOfRollingReturn, ped);
  }

  private Map.Entry<LocalDate, BigDecimal> buildEntry(int monthsToAdd) {
    return new AbstractMap.SimpleEntry<>(NOW.plusMonths(monthsToAdd), ONE);
  }

  @Test
  void shouldCalculateRollingValueForEachEntry_whenAllEntriesAreInRange() {
    final var calculation = mock(RollingAbstractCalculation.class, withSettings().useConstructor(mock(
        PeriodCalculationInput.class),
        Set
            .of()));
    final var numberOfMonths = 12;
    final var portfolioReturns = totalPortfolioReturns;

    when(calculation.isInRange(any(), any(), any())).thenReturn(true);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt(), any());
    calculation.calculatePeriodForNumberOfMonths(numberOfMonths, portfolioReturns);

    verify(calculation, times(13)).calculateRollingValue(eq(12), any());
  }

  @Test
  void shouldRoundValuesToUserScale_whenFormattingRollingReturns() {
    final var calculation = mock(RollingAbstractCalculation.class, withSettings().useConstructor(mock(
        PeriodCalculationInput.class),
        Set
            .of()));

    final var totalPortfolioReturns = new TreeMap<LocalDate, BigDecimal>();
    totalPortfolioReturns.put(NOW.plusMonths(1), BigDecimal.valueOf(1.123456789123456));
    totalPortfolioReturns.put(NOW.plusMonths(2), BigDecimal.valueOf(1.123456789987654321));
    final var expected = new TreeMap<LocalDate, BigDecimal>();
    expected.put(NOW.plusMonths(1), BigDecimal.valueOf(1.1234567891));
    expected.put(NOW.plusMonths(2), BigDecimal.valueOf(1.1234567899));

    doCallRealMethod().when(calculation).toUserFormat(any());
    final NavigableMap<LocalDate, BigDecimal> actual = calculation.toUserFormat(totalPortfolioReturns);

    ComparisonUtils.compareMaps(expected, actual);
    assertEquals(expected.size(), actual.size());
  }

  @Test
  void shouldReturnNull_whenFormattingNullRollingReturns() {
    final var calculation = mock(RollingAbstractCalculation.class, withSettings().useConstructor(mock(
        PeriodCalculationInput.class),
        Set
            .of()));

    doCallRealMethod().when(calculation).toUserFormat(any());
    final NavigableMap<LocalDate, BigDecimal> actual = calculation.toUserFormat(null);

    Assertions.assertNull(actual);
  }

  @Test
  void shouldReturnTrue_whenEntryDateIsWithinRange() {
    final var calculation = mock(RollingAbstractCalculation.class, withSettings().useConstructor(mock(
        PeriodCalculationInput.class),
        Set
            .of()));

    final var returnEntry = new AbstractMap.SimpleEntry<>(NOW.plusMonths(1), TEN);
    final var startDateOfRollingReturn = NOW;
    final var ped = NOW.plusMonths(2);

    doCallRealMethod().when(calculation).isInRange(any(), any(), any());
    final var actual = calculation.isInRange(returnEntry, startDateOfRollingReturn, ped);

    Assertions.assertTrue(actual);
  }

  @Test
  void shouldReturnFalse_whenEntryDateIsOutsideRange() {
    final var calculation = mock(RollingAbstractCalculation.class, withSettings().useConstructor(mock(
        PeriodCalculationInput.class),
        Set
            .of()));

    final var returnEntry = new AbstractMap.SimpleEntry<>(NOW.minusMonths(1), TEN);
    final var startDateOfRollingReturn = NOW;
    final var ped = NOW.plusMonths(2);

    doCallRealMethod().when(calculation).isInRange(any(), any(), any());
    final var actual = calculation.isInRange(returnEntry, startDateOfRollingReturn, ped);

    Assertions.assertFalse(actual);
  }

  @Test
  void shouldDelegateToOverloadedMethod_whenCalculatingByMonthsOnly() {
    final var calculation = mock(RollingAbstractCalculation.class, withSettings().useConstructor(mock(
        PeriodCalculationInput.class),
        Set
            .of()));
    final var numberOfMonths = 12;

    when(calculation.getPortfolioTotalReturns()).thenReturn(new TreeMap());

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(numberOfMonths);
    verify(calculation).calculatePeriodForNumberOfMonths(12, new TreeMap<>());
  }

  @Test
  void shouldMapRollingIntervals_whenBuildingRollingIntervalResults() {
    final var calculation = mock(RollingAbstractCalculation.class, withSettings().useConstructor(mock(
        PeriodCalculationInput.class),
        Set
            .of()));
    final Map<String, NavigableMap<LocalDate, BigDecimal>> result = Map.of("12", totalPortfolioReturns);

    doCallRealMethod().when(calculation).getRollingIntervalResults(anyMap());
    final Set<RollingIntervalResult> actual = calculation.getRollingIntervalResults(result);

    assertEquals("12", actual.stream().findFirst().get().period());
    assertEquals(totalPortfolioReturns, actual.stream().findFirst().get().values());
  }

  @AfterAll
  static void tearDown() {
    totalPortfolioReturns.clear();
  }

}