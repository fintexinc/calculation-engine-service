package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.TreynorRatioResult;
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
import static com.fintex.ce.model.util.BigDecimalConstants.TWO;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

  /** Real 12-month portfolio + tBills so the constructor's restrictTBillsRange call doesn't NPE on mocks. */
  private static PeriodCalculationInput input() {
    NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    for (int i = 0; i < 12; i++) {
      returns.put(LocalDate.now().minusMonths(i), ONE);
    }
    return PeriodCalculationInput.builder().weightedAveragePortfolioReturns(returns).build();
  }

  private static TreeMap<LocalDate, BigDecimal> tBills() {
    TreeMap<LocalDate, BigDecimal> tBills = new TreeMap<>();
    for (int i = 0; i < 12; i++) {
      tBills.put(LocalDate.now().minusMonths(i), ONE);
    }
    return tBills;
  }

  @Test
  void shouldResolvePeriodStartDate_whenCalculatingPeriod() {
    final var beta = mock(BetaCalculation.class);
    final var tBills = tBills();
    final var calculation = mock(TreynorRatioCalculation.class,
        withSettings().useConstructor(input(), Set.of(), tBills, beta));

    when(calculation.getPortfolioTotalReturns()).thenReturn(tBills);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(calculation).getPeriodStartDate(TWELVE, tBills);
  }

  @Test
  void shouldCalculateAnnualizedReturnsForPortfolioAndRiskFree_whenCalculatingPeriod() {
    final var tBills = tBills();
    final var beta = mock(BetaCalculation.class);
    final var calculation = mock(TreynorRatioCalculation.class,
        withSettings().useConstructor(input(), Set.of(), tBills, beta));
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
    final var tBills = tBills();
    final var beta = mock(BetaCalculation.class);
    final var calculation = mock(TreynorRatioCalculation.class,
        withSettings().useConstructor(input(), Set.of(), tBills, beta));
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
    final var tBills = tBills();
    final var beta = mock(BetaCalculation.class);
    final var calculation = mock(TreynorRatioCalculation.class,
        withSettings().useConstructor(input(), Set.of(), tBills, beta));
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
    final var tBills = tBills();
    final var beta = mock(BetaCalculation.class);
    final var calculation = mock(TreynorRatioCalculation.class,
        withSettings().useConstructor(input(), Set.of(), tBills, beta));
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
    final var beta = mock(BetaCalculation.class);
    final var calculation = new TreynorRatioCalculation(input, Set.of(), shortTBills, beta);

    final CalculationException ex = assertThrows(CalculationException.class,
        () -> calculation.calculatePeriodForNumberOfMonths(12));
    assertEquals(ErrorCode.MISSING_TBILL_RATE, ex.getErrorCode());
    assertEquals("Missing T-Bill rate for date " + LocalDate.now().minusMonths(1), ex.getMessage());
    assertEquals(Map.of("param-1", LocalDate.now().minusMonths(1)), ex.getMetadata());
  }

  @Test
  void shouldThrowMissingTBillRate_whenTBillsHavePublicationLag() {
    // Publication-lag scenario: portfolio covers the last 13 months but T-Bills only the prior 12 (lagging by 1
    // month). Count gate passes (tBills.size() == 12 == numberOfMonths) but the period window starts AFTER the last
    // T-Bill date — the per-date RiskFreeWindowValidator.requireCoverage check must throw MISSING_TBILL_RATE instead
    // of letting calculateAverageArithmeticAnnualizedReturn silently divide an undersized window by numberOfMonths.
    NavigableMap<LocalDate, BigDecimal> portfolioReturns = new TreeMap<>();
    for (int i = 0; i < 13; i++) {
      portfolioReturns.put(LocalDate.now().minusMonths(i), ONE);
    }
    PeriodCalculationInput input = PeriodCalculationInput.builder()
        .weightedAveragePortfolioReturns(portfolioReturns)
        .build();
    NavigableMap<LocalDate, BigDecimal> laggingTBills = new TreeMap<>();
    for (int i = 1; i <= 12; i++) {
      laggingTBills.put(LocalDate.now().minusMonths(i), ONE);
    }

    final var beta = mock(BetaCalculation.class);
    final var calculation = mock(TreynorRatioCalculation.class,
        withSettings().useConstructor(input, Set.of(), laggingTBills, beta));

    doCallRealMethod().when(calculation).getPortfolioTotalReturns();
    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    doCallRealMethod().when(calculation).getPeriodStartDate(anyInt(), any());
    doCallRealMethod().when(calculation).getSubMapByPeriodStartDate(any(), any());

    CalculationException ex = assertThrows(CalculationException.class,
        () -> calculation.calculatePeriodForNumberOfMonths(TWELVE));
    assertEquals(ErrorCode.MISSING_TBILL_RATE, ex.getErrorCode());
    assertEquals("Missing T-Bill rate for date " + LocalDate.now(), ex.getMessage());
    assertEquals(Map.of("param-1", LocalDate.now()), ex.getMetadata());
  }

}
