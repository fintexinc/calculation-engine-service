package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.RollingIntervalResult;
import com.fintex.ce.model.domain.result.rolling.RollingSharpeRatioResult;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static com.fintex.ce.model.util.BigDecimalConstants.TEN_THOUSAND;
import static com.fintex.ce.model.util.BigDecimalConstants.TWO;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@Disabled("metric unsupported")
class RollingSharpeRatioCalculationTest {

  private static NavigableMap<LocalDate, BigDecimal> portfolioReturns;

  @BeforeAll
  static void setUp() {
    portfolioReturns = new TreeMap<>();
    portfolioReturns.put(LocalDate.now().minusMonths(12), HUNDRED);
    portfolioReturns.put(LocalDate.now().minusMonths(11), TEN);
    portfolioReturns.put(LocalDate.now().minusMonths(10), HUNDRED);
    portfolioReturns.put(LocalDate.now().minusMonths(9), ONE);
    portfolioReturns.put(LocalDate.now().minusMonths(8), TWO);
    portfolioReturns.put(LocalDate.now().minusMonths(7), TEN_THOUSAND);
    portfolioReturns.put(LocalDate.now().minusMonths(6), ONE);
    portfolioReturns.put(LocalDate.now().minusMonths(5), HUNDRED);
    portfolioReturns.put(LocalDate.now().minusMonths(4), TEN_THOUSAND);
    portfolioReturns.put(LocalDate.now().minusMonths(3), ONE);
    portfolioReturns.put(LocalDate.now().minusMonths(2), HUNDRED);
    portfolioReturns.put(LocalDate.now().minusMonths(1), TEN_THOUSAND);
  }

  @Test
  void shouldReturnSharpeRatioValue_whenCalculatingRollingValue() {
    final var context = mock(PeriodCalculationInput.class);
    final var sharpeRatioCalculation = mock(SharpeRatioCalculation.class);
    final var calculation = mock(RollingSharpeRatioCalculation.class, withSettings().useConstructor(context, Set.of(),
        sharpeRatioCalculation));
    final int numberOfMonths = 12;

    when(sharpeRatioCalculation.calculatePeriodForNumberOfMonths(anyInt(), any())).thenReturn(TEN);

    doCallRealMethod().when(calculation).calculateRollingValue(numberOfMonths, portfolioReturns);
    final BigDecimal actual = calculation.calculateRollingValue(numberOfMonths, portfolioReturns);

    assertEquals(TEN, actual);
  }

  @Test
  void shouldReturnNullForWindow_whenSingleSharpeRatioWindowMissesTBillRate() {
    var context = mock(PeriodCalculationInput.class);
    var sharpeRatioCalculation = mock(SharpeRatioCalculation.class);
    var calculation = mock(RollingSharpeRatioCalculation.class, withSettings().useConstructor(context, Set.of(),
        sharpeRatioCalculation));

    when(sharpeRatioCalculation.calculatePeriodForNumberOfMonths(anyInt(), any()))
        .thenThrow(new CalculationException(ErrorCode.MISSING_TBILL_RATE, LocalDate.now().minusMonths(6)));

    doCallRealMethod().when(calculation).calculateRollingValue(12, portfolioReturns);
    BigDecimal actual = calculation.calculateRollingValue(12, portfolioReturns);

    assertNull(actual);
  }

  @Test
  void shouldPropagateOtherCalculationExceptions_whenSharpeRatioFailsForUnrelatedReason() {
    var context = mock(PeriodCalculationInput.class);
    var sharpeRatioCalculation = mock(SharpeRatioCalculation.class);
    var calculation = mock(RollingSharpeRatioCalculation.class, withSettings().useConstructor(context, Set.of(),
        sharpeRatioCalculation));

    var unrelated = new CalculationException(ErrorCode.INSUFFICIENT_MONTHLY_RETURNS_FOR_PERIOD, "12", 0);
    when(sharpeRatioCalculation.calculatePeriodForNumberOfMonths(anyInt(), any())).thenThrow(unrelated);

    doCallRealMethod().when(calculation).calculateRollingValue(12, portfolioReturns);

    CalculationException thrown = assertThrows(CalculationException.class,
        () -> calculation.calculateRollingValue(12, portfolioReturns));
    assertEquals(ErrorCode.INSUFFICIENT_MONTHLY_RETURNS_FOR_PERIOD, thrown.getErrorCode());
  }

  @Test
  void shouldDelegateToRollingIntervalResults_whenDefiningResponseType() {
    final var calculation = mock(RollingSharpeRatioCalculation.class);
    final Map<String, NavigableMap<LocalDate, BigDecimal>> result = Map.of("12", portfolioReturns);

    doCallRealMethod().when(calculation).defineResponseType(result);
    calculation.defineResponseType(result);

    verify(calculation).getRollingIntervalResults(result);
  }

  @Test
  void shouldMapRollingSharpeRatioResult_whenDefiningResponseType() {
    final var calculation = mock(RollingSharpeRatioCalculation.class);
    final Map<String, NavigableMap<LocalDate, BigDecimal>> periodValues = Map.of("12", portfolioReturns);

    final Map<LocalDate, BigDecimal> values = Map.of(LocalDate.now().minusMonths(3), TEN);
    final var intervalResult = new RollingIntervalResult("12", values);
    final var expected = new RollingSharpeRatioResult(Set.of(intervalResult));

    when(calculation.getRollingIntervalResults(anyMap())).thenReturn(Set.of(intervalResult));

    doCallRealMethod().when(calculation).defineResponseType(periodValues);
    final RollingSharpeRatioResult actual = calculation.defineResponseType(periodValues);

    assertEquals(expected.getRollingSharpeRatio(), actual.getRollingSharpeRatio());
  }

  @AfterAll
  static void tearDown() {
    portfolioReturns.clear();
  }
}