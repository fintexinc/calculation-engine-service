package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.DownsideDeviationResult;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static com.fintex.ce.model.util.BigDecimalConstants.TEN_THOUSAND;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DownsideDeviationCalculationTest {
  final int TWELVE = 12;

  @Test
  void shouldThrowMissingTBillRate_whenExcessReturnsDoNotCoverPortfolioWindow() {
    final NavigableMap<LocalDate, BigDecimal> portfolioReturns = new TreeMap<>();
    for (int i = 0; i < 12; i++) {
      portfolioReturns.put(LocalDate.now().minusMonths(i), ONE);
    }
    final PeriodCalculationInput input = PeriodCalculationInput.builder()
        .weightedAveragePortfolioReturns(portfolioReturns)
        .build();
    final NavigableMap<LocalDate, BigDecimal> shortTBills = new TreeMap<>();
    for (int i = 2; i <= 12; i++) { // missing the most recent month inside the window
      shortTBills.put(LocalDate.now().minusMonths(i), ONE);
    }
    final DownsideDeviationCalculation<DownsideDeviationResult> calculation = new DownsideDeviationCalculation<>(input,
        Set.of(), shortTBills);

    final CalculationException ex = assertThrows(CalculationException.class,
        () -> calculation.calculatePeriodForNumberOfMonths(12));
    assertEquals(ErrorCode.MISSING_TBILL_RATE, ex.getErrorCode());
    assertEquals("Missing T-Bill rate for date " + LocalDate.now().minusMonths(1), ex.getMessage());
    assertEquals(Map.of("param-1", LocalDate.now().minusMonths(1)), ex.getMetadata());
  }

  @Test
  void shouldResolvePeriodStartDate_whenCalculatingPeriod() {
    final var calculation = mock(DownsideDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    calculation.portfolioExcessReturn = treeMap;

    when(treeMap.size()).thenReturn(TWELVE);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(TWELVE);
    verify(calculation).getPeriodStartDate(12, treeMap);
  }

  @Test
  void shouldGetSubMapByStartDate_whenCalculatingPeriod() {
    final var calculation = mock(DownsideDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var date = LocalDate.now();
    calculation.portfolioExcessReturn = treeMap;
    when(treeMap.size()).thenReturn(TWELVE);
    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(TWELVE);
    // Twice now: once for the validation window (vs portfolio total returns), once for the excess-return submap.
    verify(calculation, times(2)).getSubMapByPeriodStartDate(date, treeMap);
  }

  @Test
  void shouldCalculateDownsideReturnSquared_whenCalculatingPeriod() {
    final var calculation = mock(DownsideDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var date = LocalDate.now();
    calculation.portfolioExcessReturn = treeMap;
    when(treeMap.size()).thenReturn(TWELVE);
    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(TWELVE);
    verify(calculation).calculateDownsideReturnSquared(treeMap);
  }

  @Test
  void shouldCalculateDownsideDeviation_whenCalculatingPeriod() {
    final var calculation = mock(DownsideDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final LocalDate date = LocalDate.now();
    calculation.portfolioExcessReturn = treeMap;
    when(treeMap.size()).thenReturn(TWELVE);
    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(calculation.calculateDownsideReturnSquared(any())).thenReturn(treeMap);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(TWELVE);
    verify(calculation).calculateDownsideDeviation(TWELVE, treeMap);
  }

  @Test
  void shouldReturnNull_whenPortfolioReturnsSizeIsLessThanPeriod() {
    final var calculation = mock(DownsideDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    calculation.portfolioExcessReturn = treeMap;
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(BigDecimal.ONE.intValue());

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal result = calculation.calculatePeriodForNumberOfMonths(TWELVE);
    assertNull(result);
  }

  @Test
  void shouldKeepOnlyNegativeExcessReturnsSquared_whenCalculatingDownsideReturnSquared() {
    final var calculation = mock(DownsideDeviationCalculation.class);
    final var date = LocalDate.now();
    final var portfolioExcessReturnsInPeriod = new TreeMap(Map.of(date, ONE, date.plusMonths(1), BigDecimal.valueOf(
        -5.2222), date.plusMonths(2), TEN_THOUSAND));

    doCallRealMethod().when(calculation).calculateDownsideReturnSquared(any());
    final TreeMap downsideReturnSquared = calculation.calculateDownsideReturnSquared(portfolioExcessReturnsInPeriod);
    assertEquals(1, downsideReturnSquared.size());
    assertEquals(BigDecimal.valueOf(27.271372839999998), downsideReturnSquared.firstEntry().getValue());
    assertEquals(date.plusMonths(1), downsideReturnSquared.firstKey());
  }

  @Test
  void shouldCalculateDownsideDeviationValue_whenSquaredReturnsProvided() {
    final var calculation = mock(DownsideDeviationCalculation.class);
    final var date = LocalDate.now();
    final var downsideReturnSquared = new TreeMap(Map.of(date, BigDecimal.valueOf(2), date.plusMonths(1),
        BigDecimal.valueOf(3), date.plusMonths(2), BigDecimal.valueOf(4)));

    doCallRealMethod().when(calculation).calculateDownsideDeviation(anyInt(), any());
    final BigDecimal downsideDeviation = calculation.calculateDownsideDeviation(TWELVE, downsideReturnSquared);
    assertEquals(toUserScale(BigDecimal.valueOf(2.99999999999999725114029147225)), toUserScale(downsideDeviation));
  }

  @Test
  void shouldMapIntervalResults_whenDefiningResponseType() {
    final var calculation = mock(DownsideDeviationCalculation.class);
    final var pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05", BigDecimal.ONE));
    final var interval1 = new TimeIntervalResult("2000-01-12", ZERO);
    final var interval2 = new TimeIntervalResult("2020-01-05", BigDecimal.ONE);
    final var expected = Set.of(interval1, interval2);
    when(calculation.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(calculation).defineResponseType(anySet());
    final DownsideDeviationResult actual = (DownsideDeviationResult) calculation.defineResponseType(pairs);
    assertEquals(expected, actual.getDownsideDeviation());
  }
}
