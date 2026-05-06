package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.TreynorRatioResult;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static com.fintex.ce.model.util.BigDecimalConstants.TWO;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class TreynorRatioCalculationTest {

  final int TWELVE = 12;

  @Test
  void shouldResolvePeriodStartDate_whenCalculatingPeriod() {
    final var beta = mock(BetaCalculation.class);
    final var tBills = mock(TreeMap.class);
    final var calculation = mock(TreynorRatioCalculation.class,
        withSettings().useConstructor(mock(PeriodCalculationInput.class), mock(Set.class), tBills, beta));

    when(calculation.getPortfolioTotalReturns()).thenReturn(tBills);
    when(tBills.size()).thenReturn(TWELVE);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(calculation).getPeriodStartDate(TWELVE, tBills);
  }

  @Test
  void shouldCalculateAnnualizedReturnsForPortfolioAndRiskFree_whenCalculatingPeriod() {
    final var tBills = mock(TreeMap.class);
    final var beta = mock(BetaCalculation.class);
    final var calculation = mock(TreynorRatioCalculation.class,
        withSettings().useConstructor(mock(PeriodCalculationInput.class), mock(Set.class), tBills, beta));
    final var treeMap = mock(TreeMap.class);
    final var date = LocalDate.now();

    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);
    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(calculation, times(2)).calculateAverageArithmeticAnnualizedReturn(any(), eq(date), eq(TWELVE));
  }

  @Test
  void shouldDelegateToBetaCalculation_whenCalculatingPeriod() {
    final var tBills = mock(TreeMap.class);
    final var beta = mock(BetaCalculation.class);
    final var calculation = mock(TreynorRatioCalculation.class,
        withSettings().useConstructor(mock(PeriodCalculationInput.class), mock(Set.class), tBills, beta));
    final var treeMap = mock(TreeMap.class);
    final var date = LocalDate.now();

    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);
    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(beta).calculatePeriodForNumberOfMonths(TWELVE);
  }

  @Test
  void shouldReturnNull_whenBetaIsNull() {
    final var tBills = mock(TreeMap.class);
    final var beta = mock(BetaCalculation.class);
    final var calculation = mock(TreynorRatioCalculation.class,
        withSettings().useConstructor(mock(PeriodCalculationInput.class), mock(Set.class), tBills, beta));
    final var treeMap = mock(TreeMap.class);
    final var date = LocalDate.now();

    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);
    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(beta.calculatePeriodForNumberOfMonths(TWELVE)).thenReturn(null);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final var actual = calculation.calculatePeriodForNumberOfMonths(TWELVE);

    assertNull(actual);
  }

  @Test
  void shouldCalculateTreynorRatio_whenInputsAreAvailable() {
    final var tBills = mock(TreeMap.class);
    final var beta = mock(BetaCalculation.class);
    final var calculation = mock(TreynorRatioCalculation.class,
        withSettings().useConstructor(mock(PeriodCalculationInput.class), mock(Set.class), tBills, beta));
    final var treeMap = mock(TreeMap.class);
    final var date = LocalDate.now();

    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);
    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(calculation.calculateAverageArithmeticAnnualizedReturn(any(), any(), anyInt())).thenReturn(TEN);
    when(beta.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(ONE);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(calculation).calculateTreynorRatio(TEN, TEN, ONE);
  }

  @Test
  void shouldReturnNull_whenPortfolioSizeIsLessThanPeriod() {
    final var calculation = mock(TreynorRatioCalculation.class);
    final var treeMap = mock(TreeMap.class);

    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(10);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(TWELVE);

    assertNull(actual);
  }

  @Test
  void shouldMapIntervalResults_whenDefiningResponseType() {
    final var calculation = mock(TreynorRatioCalculation.class);
    final var pairs = Set.of(Pair.of("2015-01-01", ZERO), Pair.of("2018-02-02", ONE));

    final var interval1 = new TimeIntervalResult("2015-01-01", ZERO);
    final var interval2 = new TimeIntervalResult("2018-02-02", ONE);
    final var expected = Set.of(interval1, interval2);

    when(calculation.formTimeIntervalResult(anySet())).thenReturn(expected);
    doCallRealMethod().when(calculation).defineResponseType(anySet());

    final TreynorRatioResult result = calculation.defineResponseType(pairs);

    assertEquals(expected, result.getTreynorRatio());
  }

  @Test
  void shouldCalculateTreynorRatioValue_whenValuesProvided() {
    final var calculation = mock(TreynorRatioCalculation.class);

    doCallRealMethod().when(calculation).calculateTreynorRatio(any(), any(), any());
    final BigDecimal returnValue = calculation.calculateTreynorRatio(TEN, TWO, TEN);

    assertEquals(toUserScale(BigDecimal.valueOf(0.8)), toUserScale(returnValue));
  }

}
