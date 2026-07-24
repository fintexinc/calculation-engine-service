package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.IntervalResult;
import com.fintex.ce.model.domain.result.RollingIntervalResult;
import com.fintex.ce.model.domain.result.rolling.RollingCorrelationResult;

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
import static com.fintex.ce.model.util.BigDecimalConstants.TEN_THOUSAND;
import static com.fintex.ce.model.util.BigDecimalConstants.TWO;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@Disabled("metric unsupported")
class RollingCorrelationCalculationTest {

  private static final int TWELVE = 12;
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
  void shouldReturnNull_whenBenchmarkReturnsSizeIsLessThanWindow() {
    var correlationCalculation = mock(CorrelationCalculation.class);
    var benchmarkTotalReturns = mock(TreeMap.class);
    var context = mock(PeriodCalculationInput.class);
    var calculation = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(context, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(benchmarkTotalReturns.size()).thenReturn(1);

    doCallRealMethod().when(calculation).calculateRollingValue(anyInt(), any());
    BigDecimal actual = calculation.calculateRollingValue(TWELVE, portfolioReturns);

    assertNull(actual);
  }

  @Test
  void shouldInitializePortfolioReturns_whenCalculatingRollingValue() {
    var correlationCalculation = mock(CorrelationCalculation.class);
    var benchmarkTotalReturns = portfolioReturns;
    var context = mock(PeriodCalculationInput.class);
    var calculation = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(context, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    doCallRealMethod().when(calculation).calculateRollingValue(anyInt(), any());
    calculation.calculateRollingValue(TWELVE, portfolioReturns);

    verify(calculation).initializePortfolioReturns(portfolioReturns);
  }

  @Test
  void shouldInitializeBenchmarkReturns_whenCalculatingRollingValue() {
    var correlationCalculation = mock(CorrelationCalculation.class);
    var benchmarkTotalReturns = portfolioReturns;
    var context = mock(PeriodCalculationInput.class);
    var calculation = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(context, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(calculation.initializePortfolioReturns(any())).thenReturn(portfolioReturns);
    doCallRealMethod().when(calculation).calculateRollingValue(anyInt(), any());
    calculation.calculateRollingValue(TWELVE, portfolioReturns);

    verify(calculation).initializeBenchmarkReturns(portfolioReturns);
  }

  @Test
  void shouldDelegateToCorrelationCalculation_whenCalculatingRollingValue() {
    var correlationCalculation = mock(CorrelationCalculation.class);
    var benchmarkTotalReturns = portfolioReturns;
    var context = mock(PeriodCalculationInput.class);
    var calculation = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(context, Set.of(),
        correlationCalculation, benchmarkTotalReturns));
    var benchmarkReturns = portfolioReturns;

    when(calculation.initializePortfolioReturns(any())).thenReturn(portfolioReturns);
    when(calculation.initializeBenchmarkReturns(any())).thenReturn(benchmarkReturns);

    doCallRealMethod().when(calculation).calculateRollingValue(anyInt(), any());
    calculation.calculateRollingValue(TWELVE, portfolioReturns);

    verify(correlationCalculation).calculateCorrelation(portfolioReturns, benchmarkReturns);
  }

  @Test
  void shouldReturnCorrelationValue_whenInputsArePrepared() {
    var correlationCalculation = mock(CorrelationCalculation.class);
    var benchmarkTotalReturns = portfolioReturns;
    var context = mock(PeriodCalculationInput.class);
    var calculation = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(context, Set.of(),
        correlationCalculation, benchmarkTotalReturns));
    var benchmarkReturns = portfolioReturns;

    when(calculation.initializePortfolioReturns(any())).thenReturn(portfolioReturns);
    when(calculation.initializeBenchmarkReturns(any())).thenReturn(benchmarkReturns);
    when(correlationCalculation.calculateCorrelation(anyMap(), anyMap())).thenReturn(TEN);

    doCallRealMethod().when(calculation).calculateRollingValue(anyInt(), any());
    BigDecimal actual = calculation.calculateRollingValue(TWELVE, portfolioReturns);

    assertSame(TEN, actual);
  }

  @Test
  void shouldReturnNull_whenCorrelationCannotBeCalculated() {
    var correlationCalculation = mock(CorrelationCalculation.class);
    var benchmarkTotalReturns = portfolioReturns;
    var context = mock(PeriodCalculationInput.class);
    var calculation = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(context, Set.of(),
        correlationCalculation, benchmarkTotalReturns));
    var benchmarkReturns = portfolioReturns;

    when(calculation.initializePortfolioReturns(any())).thenReturn(portfolioReturns);
    when(calculation.initializeBenchmarkReturns(any())).thenReturn(benchmarkReturns);
    when(correlationCalculation.calculateCorrelation(anyMap(), anyMap())).thenReturn(null);

    doCallRealMethod().when(calculation).calculateRollingValue(anyInt(), any());
    BigDecimal actual = calculation.calculateRollingValue(TWELVE, portfolioReturns);

    assertNull(actual);
  }

  @Test
  void shouldGetAdjustedPortfolioReturns_whenBenchmarkStartsLater() {
    var correlationCalculation = mock(CorrelationCalculation.class);
    var benchmarkTotalReturns = portfolioReturns;
    var context = mock(PeriodCalculationInput.class);
    var calculation = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(context, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(calculation.isBenchmarkStartDateGreaterThanPortfolioStartDate(any())).thenReturn(true);

    doCallRealMethod().when(calculation).initializePortfolioReturns(any());
    calculation.initializePortfolioReturns(portfolioReturns);

    verify(calculation).getReturns(any(), any());
  }

  @Test
  void shouldGetAdjustedBenchmarkReturns_whenBenchmarkStartsLater() {
    var correlationCalculation = mock(CorrelationCalculation.class);
    var benchmarkTotalReturns = portfolioReturns;
    var context = mock(PeriodCalculationInput.class);
    var calculation = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(context, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(calculation.isBenchmarkStartDateGreaterThanPortfolioStartDate(any())).thenReturn(true);

    doCallRealMethod().when(calculation).initializeBenchmarkReturns(any());
    calculation.initializeBenchmarkReturns(portfolioReturns);

    verify(calculation).getReturns(any(), any());
  }

  @Test
  void shouldReturnSamePortfolioReturns_whenBenchmarkDoesNotStartLater() {
    var correlationCalculation = mock(CorrelationCalculation.class);
    var benchmarkTotalReturns = portfolioReturns;
    var context = mock(PeriodCalculationInput.class);
    var calculation = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(context, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(calculation.isBenchmarkStartDateGreaterThanPortfolioStartDate(any())).thenReturn(false);

    doCallRealMethod().when(calculation).initializePortfolioReturns(any());
    NavigableMap<LocalDate, BigDecimal> actual = calculation.initializePortfolioReturns(portfolioReturns);

    Assertions.assertEquals(portfolioReturns, actual);
  }

  @Test
  void shouldReturnAdjustedPortfolioReturns_whenBenchmarkStartsLater() {
    var correlationCalculation = mock(CorrelationCalculation.class);
    var benchmarkTotalReturns = portfolioReturns;
    var context = mock(PeriodCalculationInput.class);
    var result = mock(NavigableMap.class);
    var calculation = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(context, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(calculation.getReturns(any(), any())).thenReturn(result);
    when(calculation.isBenchmarkStartDateGreaterThanPortfolioStartDate(any())).thenReturn(true);

    doCallRealMethod().when(calculation).initializePortfolioReturns(any());
    NavigableMap<LocalDate, BigDecimal> actual = calculation.initializePortfolioReturns(portfolioReturns);

    Assertions.assertEquals(result, actual);
  }

  @Test
  void shouldReturnBenchmarkTail_whenBenchmarkDoesNotStartLater() {
    var correlationCalculation = mock(CorrelationCalculation.class);
    var benchmarkTotalReturns = portfolioReturns;
    var context = mock(PeriodCalculationInput.class);
    var calculation = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(context, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(calculation.isBenchmarkStartDateGreaterThanPortfolioStartDate(any())).thenReturn(false);

    doCallRealMethod().when(calculation).initializeBenchmarkReturns(any());
    NavigableMap<LocalDate, BigDecimal> actual = calculation.initializeBenchmarkReturns(portfolioReturns);

    Assertions.assertEquals(TWELVE, actual.size());
  }

  @Test
  void shouldReturnAdjustedBenchmarkReturns_whenBenchmarkStartsLater() {
    var correlationCalculation = mock(CorrelationCalculation.class);
    var benchmarkTotalReturns = portfolioReturns;
    var context = mock(PeriodCalculationInput.class);
    var result = mock(NavigableMap.class);
    var calculation = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(context, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(calculation.getReturns(any(), any())).thenReturn(result);
    when(calculation.isBenchmarkStartDateGreaterThanPortfolioStartDate(any())).thenReturn(true);

    doCallRealMethod().when(calculation).initializeBenchmarkReturns(any());
    NavigableMap<LocalDate, BigDecimal> actual = calculation.initializeBenchmarkReturns(portfolioReturns);

    Assertions.assertEquals(result, actual);
  }

  @Test
  void shouldReturnBenchmarkRangeMatchingPortfolioSize_whenGettingReturns() {
    var correlationCalculation = mock(CorrelationCalculation.class);
    var benchmarkTotalReturns = new TreeMap<>();
    var context = mock(PeriodCalculationInput.class);
    var calculation = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(context, Set.of(),
        correlationCalculation, benchmarkTotalReturns));
    var portfolioReturns = mock(NavigableMap.class);

    benchmarkTotalReturns.put(LocalDate.now().minusMonths(12), HUNDRED);
    benchmarkTotalReturns.put(LocalDate.now().minusMonths(11), HUNDRED);
    benchmarkTotalReturns.put(LocalDate.now().minusMonths(10), HUNDRED);

    when(portfolioReturns.size()).thenReturn(2);

    doCallRealMethod().when(calculation).getReturns(any(), any());
    NavigableMap<LocalDate, BigDecimal> actual = calculation.getReturns(portfolioReturns,
        RollingCorrelationCalculationTest.portfolioReturns);

    Assertions.assertEquals(TWO.intValue(), actual.size());
  }

  @Test
  void shouldReturnFalse_whenBenchmarkStartsNotLaterThanPortfolio() {
    var correlationCalculation = mock(CorrelationCalculation.class);
    var benchmarkTotalReturns = portfolioReturns;
    var context = mock(PeriodCalculationInput.class);
    var calculation = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(context, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    doCallRealMethod().when(calculation).isBenchmarkStartDateGreaterThanPortfolioStartDate(any());
    boolean actual = calculation.isBenchmarkStartDateGreaterThanPortfolioStartDate(portfolioReturns);

    assertFalse(actual);
  }

  @Test
  void shouldReturnTrue_whenBenchmarkStartsLaterThanPortfolio() {
    var correlationCalculation = mock(CorrelationCalculation.class);
    var benchmarkTotalReturns = portfolioReturns;
    NavigableMap<LocalDate, BigDecimal> portfolioReturns = mock(NavigableMap.class);
    var context = mock(PeriodCalculationInput.class);
    var calculation = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(context, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(portfolioReturns.firstKey()).thenReturn(LocalDate.now().minusMonths(13));

    doCallRealMethod().when(calculation).isBenchmarkStartDateGreaterThanPortfolioStartDate(any());
    boolean actual = calculation.isBenchmarkStartDateGreaterThanPortfolioStartDate(portfolioReturns);

    assertTrue(actual);
  }

  @Test
  void shouldDelegateToRollingIntervalResults_whenDefiningResponseType() {
    var calculation = mock(RollingCorrelationCalculation.class);
    NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    returns.put(LocalDate.now().minusMonths(3), TEN);
    var result = Set.of(Pair.of("12", returns));

    doCallRealMethod().when(calculation).defineResponseType(result);
    calculation.defineResponseType(result);

    verify(calculation).getRollingIntervalResults(result);
  }

  @Test
  void shouldMapRollingCorrelationResult_whenDefiningResponseType() {
    var calculation = mock(RollingCorrelationCalculation.class);
    NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    returns.put(LocalDate.now().minusMonths(3), TEN);
    var periodValues = Set.of(Pair.of("12", returns));

    LinkedHashSet<IntervalResult> res = new LinkedHashSet<>();
    res.add(new IntervalResult(LocalDate.now().minusMonths(3), TEN));
    var intervalResult = new RollingIntervalResult("12", res);
    var expected = new RollingCorrelationResult(Set.of(intervalResult));

    when(calculation.getRollingIntervalResults(anySet())).thenReturn(Set.of(intervalResult));

    doCallRealMethod().when(calculation).defineResponseType(periodValues);
    RollingCorrelationResult actual = calculation.defineResponseType(periodValues);

    Assertions.assertEquals(expected.getRollingCorrelation(), actual.getRollingCorrelation());
  }

  @AfterAll
  static void tearDown() {
    portfolioReturns.clear();
  }
}
