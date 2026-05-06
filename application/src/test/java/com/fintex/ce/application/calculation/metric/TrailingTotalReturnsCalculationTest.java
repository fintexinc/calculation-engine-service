package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.returns.TrailingTotalReturnsResult;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.TreeMap;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrailingTotalReturnsCalculationTest {

  @Test
  void shouldDelegateToFormTimeIntervalResult_whenDefiningResponseType() {
    final var calculation = mock(TrailingTotalReturnsCalculation.class);

    final var pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05", BigDecimal.ONE));

    doCallRealMethod().when(calculation).defineResponseType(anySet());
    calculation.defineResponseType(pairs);

    verify(calculation).formTimeIntervalResult(pairs);
  }

  @Test
  void shouldReturnNull_whenPeriodExceedsPortfolioSize() {
    final var calculation = mock(TrailingTotalReturnsCalculation.class);

    final var treeMap = mock(TreeMap.class);
    when(treeMap.size()).thenReturn(1);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(2);

    assertNull(actual);
  }

  @Test
  void shouldReturnNull_whenPeriodExceedsProvidedReturnsSize() {
    final var calculation = mock(TrailingTotalReturnsCalculation.class);

    final var totalReturns = mock(TreeMap.class);
    when(totalReturns.size()).thenReturn(1);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt(), any());
    final BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(2, totalReturns);

    assertNull(actual);
  }

  @Test
  void shouldCalculateTrailingTotalReturn_whenPeriodEqualsTwelveMonths() {
    final var calculation = mock(TrailingTotalReturnsCalculation.class);

    when(calculation.calculateProductForPeriod(eq(12), any())).thenReturn(TEN);

    final var treeMap = mock(TreeMap.class);
    when(treeMap.size()).thenReturn(12);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt(), any());
    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(12);

    assertEquals(0, BigDecimal.valueOf(9).compareTo(actual));
  }

  @Test
  void shouldCalculateTrailingTotalReturn_whenPeriodIsLessThanTwelveMonths() {
    final var t = mock(TrailingTotalReturnsCalculation.class);

    when(t.calculateProductForPeriod(eq(11), any())).thenReturn(TEN);

    final var treeMap = mock(TreeMap.class);
    when(treeMap.size()).thenReturn(11);
    when(t.getPortfolioTotalReturns()).thenReturn(treeMap);

    doCallRealMethod().when(t).calculatePeriodForNumberOfMonths(anyInt(), any());
    doCallRealMethod().when(t).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal actual = t.calculatePeriodForNumberOfMonths(11);

    assertEquals(0, BigDecimal.valueOf(9).compareTo(actual));
  }

  @Test
  void shouldCalculateAnnualizedTrailingTotalReturn_whenPeriodExceedsTwelveMonths() {
    final var t = mock(TrailingTotalReturnsCalculation.class);
    doCallRealMethod().when(t).calculatePeriodForNumberOfMonths(24);
    when(t.calculateProductForPeriod(eq(24), any())).thenReturn(TEN);

    final var m = mock(TreeMap.class);
    when(m.size()).thenReturn(24);
    when(t.getPortfolioTotalReturns()).thenReturn(m);

    doCallRealMethod().when(t).calculatePeriodForNumberOfMonths(anyInt(), any());
    doCallRealMethod().when(t).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal actual = t.calculatePeriodForNumberOfMonths(24);

    assertEquals(0, new BigDecimal("2.1622776601683795").compareTo(actual));
  }

  @Test
  void shouldMapIntervalResults_whenDefiningResponseType() {
    final var calculation = mock(TrailingTotalReturnsCalculation.class);

    final var pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05", ONE));

    final var expected = Set.of(
        new TimeIntervalResult("2000-01-12", ZERO),
        new TimeIntervalResult("2020-01-05", BigDecimal.ONE));
    when(calculation.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(calculation).defineResponseType(anySet());
    final TrailingTotalReturnsResult actual = calculation.defineResponseType(pairs);

    assertEquals(expected, actual.getTrailingTotalReturn());
  }

}
