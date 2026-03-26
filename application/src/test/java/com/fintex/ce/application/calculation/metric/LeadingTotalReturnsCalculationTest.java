package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.domain.model.result.core.TimeIntervalResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LeadingTotalReturnsCalculationTest {

  @Test
  void shouldDelegateToFormTimeIntervalResult_whenDefiningResponseType() {
    final var sut = mock(LeadingTotalReturnsCalculation.class);

    final var pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05", BigDecimal.ONE));

    doCallRealMethod().when(sut).defineResponseType(anySet());
    sut.defineResponseType(pairs);

    verify(sut).formTimeIntervalResult(pairs);
  }

  @Test
  void shouldReturnLeadingTotalReturn_whenDefiningResponseType() {
    final var sut = mock(LeadingTotalReturnsCalculation.class);

    final var pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05", ONE));

    final var expected = Set.of(
        new TimeIntervalResult("2000-01-12", ZERO),
        new TimeIntervalResult("2020-01-05", BigDecimal.ONE));

    when(sut.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(sut).defineResponseType(anySet());
    final var actual = sut.defineResponseType(pairs);

    assertEquals(expected, actual.getLeadingTotalReturn());
  }

  @Test
  void shouldReturnNull_whenPeriodIsGreaterThanAvailableReturns() {
    final var sut = mock(LeadingTotalReturnsCalculation.class);

    final var period = 11;
    final var portfolioTotalReturn = mock(TreeMap.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(10);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    final var actual = sut.calculatePeriodForNumberOfMonths(period);

    assertNull(actual);
  }

  @Test
  void shouldCallCalculateProductForPeriod_whenPeriodFitsReturns() {
    final var sut = mock(LeadingTotalReturnsCalculation.class);

    final var period = 6;
    final var portfolioTotalReturn = mock(TreeMap.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(120);
    when(sut.calculateProductForPeriod(period, portfolioTotalReturn)).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    sut.calculatePeriodForNumberOfMonths(period);

    verify(sut).calculateProductForPeriod(period, portfolioTotalReturn);
  }

  @Test
  void shouldSubtractOneWithoutAnnualization_whenPeriodIsLessThanTwelveMonths() {
    final var sut = mock(LeadingTotalReturnsCalculation.class);

    final var period = 6;
    final var portfolioTotalReturn = mock(TreeMap.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(120);
    when(sut.calculateProductForPeriod(period, portfolioTotalReturn)).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    final var actual = sut.calculatePeriodForNumberOfMonths(period);

    assertEquals(0, BigDecimal.valueOf(9).compareTo(actual));
  }

  @Test
  void shouldSubtractOneWithoutAnnualization_whenPeriodEqualsTwelveMonths() {
    final var sut = mock(LeadingTotalReturnsCalculation.class);

    final var period = 12;
    final var portfolioTotalReturn = mock(TreeMap.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(120);
    when(sut.calculateProductForPeriod(period, portfolioTotalReturn)).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    final var actual = sut.calculatePeriodForNumberOfMonths(period);

    assertEquals(0, BigDecimal.valueOf(9).compareTo(actual));
  }

  @Test
  void shouldAnnualizeReturn_whenPeriodIsGreaterThanTwelveMonths() {
    final var sut = mock(LeadingTotalReturnsCalculation.class);

    final var period = 24;
    final var portfolioTotalReturn = mock(TreeMap.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturn);
    when(portfolioTotalReturn.size()).thenReturn(120);
    when(sut.calculateProductForPeriod(period, portfolioTotalReturn)).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    final var actual = sut.calculatePeriodForNumberOfMonths(period);

    assertEquals(0, BigDecimal.valueOf(2.1622776601683795).compareTo(actual));
  }

  @Test
  void shouldKeepFirstNMonths_whenFilteringRequiredMonths() {
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
    final var actual = sut.filterRequiredMonthsForPeriod(period, returns);

    assertEquals(expected, actual);
  }

  @Test
  void shouldKeepSingleFirstMonth_whenFilteringForOneMonthPeriod() {
    final var sut = mock(LeadingTotalReturnsCalculation.class);

    final var period = 1;
    final NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    returns.put(LocalDate.of(2019, 1, 31), ONE);
    returns.put(LocalDate.of(2019, 2, 28), ONE);

    final NavigableMap<LocalDate, BigDecimal> expected = new TreeMap<>();
    expected.put(LocalDate.of(2019, 1, 31), ONE);

    doCallRealMethod().when(sut).filterRequiredMonthsForPeriod(anyLong(), any());
    final var actual = sut.filterRequiredMonthsForPeriod(period, returns);

    assertEquals(expected, actual);
  }

}