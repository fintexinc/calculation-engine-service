package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.CorrelationCalculation;
import com.fintex.ce.application.calculation.RollingCorrelationCalculation;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.port.input.result.RollingCorrelationResult;
import com.fintex.ce.port.input.result.core.RollingIntervalResult;
import com.fintex.ce.port.input.result.core.IntervalResult;
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

import static com.fintex.ce.domain.constant.BigDecimalConstants.*;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
  void calculateRollingValue_checkResult() {
    // SETUP
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = mock(TreeMap.class);
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));
    final int numberOfMonths = TWELVE;

    when(benchmarkTotalReturns.size()).thenReturn(1);

    doCallRealMethod().when(sut).calculateRollingValue(anyInt(), any());
    // ACT
    final BigDecimal actual = sut.calculateRollingValue(numberOfMonths, portfolioReturns);

    // VERIFY
    assertNull(actual);
  }

  @Test
  void calculateRollingValue_verifyInitializePortfilioReturns() {
    // SETUP
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    final int numberOfMonths = TWELVE;

    doCallRealMethod().when(sut).calculateRollingValue(anyInt(), any());
    // ACT
    sut.calculateRollingValue(numberOfMonths, portfolioReturns);

    // VERIFY
    verify(sut).initializePortfolioReturns(portfolioReturns);
  }

  @Test
  void calculateRollingValue_verifyInitializeBanchmarkReturns() {
    // SETUP
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    final int numberOfMonths = TWELVE;

    when(sut.initializePortfolioReturns(any())).thenReturn(portfolioReturns);
    doCallRealMethod().when(sut).calculateRollingValue(anyInt(), any());
    // ACT
    sut.calculateRollingValue(numberOfMonths, portfolioReturns);

    // VERIFY
    verify(sut).initializeBenchmarkReturns(portfolioReturns);
  }

  @Test
  void calculateRollingValue_verifyCalculateCorrelation() {
    // SETUP
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
    // ACT
    sut.calculateRollingValue(numberOfMonths, portfolioReturns);

    // VERIFY
    verify(correlationCalculation).calculateCorrelation(portfolioReturns, benchmarkReturns);
  }

  @Test
  void calculateRollingValue_checkResult2() {
    // SETUP
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
    // ACT
    final BigDecimal actual = sut.calculateRollingValue(numberOfMonths, portfolioReturns);

    // VERIFY
    assertSame(TEN, actual);
  }

  @Test
  void initializePortfolioReturns_verifyGetReturns() {
    // SETUP
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(sut.isBenchmarkStartDateGreaterThanPortfolioStartDate(any())).thenReturn(true);

    doCallRealMethod().when(sut).initializePortfolioReturns(any());
    // ACT
    sut.initializePortfolioReturns(portfolioReturns);

    // VERIFY
    verify(sut).getReturns(any(), any());
  }

  @Test
  void initializeBenchmarkReturns_verifyGetReturns() {
    // SETUP
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(sut.isBenchmarkStartDateGreaterThanPortfolioStartDate(any())).thenReturn(true);

    doCallRealMethod().when(sut).initializeBenchmarkReturns(any());
    // ACT
    sut.initializeBenchmarkReturns(portfolioReturns);

    // VERIFY
    verify(sut).getReturns(any(), any());
  }

  @Test
  void initializePortfolioReturns_checkResult() {
    // SETUP
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(sut.isBenchmarkStartDateGreaterThanPortfolioStartDate(any())).thenReturn(false);

    doCallRealMethod().when(sut).initializePortfolioReturns(any());
    // ACT
    final NavigableMap<LocalDate, BigDecimal> actual = sut.initializePortfolioReturns(portfolioReturns);

    // VERIFY
    Assertions.assertEquals(portfolioReturns, actual);
  }

  @Test
  void initializePortfolioReturns_checkResult2() {
    // SETUP
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final var calculationDTO = mock(CalculationDTO.class);
    final var result = mock(NavigableMap.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(sut.getReturns(any(), any())).thenReturn(result);
    when(sut.isBenchmarkStartDateGreaterThanPortfolioStartDate(any())).thenReturn(true);

    doCallRealMethod().when(sut).initializePortfolioReturns(any());
    // ACT
    final NavigableMap<LocalDate, BigDecimal> actual = sut.initializePortfolioReturns(portfolioReturns);

    // VERIFY
    Assertions.assertEquals(result, actual);
  }

  @Test
  void initializeBenchmarkReturns_checkResult() {
    // SETUP
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(sut.isBenchmarkStartDateGreaterThanPortfolioStartDate(any())).thenReturn(false);

    doCallRealMethod().when(sut).initializeBenchmarkReturns(any());
    // ACT
    final NavigableMap<LocalDate, BigDecimal> actual = sut.initializeBenchmarkReturns(portfolioReturns);

    // VERIFY
    Assertions.assertEquals(TWELVE, actual.size());
  }

  @Test
  void initializeBenchmarkReturns_checkResult2() {
    // SETUP
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final var calculationDTO = mock(CalculationDTO.class);
    final var result = mock(NavigableMap.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(sut.getReturns(any(), any())).thenReturn(result);
    when(sut.isBenchmarkStartDateGreaterThanPortfolioStartDate(any())).thenReturn(true);

    doCallRealMethod().when(sut).initializeBenchmarkReturns(any());
    // ACT
    final NavigableMap<LocalDate, BigDecimal> actual = sut.initializeBenchmarkReturns(portfolioReturns);

    // VERIFY
    Assertions.assertEquals(result, actual);
  }

  @Test
  void getReturns_checkResult() {
    // SETUP
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
    // ACT
    final NavigableMap<LocalDate, BigDecimal> actual = sut.getReturns(portfolioReturns,
        RollingCorrelationCalculationTest.portfolioReturns);

    // VERIFY
    Assertions.assertEquals(TWO.intValue(), actual.size());
  }

  @Test
  void isBenchmarkFirstKeyGreaterThanPortfolioFirstKey_checkResult() {
    // SETUP
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    doCallRealMethod().when(sut).isBenchmarkStartDateGreaterThanPortfolioStartDate(any());
    // ACT
    final boolean actual = sut.isBenchmarkStartDateGreaterThanPortfolioStartDate(portfolioReturns);

    // VERIFY
    assertFalse(actual);
  }

  @Test
  void isBenchmarkFirstKeyGreaterThanPortfolioFirstKey_checkResult2() {
    // SETUP
    final var correlationCalculation = mock(CorrelationCalculation.class);
    final var benchmarkTotalReturns = portfolioReturns;
    final NavigableMap<LocalDate, BigDecimal> portfolioReturns = mock(NavigableMap.class);
    final var calculationDTO = mock(CalculationDTO.class);
    final var sut = mock(RollingCorrelationCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        correlationCalculation, benchmarkTotalReturns));

    when(portfolioReturns.firstKey()).thenReturn(LocalDate.now().minusMonths(13));

    doCallRealMethod().when(sut).isBenchmarkStartDateGreaterThanPortfolioStartDate(any());
    // ACT
    final boolean actual = sut.isBenchmarkStartDateGreaterThanPortfolioStartDate(portfolioReturns);

    // VERIFY
    assertTrue(actual);
  }

  @Test
  void defineResponseType_verifyGetRollingIntervalResultS() {
    // SETUP
    final var sut = mock(RollingCorrelationCalculation.class);
    final NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    returns.put(LocalDate.now().minusMonths(3), TEN);
    final var result = Set.of(Pair.of("12", returns));

    doCallRealMethod().when(sut).defineResponseType(result);
    // ACT
    sut.defineResponseType(result);

    // VERIFY
    verify(sut).getRollingIntervalResults(result);
  }

  @Test
  void defineResponseType_checkResult() {
    // SETUP
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
    // ACT
    final RollingCorrelationResult actual = sut.defineResponseType(result);

    // VERIFY
    Assertions.assertEquals(expected.getRollingCorrelation(), actual.getRollingCorrelation());
  }

  @AfterAll
  static void tearDown() {
    portfolioReturns.clear();
  }
}