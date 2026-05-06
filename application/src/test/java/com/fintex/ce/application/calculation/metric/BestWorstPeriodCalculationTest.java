package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.model.domain.result.period.BestWorstPeriodsResult;
import com.fintex.ce.model.domain.result.period.PeriodValueResult;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class BestWorstPeriodCalculationTest {

  @Test
  void shouldCalculate_whenCalculateMonthRollingCumulativeReturnsReturnsNull() {
    final TreeMap<LocalDate, BigDecimal> portfolioReturns = null;
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    doCallRealMethod().when(calculation).calculate();
    final BestWorstPeriodsResult result = calculation.calculate();

    assertNull(result);
  }

  @Test
  void shouldCalculate_whenVerifyCalculateMonthRollingCumulativeReturns() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of(
        12L)));

    when(calculation.calculateMonthRollingCumulativeReturns(any())).thenReturn(mock(TreeMap.class));
    doNothing().when(calculation).calculateBestWorstPeriodValues(any(), any());

    doCallRealMethod().when(calculation).calculate();
    calculation.calculate();

    verify(calculation).calculateMonthRollingCumulativeReturns(any());
  }

  @Test
  void shouldCalculate_whenVerifyCalculateBestWorstPeriodValues() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of(12L,
        24L)));

    when(calculation.calculateMonthRollingCumulativeReturns(any())).thenReturn(mock(TreeMap.class));
    doNothing().when(calculation).calculateBestWorstPeriodValues(any(), any());

    doCallRealMethod().when(calculation).calculate();
    calculation.calculate();

    verify(calculation, times(2)).calculateBestWorstPeriodValues(any(), any());
  }

  @Test
  void shouldCalculate_whenCheckResult() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final TreeMap<LocalDate, BigDecimal> weightedAveragePortfolioMReturns = getPortfolioReturns();
    when(calculation.calculateMonthRollingCumulativeReturns(any())).thenReturn(mock(TreeMap.class));
    doNothing().when(calculation).calculateBestWorstPeriodValues(any(), any());

    doCallRealMethod().when(calculation).calculate();
    final BestWorstPeriodsResult result = calculation.calculate();

    assertEquals(result.getPerformanceEndDate(), LocalDate.of(2020, 12, 31));
    assertEquals(result.getPerformanceStartDate(), LocalDate.of(2019, 12, 31));
    Assertions.assertTrue(result.getBestWorstPeriods().getNumberOfPeriods().isEmpty());
    Assertions.assertTrue(result.getBestWorstPeriods().getAverage().isEmpty());
    Assertions.assertTrue(result.getBestWorstPeriods().getBestPeriodDate().isEmpty());
    Assertions.assertTrue(result.getBestWorstPeriods().getBestPeriodPct().isEmpty());
    Assertions.assertTrue(result.getBestWorstPeriods().getPctPositive().isEmpty());
    Assertions.assertTrue(result.getBestWorstPeriods().getWorstPeriodDate().isEmpty());
    Assertions.assertTrue(result.getBestWorstPeriods().getWorstPeriodPct().isEmpty());
  }

  @Test
  void shouldCalculateMonthRollingCumulativeReturns_whenCheckResult() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    doCallRealMethod().when(calculation).calculateMonthRollingCumulativeReturns(any());
    final TreeMap<LocalDate, BigDecimal> returns = calculation.calculateMonthRollingCumulativeReturns(12L);

    assertFalse(returns.isEmpty());
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("0.010943190803711")), returns.firstEntry().getValue());
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("0.145441088575715")), returns.get(LocalDate.of(2020, 11,
        30)));
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("0.146898946438724")), returns.lastEntry().getValue());
  }

  @Test
  void shouldCalculateBestWorstPeriodValues_whenVerifyGetStartOfPeriodsDate() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    when(calculation.getStartOfPeriodsDate(any(), any())).thenReturn(LocalDate.now());

    final TreeMap<LocalDate, BigDecimal> rollingCumulativeReturns = mock(TreeMap.class);
    when(rollingCumulativeReturns.lastKey()).thenReturn(LocalDate.now().plusMonths(1));

    doCallRealMethod().when(calculation).calculateBestWorstPeriodValues(any(), any());
    calculation.calculateBestWorstPeriodValues(3L, rollingCumulativeReturns);

    verify(calculation).getStartOfPeriodsDate(3L, rollingCumulativeReturns);
  }

  @Test
  void shouldCalculateBestWorstPeriodValues_whenVerifyGetSubMapByStartPeriodDate() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var startOfPeriod = LocalDate.now();
    when(calculation.getStartOfPeriodsDate(any(), any())).thenReturn(startOfPeriod);
    final var rollingCumulativeReturns = mock(TreeMap.class);
    when(rollingCumulativeReturns.lastKey()).thenReturn(startOfPeriod.plusMonths(1));
    when(calculation.getMapByPeriodStartDate(any(), any())).thenReturn(rollingCumulativeReturns);

    doCallRealMethod().when(calculation).calculateBestWorstPeriodValues(any(), any());
    calculation.calculateBestWorstPeriodValues(3L, rollingCumulativeReturns);

    verify(calculation).getMapByPeriodStartDate(rollingCumulativeReturns, startOfPeriod);
  }

  @Test
  void shouldCalculateBestWorstPeriodValues_whenVerifyCalculateNumberOfPeriods() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var startOfPeriod = LocalDate.now();
    when(calculation.getStartOfPeriodsDate(any(), any())).thenReturn(startOfPeriod);

    final var rollingCumulativeReturns = mock(TreeMap.class);
    when(rollingCumulativeReturns.lastKey()).thenReturn(startOfPeriod.plusMonths(1));
    when(calculation.getMapByPeriodStartDate(any(), any())).thenReturn(rollingCumulativeReturns);

    doCallRealMethod().when(calculation).calculateBestWorstPeriodValues(any(), any());
    calculation.calculateBestWorstPeriodValues(3L, rollingCumulativeReturns);

    verify(calculation).calculateNumberOfPeriods(3L, rollingCumulativeReturns);
  }

  @Test
  void shouldCalculateBestWorstPeriodValues_whenVerifyCalculateAverage() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var startOfPeriod = LocalDate.now();
    when(calculation.getStartOfPeriodsDate(any(), any())).thenReturn(startOfPeriod);

    final var rollingCumulativeReturns = mock(TreeMap.class);
    when(rollingCumulativeReturns.lastKey()).thenReturn(startOfPeriod.plusMonths(1));
    when(calculation.getMapByPeriodStartDate(any(), any())).thenReturn(rollingCumulativeReturns);

    doCallRealMethod().when(calculation).calculateBestWorstPeriodValues(any(), any());
    calculation.calculateBestWorstPeriodValues(3L, rollingCumulativeReturns);

    verify(calculation).calculateAverage(3L, rollingCumulativeReturns);
  }

  @Test
  void shouldCalculateBestWorstPeriodValues_whenVerifyCalculateBestPeriodValue() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var startOfPeriod = LocalDate.now();
    when(calculation.getStartOfPeriodsDate(any(), any())).thenReturn(startOfPeriod);
    final var rollingCumulativeReturns = mock(TreeMap.class);
    when(rollingCumulativeReturns.lastKey()).thenReturn(startOfPeriod.plusMonths(1));
    when(calculation.getMapByPeriodStartDate(any(), any())).thenReturn(rollingCumulativeReturns);

    doCallRealMethod().when(calculation).calculateBestWorstPeriodValues(any(), any());
    calculation.calculateBestWorstPeriodValues(3L, rollingCumulativeReturns);

    verify(calculation).calculateBestPeriodValue(3L, rollingCumulativeReturns);
  }

  @Test
  void shouldCalculateBestWorstPeriodValues_whenVerifyCalculateWorstPeriodValue() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var startOfPeriod = LocalDate.now();
    when(calculation.getStartOfPeriodsDate(any(), any())).thenReturn(startOfPeriod);
    final var rollingCumulativeReturns = mock(TreeMap.class);
    when(rollingCumulativeReturns.lastKey()).thenReturn(startOfPeriod.plusMonths(1));
    when(calculation.getMapByPeriodStartDate(any(), any())).thenReturn(rollingCumulativeReturns);

    doCallRealMethod().when(calculation).calculateBestWorstPeriodValues(any(), any());
    calculation.calculateBestWorstPeriodValues(3L, rollingCumulativeReturns);

    verify(calculation).calculateWorstPeriodValue(3L, rollingCumulativeReturns);
  }

  @Test
  void shouldCalculateBestWorstPeriodValues_whenVerifyCalculatePositive() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var startOfPeriod = LocalDate.now();
    when(calculation.getStartOfPeriodsDate(any(), any())).thenReturn(startOfPeriod);
    final var rollingCumulativeReturns = mock(TreeMap.class);
    when(rollingCumulativeReturns.lastKey()).thenReturn(startOfPeriod.plusMonths(1));
    when(calculation.getMapByPeriodStartDate(any(), any())).thenReturn(rollingCumulativeReturns);

    doCallRealMethod().when(calculation).calculateBestWorstPeriodValues(any(), any());
    calculation.calculateBestWorstPeriodValues(3L, rollingCumulativeReturns);

    verify(calculation).calculatePositive(3L, rollingCumulativeReturns);
  }

  @Test
  void shouldCalculateBestWorstPeriodValues_whenVerifyGetDefaultValues() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var startOfPeriod = LocalDate.now();
    when(calculation.getStartOfPeriodsDate(any(), any())).thenReturn(startOfPeriod);
    final var rollingCumulativeReturns = mock(TreeMap.class);
    when(rollingCumulativeReturns.lastKey()).thenReturn(startOfPeriod.minusMonths(1));
    when(calculation.getMapByPeriodStartDate(any(), any())).thenReturn(rollingCumulativeReturns);

    doCallRealMethod().when(calculation).calculateBestWorstPeriodValues(any(), any());
    calculation.calculateBestWorstPeriodValues(3L, rollingCumulativeReturns);

    verify(calculation).addDefaultValues(3L);
  }

  @Test
  void shouldGetSubMapByPeriodStartDate_whenCheckResult() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));
    final var rollingReturns = getPortfolioReturns();

    doCallRealMethod().when(calculation).getMapByPeriodStartDate(any(), any());
    final TreeMap<LocalDate, BigDecimal> returns = calculation.getMapByPeriodStartDate(rollingReturns, LocalDate.of(2020, 05,
        31));

    assertFalse(returns.isEmpty());
    assertEquals(LocalDate.of(2020, 5, 31), returns.firstKey());
    assertEquals(LocalDate.of(2020, 12, 31), returns.lastKey());
  }

  @Test
  void shouldCalculateNumberOfPeriods_whenCheckResult() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));
    final var rollingReturns = getPortfolioReturns();

    doCallRealMethod().when(calculation).calculateNumberOfPeriods(any(), any());
    calculation.calculateNumberOfPeriods(12L, rollingReturns);

    assertFalse(calculation.bestWorstPeriodData.getNumberOfPeriods().isEmpty());
    assertEquals(12L, calculation.bestWorstPeriodData.getNumberOfPeriods().get(0).period());
    assertEquals(BigDecimal.valueOf(13), calculation.bestWorstPeriodData.getNumberOfPeriods().get(0).value());
  }

  @Test
  void shouldCalculateAverage_whenCheckResultWhenPeriodIsLessThenTwelve() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var rollingReturns = getPortfolioReturns();
    doCallRealMethod().when(calculation).calculateAverage(any(), any());
    doCallRealMethod().when(calculation).annualize(any(), any());

    doCallRealMethod().when(calculation).calculateAverage(any(), any());
    doCallRealMethod().when(calculation).annualize(any(), any());
    calculation.calculateAverage(3L, rollingReturns);

    assertFalse(calculation.bestWorstPeriodData.getAverage().isEmpty());
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("1.011499443237")), DecimalUtils.toUserScale(
        calculation.bestWorstPeriodData.getAverage().get(0).value()));
    assertEquals(3L, calculation.bestWorstPeriodData.getAverage().get(0).period());
  }

  @Test
  void shouldCalculateAverage_whenVerifyAnnualize() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var rollingReturns = getPortfolioReturns();
    doCallRealMethod().when(calculation).calculateAverage(any(), any());
    calculation.calculateAverage(12L, rollingReturns);

    verify(calculation).annualize(any(), eq(12L));
  }

  @Test
  void shouldCalculateAverage_whenCheckResultWhenPeriodIsMoreThenTwelve() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var rollingReturns = getPortfolioReturns();

    doCallRealMethod().when(calculation).calculateAverage(any(), any());
    doCallRealMethod().when(calculation).annualize(any(), any());
    calculation.calculateAverage(24L, rollingReturns);

    assertFalse(calculation.bestWorstPeriodData.getAverage().isEmpty());
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("0.418273402146778")), DecimalUtils.toUserScale(
        calculation.bestWorstPeriodData.getAverage().get(0).value()));
    assertEquals(24L, calculation.bestWorstPeriodData.getAverage().get(0).period());
  }

  @Test
  void shouldCalculateBestPeriodValue_whenVerifyAnnualize() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));
    final var rollingReturns = getPortfolioReturns();

    doCallRealMethod().when(calculation).calculateBestPeriodValue(any(), any());
    calculation.calculateBestPeriodValue(12L, rollingReturns);

    verify(calculation).annualize(any(), eq(12L));
  }

  @Test
  void shouldCalculateBestPeriodValue_whenCheckResultWhenPeriodIsLessThenTwelve() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));
    final var rollingReturns = getPortfolioReturns();

    doCallRealMethod().when(calculation).calculateBestPeriodValue(any(), any());
    doCallRealMethod().when(calculation).annualize(any(), any());
    calculation.calculateBestPeriodValue(6L, rollingReturns);

    assertFalse(calculation.bestWorstPeriodData.getBestPeriodPct().isEmpty());
    assertFalse(calculation.bestWorstPeriodData.getBestPeriodDate().isEmpty());
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("1.03431353421321")), DecimalUtils.toUserScale(
        calculation.bestWorstPeriodData.getBestPeriodPct().get(0).value()));
    assertEquals(6L, calculation.bestWorstPeriodData.getBestPeriodPct().get(0).period());
    assertEquals(6L, calculation.bestWorstPeriodData.getBestPeriodDate().get(0).period());
    assertEquals(LocalDate.of(2020, 03, 31), calculation.bestWorstPeriodData.getBestPeriodDate().get(0).interval()
        .endDate());
    assertEquals(LocalDate.of(2019, 10, 1), calculation.bestWorstPeriodData.getBestPeriodDate().get(0).interval()
        .startDate());
  }

  @Test
  void shouldCalculateBestPeriodValue_whenCheckResultWhenPeriodIsMoreThenTwelve() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));
    final var rollingReturns = getPortfolioReturns();

    doCallRealMethod().when(calculation).calculateBestPeriodValue(any(), any());
    doCallRealMethod().when(calculation).annualize(any(), any());
    calculation.calculateBestPeriodValue(24L, rollingReturns);

    assertFalse(calculation.bestWorstPeriodData.getBestPeriodPct().isEmpty());
    assertFalse(calculation.bestWorstPeriodData.getBestPeriodDate().isEmpty());
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("0.426293635340637")), DecimalUtils.toUserScale(
        calculation.bestWorstPeriodData.getBestPeriodPct().get(0).value()));
    assertEquals(24L, calculation.bestWorstPeriodData.getBestPeriodPct().get(0).period());
    assertEquals(24L, calculation.bestWorstPeriodData.getBestPeriodDate().get(0).period());
    assertEquals(LocalDate.of(2020, 03, 31), calculation.bestWorstPeriodData.getBestPeriodDate().get(0).interval()
        .endDate());
    assertEquals(LocalDate.of(2018, 04, 1), calculation.bestWorstPeriodData.getBestPeriodDate().get(0).interval()
        .startDate());
  }

  @Test
  void shouldCalculateBestPeriodValue_whenCheckResultWhenPeriodIsMoreThenTwelveAndTwoWorstPeriodValueAreEqual() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var rollingReturns = getPortfolioReturns();
    rollingReturns.put(rollingReturns.lastKey(), rollingReturns.get(LocalDate.of(2020, 03, 31)));

    doCallRealMethod().when(calculation).calculateBestPeriodValue(any(), any());
    doCallRealMethod().when(calculation).annualize(any(), any());
    calculation.calculateBestPeriodValue(24L, rollingReturns);

    assertFalse(calculation.bestWorstPeriodData.getBestPeriodPct().isEmpty());
    assertFalse(calculation.bestWorstPeriodData.getBestPeriodDate().isEmpty());
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("0.426293635340637")), DecimalUtils.toUserScale(
        calculation.bestWorstPeriodData.getBestPeriodPct().get(0).value()));
    assertEquals(24L, calculation.bestWorstPeriodData.getBestPeriodPct().get(0).period());
    assertEquals(24L, calculation.bestWorstPeriodData.getBestPeriodDate().get(0).period());
    assertEquals(LocalDate.of(2020, 12, 31), calculation.bestWorstPeriodData.getBestPeriodDate().get(0).interval()
        .endDate());
    assertEquals(LocalDate.of(2019, 1, 1), calculation.bestWorstPeriodData.getBestPeriodDate().get(0).interval()
        .startDate());
  }

  @Test
  void shouldCalculateWorstPeriodValue_whenCheckResultWhenPeriodIsLessThenTwelve() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var rollingReturns = getPortfolioReturns();
    doCallRealMethod().when(calculation).calculateWorstPeriodValue(any(), any());
    doCallRealMethod().when(calculation).annualize(any(), any());
    doCallRealMethod().when(calculation).calculateWorstPeriodValue(any(), any());
    doCallRealMethod().when(calculation).annualize(any(), any());
    calculation.calculateWorstPeriodValue(6L, rollingReturns);

    assertFalse(calculation.bestWorstPeriodData.getWorstPeriodPct().isEmpty());
    assertFalse(calculation.bestWorstPeriodData.getWorstPeriodDate().isEmpty());
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("0.994895485347306")), DecimalUtils.toUserScale(
        calculation.bestWorstPeriodData.getWorstPeriodPct().get(0).value()));
    assertEquals(6L, calculation.bestWorstPeriodData.getWorstPeriodPct().get(0).period());
    assertEquals(6L, calculation.bestWorstPeriodData.getWorstPeriodDate().get(0).period());
    assertEquals(LocalDate.of(2020, 1, 31), calculation.bestWorstPeriodData.getWorstPeriodDate().get(0).interval()
        .endDate());
    assertEquals(LocalDate.of(2019, 8, 1), calculation.bestWorstPeriodData.getWorstPeriodDate().get(0).interval()
        .startDate());
  }

  @Test
  void shouldCalculateWorstPeriodValue_whenCheckResultWhenPeriodIsMoreThenTwelve() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var rollingReturns = getPortfolioReturns();
    doCallRealMethod().when(calculation).calculateWorstPeriodValue(any(), any());
    doCallRealMethod().when(calculation).annualize(any(), any());

    calculation.calculateWorstPeriodValue(24L, rollingReturns);

    assertFalse(calculation.bestWorstPeriodData.getWorstPeriodPct().isEmpty());
    assertFalse(calculation.bestWorstPeriodData.getWorstPeriodDate().isEmpty());
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("0.412407690911978")), DecimalUtils.toUserScale(
        calculation.bestWorstPeriodData.getWorstPeriodPct().get(0).value()));
    assertEquals(24L, calculation.bestWorstPeriodData.getWorstPeriodPct().get(0).period());
    assertEquals(24L, calculation.bestWorstPeriodData.getWorstPeriodDate().get(0).period());
    assertEquals(LocalDate.of(2020, 1, 31), calculation.bestWorstPeriodData.getWorstPeriodDate().get(0).interval()
        .endDate());
    assertEquals(LocalDate.of(2018, 2, 1), calculation.bestWorstPeriodData.getWorstPeriodDate().get(0).interval()
        .startDate());
  }

  @Test
  void shouldCalculateWorstPeriodValue_whenCheckResultWhenPeriodIsMoreThenTwelveAndTwoWorstPeriodValueAreEqual() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var rollingReturns = getPortfolioReturns();
    rollingReturns.put(rollingReturns.lastKey(), rollingReturns.get(LocalDate.of(2020, 1, 31)));

    doCallRealMethod().when(calculation).calculateWorstPeriodValue(any(), any());
    doCallRealMethod().when(calculation).annualize(any(), any());
    calculation.calculateWorstPeriodValue(24L, rollingReturns);

    assertFalse(calculation.bestWorstPeriodData.getWorstPeriodPct().isEmpty());
    assertFalse(calculation.bestWorstPeriodData.getWorstPeriodDate().isEmpty());
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("0.412407690911978")), DecimalUtils.toUserScale(
        calculation.bestWorstPeriodData.getWorstPeriodPct().get(0).value()));
    assertEquals(24L, calculation.bestWorstPeriodData.getWorstPeriodPct().get(0).period());
    assertEquals(24L, calculation.bestWorstPeriodData.getWorstPeriodDate().get(0).period());
    assertEquals(LocalDate.of(2020, 12, 31), calculation.bestWorstPeriodData.getWorstPeriodDate().get(0).interval()
        .endDate());
    assertEquals(LocalDate.of(2019, 1, 1), calculation.bestWorstPeriodData.getWorstPeriodDate().get(0).interval()
        .startDate());
  }

  @Test
  void shouldCalculatePositive_whenVerifyGetNumberOfPeriodsByPeriod() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var rollingReturns = getPortfolioReturns();
    when(calculation.getNumberOfPeriodsByPeriod(any())).thenReturn(new PeriodValueResult(6L, BigDecimal.valueOf(13)));
    doCallRealMethod().when(calculation).calculatePositive(any(), any());

    calculation.calculatePositive(6L, rollingReturns);

    verify(calculation).getNumberOfPeriodsByPeriod(6L);
  }

  @Test
  void shouldCalculatePositive_whenCheckResultWhenAllPositive() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var rollingReturns = getPortfolioReturns();
    when(calculation.getNumberOfPeriodsByPeriod(any())).thenReturn(new PeriodValueResult(6L, BigDecimal.valueOf(13)));

    doCallRealMethod().when(calculation).calculatePositive(any(), any());
    calculation.calculatePositive(6L, rollingReturns);

    assertFalse(calculation.bestWorstPeriodData.getPctPositive().isEmpty());
    assertEquals(6L, calculation.bestWorstPeriodData.getPctPositive().get(0).period());
    assertEquals(DecimalUtils.toUserScale(BigDecimal.valueOf(1)), DecimalUtils.toUserScale(calculation.bestWorstPeriodData
        .getPctPositive().get(0).value()));
  }

  @Test
  void shouldCalculatePositive_whenCheckResultWhenTwoRecordsAreNegative() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var rollingReturns = getPortfolioReturns();
    rollingReturns.put(rollingReturns.firstKey(), BigDecimal.valueOf(-1.01094319080371));
    rollingReturns.put(rollingReturns.lastKey(), BigDecimal.valueOf(-1.01094319080371));
    when(calculation.getNumberOfPeriodsByPeriod(any())).thenReturn(new PeriodValueResult(6L, BigDecimal.valueOf(13)));

    doCallRealMethod().when(calculation).calculatePositive(any(), any());
    calculation.calculatePositive(6L, rollingReturns);

    assertFalse(calculation.bestWorstPeriodData.getPctPositive().isEmpty());
    assertEquals(6L, calculation.bestWorstPeriodData.getPctPositive().get(0).period());
    assertEquals(DecimalUtils.toUserScale(BigDecimal.valueOf(0.846153846153846)), DecimalUtils.toUserScale(
        calculation.bestWorstPeriodData.getPctPositive().get(0).value()));
  }

  @Test
  void shouldGetNumberOfPeriodsByPeriod_whenCheckResult() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    calculation.bestWorstPeriodData.getNumberOfPeriods().add(new PeriodValueResult(6L, BigDecimal.valueOf(13)));

    doCallRealMethod().when(calculation).getNumberOfPeriodsByPeriod(any());
    final PeriodValueResult numberOfPeriodsByPeriod = calculation.getNumberOfPeriodsByPeriod(6L);

    assertEquals(6L, numberOfPeriodsByPeriod.period());
    assertEquals(BigDecimal.valueOf(13), numberOfPeriodsByPeriod.value());
  }

  @Test
  void shouldGetPeriodStartDate_whenCheckResult() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    doCallRealMethod().when(calculation).getStartOfPeriodsDate(anyLong(), any());
    final LocalDate periodStartDate = calculation.getStartOfPeriodsDate(12L, getPortfolioReturns());

    assertEquals(toLastDayOfMonth(LocalDate.of(2020, 11, 1)), periodStartDate);
  }

  @Test
  void shouldGetDefaultValues_whenCheckResult() {
    final var portfolioReturns = getPortfolioReturns();
    final var calculation = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    doCallRealMethod().when(calculation).addDefaultValues(any());
    calculation.addDefaultValues(6L);

    assertFalse(calculation.bestWorstPeriodData.getPctPositive().isEmpty());
    assertFalse(calculation.bestWorstPeriodData.getWorstPeriodDate().isEmpty());
    assertFalse(calculation.bestWorstPeriodData.getWorstPeriodPct().isEmpty());
    assertFalse(calculation.bestWorstPeriodData.getBestPeriodDate().isEmpty());
    assertFalse(calculation.bestWorstPeriodData.getBestPeriodPct().isEmpty());
    assertFalse(calculation.bestWorstPeriodData.getNumberOfPeriods().isEmpty());
    assertFalse(calculation.bestWorstPeriodData.getAverage().isEmpty());
    assertEquals(6L, calculation.bestWorstPeriodData.getPctPositive().get(0).period());
    assertEquals(null, calculation.bestWorstPeriodData.getPctPositive().get(0).value());
    assertEquals(6L, calculation.bestWorstPeriodData.getWorstPeriodDate().get(0).period());
    assertNull(calculation.bestWorstPeriodData.getWorstPeriodDate().get(0).interval());
    assertEquals(6L, calculation.bestWorstPeriodData.getWorstPeriodPct().get(0).period());
    assertEquals(null, calculation.bestWorstPeriodData.getWorstPeriodPct().get(0).value());
    assertEquals(6L, calculation.bestWorstPeriodData.getBestPeriodDate().get(0).period());
    assertNull(calculation.bestWorstPeriodData.getBestPeriodDate().get(0).interval());
    assertEquals(6L, calculation.bestWorstPeriodData.getBestPeriodPct().get(0).period());
    assertEquals(null, calculation.bestWorstPeriodData.getBestPeriodPct().get(0).value());
    assertEquals(6L, calculation.bestWorstPeriodData.getNumberOfPeriods().get(0).period());
    assertEquals(ZERO, calculation.bestWorstPeriodData.getNumberOfPeriods().get(0).value());
    assertEquals(6L, calculation.bestWorstPeriodData.getAverage().get(0).period());
    assertEquals(null, calculation.bestWorstPeriodData.getAverage().get(0).value());
  }

  private TreeMap<LocalDate, BigDecimal> getPortfolioReturns() {
    final var date = LocalDate.of(2020, 12, 1);
    Map<LocalDate, BigDecimal> map = new HashMap<>();
    map.put(toLastDayOfMonth(date), new BigDecimal("1.01222986673534"));
    map.put(toLastDayOfMonth(date.minusMonths(12)), new BigDecimal("1.01094319080371"));
    map.put(toLastDayOfMonth(date.minusMonths(11)), new BigDecimal("0.994895485347306"));
    map.put(toLastDayOfMonth(date.minusMonths(10)), new BigDecimal("1.02297440154456"));
    map.put(toLastDayOfMonth(date.minusMonths(9)), new BigDecimal("1.03431353421321"));
    map.put(toLastDayOfMonth(date.minusMonths(8)), new BigDecimal("1.01111160279157"));
    map.put(toLastDayOfMonth(date.minusMonths(7)), new BigDecimal("0.998508625796384"));
    map.put(toLastDayOfMonth(date.minusMonths(6)), new BigDecimal("0.996781991187829"));
    map.put(toLastDayOfMonth(date.minusMonths(5)), new BigDecimal("1.01213800595451"));
    map.put(toLastDayOfMonth(date.minusMonths(4)), new BigDecimal("1.02031184300726"));
    map.put(toLastDayOfMonth(date.minusMonths(3)), new BigDecimal("1.01074832088959"));
    map.put(toLastDayOfMonth(date.minusMonths(2)), new BigDecimal("1.01608812281602"));
    map.put(toLastDayOfMonth(date.minusMonths(1)), new BigDecimal("1.00844777099365"));
    return new TreeMap<>(map);
  }

}
