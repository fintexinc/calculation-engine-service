package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.model.domain.result.KeyValueResult;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class Growth10KCalculationTest {

  @Test
  void shouldCalculate_whenVerifyCalculateGrowth10K() {
    final var portfolioReturns = getPortfolioReturns();
    final var dateRange = DateRange.UNBOUNDED;
    final var calculation = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, dateRange,
        false));

    doCallRealMethod().when(calculation).calculate();
    calculation.calculate();

    verify(calculation).calculateGrowth10K(portfolioReturns);
  }

  @Test
  void shouldCalculate_whenVerifyGetPortfolioEndDate() {
    final var portfolioReturns = getPortfolioReturns();
    final var dateRange = DateRange.UNBOUNDED;
    final var calculation = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, dateRange,
        false));

    when(calculation.calculateGrowth10K(portfolioReturns)).thenReturn(mock(List.class));

    doCallRealMethod().when(calculation).calculate();
    calculation.calculate();

    verify(calculation).getPortfolioEndDate(portfolioReturns);
  }

  @Test
  void shouldCalculate_whenVerifyGetPortfolioStartDate() {
    final var portfolioReturns = getPortfolioReturns();
    final var dateRange = DateRange.UNBOUNDED;
    final var calculation = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, dateRange,
        false));

    when(calculation.calculateGrowth10K(portfolioReturns)).thenReturn(mock(List.class));

    doCallRealMethod().when(calculation).calculate();
    calculation.calculate();

    verify(calculation).getPortfolioStartDate(portfolioReturns);
  }

  @Test
  void shouldCalculateGrowth10K_whenVerifySetFirstGrowth10KValue() {
    final var portfolioReturns = getPortfolioReturns();
    final var dateRange = DateRange.UNBOUNDED;
    final var calculation = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, dateRange,
        false));

    doCallRealMethod().when(calculation).calculateGrowth10K(portfolioReturns);
    calculation.calculateGrowth10K(portfolioReturns);

    verify(calculation).setFirstGrowth10KValue(eq(portfolioReturns), any());
  }

  @Test
  void shouldCalculateGrowth10K_whenVerifyCalculateGrowth10K() {
    final var portfolioReturns = getPortfolioReturns();
    final var dateRange = DateRange.UNBOUNDED;
    final var calculation = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, dateRange,
        false));

    doCallRealMethod().when(calculation).calculateGrowth10K(portfolioReturns);
    calculation.calculateGrowth10K(portfolioReturns);

    verify(calculation).calculateGrowth10K(eq(portfolioReturns), any());
  }

  @Test
  void shouldCalculateGrowth10K_whenVerifyPopulateGrowth10KValuesAfterLastDate() {
    final var portfolioReturns = getPortfolioReturns();
    final var dateRange = DateRange.UNBOUNDED;
    final var calculation = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, dateRange,
        false));

    doCallRealMethod().when(calculation).calculateGrowth10K(portfolioReturns);
    calculation.calculateGrowth10K(portfolioReturns);

    verify(calculation).populateGrowth10KValuesAfterLastDate(eq(portfolioReturns), any());
  }

  @Test
  void shouldCalculateGrowth10K_whenVerifyReturnsEmptyMapWhenPortfolioReturnsIsNull() {
    final var calculation = mock(Growth10KCalculation.class);

    doCallRealMethod().when(calculation).calculateGrowth10K(any());
    final List<KeyValueResult> growth10K = calculation.calculateGrowth10K(null);

    assertTrue(growth10K.isEmpty());
  }

  @Test
  void shouldCalculateGrowth10K_whenVerifyReturnsEmptyMapWhenPortfolioReturnsIsEmpty() {
    final var calculation = mock(Growth10KCalculation.class);

    doCallRealMethod().when(calculation).calculateGrowth10K(any());
    final List<KeyValueResult> growth10K = calculation.calculateGrowth10K(new TreeMap<>(Map.of()));

    assertTrue(growth10K.isEmpty());
  }

  @Test
  void shouldSetFirstGrowth10KValue_whenCheckResult() {
    final var calculation = mock(Growth10KCalculation.class);
    final TreeMap<LocalDate, BigDecimal> growth10K = new TreeMap<>();
    final var portfolioReturns = getPortfolioReturns();

    doCallRealMethod().when(calculation).setFirstGrowth10KValue(any(), any());
    calculation.setFirstGrowth10KValue(portfolioReturns, growth10K);

    assertEquals(1, growth10K.size());
    assertEquals(portfolioReturns.firstKey().minusMonths(1), growth10K.firstKey());
    assertEquals(new BigDecimal(10000), growth10K.firstEntry().getValue());
  }

  @Test
  void shouldCalculateGrowth10K_whenVerifyGetGrowth10KValue() {
    final var calculation = mock(Growth10KCalculation.class);
    final TreeMap<LocalDate, BigDecimal> growth10K = new TreeMap<>();
    final var portfolioReturns = getPortfolioReturns();

    doCallRealMethod().when(calculation).calculateGrowth10K(any(), any());
    calculation.calculateGrowth10K(portfolioReturns, growth10K);

    verify(calculation, times(12)).getGrowth10KValue(any(), any());
  }

  @Test
  void shouldCalculateGrowth10K_whenCheckResultDates() {
    final var calculation = mock(Growth10KCalculation.class);
    final TreeMap<LocalDate, BigDecimal> growth10K = new TreeMap<>();
    final var portfolioReturns = getPortfolioReturns();

    doCallRealMethod().when(calculation).calculateGrowth10K(any(), any());
    calculation.calculateGrowth10K(portfolioReturns, growth10K);

    assertEquals(portfolioReturns.keySet(), growth10K.keySet());
  }

  @Test
  void shouldGetGrowth10KValue_whenCheckResultDates() {
    final var calculation = mock(Growth10KCalculation.class);
    final var portfolioReturns = getPortfolioReturns();
    final TreeMap<LocalDate, BigDecimal> growth10K = new TreeMap<>(Map.of(portfolioReturns.firstKey(), new BigDecimal(
        10300)));

    doCallRealMethod().when(calculation).getGrowth10KValue(any(), any());
    final BigDecimal growth10KValue = calculation.getGrowth10KValue(growth10K, portfolioReturns.firstEntry());

    assertEquals(growth10K.firstKey(), portfolioReturns.firstKey());
    assertEquals(toUserScale(new BigDecimal(103000.0000000000)), growth10KValue);
  }

  @Test
  void shouldPopulateGrowth10KValuesAfterLastDate_whenVerifyGetNextPortfolioReturnsMonth() {
    final var portfolioReturns = getPortfolioReturns();
    final var dateRange = DateRange.UNBOUNDED;
    final var calculation = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, dateRange,
        false));

    when(calculation.getPortfolioEndDate(any())).thenReturn(toLastDayOfMonth(LocalDate.of(2021, 5, 1)));
    when(calculation.getNextPortfolioReturnsMonth(any())).thenReturn(LocalDate.now());
    when(calculation.putDefaultGrowth10KValueAndGetNextPortfolioReturnsMonth(any(), any()))
        .thenReturn(toLastDayOfMonth(LocalDate.of(2021, 6, 1)));

    doCallRealMethod().when(calculation).populateGrowth10KValuesAfterLastDate(any(), any());
    calculation.populateGrowth10KValuesAfterLastDate(portfolioReturns, new TreeMap<>(Map.of()));

    verify(calculation).getNextPortfolioReturnsMonth(any());
  }

  @Test
  void shouldPopulateGrowth10KValuesAfterLastDate_whenVerifyPutDefaultGrowth10KValueAndGetNextPortfolioReturnsMonth() {
    final var portfolioReturns = getPortfolioReturns();
    final var dateRange = DateRange.UNBOUNDED;
    final var calculation = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, dateRange,
        false));

    when(calculation.getPortfolioEndDate(any())).thenReturn(toLastDayOfMonth(LocalDate.of(2021, 5, 1)));
    when(calculation.getNextPortfolioReturnsMonth(any())).thenReturn(LocalDate.of(2021, 5, 31));
    when(calculation.putDefaultGrowth10KValueAndGetNextPortfolioReturnsMonth(any(), any()))
        .thenReturn(toLastDayOfMonth(LocalDate.of(2021, 6, 1)));

    doCallRealMethod().when(calculation).populateGrowth10KValuesAfterLastDate(any(), any());
    calculation.populateGrowth10KValuesAfterLastDate(portfolioReturns, new TreeMap<>(Map.of()));

    verify(calculation).putDefaultGrowth10KValueAndGetNextPortfolioReturnsMonth(any(), any());
  }

  @Test
  void shouldGetNextPortfolioReturnsMonth_whenCheckResult() {
    final var calculation = mock(Growth10KCalculation.class);
    final var growth10K = new TreeMap<>(Map.of(toLastDayOfMonth(LocalDate.of(2021, 5, 1)), ONE));

    doCallRealMethod().when(calculation).getNextPortfolioReturnsMonth(any());
    final LocalDate nextPortfolioReturnsMonth = calculation.getNextPortfolioReturnsMonth(growth10K);

    assertEquals(toLastDayOfMonth(LocalDate.of(2021, 6, 1)), nextPortfolioReturnsMonth);
  }

  @Test
  void shouldGetPortfolioEndDate_whenCheckResultWhenCustomEndDateIsNull() {
    final var portfolioReturns = getPortfolioReturns();
    final var dateRange = DateRange.UNBOUNDED;
    final var calculation = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, dateRange,
        false));

    doCallRealMethod().when(calculation).getPortfolioEndDate(any());
    final LocalDate portfolioEndDate = calculation.getPortfolioEndDate(getPortfolioReturns());

    assertEquals(toLastDayOfMonth(LocalDate.of(2020, 12, 20)), portfolioEndDate);
  }

  @Test
  void shouldGetPortfolioEndDate_whenCheckResultWhenCustomEndDateIsPopulated() {
    final var portfolioReturns = getPortfolioReturns();
    final var dateRange = new DateRange(
        toLastDayOfMonth(LocalDate.of(2020, 5, 1)),
        toLastDayOfMonth(LocalDate.of(2021, 5, 1)));
    final var calculation = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, dateRange,
        false));

    doCallRealMethod().when(calculation).getPortfolioEndDate(any());
    final LocalDate portfolioEndDate = calculation.getPortfolioEndDate(getPortfolioReturns());

    assertEquals(toLastDayOfMonth(LocalDate.of(2021, 5, 20)), portfolioEndDate);
  }

  @Test
  void shouldGetPortfolioStartDate_whenCheckResultWhenCustomStartDateIsNull() {
    final var portfolioReturns = getPortfolioReturns();
    final var dateRange = DateRange.UNBOUNDED;
    final var calculation = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, dateRange,
        false));

    doCallRealMethod().when(calculation).getPortfolioStartDate(any());
    final LocalDate portfolioEndDate = calculation.getPortfolioStartDate(getPortfolioReturns());

    assertEquals(toLastDayOfMonth(LocalDate.of(2019, 12, 20)), portfolioEndDate);
  }

  @Test
  void shouldGetPortfolioStartDate_whenCheckResultWhenCustomStartDateIsPopulated() {
    final var portfolioReturns = getPortfolioReturns();
    final var dateRange = new DateRange(
        toLastDayOfMonth(LocalDate.of(2020, 5, 1)),
        toLastDayOfMonth(LocalDate.of(2021, 5, 1)));
    final var calculation = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, dateRange,
        false));

    doCallRealMethod().when(calculation).getPortfolioStartDate(any());
    final LocalDate portfolioEndDate = calculation.getPortfolioStartDate(getPortfolioReturns());

    assertEquals(toLastDayOfMonth(LocalDate.of(2020, 5, 20)), portfolioEndDate);
  }

  @Test
  void shouldPutDefaultGrowth10KValueAndGetNextPortfolioReturnsMonth_whenCheckResult() {
    final var calculation = mock(Growth10KCalculation.class);
    final var date1 = toLastDayOfMonth(LocalDate.of(2021, 5, 31));
    final var date2 = toLastDayOfMonth(LocalDate.of(2021, 4, 30));
    final var growth10K = new TreeMap<>(Map.of(date1, ONE));

    when(calculation.getNextPortfolioReturnsMonth(any())).thenReturn(LocalDate.now());
    doCallRealMethod().when(calculation).putDefaultGrowth10KValueAndGetNextPortfolioReturnsMonth(any(), any());
    calculation.putDefaultGrowth10KValueAndGetNextPortfolioReturnsMonth(growth10K, date2);

    assertEquals(2, growth10K.size());
    assertEquals(date2, growth10K.firstKey());
    assertEquals(ONE, growth10K.get(date1));
  }

  private TreeMap<LocalDate, BigDecimal> getPortfolioReturns() {
    final var date = LocalDate.of(2020, 12, 1);
    final Map<LocalDate, BigDecimal> map = new HashMap<>();
    map.put(toLastDayOfMonth(date), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(1)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(2)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(3)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(5)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(6)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(7)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(8)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(9)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(10)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(11)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(12)), TEN);
    return new TreeMap<>(map);
  }

}
