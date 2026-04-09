package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.domain.model.result.BestWorstPeriodsResult;
import com.fintex.ce.domain.model.result.bestworstperiods.PeriodValueResult;
import com.fintex.ce.util.DecimalUtils;

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
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    doCallRealMethod().when(sut).calculate();
    final BestWorstPeriodsResult responseDTO = sut.calculate();

    assertNull(responseDTO);
  }

  @Test
  void shouldCalculate_whenVerifyCalculateMonthRollingCumulativeReturns() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of(
        12L)));

    when(sut.calculateMonthRollingCumulativeReturns(any())).thenReturn(mock(TreeMap.class));
    doNothing().when(sut).calculateBestWorstPeriodValues(any(), any());

    doCallRealMethod().when(sut).calculate();
    sut.calculate();

    verify(sut).calculateMonthRollingCumulativeReturns(any());
  }

  @Test
  void shouldCalculate_whenVerifyCalculateBestWorstPeriodValues() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of(12L,
        24L)));

    when(sut.calculateMonthRollingCumulativeReturns(any())).thenReturn(mock(TreeMap.class));
    doNothing().when(sut).calculateBestWorstPeriodValues(any(), any());

    doCallRealMethod().when(sut).calculate();
    sut.calculate();

    verify(sut, times(2)).calculateBestWorstPeriodValues(any(), any());
  }

  @Test
  void shouldCalculate_whenCheckResult() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final TreeMap<LocalDate, BigDecimal> weightedAveragePortfolioMReturns = getPortfolioReturns();
    when(sut.calculateMonthRollingCumulativeReturns(any())).thenReturn(mock(TreeMap.class));
    doNothing().when(sut).calculateBestWorstPeriodValues(any(), any());

    doCallRealMethod().when(sut).calculate();
    final BestWorstPeriodsResult responseDTO = sut.calculate();

    assertEquals(responseDTO.getPed(), LocalDate.of(2020, 12, 31));
    assertEquals(responseDTO.getPsd(), LocalDate.of(2019, 12, 31));
    Assertions.assertTrue(responseDTO.getBestWorstPeriods().getNumberOfPeriods().isEmpty());
    Assertions.assertTrue(responseDTO.getBestWorstPeriods().getAverage().isEmpty());
    Assertions.assertTrue(responseDTO.getBestWorstPeriods().getBestPeriodDate().isEmpty());
    Assertions.assertTrue(responseDTO.getBestWorstPeriods().getBestPeriodPct().isEmpty());
    Assertions.assertTrue(responseDTO.getBestWorstPeriods().getPctPositive().isEmpty());
    Assertions.assertTrue(responseDTO.getBestWorstPeriods().getWorstPeriodDate().isEmpty());
    Assertions.assertTrue(responseDTO.getBestWorstPeriods().getWorstPeriodPct().isEmpty());
  }

  @Test
  void shouldCalculateMonthRollingCumulativeReturns_whenCheckResult() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    doCallRealMethod().when(sut).calculateMonthRollingCumulativeReturns(any());
    final TreeMap<LocalDate, BigDecimal> returns = sut.calculateMonthRollingCumulativeReturns(12L);

    assertFalse(returns.isEmpty());
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("0.010943190803711")), returns.firstEntry().getValue());
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("0.145441088575715")), returns.get(LocalDate.of(2020, 11,
        30)));
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("0.146898946438724")), returns.lastEntry().getValue());
  }

  @Test
  void shouldCalculateBestWorstPeriodValues_whenVerifyGetStartOfPeriodsDate() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    when(sut.getStartOfPeriodsDate(any(), any())).thenReturn(LocalDate.now());

    final TreeMap<LocalDate, BigDecimal> rollingCumulativeReturns = mock(TreeMap.class);
    when(rollingCumulativeReturns.lastKey()).thenReturn(LocalDate.now().plusMonths(1));

    doCallRealMethod().when(sut).calculateBestWorstPeriodValues(any(), any());
    sut.calculateBestWorstPeriodValues(3L, rollingCumulativeReturns);

    verify(sut).getStartOfPeriodsDate(3L, rollingCumulativeReturns);
  }

  @Test
  void shouldCalculateBestWorstPeriodValues_whenVerifyGetSubMapByStartPeriodDate() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var startOfPeriod = LocalDate.now();
    when(sut.getStartOfPeriodsDate(any(), any())).thenReturn(startOfPeriod);
    final var rollingCumulativeReturns = mock(TreeMap.class);
    when(rollingCumulativeReturns.lastKey()).thenReturn(startOfPeriod.plusMonths(1));
    when(sut.getMapByPeriodStartDate(any(), any())).thenReturn(rollingCumulativeReturns);

    doCallRealMethod().when(sut).calculateBestWorstPeriodValues(any(), any());
    sut.calculateBestWorstPeriodValues(3L, rollingCumulativeReturns);

    verify(sut).getMapByPeriodStartDate(rollingCumulativeReturns, startOfPeriod);
  }

  @Test
  void shouldCalculateBestWorstPeriodValues_whenVerifyCalculateNumberOfPeriods() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var startOfPeriod = LocalDate.now();
    when(sut.getStartOfPeriodsDate(any(), any())).thenReturn(startOfPeriod);

    final var rollingCumulativeReturns = mock(TreeMap.class);
    when(rollingCumulativeReturns.lastKey()).thenReturn(startOfPeriod.plusMonths(1));
    when(sut.getMapByPeriodStartDate(any(), any())).thenReturn(rollingCumulativeReturns);

    doCallRealMethod().when(sut).calculateBestWorstPeriodValues(any(), any());
    sut.calculateBestWorstPeriodValues(3L, rollingCumulativeReturns);

    verify(sut).calculateNumberOfPeriods(3L, rollingCumulativeReturns);
  }

  @Test
  void shouldCalculateBestWorstPeriodValues_whenVerifyCalculateAverage() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var startOfPeriod = LocalDate.now();
    when(sut.getStartOfPeriodsDate(any(), any())).thenReturn(startOfPeriod);

    final var rollingCumulativeReturns = mock(TreeMap.class);
    when(rollingCumulativeReturns.lastKey()).thenReturn(startOfPeriod.plusMonths(1));
    when(sut.getMapByPeriodStartDate(any(), any())).thenReturn(rollingCumulativeReturns);

    doCallRealMethod().when(sut).calculateBestWorstPeriodValues(any(), any());
    sut.calculateBestWorstPeriodValues(3L, rollingCumulativeReturns);

    verify(sut).calculateAverage(3L, rollingCumulativeReturns);
  }

  @Test
  void shouldCalculateBestWorstPeriodValues_whenVerifyCalculateBestPeriodValue() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var startOfPeriod = LocalDate.now();
    when(sut.getStartOfPeriodsDate(any(), any())).thenReturn(startOfPeriod);
    final var rollingCumulativeReturns = mock(TreeMap.class);
    when(rollingCumulativeReturns.lastKey()).thenReturn(startOfPeriod.plusMonths(1));
    when(sut.getMapByPeriodStartDate(any(), any())).thenReturn(rollingCumulativeReturns);

    doCallRealMethod().when(sut).calculateBestWorstPeriodValues(any(), any());
    sut.calculateBestWorstPeriodValues(3L, rollingCumulativeReturns);

    verify(sut).calculateBestPeriodValue(3L, rollingCumulativeReturns);
  }

  @Test
  void shouldCalculateBestWorstPeriodValues_whenVerifyCalculateWorstPeriodValue() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var startOfPeriod = LocalDate.now();
    when(sut.getStartOfPeriodsDate(any(), any())).thenReturn(startOfPeriod);
    final var rollingCumulativeReturns = mock(TreeMap.class);
    when(rollingCumulativeReturns.lastKey()).thenReturn(startOfPeriod.plusMonths(1));
    when(sut.getMapByPeriodStartDate(any(), any())).thenReturn(rollingCumulativeReturns);

    doCallRealMethod().when(sut).calculateBestWorstPeriodValues(any(), any());
    sut.calculateBestWorstPeriodValues(3L, rollingCumulativeReturns);

    verify(sut).calculateWorstPeriodValue(3L, rollingCumulativeReturns);
  }

  @Test
  void shouldCalculateBestWorstPeriodValues_whenVerifyCalculatePositive() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var startOfPeriod = LocalDate.now();
    when(sut.getStartOfPeriodsDate(any(), any())).thenReturn(startOfPeriod);
    final var rollingCumulativeReturns = mock(TreeMap.class);
    when(rollingCumulativeReturns.lastKey()).thenReturn(startOfPeriod.plusMonths(1));
    when(sut.getMapByPeriodStartDate(any(), any())).thenReturn(rollingCumulativeReturns);

    doCallRealMethod().when(sut).calculateBestWorstPeriodValues(any(), any());
    sut.calculateBestWorstPeriodValues(3L, rollingCumulativeReturns);

    verify(sut).calculatePositive(3L, rollingCumulativeReturns);
  }

  @Test
  void shouldCalculateBestWorstPeriodValues_whenVerifyGetDefaultValues() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var startOfPeriod = LocalDate.now();
    when(sut.getStartOfPeriodsDate(any(), any())).thenReturn(startOfPeriod);
    final var rollingCumulativeReturns = mock(TreeMap.class);
    when(rollingCumulativeReturns.lastKey()).thenReturn(startOfPeriod.minusMonths(1));
    when(sut.getMapByPeriodStartDate(any(), any())).thenReturn(rollingCumulativeReturns);

    doCallRealMethod().when(sut).calculateBestWorstPeriodValues(any(), any());
    sut.calculateBestWorstPeriodValues(3L, rollingCumulativeReturns);

    verify(sut).addDefaultValues(3L);
  }

  @Test
  void shouldGetSubMapByPeriodStartDate_whenCheckResult() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));
    final var rollingReturns = getPortfolioReturns();

    doCallRealMethod().when(sut).getMapByPeriodStartDate(any(), any());
    final TreeMap<LocalDate, BigDecimal> returns = sut.getMapByPeriodStartDate(rollingReturns, LocalDate.of(2020, 05,
        31));

    assertFalse(returns.isEmpty());
    assertEquals(LocalDate.of(2020, 5, 31), returns.firstKey());
    assertEquals(LocalDate.of(2020, 12, 31), returns.lastKey());
  }

  @Test
  void shouldCalculateNumberOfPeriods_whenCheckResult() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));
    final var rollingReturns = getPortfolioReturns();

    doCallRealMethod().when(sut).calculateNumberOfPeriods(any(), any());
    sut.calculateNumberOfPeriods(12L, rollingReturns);

    assertFalse(sut.bestWorstPeriodDTO.getNumberOfPeriods().isEmpty());
    assertEquals(12L, sut.bestWorstPeriodDTO.getNumberOfPeriods().get(0).getPeriod());
    assertEquals(BigDecimal.valueOf(13), sut.bestWorstPeriodDTO.getNumberOfPeriods().get(0).getValue());
  }

  @Test
  void shouldCalculateAverage_whenCheckResultWhenPeriodIsLessThenTwelve() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var rollingReturns = getPortfolioReturns();
    doCallRealMethod().when(sut).calculateAverage(any(), any());
    doCallRealMethod().when(sut).annualize(any(), any());

    doCallRealMethod().when(sut).calculateAverage(any(), any());
    doCallRealMethod().when(sut).annualize(any(), any());
    sut.calculateAverage(3L, rollingReturns);

    assertFalse(sut.bestWorstPeriodDTO.getAverage().isEmpty());
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("1.011499443237")), DecimalUtils.toUserScale(
        sut.bestWorstPeriodDTO.getAverage().get(0).getValue()));
    assertEquals(3L, sut.bestWorstPeriodDTO.getAverage().get(0).getPeriod());
  }

  @Test
  void shouldCalculateAverage_whenVerifyAnnualize() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var rollingReturns = getPortfolioReturns();
    doCallRealMethod().when(sut).calculateAverage(any(), any());
    sut.calculateAverage(12L, rollingReturns);

    verify(sut).annualize(any(), eq(12L));
  }

  @Test
  void shouldCalculateAverage_whenCheckResultWhenPeriodIsMoreThenTwelve() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var rollingReturns = getPortfolioReturns();

    doCallRealMethod().when(sut).calculateAverage(any(), any());
    doCallRealMethod().when(sut).annualize(any(), any());
    sut.calculateAverage(24L, rollingReturns);

    assertFalse(sut.bestWorstPeriodDTO.getAverage().isEmpty());
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("0.418273402146778")), DecimalUtils.toUserScale(
        sut.bestWorstPeriodDTO.getAverage().get(0).getValue()));
    assertEquals(24L, sut.bestWorstPeriodDTO.getAverage().get(0).getPeriod());
  }

  @Test
  void shouldCalculateBestPeriodValue_whenVerifyAnnualize() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));
    final var rollingReturns = getPortfolioReturns();

    doCallRealMethod().when(sut).calculateBestPeriodValue(any(), any());
    sut.calculateBestPeriodValue(12L, rollingReturns);

    verify(sut).annualize(any(), eq(12L));
  }

  @Test
  void shouldCalculateBestPeriodValue_whenCheckResultWhenPeriodIsLessThenTwelve() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));
    final var rollingReturns = getPortfolioReturns();

    doCallRealMethod().when(sut).calculateBestPeriodValue(any(), any());
    doCallRealMethod().when(sut).annualize(any(), any());
    sut.calculateBestPeriodValue(6L, rollingReturns);

    assertFalse(sut.bestWorstPeriodDTO.getBestPeriodPct().isEmpty());
    assertFalse(sut.bestWorstPeriodDTO.getBestPeriodDate().isEmpty());
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("1.03431353421321")), DecimalUtils.toUserScale(
        sut.bestWorstPeriodDTO.getBestPeriodPct().get(0).getValue()));
    assertEquals(6L, sut.bestWorstPeriodDTO.getBestPeriodPct().get(0).getPeriod());
    assertEquals(6L, sut.bestWorstPeriodDTO.getBestPeriodDate().get(0).getPeriod());
    assertEquals(LocalDate.of(2020, 03, 31), sut.bestWorstPeriodDTO.getBestPeriodDate().get(0).getInterval()
        .getEndDate());
    assertEquals(LocalDate.of(2019, 10, 1), sut.bestWorstPeriodDTO.getBestPeriodDate().get(0).getInterval()
        .getStartDate());
  }

  @Test
  void shouldCalculateBestPeriodValue_whenCheckResultWhenPeriodIsMoreThenTwelve() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));
    final var rollingReturns = getPortfolioReturns();

    doCallRealMethod().when(sut).calculateBestPeriodValue(any(), any());
    doCallRealMethod().when(sut).annualize(any(), any());
    sut.calculateBestPeriodValue(24L, rollingReturns);

    assertFalse(sut.bestWorstPeriodDTO.getBestPeriodPct().isEmpty());
    assertFalse(sut.bestWorstPeriodDTO.getBestPeriodDate().isEmpty());
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("0.426293635340637")), DecimalUtils.toUserScale(
        sut.bestWorstPeriodDTO.getBestPeriodPct().get(0).getValue()));
    assertEquals(24L, sut.bestWorstPeriodDTO.getBestPeriodPct().get(0).getPeriod());
    assertEquals(24L, sut.bestWorstPeriodDTO.getBestPeriodDate().get(0).getPeriod());
    assertEquals(LocalDate.of(2020, 03, 31), sut.bestWorstPeriodDTO.getBestPeriodDate().get(0).getInterval()
        .getEndDate());
    assertEquals(LocalDate.of(2018, 04, 1), sut.bestWorstPeriodDTO.getBestPeriodDate().get(0).getInterval()
        .getStartDate());
  }

  @Test
  void shouldCalculateBestPeriodValue_whenCheckResultWhenPeriodIsMoreThenTwelveAndTwoWorstPeriodValueAreEqual() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var rollingReturns = getPortfolioReturns();
    rollingReturns.put(rollingReturns.lastKey(), rollingReturns.get(LocalDate.of(2020, 03, 31)));

    doCallRealMethod().when(sut).calculateBestPeriodValue(any(), any());
    doCallRealMethod().when(sut).annualize(any(), any());
    sut.calculateBestPeriodValue(24L, rollingReturns);

    assertFalse(sut.bestWorstPeriodDTO.getBestPeriodPct().isEmpty());
    assertFalse(sut.bestWorstPeriodDTO.getBestPeriodDate().isEmpty());
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("0.426293635340637")), DecimalUtils.toUserScale(
        sut.bestWorstPeriodDTO.getBestPeriodPct().get(0).getValue()));
    assertEquals(24L, sut.bestWorstPeriodDTO.getBestPeriodPct().get(0).getPeriod());
    assertEquals(24L, sut.bestWorstPeriodDTO.getBestPeriodDate().get(0).getPeriod());
    assertEquals(LocalDate.of(2020, 12, 31), sut.bestWorstPeriodDTO.getBestPeriodDate().get(0).getInterval()
        .getEndDate());
    assertEquals(LocalDate.of(2019, 1, 1), sut.bestWorstPeriodDTO.getBestPeriodDate().get(0).getInterval()
        .getStartDate());
  }

  @Test
  void shouldCalculateWorstPeriodValue_whenCheckResultWhenPeriodIsLessThenTwelve() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var rollingReturns = getPortfolioReturns();
    doCallRealMethod().when(sut).calculateWorstPeriodValue(any(), any());
    doCallRealMethod().when(sut).annualize(any(), any());
    doCallRealMethod().when(sut).calculateWorstPeriodValue(any(), any());
    doCallRealMethod().when(sut).annualize(any(), any());
    sut.calculateWorstPeriodValue(6L, rollingReturns);

    assertFalse(sut.bestWorstPeriodDTO.getWorstPeriodPct().isEmpty());
    assertFalse(sut.bestWorstPeriodDTO.getWorstPeriodDate().isEmpty());
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("0.994895485347306")), DecimalUtils.toUserScale(
        sut.bestWorstPeriodDTO.getWorstPeriodPct().get(0).getValue()));
    assertEquals(6L, sut.bestWorstPeriodDTO.getWorstPeriodPct().get(0).getPeriod());
    assertEquals(6L, sut.bestWorstPeriodDTO.getWorstPeriodDate().get(0).getPeriod());
    assertEquals(LocalDate.of(2020, 1, 31), sut.bestWorstPeriodDTO.getWorstPeriodDate().get(0).getInterval()
        .getEndDate());
    assertEquals(LocalDate.of(2019, 8, 1), sut.bestWorstPeriodDTO.getWorstPeriodDate().get(0).getInterval()
        .getStartDate());
  }

  @Test
  void shouldCalculateWorstPeriodValue_whenCheckResultWhenPeriodIsMoreThenTwelve() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var rollingReturns = getPortfolioReturns();
    doCallRealMethod().when(sut).calculateWorstPeriodValue(any(), any());
    doCallRealMethod().when(sut).annualize(any(), any());

    sut.calculateWorstPeriodValue(24L, rollingReturns);

    assertFalse(sut.bestWorstPeriodDTO.getWorstPeriodPct().isEmpty());
    assertFalse(sut.bestWorstPeriodDTO.getWorstPeriodDate().isEmpty());
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("0.412407690911978")), DecimalUtils.toUserScale(
        sut.bestWorstPeriodDTO.getWorstPeriodPct().get(0).getValue()));
    assertEquals(24L, sut.bestWorstPeriodDTO.getWorstPeriodPct().get(0).getPeriod());
    assertEquals(24L, sut.bestWorstPeriodDTO.getWorstPeriodDate().get(0).getPeriod());
    assertEquals(LocalDate.of(2020, 1, 31), sut.bestWorstPeriodDTO.getWorstPeriodDate().get(0).getInterval()
        .getEndDate());
    assertEquals(LocalDate.of(2018, 2, 1), sut.bestWorstPeriodDTO.getWorstPeriodDate().get(0).getInterval()
        .getStartDate());
  }

  @Test
  void shouldCalculateWorstPeriodValue_whenCheckResultWhenPeriodIsMoreThenTwelveAndTwoWorstPeriodValueAreEqual() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var rollingReturns = getPortfolioReturns();
    rollingReturns.put(rollingReturns.lastKey(), rollingReturns.get(LocalDate.of(2020, 1, 31)));

    doCallRealMethod().when(sut).calculateWorstPeriodValue(any(), any());
    doCallRealMethod().when(sut).annualize(any(), any());
    sut.calculateWorstPeriodValue(24L, rollingReturns);

    assertFalse(sut.bestWorstPeriodDTO.getWorstPeriodPct().isEmpty());
    assertFalse(sut.bestWorstPeriodDTO.getWorstPeriodDate().isEmpty());
    assertEquals(DecimalUtils.toUserScale(new BigDecimal("0.412407690911978")), DecimalUtils.toUserScale(
        sut.bestWorstPeriodDTO.getWorstPeriodPct().get(0).getValue()));
    assertEquals(24L, sut.bestWorstPeriodDTO.getWorstPeriodPct().get(0).getPeriod());
    assertEquals(24L, sut.bestWorstPeriodDTO.getWorstPeriodDate().get(0).getPeriod());
    assertEquals(LocalDate.of(2020, 12, 31), sut.bestWorstPeriodDTO.getWorstPeriodDate().get(0).getInterval()
        .getEndDate());
    assertEquals(LocalDate.of(2019, 1, 1), sut.bestWorstPeriodDTO.getWorstPeriodDate().get(0).getInterval()
        .getStartDate());
  }

  @Test
  void shouldCalculatePositive_whenVerifyGetNumberOfPeriodsByPeriod() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var rollingReturns = getPortfolioReturns();
    when(sut.getNumberOfPeriodsByPeriod(any())).thenReturn(new PeriodValueResult(6L, BigDecimal.valueOf(13)));
    doCallRealMethod().when(sut).calculatePositive(any(), any());

    sut.calculatePositive(6L, rollingReturns);

    verify(sut).getNumberOfPeriodsByPeriod(6L);
  }

  @Test
  void shouldCalculatePositive_whenCheckResultWhenAllPositive() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var rollingReturns = getPortfolioReturns();
    when(sut.getNumberOfPeriodsByPeriod(any())).thenReturn(new PeriodValueResult(6L, BigDecimal.valueOf(13)));

    doCallRealMethod().when(sut).calculatePositive(any(), any());
    sut.calculatePositive(6L, rollingReturns);

    assertFalse(sut.bestWorstPeriodDTO.getPctPositive().isEmpty());
    assertEquals(6L, sut.bestWorstPeriodDTO.getPctPositive().get(0).getPeriod());
    assertEquals(DecimalUtils.toUserScale(BigDecimal.valueOf(1)), DecimalUtils.toUserScale(sut.bestWorstPeriodDTO
        .getPctPositive().get(0).getValue()));
  }

  @Test
  void shouldCalculatePositive_whenCheckResultWhenTwoRecordsAreNegative() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    final var rollingReturns = getPortfolioReturns();
    rollingReturns.put(rollingReturns.firstKey(), BigDecimal.valueOf(-1.01094319080371));
    rollingReturns.put(rollingReturns.lastKey(), BigDecimal.valueOf(-1.01094319080371));
    when(sut.getNumberOfPeriodsByPeriod(any())).thenReturn(new PeriodValueResult(6L, BigDecimal.valueOf(13)));

    doCallRealMethod().when(sut).calculatePositive(any(), any());
    sut.calculatePositive(6L, rollingReturns);

    assertFalse(sut.bestWorstPeriodDTO.getPctPositive().isEmpty());
    assertEquals(6L, sut.bestWorstPeriodDTO.getPctPositive().get(0).getPeriod());
    assertEquals(DecimalUtils.toUserScale(BigDecimal.valueOf(0.846153846153846)), DecimalUtils.toUserScale(
        sut.bestWorstPeriodDTO.getPctPositive().get(0).getValue()));
  }

  @Test
  void shouldGetNumberOfPeriodsByPeriod_whenCheckResult() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    sut.bestWorstPeriodDTO.getNumberOfPeriods().add(new PeriodValueResult(6L, BigDecimal.valueOf(13)));

    doCallRealMethod().when(sut).getNumberOfPeriodsByPeriod(any());
    final PeriodValueResult numberOfPeriodsByPeriod = sut.getNumberOfPeriodsByPeriod(6L);

    assertEquals(6L, numberOfPeriodsByPeriod.getPeriod());
    assertEquals(BigDecimal.valueOf(13), numberOfPeriodsByPeriod.getValue());
  }

  @Test
  void shouldGetPeriodStartDate_whenCheckResult() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    doCallRealMethod().when(sut).getStartOfPeriodsDate(anyLong(), any());
    final LocalDate periodStartDate = sut.getStartOfPeriodsDate(12L, getPortfolioReturns());

    assertEquals(toLastDayOfMonth(LocalDate.of(2020, 11, 1)), periodStartDate);
  }

  @Test
  void shouldGetDefaultValues_whenCheckResult() {
    final var portfolioReturns = getPortfolioReturns();
    final var sut = mock(BestWorstPeriodCalculation.class, withSettings().useConstructor(portfolioReturns, Set.of()));

    doCallRealMethod().when(sut).addDefaultValues(any());
    sut.addDefaultValues(6L);

    assertFalse(sut.bestWorstPeriodDTO.getPctPositive().isEmpty());
    assertFalse(sut.bestWorstPeriodDTO.getWorstPeriodDate().isEmpty());
    assertFalse(sut.bestWorstPeriodDTO.getWorstPeriodPct().isEmpty());
    assertFalse(sut.bestWorstPeriodDTO.getBestPeriodDate().isEmpty());
    assertFalse(sut.bestWorstPeriodDTO.getBestPeriodPct().isEmpty());
    assertFalse(sut.bestWorstPeriodDTO.getNumberOfPeriods().isEmpty());
    assertFalse(sut.bestWorstPeriodDTO.getAverage().isEmpty());
    assertEquals(6L, sut.bestWorstPeriodDTO.getPctPositive().get(0).getPeriod());
    assertEquals(null, sut.bestWorstPeriodDTO.getPctPositive().get(0).getValue());
    assertEquals(6L, sut.bestWorstPeriodDTO.getWorstPeriodDate().get(0).getPeriod());
    assertNull(sut.bestWorstPeriodDTO.getWorstPeriodDate().get(0).getInterval());
    assertEquals(6L, sut.bestWorstPeriodDTO.getWorstPeriodPct().get(0).getPeriod());
    assertEquals(null, sut.bestWorstPeriodDTO.getWorstPeriodPct().get(0).getValue());
    assertEquals(6L, sut.bestWorstPeriodDTO.getBestPeriodDate().get(0).getPeriod());
    assertNull(sut.bestWorstPeriodDTO.getBestPeriodDate().get(0).getInterval());
    assertEquals(6L, sut.bestWorstPeriodDTO.getBestPeriodPct().get(0).getPeriod());
    assertEquals(null, sut.bestWorstPeriodDTO.getBestPeriodPct().get(0).getValue());
    assertEquals(6L, sut.bestWorstPeriodDTO.getNumberOfPeriods().get(0).getPeriod());
    assertEquals(ZERO, sut.bestWorstPeriodDTO.getNumberOfPeriods().get(0).getValue());
    assertEquals(6L, sut.bestWorstPeriodDTO.getAverage().get(0).getPeriod());
    assertEquals(null, sut.bestWorstPeriodDTO.getAverage().get(0).getValue());
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
