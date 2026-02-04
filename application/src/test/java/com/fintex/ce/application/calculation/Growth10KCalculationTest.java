package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.Growth10KCalculation;
import com.fintex.ce.domain.model.CommonDates;
import com.fintex.ce.application.result.core.KeyValueResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class Growth10KCalculationTest {

  @Test
  void calculate_verifyCalculateGrowth10K() {
    // SETUP
    final var portfolioReturns = getPortfolioReturns();
    final var commonDates = mock(CommonDates.class);
    final var sut = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, commonDates,
        false));

    doCallRealMethod().when(sut).calculate();
    // ACT
    sut.calculate();

    // VERIFY
    verify(sut).calculateGrowth10K(portfolioReturns);
  }

  @Test
  void calculate_verifyGetPortfolioEndDate() {
    // SETUP
    final var portfolioReturns = getPortfolioReturns();
    final var commonDates = mock(CommonDates.class);
    final var sut = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, commonDates,
        false));

    when(sut.calculateGrowth10K(portfolioReturns)).thenReturn(mock(List.class));

    doCallRealMethod().when(sut).calculate();
    // ACT
    sut.calculate();

    // VERIFY
    verify(sut).getPortfolioEndDate(portfolioReturns);
  }

  @Test
  void calculate_verifyGetPortfolioStartDate() {
    // SETUP
    final var portfolioReturns = getPortfolioReturns();
    final var commonDates = mock(CommonDates.class);
    final var sut = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, commonDates,
        false));

    when(sut.calculateGrowth10K(portfolioReturns)).thenReturn(mock(List.class));

    doCallRealMethod().when(sut).calculate();
    // ACT
    sut.calculate();

    // VERIFY
    verify(sut).getPortfolioStartDate(portfolioReturns);
  }

  @Test
  void calculateGrowth10K_verifySetFirstGrowth10KValue() {
    // SETUP
    final var portfolioReturns = getPortfolioReturns();
    final var commonDates = mock(CommonDates.class);
    final var sut = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, commonDates,
        false));

    doCallRealMethod().when(sut).calculateGrowth10K(portfolioReturns);
    // ACT
    sut.calculateGrowth10K(portfolioReturns);

    // VERIFY
    verify(sut).setFirstGrowth10KValue(eq(portfolioReturns), any());
  }

  @Test
  void calculateGrowth10K_verifyCalculateGrowth10K() {
    // SETUP
    final var portfolioReturns = getPortfolioReturns();
    final var commonDates = mock(CommonDates.class);
    final var sut = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, commonDates,
        false));

    doCallRealMethod().when(sut).calculateGrowth10K(portfolioReturns);
    // ACT
    sut.calculateGrowth10K(portfolioReturns);

    // VERIFY
    verify(sut).calculateGrowth10K(eq(portfolioReturns), any());
  }

  @Test
  void calculateGrowth10K_verifyPopulateGrowth10KValuesAfterLastDate() {
    // SETUP
    final var portfolioReturns = getPortfolioReturns();
    final var commonDates = mock(CommonDates.class);
    final var sut = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, commonDates,
        false));

    doCallRealMethod().when(sut).calculateGrowth10K(portfolioReturns);
    // ACT
    sut.calculateGrowth10K(portfolioReturns);

    // VERIFY
    verify(sut).populateGrowth10KValuesAfterLastDate(eq(portfolioReturns), any());
  }

  @Test
  void calculateGrowth10K_verifyReturnsEmptyMapWhenPortfolioReturnsIsNull() {
    // SETUP
    final var sut = mock(Growth10KCalculation.class);

    doCallRealMethod().when(sut).calculateGrowth10K(any());
    // ACT
    final List<KeyValueResult> growth10K = sut.calculateGrowth10K(null);

    // VERIFY
    assertTrue(growth10K.isEmpty());
  }

  @Test
  void calculateGrowth10K_verifyReturnsEmptyMapWhenPortfolioReturnsIsEmpty() {
    // SETUP
    final var sut = mock(Growth10KCalculation.class);

    doCallRealMethod().when(sut).calculateGrowth10K(any());
    // ACT
    final List<KeyValueResult> growth10K = sut.calculateGrowth10K(new TreeMap<>(Map.of()));

    // VERIFY
    assertTrue(growth10K.isEmpty());
  }

  @Test
  void setFirstGrowth10KValue_checkResult() {
    // SETUP
    final var sut = mock(Growth10KCalculation.class);
    final TreeMap<LocalDate, BigDecimal> growth10K = new TreeMap<>();
    final var portfolioReturns = getPortfolioReturns();

    doCallRealMethod().when(sut).setFirstGrowth10KValue(any(), any());
    // ACT
    sut.setFirstGrowth10KValue(portfolioReturns, growth10K);

    // VERIFY
    assertEquals(1, growth10K.size());
    assertEquals(portfolioReturns.firstKey().minusMonths(1), growth10K.firstKey());
    assertEquals(new BigDecimal(10000), growth10K.firstEntry().getValue());
  }

  @Test
  void calculateGrowth10K_verifyGetGrowth10KValue() {
    // SETUP
    final var sut = mock(Growth10KCalculation.class);
    final TreeMap<LocalDate, BigDecimal> growth10K = new TreeMap<>();
    final var portfolioReturns = getPortfolioReturns();

    doCallRealMethod().when(sut).calculateGrowth10K(any(), any());
    // ACT
    sut.calculateGrowth10K(portfolioReturns, growth10K);

    // VERIFY
    verify(sut, times(12)).getGrowth10KValue(any(), any());
  }

  @Test
  void calculateGrowth10K_checkResultDates() {
    // SETUP
    final var sut = mock(Growth10KCalculation.class);
    final TreeMap<LocalDate, BigDecimal> growth10K = new TreeMap<>();
    final var portfolioReturns = getPortfolioReturns();

    doCallRealMethod().when(sut).calculateGrowth10K(any(), any());
    // ACT
    sut.calculateGrowth10K(portfolioReturns, growth10K);

    // VERIFY
    assertEquals(portfolioReturns.keySet(), growth10K.keySet());
  }

  @Test
  void getGrowth10KValue_checkResultDates() {
    // SETUP
    final var sut = mock(Growth10KCalculation.class);
    final var portfolioReturns = getPortfolioReturns();
    final TreeMap<LocalDate, BigDecimal> growth10K = new TreeMap<>(Map.of(portfolioReturns.firstKey(), new BigDecimal(
        10300)));

    doCallRealMethod().when(sut).getGrowth10KValue(any(), any());
    // ACT
    final BigDecimal growth10KValue = sut.getGrowth10KValue(growth10K, portfolioReturns.firstEntry());

    // VERIFY
    assertEquals(growth10K.firstKey(), portfolioReturns.firstKey());
    assertEquals(toUserScale(new BigDecimal(103000.0000000000)), growth10KValue);
  }

  @Test
  void populateGrowth10KValuesAfterLastDate_verifyGetNextPortfolioReturnsMonth() {
    // SETUP
    final var portfolioReturns = getPortfolioReturns();
    final var commonDates = mock(CommonDates.class);
    final var sut = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, commonDates,
        false));

    when(sut.getPortfolioEndDate(any())).thenReturn(toLastDayOfMonth(LocalDate.of(2021, 5, 1)));
    when(sut.getNextPortfolioReturnsMonth(any())).thenReturn(LocalDate.now());
    when(sut.putDefaultGrowth10KValueAndGetNextPortfolioReturnsMonth(any(), any()))
        .thenReturn(toLastDayOfMonth(LocalDate.of(2021, 6, 1)));

    doCallRealMethod().when(sut).populateGrowth10KValuesAfterLastDate(any(), any());
    // ACT
    sut.populateGrowth10KValuesAfterLastDate(portfolioReturns, new TreeMap<>(Map.of()));

    // VERIFY
    verify(sut).getNextPortfolioReturnsMonth(any());
  }

  @Test
  void populateGrowth10KValuesAfterLastDate_verifyPutDefaultGrowth10KValueAndGetNextPortfolioReturnsMonth() {
    // SETUP
    final var portfolioReturns = getPortfolioReturns();
    final var commonDates = mock(CommonDates.class);
    final var sut = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, commonDates,
        false));

    when(sut.getPortfolioEndDate(any())).thenReturn(toLastDayOfMonth(LocalDate.of(2021, 5, 1)));
    when(sut.getNextPortfolioReturnsMonth(any())).thenReturn(LocalDate.of(2021, 5, 31));
    when(sut.putDefaultGrowth10KValueAndGetNextPortfolioReturnsMonth(any(), any()))
        .thenReturn(toLastDayOfMonth(LocalDate.of(2021, 6, 1)));

    doCallRealMethod().when(sut).populateGrowth10KValuesAfterLastDate(any(), any());
    // ACT
    sut.populateGrowth10KValuesAfterLastDate(portfolioReturns, new TreeMap<>(Map.of()));

    // VERIFY
    verify(sut).putDefaultGrowth10KValueAndGetNextPortfolioReturnsMonth(any(), any());
  }

  @Test
  void getNextPortfolioReturnsMonth_checkResult() {
    // SETUP
    final var sut = mock(Growth10KCalculation.class);
    final var growth10K = new TreeMap<>(Map.of(toLastDayOfMonth(LocalDate.of(2021, 5, 1)), ONE));

    doCallRealMethod().when(sut).getNextPortfolioReturnsMonth(any());
    // ACT
    final LocalDate nextPortfolioReturnsMonth = sut.getNextPortfolioReturnsMonth(growth10K);

    // VERIFY
    assertEquals(toLastDayOfMonth(LocalDate.of(2021, 6, 1)), nextPortfolioReturnsMonth);
  }

  @Test
  void getPortfolioEndDate_checkResultWhenCustomEndDateIsNull() {
    // SETUP
    final var portfolioReturns = getPortfolioReturns();
    final var commonDates = mock(CommonDates.class);
    final var sut = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, commonDates,
        false));

    doCallRealMethod().when(sut).getPortfolioEndDate(any());
    // ACT
    final LocalDate portfolioEndDate = sut.getPortfolioEndDate(getPortfolioReturns());

    // VERIFY
    assertEquals(toLastDayOfMonth(LocalDate.of(2020, 12, 20)), portfolioEndDate);
  }

  @Test
  void getPortfolioEndDate_checkResultWhenCustomEndDateIsPopulated() {
    // SETUP
    final var portfolioReturns = getPortfolioReturns();
    final var commonDates = new CommonDates(
        toLastDayOfMonth(LocalDate.of(2020, 5, 1)),
        toLastDayOfMonth(LocalDate.of(2021, 5, 1)));
    final var sut = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, commonDates,
        false));

    doCallRealMethod().when(sut).getPortfolioEndDate(any());
    // ACT
    final LocalDate portfolioEndDate = sut.getPortfolioEndDate(getPortfolioReturns());

    // VERIFY
    assertEquals(toLastDayOfMonth(LocalDate.of(2021, 5, 20)), portfolioEndDate);
  }

  @Test
  void getPortfolioStartDate_checkResultWhenCustomStartDateIsNull() {
    // SETUP
    final var portfolioReturns = getPortfolioReturns();
    final var commonDates = mock(CommonDates.class);
    final var sut = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, commonDates,
        false));

    doCallRealMethod().when(sut).getPortfolioStartDate(any());
    // ACT
    final LocalDate portfolioEndDate = sut.getPortfolioStartDate(getPortfolioReturns());

    // VERIFY
    assertEquals(toLastDayOfMonth(LocalDate.of(2019, 12, 20)), portfolioEndDate);
  }

  @Test
  void getPortfolioStartDate_checkResultWhenCustomStartDateIsPopulated() {
    // SETUP
    final var portfolioReturns = getPortfolioReturns();
    final var commonDates = new CommonDates(
        toLastDayOfMonth(LocalDate.of(2020, 5, 1)),
        toLastDayOfMonth(LocalDate.of(2021, 5, 1)));
    final var sut = mock(Growth10KCalculation.class, withSettings().useConstructor(portfolioReturns, commonDates,
        false));

    doCallRealMethod().when(sut).getPortfolioStartDate(any());
    // ACT
    final LocalDate portfolioEndDate = sut.getPortfolioStartDate(getPortfolioReturns());

    // VERIFY
    assertEquals(toLastDayOfMonth(LocalDate.of(2020, 5, 20)), portfolioEndDate);
  }

  @Test
  void putDefaultGrowth10KValueAndGetNextPortfolioReturnsMonth_checkResult() {
    // SETUP
    final var sut = mock(Growth10KCalculation.class);
    final var date1 = toLastDayOfMonth(LocalDate.of(2021, 5, 31));
    final var date2 = toLastDayOfMonth(LocalDate.of(2021, 4, 30));
    final var growth10K = new TreeMap<>(Map.of(date1, ONE));

    when(sut.getNextPortfolioReturnsMonth(any())).thenReturn(LocalDate.now());
    doCallRealMethod().when(sut).putDefaultGrowth10KValueAndGetNextPortfolioReturnsMonth(any(), any());
    // ACT
    sut.putDefaultGrowth10KValueAndGetNextPortfolioReturnsMonth(growth10K, date2);

    // VERIFY
    assertEquals(2, growth10K.size());
    assertEquals(date2, growth10K.firstKey());
    assertEquals(ONE, growth10K.get(date1));
  }

  // TODO: 9/14/2020 add beforeAll and AfterAll methods
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
