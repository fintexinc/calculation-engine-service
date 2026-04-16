package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.result.IntervalResult;
import com.fintex.ce.model.domain.result.RollingIntervalResult;
import com.fintex.ce.model.domain.result.rolling.RollingCorrelationResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = mock(TreeMap.class);
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));
    final int numberOfMonths = TWELVE;

    when(benchmarkTotalReturns.size()).thenReturn(1);

    doCallRealMethod().when(sut).calculateRollingValue(anyInt(), any());
    final BigDecimal actual = sut.calculateRollingValue(numberOfMonths, portfolioReturns);

    assertNull(actual);
  }

  @Test
  void shouldInitializePortfolioReturns_whenCalculatingRollingValue() {
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    final int numberOfMonths = TWELVE;

    doCallRealMethod().when(sut).calculateRollingValue(anyInt(), any());
    sut.calculateRollingValue(numberOfMonths, portfolioReturns);

    verify(sut).initializePortfolioReturns(portfolioReturns);
  }

  @Test
  void shouldInitializeBenchmarkReturns_whenCalculatingRollingValue() {
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    final int numberOfMonths = TWELVE;

    when(sut.initializePortfolioReturns(any())).thenReturn(portfolioReturns);
    doCallRealMethod().when(sut).calculateRollingValue(anyInt(), any());
    sut.calculateRollingValue(numberOfMonths, portfolioReturns);

    verify(sut).initializeBenchmarkReturns(portfolioReturns);
  }

  @Test
  void shouldDelegateToCorrelationCalculation_whenCalculatingRollingValue() {
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));
    final int numberOfMonths = TWELVE;
    final var benchmarkReturns = portfolioReturns;

    when(sut.initializePortfolioReturns(any())).thenReturn(portfolioReturns);
    when(sut.initializeBenchmarkReturns(any())).thenReturn(benchmarkReturns);

    doCallRealMethod().when(sut).calculateRollingValue(anyInt(), any());
    sut.calculateRollingValue(numberOfMonths, portfolioReturns);

    verify(correlationCalculation).calculateCorrelation(portfolioReturns, benchmarkReturns);
  }

  @Test
  void shouldReturnCorrelationValue_whenInputsArePrepared() {
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));
    final int numberOfMonths = TWELVE;
    final var benchmarkReturns = portfolioReturns;

    when(sut.initializePortfolioReturns(any())).thenReturn(portfolioReturns);
    when(sut.initializeBenchmarkReturns(any())).thenReturn(benchmarkReturns);
    when(correlationCalculation.calculateCorrelation(anyMap(), anyMap())).thenReturn(TEN);

    doCallRealMethod().when(sut).calculateRollingValue(anyInt(), any());
    final BigDecimal actual = sut.calculateRollingValue(numberOfMonths, portfolioReturns);

    assertSame(TEN, actual);
  }

  @Test
  void shouldGetAdjustedPortfolioReturns_whenBenchmarkStartsLater() {
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(sut.isBenchmarkStartDateGreaterThanPortfolioStartDate(any())).thenReturn(true);

    doCallRealMethod().when(sut).initializePortfolioReturns(any());
    sut.initializePortfolioReturns(portfolioReturns);

    verify(sut).getReturns(any(), any());
  }

  @Test
  void shouldGetAdjustedBenchmarkReturns_whenBenchmarkStartsLater() {
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(sut.isBenchmarkStartDateGreaterThanPortfolioStartDate(any())).thenReturn(true);

    doCallRealMethod().when(sut).initializeBenchmarkReturns(any());
    sut.initializeBenchmarkReturns(portfolioReturns);

    verify(sut).getReturns(any(), any());
  }

  @Test
  void shouldReturnSamePortfolioReturns_whenBenchmarkDoesNotStartLater() {
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(sut.isBenchmarkStartDateGreaterThanPortfolioStartDate(any())).thenReturn(false);

    doCallRealMethod().when(sut).initializePortfolioReturns(any());
    final NavigableMap<LocalDate, BigDecimal> actual = sut.initializePortfolioReturns(portfolioReturns);

    Assertions.assertEquals(portfolioReturns, actual);
  }

  @Test
  void shouldReturnAdjustedPortfolioReturns_whenBenchmarkStartsLater() {
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final var calculationDTO = mock(CalculationDTO.class);
    final var result = mock(NavigableMap.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(sut.getReturns(any(), any())).thenReturn(result);
    when(sut.isBenchmarkStartDateGreaterThanPortfolioStartDate(any())).thenReturn(true);

    doCallRealMethod().when(sut).initializePortfolioReturns(any());
    final NavigableMap<LocalDate, BigDecimal> actual = sut.initializePortfolioReturns(portfolioReturns);

    Assertions.assertEquals(result, actual);
  }

  @Test
  void shouldReturnBenchmarkTail_whenBenchmarkDoesNotStartLater() {
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(sut.isBenchmarkStartDateGreaterThanPortfolioStartDate(any())).thenReturn(false);

    doCallRealMethod().when(sut).initializeBenchmarkReturns(any());
    final NavigableMap<LocalDate, BigDecimal> actual = sut.initializeBenchmarkReturns(portfolioReturns);

    Assertions.assertEquals(TWELVE, actual.size());
  }

  @Test
  void shouldReturnAdjustedBenchmarkReturns_whenBenchmarkStartsLater() {
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final var calculationDTO = mock(CalculationDTO.class);
    final var result = mock(NavigableMap.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(sut.getReturns(any(), any())).thenReturn(result);
    when(sut.isBenchmarkStartDateGreaterThanPortfolioStartDate(any())).thenReturn(true);

    doCallRealMethod().when(sut).initializeBenchmarkReturns(any());
    final NavigableMap<LocalDate, BigDecimal> actual = sut.initializeBenchmarkReturns(portfolioReturns);

    Assertions.assertEquals(result, actual);
  }

  @Test
  void shouldReturnBenchmarkRangeMatchingPortfolioSize_whenGettingReturns() {
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = new TreeMap<>();
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));
    final var portfolioReturns = mock(NavigableMap.class);

    benchmarkTotalReturns.put(LocalDate.now().minusMonths(12), HUNDRED);
    benchmarkTotalReturns.put(LocalDate.now().minusMonths(11), HUNDRED);
    benchmarkTotalReturns.put(LocalDate.now().minusMonths(10), HUNDRED);

    when(portfolioReturns.size()).thenReturn(2);

    doCallRealMethod().when(sut).getReturns(any(), any());
    final NavigableMap<LocalDate, BigDecimal> actual = sut.getReturns(portfolioReturns,
        RollingCorrelationCalculationTest.portfolioReturns);

    Assertions.assertEquals(TWO.intValue(), actual.size());
  }

  @Test
  void shouldReturnFalse_whenBenchmarkStartsNotLaterThanPortfolio() {
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    doCallRealMethod().when(sut).isBenchmarkStartDateGreaterThanPortfolioStartDate(any());
    final boolean actual = sut.isBenchmarkStartDateGreaterThanPortfolioStartDate(portfolioReturns);

    assertFalse(actual);
  }

  @Test
  void shouldReturnTrue_whenBenchmarkStartsLaterThanPortfolio() {
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final NavigableMap<LocalDate, BigDecimal> portfolioReturns = mock(NavigableMap.class);
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(portfolioReturns.firstKey()).thenReturn(LocalDate.now().minusMonths(13));

    doCallRealMethod().when(sut).isBenchmarkStartDateGreaterThanPortfolioStartDate(any());
    final boolean actual = sut.isBenchmarkStartDateGreaterThanPortfolioStartDate(portfolioReturns);

    assertTrue(actual);
  }

  @Test
  void shouldDelegateToRollingIntervalResults_whenDefiningResponseType() {
    final var sut = mock(RollingCorrelationCalculation.class);
    final NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    returns.put(LocalDate.now().minusMonths(3), TEN);
    final var result = Set.of(Pair.of("12", returns));

    doCallRealMethod().when(sut).defineResponseType(result);
    sut.defineResponseType(result);

    verify(sut).getRollingIntervalResults(result);
  }

  @Test
  void shouldMapRollingCorrelationResult_whenDefiningResponseType() {
    final var sut = mock(RollingCorrelationCalculation.class);
    final NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    returns.put(LocalDate.now().minusMonths(3), TEN);
    final var result = Set.of(Pair.of("12", returns));

    final LinkedHashSet<IntervalResult> res = new LinkedHashSet<>();
    res.add(new IntervalResult(LocalDate.now().minusMonths(3), TEN));
    final var resDTO = new RollingIntervalResult("12", res);
    final var expected = new RollingCorrelationResult().setRollingCorrelation(Set.of(resDTO));

    when(sut.getRollingIntervalResults(anySet())).thenReturn(Set.of(resDTO));

    doCallRealMethod().when(sut).defineResponseType(result);
    final RollingCorrelationResult actual = sut.defineResponseType(result);

    Assertions.assertEquals(expected.getRollingCorrelation(), actual.getRollingCorrelation());
  }

  @AfterAll
  static void tearDown() {
    portfolioReturns.clear();
  }
}
