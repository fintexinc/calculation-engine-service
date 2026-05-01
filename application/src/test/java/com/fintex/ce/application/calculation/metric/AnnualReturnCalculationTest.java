package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.result.KeyValueResult;
import com.fintex.ce.model.domain.result.returns.AnnualReturnResult;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.application.util.TestConstants.LOCAL_DATE_NOW;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class AnnualReturnCalculationTest {

  @Test
  void shouldPopulatePeriodDates_whenBasicDetailsArePopulated() {
    final var sut = mock(AnnualReturnCalculation.class);

    final var end = LocalDate.now().plusMonths(3);
    final var start = LocalDate.now().plusMonths(1);

    final var portfolioReturns = new TreeMap<>(Map.of(
        start, BigDecimal.ONE,
        end, BigDecimal.TEN));

    doCallRealMethod().when(sut).populateBasicDetails(any(), any());
    final AnnualReturnResult result = AnnualReturnResult.builder()
        .annualReturns(List.of())
        .warnings(List.of())
        .build();
    sut.populateBasicDetails(result, portfolioReturns);

    assertEquals(end, result.getPerformanceEndDate());
    assertEquals(start, result.getPerformanceStartDate());
    assertEquals(List.of(), result.getAnnualReturns());
  }

  @Test
  void shouldCalculateAnnualReturn_whenYearHasTwelveMonthlyReturns() {
    final var sut = mock(AnnualReturnCalculation.class);

    final var date = LocalDate.of(2020, 12, 1);

    final HashMap<LocalDate, BigDecimal> map = new HashMap<>();
    map.put(toLastDayOfMonth(date), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(1)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(2)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(3)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(4)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(5)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(6)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(7)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(8)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(9)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(10)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(11)), TEN);

    final var portfolioReturns = new TreeMap<>(map);

    doCallRealMethod().when(sut).calculateAnnualReturns(any(), any());
    final TreeMap<Integer, BigDecimal> actual = sut.calculateAnnualReturns(portfolioReturns, Set.of(date.getYear()));

    assertEquals(1, actual.size());
    assertEquals(0, actual.get(date.getYear()).compareTo(new BigDecimal("999999999999")));
  }

  @Test
  void shouldReturnEmptyAnnualReturns_whenYearHasLessThanTwelveMonthlyReturns() {
    final var sut = mock(AnnualReturnCalculation.class);

    final var date = LocalDate.of(2020, 12, 1);

    final Map<LocalDate, BigDecimal> map = new HashMap<>();
    map.put(toLastDayOfMonth(date), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(1)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(2)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(3)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(4)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(5)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(6)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(7)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(8)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(9)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(10)), TEN);

    final var portfolioReturns = new TreeMap<>(map);

    doCallRealMethod().when(sut).calculateAnnualReturns(any(), any());
    final TreeMap<Integer, BigDecimal> actual = sut.calculateAnnualReturns(portfolioReturns, Set.of(date.getYear()));

    assertEquals(0, actual.size());
  }

  @Test
  void shouldReturnEmptyAnnualReturns_whenYearDoesNotContainEndOfYearReturn() {
    final var sut = mock(AnnualReturnCalculation.class);

    final var date = LocalDate.of(2020, 12, 1);

    final Map<LocalDate, BigDecimal> map = new HashMap<>();
    map.put(toLastDayOfMonth(date.minusMonths(1)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(2)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(3)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(4)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(5)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(6)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(7)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(8)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(9)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(10)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(11)), TEN);
    map.put(toLastDayOfMonth(date.minusMonths(12)), TEN);

    final var portfolioReturns = new TreeMap<>(map);

    doCallRealMethod().when(sut).calculateAnnualReturns(any(), any());
    final TreeMap<Integer, BigDecimal> actual = sut.calculateAnnualReturns(portfolioReturns, Set.of(date.getYear()));

    assertEquals(0, actual.size());
  }

  @Test
  void shouldReturnEmptyAnnualReturns_whenYearContainsGapInMonthlyReturns() {
    final var sut = mock(AnnualReturnCalculation.class);

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

    final var portfolioReturns = new TreeMap<>(map);

    doCallRealMethod().when(sut).calculateAnnualReturns(any(), any());
    final TreeMap<Integer, BigDecimal> actual = sut.calculateAnnualReturns(portfolioReturns, Set.of(date.getYear()));

    assertEquals(0, actual.size());
  }

  @Test
  void shouldDelegateToCalculateAnnualReturns_whenCalculateIsCalled() {
    final var returns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE, LOCAL_DATE_NOW.plusMonths(1), TEN));
    final var sut = mock(AnnualReturnCalculation.class, withSettings().useConstructor(returns, List.of()));

    final HashSet<Integer> years = new HashSet<>();
    years.add(LOCAL_DATE_NOW.getYear());
    years.add(LOCAL_DATE_NOW.plusMonths(1).getYear());

    doCallRealMethod().when(sut).calculate();
    sut.calculate();

    verify(sut).calculateAnnualReturns(new TreeMap<>(returns), years);
  }

  @Test
  void shouldPopulateBasicDetails_whenCalculateIsCalled() {
    final var returns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE, LOCAL_DATE_NOW.plusMonths(1), TEN));
    final var sut = mock(AnnualReturnCalculation.class, withSettings().useConstructor(returns, List.of()));

    final TreeMap<Integer, BigDecimal> years = new TreeMap<>();
    when(sut.calculateAnnualReturns(any(), any())).thenReturn(years);

    final List<KeyValueResult> keyValueDtoS = years.entrySet().stream().map(e -> new KeyValueResult(e.getKey(), e
        .getValue()))
        .toList();

    doCallRealMethod().when(sut).calculate();
    sut.calculate();

    verify(sut).populateBasicDetails(
        argThat(arg -> (arg).getAnnualReturns().equals(keyValueDtoS)),
        eq(returns));
  }

  @Test
  void shouldReturnAnnualReturnResult_whenCalculateIsCalled() {
    final var returns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE, LOCAL_DATE_NOW.plusMonths(1), TEN));
    final var sut = mock(AnnualReturnCalculation.class, withSettings().useConstructor(returns, List.of()));

    final TreeMap<Integer, BigDecimal> param = new TreeMap<>();
    when(sut.calculateAnnualReturns(any(), any())).thenReturn(param);

    doCallRealMethod().when(sut).calculate();
    final AnnualReturnResult actual = sut.calculate();

    assertEquals(List.of(), actual.getAnnualReturns());
  }

}
