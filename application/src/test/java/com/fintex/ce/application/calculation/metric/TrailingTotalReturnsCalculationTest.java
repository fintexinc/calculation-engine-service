package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.returns.TrailingTotalReturnsResult;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;
import com.fintex.wm.commons.error.Notification;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrailingTotalReturnsCalculationTest {

  @Test
  void shouldDelegateToFormTimeIntervalResult_whenDefiningResponseType() {
    var calculation = mock(TrailingTotalReturnsCalculation.class);

    var pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05", BigDecimal.ONE));

    doCallRealMethod().when(calculation).defineResponseType(anySet());
    calculation.defineResponseType(pairs);

    verify(calculation).formTimeIntervalResult(pairs);
  }

  @Test
  void shouldReturnNull_whenPeriodExceedsPortfolioSize() {
    var calculation = mock(TrailingTotalReturnsCalculation.class);

    var treeMap = mock(TreeMap.class);
    when(treeMap.size()).thenReturn(1);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(2);

    assertNull(actual);
  }

  @Test
  void shouldReturnNull_whenPeriodExceedsProvidedReturnsSize() {
    var calculation = mock(TrailingTotalReturnsCalculation.class);

    var totalReturns = mock(TreeMap.class);
    when(totalReturns.size()).thenReturn(1);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt(), any());
    BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(2, totalReturns);

    assertNull(actual);
  }

  @Test
  void shouldCalculateTrailingTotalReturn_whenPeriodEqualsTwelveMonths() {
    var calculation = mock(TrailingTotalReturnsCalculation.class);

    when(calculation.calculateProductForPeriod(eq(12), any())).thenReturn(TEN);

    var treeMap = mock(TreeMap.class);
    when(treeMap.size()).thenReturn(12);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt(), any());
    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(12);

    assertEquals(0, BigDecimal.valueOf(9).compareTo(actual));
  }

  @Test
  void shouldCalculateTrailingTotalReturn_whenPeriodIsLessThanTwelveMonths() {
    var t = mock(TrailingTotalReturnsCalculation.class);

    when(t.calculateProductForPeriod(eq(11), any())).thenReturn(TEN);

    var treeMap = mock(TreeMap.class);
    when(treeMap.size()).thenReturn(11);
    when(t.getPortfolioTotalReturns()).thenReturn(treeMap);

    doCallRealMethod().when(t).calculatePeriodForNumberOfMonths(anyInt(), any());
    doCallRealMethod().when(t).calculatePeriodForNumberOfMonths(anyInt());
    BigDecimal actual = t.calculatePeriodForNumberOfMonths(11);

    assertEquals(0, BigDecimal.valueOf(9).compareTo(actual));
  }

  @Test
  void shouldCalculateAnnualizedTrailingTotalReturn_whenPeriodExceedsTwelveMonths() {
    var t = mock(TrailingTotalReturnsCalculation.class);
    doCallRealMethod().when(t).calculatePeriodForNumberOfMonths(24);
    when(t.calculateProductForPeriod(eq(24), any())).thenReturn(TEN);

    var m = mock(TreeMap.class);
    when(m.size()).thenReturn(24);
    when(t.getPortfolioTotalReturns()).thenReturn(m);

    doCallRealMethod().when(t).calculatePeriodForNumberOfMonths(anyInt(), any());
    doCallRealMethod().when(t).calculatePeriodForNumberOfMonths(anyInt());
    BigDecimal actual = t.calculatePeriodForNumberOfMonths(24);

    assertEquals(0, new BigDecimal("2.1622776601683795").compareTo(actual));
  }

  @Test
  void shouldThrowMissingTBillRate_whenTBillsHaveGapWithinPeriodWindow() {
    NavigableMap<LocalDate, BigDecimal> portfolioReturns = new TreeMap<>();
    NavigableMap<LocalDate, BigDecimal> tBills = new TreeMap<>();
    for (int i = 0; i < 12; i++) {
      LocalDate month = LocalDate.of(2025, 1, 31).plusMonths(i);
      portfolioReturns.put(month, BigDecimal.valueOf(1.01));
      if (i != 5) {
        tBills.put(month, BigDecimal.valueOf(0.001));
      }
    }
    PeriodCalculationInput input = new PeriodCalculationInput();
    input.setWeightedAveragePortfolioReturns(portfolioReturns);
    var calculation = TrailingTotalReturnsCalculation.withTBillPrecondition(input, Set.of(), tBills);

    CalculationException ex = assertThrows(CalculationException.class,
        () -> calculation.calculatePeriodForNumberOfMonths(12));
    assertEquals(ErrorCode.MISSING_TBILL_RATE, ex.getErrorCode());
    assertEquals("Missing T-Bill rate for date " + LocalDate.of(2025, 1, 31).plusMonths(5), ex.getMessage());
    assertEquals(Map.of("param-1", LocalDate.of(2025, 1, 31).plusMonths(5)), ex.getMetadata());
  }

  @Test
  void shouldNotValidateTBills_whenMathOnlyFactoryUsed() {
    NavigableMap<LocalDate, BigDecimal> portfolioReturns = new TreeMap<>();
    for (int i = 0; i < 12; i++) {
      portfolioReturns.put(LocalDate.of(2025, 1, 31).plusMonths(i), BigDecimal.valueOf(1.01));
    }
    PeriodCalculationInput input = new PeriodCalculationInput();
    input.setWeightedAveragePortfolioReturns(portfolioReturns);
    var calculation = TrailingTotalReturnsCalculation.mathOnly(input, Set.of());

    // mathOnly() applies no T-Bill precondition: the call must not throw, and 12 months of a 1.01 factor
    // annualized over 12 months yields 1.01^12 - 1.
    BigDecimal result = calculation.calculatePeriodForNumberOfMonths(12);
    assertEquals(0, result.compareTo(new BigDecimal("0.1268250301319698")));
  }

  @Test
  void shouldMapIntervalResults_whenDefiningResponseType() {
    var calculation = mock(TrailingTotalReturnsCalculation.class);

    var pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05", ONE));

    var expected = Set.of(
        new TimeIntervalResult("2000-01-12", ZERO),
        new TimeIntervalResult("2020-01-05", BigDecimal.ONE));
    when(calculation.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(calculation).defineResponseType(anySet());
    TrailingTotalReturnsResult actual = calculation.defineResponseType(pairs);

    assertEquals(expected, actual.getTrailingTotalReturn());
  }

  @Test
  void shouldRetainInputWarnings_whenCalculatingPeriods() {
    LocalDate date = LocalDate.parse("2025-01-31");
    Notification warning = ErrorCode.FX_RATES_UNAVAILABLE.asNotification("USD holding", Currency.USD, Currency.CAD);
    PeriodCalculationInput input = new PeriodCalculationInput(null,
        new TreeMap<>(Map.of(date, new BigDecimal("1.01"))), List.of(warning));
    TrailingTotalReturnsCalculation calculation = TrailingTotalReturnsCalculation.mathOnly(input, Set.of());

    TrailingTotalReturnsResult result = calculation.calculate(Set.of(TimePeriod.ONE_MTH));

    assertThat(result.getWarnings()).containsExactly(warning);
    assertThat(result.getTrailingTotalReturn()).singleElement().satisfies(interval -> {
      assertThat(interval.period()).isEqualTo(TimePeriod.ONE_MTH.name());
      assertThat(interval.value()).isEqualByComparingTo("0.01");
    });
    assertEquals(date, result.getPerformanceStartDate());
    assertEquals(date, result.getPerformanceEndDate());
  }
}
