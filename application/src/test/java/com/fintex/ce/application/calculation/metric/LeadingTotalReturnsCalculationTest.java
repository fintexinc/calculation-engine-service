package com.fintex.ce.application.calculation.metric;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled("metric unsupported")
class LeadingTotalReturnsCalculationTest {

  @Test
  void shouldReturnLeadingTotalReturn_whenDefiningResponseType() {
    final var calculation = mock(LeadingTotalReturnsCalculation.class);

    final Map<String, BigDecimal> input = Map.of("2000-01-12", ZERO, "2020-01-05", ONE);

    doCallRealMethod().when(calculation).defineResponseType(anyMap());
    final var actual = calculation.defineResponseType(input);

    assertEquals(input, actual.getLeadingTotalReturn());
  }

  @Test
  void shouldReturnNull_whenPeriodIsGreaterThanAvailableReturns() {
    final var calculation = mock(LeadingTotalReturnsCalculation.class);

    final var period = 11;
    final var portfolioTotalReturn = mock(TreeMap.class);

    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(10);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final var actual = calculation.calculatePeriodForNumberOfMonths(period);

    assertNull(actual);
  }

  @Test
  void shouldCallCalculateProductForPeriod_whenPeriodFitsReturns() {
    final var calculation = mock(LeadingTotalReturnsCalculation.class);

    final var period = 6;
    final var portfolioTotalReturn = mock(TreeMap.class);

    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(120);
    when(calculation.calculateProductForPeriod(period, portfolioTotalReturn)).thenReturn(TEN);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(period);

    verify(calculation).calculateProductForPeriod(period, portfolioTotalReturn);
  }

  @Test
  void shouldSubtractOneWithoutAnnualization_whenPeriodIsLessThanTwelveMonths() {
    final var calculation = mock(LeadingTotalReturnsCalculation.class);

    final var period = 6;
    final var portfolioTotalReturn = mock(TreeMap.class);

    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(120);
    when(calculation.calculateProductForPeriod(period, portfolioTotalReturn)).thenReturn(TEN);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final var actual = calculation.calculatePeriodForNumberOfMonths(period);

    assertEquals(0, BigDecimal.valueOf(9).compareTo(actual));
  }

  @Test
  void shouldSubtractOneWithoutAnnualization_whenPeriodEqualsTwelveMonths() {
    final var calculation = mock(LeadingTotalReturnsCalculation.class);

    final var period = 12;
    final var portfolioTotalReturn = mock(TreeMap.class);

    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(120);
    when(calculation.calculateProductForPeriod(period, portfolioTotalReturn)).thenReturn(TEN);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final var actual = calculation.calculatePeriodForNumberOfMonths(period);

    assertEquals(0, BigDecimal.valueOf(9).compareTo(actual));
  }

  @Test
  void shouldAnnualizeReturn_whenPeriodIsGreaterThanTwelveMonths() {
    final var calculation = mock(LeadingTotalReturnsCalculation.class);

    final var period = 24;
    final var portfolioTotalReturn = mock(TreeMap.class);

    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(120);
    when(calculation.calculateProductForPeriod(period, portfolioTotalReturn)).thenReturn(TEN);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final var actual = calculation.calculatePeriodForNumberOfMonths(period);

    assertEquals(0, BigDecimal.valueOf(2.1622776601683795).compareTo(actual));
  }

  @Test
  void shouldKeepFirstNMonths_whenFilteringRequiredMonths() {
    final var calculation = mock(LeadingTotalReturnsCalculation.class);

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

    doCallRealMethod().when(calculation).filterRequiredMonthsForPeriod(anyLong(), any());
    final var actual = calculation.filterRequiredMonthsForPeriod(period, returns);

    assertEquals(expected, actual);
  }

  @Test
  void shouldKeepSingleFirstMonth_whenFilteringForOneMonthPeriod() {
    final var calculation = mock(LeadingTotalReturnsCalculation.class);

    final var period = 1;
    final NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    returns.put(LocalDate.of(2019, 1, 31), ONE);
    returns.put(LocalDate.of(2019, 2, 28), ONE);

    final NavigableMap<LocalDate, BigDecimal> expected = new TreeMap<>();
    expected.put(LocalDate.of(2019, 1, 31), ONE);

    doCallRealMethod().when(calculation).filterRequiredMonthsForPeriod(anyLong(), any());
    final var actual = calculation.filterRequiredMonthsForPeriod(period, returns);

    assertEquals(expected, actual);
  }

}