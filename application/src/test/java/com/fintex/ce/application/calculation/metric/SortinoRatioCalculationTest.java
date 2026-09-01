package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.risk.SortinoRatioResult;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static com.fintex.ce.model.util.BigDecimalConstants.TWO;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@Disabled("metric unsupported")
class SortinoRatioCalculationTest {

  final int TWELVE = 12;

  @Test
  void shouldThrowMissingTBillRate_whenTBillsDoNotCoverPortfolioWindow() {
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
    final var downsideDeviationCalculation = mock(DownsideDeviationCalculation.class);
    final var calculation = new SortinoRatioCalculation(input, Set.of(), shortTBills, downsideDeviationCalculation);

    final CalculationException ex = assertThrows(CalculationException.class,
        () -> calculation.calculatePeriodForNumberOfMonths(12));
    assertEquals(ErrorCode.MISSING_TBILL_RATE, ex.getErrorCode());
    assertEquals("Missing T-Bill rate for date " + LocalDate.now().minusMonths(1), ex.getMessage());
    assertEquals(Map.of("param-1", LocalDate.now().minusMonths(1)), ex.getMetadata());
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetPeriodStartDate() {
    final var calculation = mock(SortinoRatioCalculation.class);
    final var treeMap = mock(TreeMap.class);

    calculation.tBills = treeMap;

    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(calculation).getPeriodStartDate(TWELVE, treeMap);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculateAverageArithmeticAnnualizedReturn() {
    final var calculation = mock(SortinoRatioCalculation.class);
    final var downsideDeviationCalculation = mock(DownsideDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var date = LocalDate.now();

    downsideDeviationCalculation.tBills = new TreeMap();
    calculation.tBills = treeMap;

    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);
    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(calculation, times(2)).calculateAverageArithmeticAnnualizedReturn(any(), eq(date), eq(TWELVE));
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetDownsideDeviation() {
    final var sortinoRatioCalculation = mock(SortinoRatioCalculation.class);
    final var downsideDeviationCalculation = mock(DownsideDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var date = LocalDate.now();

    downsideDeviationCalculation.tBills = new TreeMap();
    sortinoRatioCalculation.tBills = treeMap;

    when(sortinoRatioCalculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);
    when(sortinoRatioCalculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(sortinoRatioCalculation.calculateAverageArithmeticAnnualizedReturn(new TreeMap<>(), date, TWELVE)).thenReturn(
        BigDecimal.ONE);

    doCallRealMethod().when(sortinoRatioCalculation).calculatePeriodForNumberOfMonths(anyInt());
    sortinoRatioCalculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sortinoRatioCalculation).getDownsideDeviation(TWELVE);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculateSortinoRatio() {
    final var sortinoRatioCalculation = mock(SortinoRatioCalculation.class);
    final var downsideDeviationCalculation = mock(DownsideDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var date = LocalDate.now();

    downsideDeviationCalculation.tBills = new TreeMap();
    sortinoRatioCalculation.tBills = treeMap;

    when(sortinoRatioCalculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);
    when(sortinoRatioCalculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(sortinoRatioCalculation.calculateAverageArithmeticAnnualizedReturn(any(), any(), anyInt())).thenReturn(ONE);
    when(sortinoRatioCalculation.getDownsideDeviation(anyInt())).thenReturn(TEN);
    doCallRealMethod().when(sortinoRatioCalculation).calculatePeriodForNumberOfMonths(anyInt());

    sortinoRatioCalculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sortinoRatioCalculation).calculateSortinoRatio(ONE, ONE, TEN);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResultWhenPortfolioSizeIsLessThanTwelve() {
    final var sortinoRatioCalculation = mock(SortinoRatioCalculation.class);
    final var treeMap = mock(TreeMap.class);

    sortinoRatioCalculation.tBills = treeMap;

    when(sortinoRatioCalculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    doCallRealMethod().when(sortinoRatioCalculation).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal resultValue = sortinoRatioCalculation.calculatePeriodForNumberOfMonths(ONE.intValue());

    assertNull(resultValue);
  }

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    final var calculation = mock(SortinoRatioCalculation.class);
    final Map<String, BigDecimal> periods = Map.of("2015-01-01", ZERO, "2018-02-02", ONE);
    final Map<String, BigDecimal> expected = Map.of("2015-01-01", ZERO, "2018-02-02", ONE);

    doCallRealMethod().when(calculation).defineResponseType(anyMap());
    final SortinoRatioResult result = calculation.defineResponseType(periods);

    assertEquals(expected, result.getSortinoRatio());
  }

  @Test
  void shouldCalculateSortinoRatio_whenCheckResult() {
    final var calculation = mock(SortinoRatioCalculation.class);

    doCallRealMethod().when(calculation).calculateSortinoRatio(any(), any(), any());
    final BigDecimal returnValue = calculation.calculateSortinoRatio(TEN, TWO, TWO);

    assertEquals(toUserScale(BigDecimal.valueOf(4)), toUserScale(returnValue));
  }

  @Test
  void shouldCalculateSortinoRatio_whenCheckResult2() {
    final var calculation = mock(SortinoRatioCalculation.class);

    doCallRealMethod().when(calculation).calculateSortinoRatio(any(), any(), any());
    final BigDecimal returnValue = calculation.calculateSortinoRatio(TEN, ONE, TEN);

    assertEquals(toUserScale(BigDecimal.valueOf(0.9)), toUserScale(returnValue));
  }

  @Test
  void shouldCalculateSortinoRatio_whenCheckResult3() {
    final var calculation = mock(SortinoRatioCalculation.class);

    doCallRealMethod().when(calculation).calculateSortinoRatio(any(), any(), any());
    final BigDecimal returnValue = calculation.calculateSortinoRatio(TEN, ONE, ZERO);

    assertNull(returnValue);
  }

  @Test
  void shouldGetDownsideDeviation_whenCheckResult() {
    final var downsideDeviationCalculation = mock(DownsideDeviationCalculation.class);
    final var context = PeriodCalculationInput.builder()
        .weightedAveragePortfolioReturns(new TreeMap<>(Map.of(
            LocalDate
                .now(), TEN)))
        .build();
    final var calculation = mock(SortinoRatioCalculation.class, withSettings()
        .useConstructor(context, Set.of(), new TreeMap<>(Map.of(LocalDate.now(), TEN)),
            downsideDeviationCalculation));

    when(downsideDeviationCalculation.calculatePeriodForNumberOfMonths(TEN.intValue())).thenReturn(BigDecimal.ONE);

    doCallRealMethod().when(calculation).getDownsideDeviation(anyInt());
    final BigDecimal actual = calculation.getDownsideDeviation(TEN.intValue());

    assertEquals(ONE, actual);
  }
}
