package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.LeadingTotalReturnsCalculation;
import com.fintex.ce.port.input.result.core.TimeIntervalResult;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static java.math.BigDecimal.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

class LeadingTotalReturnsCalculationTest {

  @Test
  void defineResponseType_verifyFormTimeIntervalResult() {
    // SETUP
    final var sut = mock(LeadingTotalReturnsCalculation.class);

    final var pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05", BigDecimal.ONE));

    doCallRealMethod().when(sut).defineResponseType(anySet());
    // ACT
    sut.defineResponseType(pairs);

    // VERIFY
    verify(sut).formTimeIntervalResult(pairs);
  }

  @Test
  void defineResponseType_checkResult() {
    // SETUP
    final var sut = mock(LeadingTotalReturnsCalculation.class);

    final var pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05", ONE));

    final var expected = Set.of(
        new TimeIntervalResult("2000-01-12", ZERO),
        new TimeIntervalResult("2020-01-05", BigDecimal.ONE));

    when(sut.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(sut).defineResponseType(anySet());
    // ACT
    final var actual = sut.defineResponseType(pairs);

    // VERIFY
    assertEquals(expected, actual.getLeadingTotalReturn());
  }

  @Test
  void calculatePeriodForNumberOfMonths_returnNullWhenPeriodIsGreaterThanTotalReturns() {
    // SETUP
    final var sut = mock(LeadingTotalReturnsCalculation.class);

    final var period = 11;
    final var portfolioTotalReturn = mock(TreeMap.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(10);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final var actual = sut.calculatePeriodForNumberOfMonths(period);

    // VERIFY
    assertNull(actual);
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyCalculateProductForPeriod() {
    // SETUP
    final var sut = mock(LeadingTotalReturnsCalculation.class);

    final var period = 6;
    final var portfolioTotalReturn = mock(TreeMap.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(120);
    when(sut.calculateProductForPeriod(period, portfolioTotalReturn)).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    sut.calculatePeriodForNumberOfMonths(period);

    // VERIFY
    verify(sut).calculateProductForPeriod(period, portfolioTotalReturn);
  }

  @Test
  void calculatePeriodForNumberOfMonths_checkResultWhenPeriodIsLessThan12() {
    // SETUP
    final var sut = mock(LeadingTotalReturnsCalculation.class);

    final var period = 6;
    final var portfolioTotalReturn = mock(TreeMap.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(120);
    when(sut.calculateProductForPeriod(period, portfolioTotalReturn)).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final var actual = sut.calculatePeriodForNumberOfMonths(period);

    // VERIFY
    assertEquals(0, BigDecimal.valueOf(9).compareTo(actual));
  }

  @Test
  void calculatePeriodForNumberOfMonths_checkResultWhenPeriodIsLessThanEqual12() {
    // SETUP
    final var sut = mock(LeadingTotalReturnsCalculation.class);

    final var period = 12;
    final var portfolioTotalReturn = mock(TreeMap.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(120);
    when(sut.calculateProductForPeriod(period, portfolioTotalReturn)).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final var actual = sut.calculatePeriodForNumberOfMonths(period);

    // VERIFY
    assertEquals(0, BigDecimal.valueOf(9).compareTo(actual));
  }

  @Test
  void calculatePeriodForNumberOfMonths_checkResultWhenPeriodIs24() {
    // SETUP
    final var sut = mock(LeadingTotalReturnsCalculation.class);

    final var period = 24;
    final var portfolioTotalReturn = mock(TreeMap.class);
    final var product = mock(BigDecimal.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(120);
    when(sut.calculateProductForPeriod(period, portfolioTotalReturn)).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final var actual = sut.calculatePeriodForNumberOfMonths(period);

    // VERIFY
    assertEquals(0, BigDecimal.valueOf(2.1622776601683795).compareTo(actual));
  }

  @Test
  void filterRequiredMonthsForPeriod_checkResult1() {
    // SETUP
    final var sut = mock(LeadingTotalReturnsCalculation.class);

    final var period = 3;
    final NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    returns.put(LocalDate.of(2019, 1, 31), ONE);
    returns.put(LocalDate.of(2019, 2, 28), ONE);
    returns.put(LocalDate.of(2019, 3, 31), ONE);
    returns.put(LocalDate.of(2019, 4, 30), ONE);
    returns.put(LocalDate.of(2019, 5, 31), ONE);

    final NavigableMap<LocalDate, BigDecimal> expected = new TreeMap<>();
    expected.put(LocalDate.of(2019, 1, 31), ONE);
    expected.put(LocalDate.of(2019, 2, 28), ONE);
    expected.put(LocalDate.of(2019, 3, 31), ONE);

    doCallRealMethod().when(sut).filterRequiredMonthsForPeriod(anyLong(), any());
    // ACT
    final var actual = sut.filterRequiredMonthsForPeriod(period, returns);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void filterRequiredMonthsForPeriod_checkResult2() {
    // SETUP
    final var sut = mock(LeadingTotalReturnsCalculation.class);

    final var period = 1;
    final NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    returns.put(LocalDate.of(2019, 1, 31), ONE);
    returns.put(LocalDate.of(2019, 2, 28), ONE);

    final NavigableMap<LocalDate, BigDecimal> expected = new TreeMap<>();
    expected.put(LocalDate.of(2019, 1, 31), ONE);

    doCallRealMethod().when(sut).filterRequiredMonthsForPeriod(anyLong(), any());
    // ACT
    final var actual = sut.filterRequiredMonthsForPeriod(period, returns);

    // VERIFY
    assertEquals(expected, actual);
  }

}