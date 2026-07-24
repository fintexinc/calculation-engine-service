package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.IntervalResult;
import com.fintex.ce.model.domain.result.RollingIntervalResult;
import com.fintex.ce.model.domain.result.rolling.RollingTotalReturnsResult;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static com.fintex.ce.model.util.BigDecimalConstants.TEN_THOUSAND;
import static com.fintex.ce.model.util.BigDecimalConstants.TWO;
import static java.math.BigDecimal.TEN;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@Disabled("metric unsupported")
class RollingTotalReturnsCalculationTest {

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
  void shouldCalculateRollingValue_whenCheckResult() {
    final var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var context = mock(PeriodCalculationInput.class);
    final var calculation = mock(RollingTotalReturnsCalculation.class, withSettings().useConstructor(context, Set.of(),
        trailingTotalReturnsCalculation));
    final int numberOfMonths = 12;

    when(trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(anyInt(), any())).thenReturn(TEN);

    doCallRealMethod().when(calculation).calculateRollingValue(numberOfMonths, portfolioReturns);
    final BigDecimal actual = calculation.calculateRollingValue(numberOfMonths, portfolioReturns);

    Assertions.assertEquals(TEN, actual);
  }

  @Test
  void shouldDefineResponseType_whenVerifyGetRollingIntervalResultS() {
    final var calculation = mock(RollingTotalReturnsCalculation.class);
    final var result = Set.of(Pair.of("12", portfolioReturns));

    doCallRealMethod().when(calculation).defineResponseType(result);
    calculation.defineResponseType(result);

    verify(calculation).getRollingIntervalResults(result);
  }

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    final var calculation = mock(RollingTotalReturnsCalculation.class);
    final var periodValues = Set.of(Pair.of("12", portfolioReturns));

    final LinkedHashSet<IntervalResult> res = new LinkedHashSet<>();
    res.add(new IntervalResult(LocalDate.now().minusMonths(3), TEN));
    final var intervalResult = new RollingIntervalResult("12", res);
    final var expected = new RollingTotalReturnsResult(Set.of(intervalResult));

    when(calculation.getRollingIntervalResults(anySet())).thenReturn(Set.of(intervalResult));

    doCallRealMethod().when(calculation).defineResponseType(periodValues);
    final RollingTotalReturnsResult actual = calculation.defineResponseType(periodValues);

    Assertions.assertEquals(expected.getRollingTotalReturns(), actual.getRollingTotalReturns());
  }

  @AfterAll
  static void tearDown() {
    portfolioReturns.clear();
  }
}