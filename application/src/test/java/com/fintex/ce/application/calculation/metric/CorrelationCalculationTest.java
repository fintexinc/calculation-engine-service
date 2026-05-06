package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.util.ComparisonUtils;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.correlation.CorrelationKeyValueResult;
import com.fintex.ce.model.domain.result.correlation.CorrelationPeriodResult;
import com.fintex.ce.model.domain.result.correlation.CorrelationResult;
import com.fintex.ce.model.util.BigDecimalConstants;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static com.fintex.ce.model.util.BigDecimalConstants.TWO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class CorrelationCalculationTest {

  final int TWELVE = 12;

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculatePortfolioBaseTotalReturnValuesByPeriodWhenHoldingHasEnoughReturns() {
    final var context = mock(PeriodCalculationInput.class);
    final var map = mock(Map.class);
    final var portfolioBaseTotalReturn = Map.of(mock(PortfolioHolding.class), map);
    final var calculation = mock(CorrelationCalculation.class, withSettings()
        .useConstructor(context, portfolioBaseTotalReturn, Set.of()));

    final var treeMap = mock(TreeMap.class);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    when(calculation.hasEnoughReturns(anyInt(), any())).thenReturn(true);
    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());

    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(calculation).calculatePortfolioBaseTotalReturnValuesByPeriod(TWELVE, map);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculatePortfolioBaseTotalReturnValuesByPeriodWhenHoldingHasNoTEnoughReturns() {
    final var context = mock(PeriodCalculationInput.class);
    final var map = mock(Map.class);
    final var portfolioBaseTotalReturn = Map.of(mock(PortfolioHolding.class), map);
    final var calculation = mock(CorrelationCalculation.class, withSettings()
        .useConstructor(context, portfolioBaseTotalReturn, Set.of()));

    final var treeMap = mock(TreeMap.class);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    when(calculation.hasEnoughReturns(anyInt(), any())).thenReturn(false);
    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());

    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(calculation, times(0)).calculatePortfolioBaseTotalReturnValuesByPeriod(TWELVE, map);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetCorrelationPeriodResultWhenPortfolioReturnHasEnoughReturns() {
    final var context = mock(PeriodCalculationInput.class);
    final var monthlyReturns = mock(Map.class);
    final var portfolioBaseTotalReturn = Map.of(mock(PortfolioHolding.class), monthlyReturns);
    final var calculation = mock(CorrelationCalculation.class, withSettings()
        .useConstructor(context, portfolioBaseTotalReturn, Set.of()));

    final var treeMap = mock(TreeMap.class);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    when(calculation.hasEnoughReturns(anyInt(), any())).thenReturn(true);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());

    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(calculation).getCorrelationPeriod(any(), any(), eq(TWELVE));
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetCorrelationPeriodResultWhenPortfolioReturnHasNotEnoughReturns() {
    final var context = mock(PeriodCalculationInput.class);
    final var monthlyReturns = mock(Map.class);
    final var portfolioBaseTotalReturn = Map.of(mock(PortfolioHolding.class), monthlyReturns);
    final var calculation = mock(CorrelationCalculation.class, withSettings()
        .useConstructor(context, portfolioBaseTotalReturn, Set.of()));

    final var treeMap = mock(TreeMap.class);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    when(calculation.hasEnoughReturns(anyInt(), any())).thenReturn(false);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());

    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(calculation, times(0)).getCorrelationPeriod(any(), any(), eq(TWELVE));
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyHasEnoughReturns() {
    final var context = mock(PeriodCalculationInput.class);
    final var map = mock(Map.class);
    final var holding = mock(PortfolioHolding.class);
    final var portfolioBaseTotalReturn = Map.of(holding, map);
    final var calculation = mock(CorrelationCalculation.class, withSettings()
        .useConstructor(context, portfolioBaseTotalReturn, Set.of()));

    final var treeMap = mock(TreeMap.class);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(calculation).hasEnoughReturns(eq(TWELVE),
        argThat(entry -> entry.getKey().equals(holding) && entry.getValue().equals(map)));
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResult() {
    final var context = mock(PeriodCalculationInput.class);
    final var monthlyReturns = mock(Map.class);
    final var portfolioBaseTotalReturn = Map.of(mock(PortfolioHolding.class), monthlyReturns);
    final var calculation = mock(CorrelationCalculation.class, withSettings()
        .useConstructor(context, portfolioBaseTotalReturn, Set.of()));

    final var treeMap = mock(TreeMap.class);
    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    final var correlationPeriod = mock(CorrelationPeriodResult.class);
    when(calculation.getCorrelationPeriod(any(), any(), anyInt())).thenReturn(correlationPeriod);

    when(monthlyReturns.size()).thenReturn(14);
    doCallRealMethod().when(calculation).hasEnoughReturns(anyInt(), any());
    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final List<CorrelationPeriodResult> correlationPeriodDtoS = calculation.calculatePeriodForNumberOfMonths(TWELVE);

    assertEquals(1, correlationPeriodDtoS.size());
    assertEquals(correlationPeriod, correlationPeriodDtoS.get(0));
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResultWhenHoldingHasNotEnoughReturns() {
    final var context = mock(PeriodCalculationInput.class);
    final var monthlyReturns = mock(Map.class);
    final var portfolioBaseTotalReturn = Map.of(mock(PortfolioHolding.class), monthlyReturns);
    final var calculation = mock(CorrelationCalculation.class, withSettings()
        .useConstructor(context, portfolioBaseTotalReturn, Set.of()));
    final var treeMap = mock(TreeMap.class);

    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(BigDecimal.ONE.intValue());

    when(calculation.hasEnoughReturns(anyInt(), any())).thenReturn(false);
    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());

    final List<CorrelationPeriodResult> correlationPeriodDtoS = calculation.calculatePeriodForNumberOfMonths(TWELVE);

    assertTrue(correlationPeriodDtoS.isEmpty());
  }

  @Test
  void shouldCalculatePortfolioBaseTotalReturnValuesByPeriod_whenVerifyGetPeriodStartDate() {
    final var calculation = mock(CorrelationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var map = mock(Map.class);

    when(treeMap.size()).thenReturn(BigDecimal.ONE.intValue());
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);

    doCallRealMethod().when(calculation).calculatePortfolioBaseTotalReturnValuesByPeriod(anyInt(), anyMap());
    calculation.calculatePortfolioBaseTotalReturnValuesByPeriod(TWELVE, map);

    verify(calculation).getPeriodStartDate(TWELVE, new TreeMap<>(map));
  }

  @Test
  void shouldCalculatePortfolioBaseTotalReturnValuesByPeriod_whenVerifyGetSubMapByPeriodStartDate() {
    final var calculation = mock(CorrelationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var map = mock(Map.class);
    final var date = LocalDate.now();

    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(BigDecimal.ONE.intValue());
    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);

    doCallRealMethod().when(calculation).calculatePortfolioBaseTotalReturnValuesByPeriod(anyInt(), anyMap());
    calculation.calculatePortfolioBaseTotalReturnValuesByPeriod(TWELVE, map);

    verify(calculation).getSubMapByPeriodStartDate(date, new TreeMap<>(map));
  }

  @Test
  void shouldCalculatePortfolioBaseTotalReturnValuesByPeriod_whenCheckResult() {
    final var calculation = mock(CorrelationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var map = mock(Map.class);
    final var date = LocalDate.now();

    when(treeMap.size()).thenReturn(BigDecimal.ONE.intValue());
    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    final var portfolioBaseTotalReturnByPeriodStartDate = Map.of(date, BigDecimalConstants.TWELVE, date.plusMonths(1),
        TWO);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(new TreeMap<>(
        portfolioBaseTotalReturnByPeriodStartDate));

    doCallRealMethod().when(calculation).calculatePortfolioBaseTotalReturnValuesByPeriod(anyInt(), anyMap());
    final TreeMap<LocalDate, BigDecimal> result = new TreeMap<>(calculation.calculatePortfolioBaseTotalReturnValuesByPeriod(
        TWELVE, map));

    assertEquals(2, result.size());
    assertEquals(toUserScale(BigDecimal.valueOf(5)), toUserScale(result.firstEntry().getValue()));
    assertEquals(toUserScale(BigDecimal.valueOf(-5)), toUserScale(result.lastEntry().getValue()));
  }

  @Test
  void shouldGetCorrelationPeriod_whenVerifyCalculateCorrelation() {
    final var calculation = mock(CorrelationCalculation.class);
    final var date = LocalDate.now();
    final var map = Map.of(date, ONE);
    final var usEtfHolding = new PortfolioHolding(null, FinancialInstrumentType.ETF_US, null);
    final var mutualFundsHolding = new PortfolioHolding(null, FinancialInstrumentType.MUTUAL_FUND_CANADA, null);
    final var holdings = Map.of(usEtfHolding, map, mutualFundsHolding, map);

    when(calculation.calculateCorrelation(any(), any())).thenReturn(BigDecimal.ONE);

    doCallRealMethod().when(calculation).getCorrelationPeriod(any(), any(), anyInt());
    calculation.getCorrelationPeriod(mutualFundsHolding, holdings, TWELVE);

    verify(calculation).calculateCorrelation(map, map);
  }

  @Test
  void shouldGetCorrelationPeriod_whenVerifyMapToCorrelationPeriodResult() {
    final var calculation = mock(CorrelationCalculation.class);
    final var date = LocalDate.now();
    final var map = Map.of(date, ONE);
    final var usEtfHolding = new PortfolioHolding(null, FinancialInstrumentType.ETF_US, null);
    final var mutualFundsHolding = new PortfolioHolding(null, FinancialInstrumentType.MUTUAL_FUND_CANADA, null);
    final var holdings = Map.of(usEtfHolding, map, mutualFundsHolding, map);

    when(calculation.calculateCorrelation(any(), any())).thenReturn(BigDecimal.ONE);

    doCallRealMethod().when(calculation).getCorrelationPeriod(any(), any(), anyInt());
    calculation.getCorrelationPeriod(mutualFundsHolding, holdings, TWELVE);

    verify(calculation).mapToCorrelationPeriodResult(eq(mutualFundsHolding), eq(TWELVE), any());
  }

  @Test
  void shouldCalculateCorrelation_whenVerifyCalculateNumerator() {
    final var calculation = mock(CorrelationCalculation.class);
    final var map = mock(Map.class);

    when(calculation.calculateNumerator(any(), any())).thenReturn(BigDecimal.ONE);
    when(calculation.calculateDenominator(any(), any())).thenReturn(BigDecimal.ONE);
    when(calculation.calculateCorrelation(any(), any())).thenReturn(BigDecimal.ONE);
    doCallRealMethod().when(calculation).calculateCorrelation(any(), any());

    calculation.calculateCorrelation(map, map);

    verify(calculation).calculateNumerator(map, map);
  }

  @Test
  void shouldCalculateCorrelation_whenVerifyCalculateDenominator() {
    final var calculation = mock(CorrelationCalculation.class);
    final var map = mock(Map.class);

    when(calculation.calculateNumerator(any(), any())).thenReturn(BigDecimal.ONE);
    when(calculation.calculateDenominator(any(), any())).thenReturn(BigDecimal.ONE);
    when(calculation.calculateCorrelation(any(), any())).thenReturn(BigDecimal.ONE);

    doCallRealMethod().when(calculation).calculateCorrelation(any(), any());
    calculation.calculateCorrelation(map, map);

    verify(calculation).calculateDenominator(map, map);
  }

  @Test
  void shouldCalculateCorrelation_whenCheckResult() {
    final var calculation = mock(CorrelationCalculation.class);
    final var map = mock(Map.class);

    when(calculation.calculateNumerator(any(), any())).thenReturn(BigDecimalConstants.TWELVE);
    when(calculation.calculateDenominator(any(), any())).thenReturn(TWO);
    when(calculation.calculateCorrelation(any(), any())).thenReturn(BigDecimal.ONE);
    doCallRealMethod().when(calculation).calculateCorrelation(any(), any());

    final BigDecimal result = calculation.calculateCorrelation(map, map);

    assertEquals(toUserScale(BigDecimal.valueOf(6)), toUserScale(result));
  }

  @Test
  void shouldCalculateNumerator_whenCheckResult() {
    final var calculation = mock(CorrelationCalculation.class);
    final var date = LocalDate.now();
    final var map1 = Map.of(date, TWO, date.plusMonths(1), BigDecimalConstants.TWELVE);
    final var map2 = Map.of(date, ONE, date.plusMonths(1), BigDecimalConstants.TWELVE);

    when(calculation.calculateNumerator(any(), any())).thenReturn(BigDecimalConstants.TWELVE);
    when(calculation.calculateDenominator(any(), any())).thenReturn(TWO);
    when(calculation.calculateCorrelation(any(), any())).thenReturn(BigDecimal.ONE);
    doCallRealMethod().when(calculation).calculateNumerator(any(), any());

    final BigDecimal result = calculation.calculateNumerator(map1, map2);

    assertEquals(toUserScale(BigDecimal.valueOf(146)), toUserScale(result));
  }

  @Test
  void shouldCalculateDenominator_whenCheckResult() {
    final var calculation = mock(CorrelationCalculation.class);
    final var date = LocalDate.now();
    final var map1 = Map.of(date, TWO, date.plusMonths(1), BigDecimalConstants.TWELVE);
    final var map2 = Map.of(date, ONE, date.plusMonths(1), BigDecimalConstants.TWELVE);

    when(calculation.getSumOfSquaredValues(any())).thenReturn(BigDecimalConstants.TWELVE);

    doCallRealMethod().when(calculation).calculateDenominator(any(), any());
    final BigDecimal result = calculation.calculateDenominator(map1, map2);

    assertEquals(toUserScale(BigDecimal.valueOf(12)), toUserScale(result));
  }

  @Test
  void shouldCalculateDenominator_whenVerifyGetSumOfSquaredValues() {
    final var calculation = mock(CorrelationCalculation.class);
    final var map = mock(Map.class);

    when(calculation.getSumOfSquaredValues(any())).thenReturn(BigDecimalConstants.TWELVE);

    doCallRealMethod().when(calculation).calculateDenominator(any(), any());
    calculation.calculateDenominator(map, map);

    verify(calculation, times(2)).getSumOfSquaredValues(map);
  }

  @Test
  void shouldGetSumOfSquaredValues_whenCheckResults() {
    final var calculation = mock(CorrelationCalculation.class);
    final var date = LocalDate.now();
    final var map = Map.of(date, TWO, date.plusMonths(1), BigDecimalConstants.TWELVE);

    when(calculation.getSumOfSquaredValues(any())).thenReturn(BigDecimalConstants.TWELVE);

    doCallRealMethod().when(calculation).getSumOfSquaredValues(any());
    final BigDecimal sumOfSquaredValues = calculation.getSumOfSquaredValues(map);

    assertEquals(toUserScale(BigDecimal.valueOf(148)), toUserScale(sumOfSquaredValues));
  }

  @Test
  void shouldBuildCorrelationPeriodResult_whenMappingCorrelationValues() {
    final var calculation = mock(CorrelationCalculation.class);
    final var usEtfHolding = new PortfolioHolding(null, FinancialInstrumentType.ETF_US,
        new SecurityIdentifier("TEST", FiIdentifierType.FUNDSERV));
    final var mutualFundsHolding = new PortfolioHolding(null, FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("TEST", FiIdentifierType.FUNDSERV));
    final var map = Map.of(mutualFundsHolding, BigDecimalConstants.TWELVE);

    doCallRealMethod().when(calculation).mapToCorrelationPeriodResult(any(), anyInt(), any());
    final CorrelationPeriodResult correlationPeriod = calculation.mapToCorrelationPeriodResult(usEtfHolding, TWELVE, map);

    assertEquals(String.valueOf(TWELVE), correlationPeriod.period());
    assertEquals("ETF_US_TEST", correlationPeriod.key());
    assertEquals(1, correlationPeriod.correlations().size());
    assertEquals("MUTUAL_FUND_CANADA_TEST", correlationPeriod.correlations().get(0).correlationKey());
    assertEquals(BigDecimal.valueOf(TWELVE), correlationPeriod.correlations().get(0).value());
  }

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    final var context = mock(PeriodCalculationInput.class);
    final var map = Map.of(new PortfolioHolding(null, FinancialInstrumentType.ETF_US,
        new SecurityIdentifier("TEST", FiIdentifierType.FUNDSERV)), mock(Map.class));
    final var portfolioBaseTotalReturn = Map.of(mock(PortfolioHolding.class), map);
    final var calculation = mock(CorrelationCalculation.class, withSettings()
        .useConstructor(context, portfolioBaseTotalReturn, Set.of()));

    final var listMock = List.of(new CorrelationPeriodResult(null, null, null));
    final var pairs = Set.of(Pair.of("2000-01-12", listMock), Pair.of("2020-01-05", listMock));
    when(calculation.setPeriod(anyString(), anyList())).thenReturn(listMock);

    doCallRealMethod().when(calculation).defineResponseType(anySet());
    final CorrelationResult result = calculation.defineResponseType(pairs);

    assertEquals(2, result.getCorrelationPeriods().size());
    Assertions.assertEquals(listMock.get(0), result.getCorrelationPeriods().get(0));
    Assertions.assertEquals(listMock.get(0), result.getCorrelationPeriods().get(1));
    assertEquals(1, result.getHoldingsKey().size());
  }

  @Test
  void shouldReturnEmptyList_whenFormattingEmptyCorrelationPeriods() {
    final var calculation = mock(CorrelationCalculation.class);

    final List<CorrelationPeriodResult> expected = List.of();

    doCallRealMethod().when(calculation).toUserFormat(any());
    final var actual = calculation.toUserFormat(List.of());

    ComparisonUtils.compareCollections(expected, actual);
  }

  @Test
  void shouldReturnNull_whenFormattingNullCorrelationPeriods() {
    final var calculation = mock(CorrelationCalculation.class);

    doCallRealMethod().when(calculation).toUserFormat(any());
    final var actual = calculation.toUserFormat(null);

    assertNull(actual);
  }

  @Test
  void shouldKeepValuesUnchanged_whenFormattingAlreadyScaledCorrelationPeriods() {
    final var calculation = mock(CorrelationCalculation.class);

    final var correlationPeriod = new CorrelationPeriodResult(null, null, List.of());
    final var argument = List.of(correlationPeriod);

    final var correlationPeriodDtoExpected = new CorrelationPeriodResult(null, null, List.of());
    final var expected = List.of(correlationPeriodDtoExpected);

    doCallRealMethod().when(calculation).toUserFormat(any());
    final var actual = calculation.toUserFormat(argument);

    assertEquals(expected, actual);
  }

  @Test
  void shouldRoundCorrelationValues_whenFormattingCorrelationPeriods() {
    final var calculation = mock(CorrelationCalculation.class);

    final var correlationKeyValueResult = new CorrelationKeyValueResult(null, new BigDecimal("0.123456789112345"));
    final var correlationPeriod = new CorrelationPeriodResult(null, null, List.of(correlationKeyValueResult));
    final var argument = List.of(correlationPeriod);

    final var correlationKeyValueResultExpected = new CorrelationKeyValueResult(null, new BigDecimal("0.1234567891"));
    final var correlationPeriodDtoExpected = new CorrelationPeriodResult(null, null,
        List.of(correlationKeyValueResultExpected));
    final var expected = List.of(correlationPeriodDtoExpected);

    doCallRealMethod().when(calculation).toUserFormat(any());
    final var actual = calculation.toUserFormat(argument);

    assertEquals(expected, actual);
  }

  @Test
  void shouldSetPeriod_whenCheckResult() {
    final var calculation = mock(CorrelationCalculation.class);

    final String period = "SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE";
    final var correlationPeriod = new CorrelationPeriodResult("20", null, null);
    final var periods = List.of(correlationPeriod);

    doCallRealMethod().when(calculation).setPeriod(anyString(), anyList());
    final var actual = calculation.setPeriod(period, periods);

    assertEquals(period, actual.get(0).period());
  }

}
