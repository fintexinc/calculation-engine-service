package com.fintex.ce.application.calculation;

import com.fintex.ce.domain.constant.BigDecimalConstants;
import com.fintex.ce.domain.model.enumeration.HoldingType;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.CorrelationResult;
import com.fintex.ce.domain.model.result.correlation.CorrelationKeyValueResult;
import com.fintex.ce.domain.model.result.correlation.CorrelationPeriodResult;
import com.fintex.ce.util.ComparisonUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.domain.constant.BigDecimalConstants.ONE;
import static com.fintex.ce.domain.constant.BigDecimalConstants.TWO;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
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
    final var calculationDTO = mock(CalculationDTO.class);
    final var map = mock(Map.class);
    final var portfolioBaseTotalReturn = Map.of(mock(Holding.class), map);
    final var sut = mock(CorrelationCalculation.class, withSettings()
        .useConstructor(calculationDTO, portfolioBaseTotalReturn, Set.of()));

    final var treeMap = mock(TreeMap.class);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    when(sut.hasEnoughReturns(anyInt(), any())).thenReturn(true);
    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

    sut.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sut).calculatePortfolioBaseTotalReturnValuesByPeriod(TWELVE, map);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculatePortfolioBaseTotalReturnValuesByPeriodWhenHoldingHasNoTEnoughReturns() {
    final var calculationDTO = mock(CalculationDTO.class);
    final var map = mock(Map.class);
    final var portfolioBaseTotalReturn = Map.of(mock(Holding.class), map);
    final var sut = mock(CorrelationCalculation.class, withSettings()
        .useConstructor(calculationDTO, portfolioBaseTotalReturn, Set.of()));

    final var treeMap = mock(TreeMap.class);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    when(sut.hasEnoughReturns(anyInt(), any())).thenReturn(false);
    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

    sut.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sut, times(0)).calculatePortfolioBaseTotalReturnValuesByPeriod(TWELVE, map);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetCorrelationPeriodResultWhenPortfolioReturnHasEnoughReturns() {
    final var calculationDTO = mock(CalculationDTO.class);
    final var monthlyReturns = mock(Map.class);
    final var portfolioBaseTotalReturn = Map.of(mock(Holding.class), monthlyReturns);
    final var sut = mock(CorrelationCalculation.class, withSettings()
        .useConstructor(calculationDTO, portfolioBaseTotalReturn, Set.of()));

    final var treeMap = mock(TreeMap.class);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    when(sut.hasEnoughReturns(anyInt(), any())).thenReturn(true);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

    sut.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sut).getCorrelationPeriod(any(), any(), eq(TWELVE));
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetCorrelationPeriodResultWhenPortfolioReturnHasNotEnoughReturns() {
    final var calculationDTO = mock(CalculationDTO.class);
    final var monthlyReturns = mock(Map.class);
    final var portfolioBaseTotalReturn = Map.of(mock(Holding.class), monthlyReturns);
    final var sut = mock(CorrelationCalculation.class, withSettings()
        .useConstructor(calculationDTO, portfolioBaseTotalReturn, Set.of()));

    final var treeMap = mock(TreeMap.class);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    when(sut.hasEnoughReturns(anyInt(), any())).thenReturn(false);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

    sut.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sut, times(0)).getCorrelationPeriod(any(), any(), eq(TWELVE));
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyHasEnoughReturns() {
    final var calculationDTO = mock(CalculationDTO.class);
    final var map = mock(Map.class);
    final var holding = mock(Holding.class);
    final var portfolioBaseTotalReturn = Map.of(holding, map);
    final var sut = mock(CorrelationCalculation.class, withSettings()
        .useConstructor(calculationDTO, portfolioBaseTotalReturn, Set.of()));

    final var treeMap = mock(TreeMap.class);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sut).hasEnoughReturns(eq(TWELVE),
        argThat(entry -> entry.getKey().equals(holding) && entry.getValue().equals(map)));
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResult() {
    final var calculationDTO = mock(CalculationDTO.class);
    final var monthlyReturns = mock(Map.class);
    final var portfolioBaseTotalReturn = Map.of(mock(Holding.class), monthlyReturns);
    final var sut = mock(CorrelationCalculation.class, withSettings()
        .useConstructor(calculationDTO, portfolioBaseTotalReturn, Set.of()));

    final var treeMap = mock(TreeMap.class);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    final var correlationPeriodDTO = mock(CorrelationPeriodResult.class);
    when(sut.getCorrelationPeriod(any(), any(), anyInt())).thenReturn(correlationPeriodDTO);

    when(monthlyReturns.size()).thenReturn(14);
    doCallRealMethod().when(sut).hasEnoughReturns(anyInt(), any());
    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    final List<CorrelationPeriodResult> correlationPeriodDTOS = sut.calculatePeriodForNumberOfMonths(TWELVE);

    assertEquals(1, correlationPeriodDTOS.size());
    assertEquals(correlationPeriodDTO, correlationPeriodDTOS.get(0));
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResultWhenHoldingHasNotEnoughReturns() {
    final var calculationDTO = mock(CalculationDTO.class);
    final var monthlyReturns = mock(Map.class);
    final var portfolioBaseTotalReturn = Map.of(mock(Holding.class), monthlyReturns);
    final var sut = mock(CorrelationCalculation.class, withSettings()
        .useConstructor(calculationDTO, portfolioBaseTotalReturn, Set.of()));
    final var treeMap = mock(TreeMap.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(BigDecimal.ONE.intValue());

    when(sut.hasEnoughReturns(anyInt(), any())).thenReturn(false);
    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

    final List<CorrelationPeriodResult> correlationPeriodDTOS = sut.calculatePeriodForNumberOfMonths(TWELVE);

    assertTrue(correlationPeriodDTOS.isEmpty());
  }

  @Test
  void shouldCalculatePortfolioBaseTotalReturnValuesByPeriod_whenVerifyGetPeriodStartDate() {
    final var sut = mock(CorrelationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var map = mock(Map.class);

    when(treeMap.size()).thenReturn(BigDecimal.ONE.intValue());
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);

    doCallRealMethod().when(sut).calculatePortfolioBaseTotalReturnValuesByPeriod(anyInt(), anyMap());
    sut.calculatePortfolioBaseTotalReturnValuesByPeriod(TWELVE, map);

    verify(sut).getPeriodStartDate(TWELVE, new TreeMap<>(map));
  }

  @Test
  void shouldCalculatePortfolioBaseTotalReturnValuesByPeriod_whenVerifyGetSubMapByPeriodStartDate() {
    final var sut = mock(CorrelationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var map = mock(Map.class);
    final var date = LocalDate.now();

    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(BigDecimal.ONE.intValue());
    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);

    doCallRealMethod().when(sut).calculatePortfolioBaseTotalReturnValuesByPeriod(anyInt(), anyMap());
    sut.calculatePortfolioBaseTotalReturnValuesByPeriod(TWELVE, map);

    verify(sut).getSubMapByPeriodStartDate(date, new TreeMap<>(map));
  }

  @Test
  void shouldCalculatePortfolioBaseTotalReturnValuesByPeriod_whenCheckResult() {
    final var sut = mock(CorrelationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var map = mock(Map.class);
    final var date = LocalDate.now();

    when(treeMap.size()).thenReturn(BigDecimal.ONE.intValue());
    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    final var portfolioBaseTotalReturnByPeriodStartDate = Map.of(date, BigDecimalConstants.TWELVE, date.plusMonths(1),
        TWO);
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(new TreeMap<>(
        portfolioBaseTotalReturnByPeriodStartDate));

    doCallRealMethod().when(sut).calculatePortfolioBaseTotalReturnValuesByPeriod(anyInt(), anyMap());
    final TreeMap<LocalDate, BigDecimal> result = new TreeMap<>(sut.calculatePortfolioBaseTotalReturnValuesByPeriod(
        TWELVE, map));

    assertEquals(2, result.size());
    assertEquals(toUserScale(BigDecimal.valueOf(5)), toUserScale(result.firstEntry().getValue()));
    assertEquals(toUserScale(BigDecimal.valueOf(-5)), toUserScale(result.lastEntry().getValue()));
  }

  @Test
  void shouldGetCorrelationPeriod_whenVerifyCalculateCorrelation() {
    final var sut = mock(CorrelationCalculation.class);
    final var date = LocalDate.now();
    final var map = Map.of(date, ONE);
    final var usEtfHolding = new Holding().setType(HoldingType.US_ETF);
    final var mutualFundsHolding = new Holding().setType(HoldingType.CANADA_MUTUAL_FUNDS);
    final var holdings = Map.of(usEtfHolding, map, mutualFundsHolding, map);

    when(sut.calculateCorrelation(any(), any())).thenReturn(BigDecimal.ONE);

    doCallRealMethod().when(sut).getCorrelationPeriod(any(), any(), anyInt());
    sut.getCorrelationPeriod(mutualFundsHolding, holdings, TWELVE);

    verify(sut).calculateCorrelation(map, map);
  }

  @Test
  void shouldGetCorrelationPeriod_whenVerifyMapToCorrelationPeriodResult() {
    final var sut = mock(CorrelationCalculation.class);
    final var date = LocalDate.now();
    final var map = Map.of(date, ONE);
    final var usEtfHolding = new Holding().setType(HoldingType.US_ETF);
    final var mutualFundsHolding = new Holding().setType(HoldingType.CANADA_MUTUAL_FUNDS);
    final var holdings = Map.of(usEtfHolding, map, mutualFundsHolding, map);

    when(sut.calculateCorrelation(any(), any())).thenReturn(BigDecimal.ONE);

    doCallRealMethod().when(sut).getCorrelationPeriod(any(), any(), anyInt());
    sut.getCorrelationPeriod(mutualFundsHolding, holdings, TWELVE);

    verify(sut).mapToCorrelationPeriodResult(eq(mutualFundsHolding), eq(TWELVE), any());
  }

  @Test
  void shouldCalculateCorrelation_whenVerifyCalculateNumerator() {
    final var sut = mock(CorrelationCalculation.class);
    final var map = mock(Map.class);

    when(sut.calculateNumerator(any(), any())).thenReturn(BigDecimal.ONE);
    when(sut.calculateDenominator(any(), any())).thenReturn(BigDecimal.ONE);
    when(sut.calculateCorrelation(any(), any())).thenReturn(BigDecimal.ONE);
    doCallRealMethod().when(sut).calculateCorrelation(any(), any());

    sut.calculateCorrelation(map, map);

    verify(sut).calculateNumerator(map, map);
  }

  @Test
  void shouldCalculateCorrelation_whenVerifyCalculateDenominator() {
    final var sut = mock(CorrelationCalculation.class);
    final var map = mock(Map.class);

    when(sut.calculateNumerator(any(), any())).thenReturn(BigDecimal.ONE);
    when(sut.calculateDenominator(any(), any())).thenReturn(BigDecimal.ONE);
    when(sut.calculateCorrelation(any(), any())).thenReturn(BigDecimal.ONE);

    doCallRealMethod().when(sut).calculateCorrelation(any(), any());
    sut.calculateCorrelation(map, map);

    verify(sut).calculateDenominator(map, map);
  }

  @Test
  void shouldCalculateCorrelation_whenCheckResult() {
    final var sut = mock(CorrelationCalculation.class);
    final var map = mock(Map.class);

    when(sut.calculateNumerator(any(), any())).thenReturn(BigDecimalConstants.TWELVE);
    when(sut.calculateDenominator(any(), any())).thenReturn(TWO);
    when(sut.calculateCorrelation(any(), any())).thenReturn(BigDecimal.ONE);
    doCallRealMethod().when(sut).calculateCorrelation(any(), any());

    final BigDecimal result = sut.calculateCorrelation(map, map);

    assertEquals(toUserScale(BigDecimal.valueOf(6)), toUserScale(result));
  }

  @Test
  void shouldCalculateNumerator_whenCheckResult() {
    final var sut = mock(CorrelationCalculation.class);
    final var date = LocalDate.now();
    final var map1 = Map.of(date, TWO, date.plusMonths(1), BigDecimalConstants.TWELVE);
    final var map2 = Map.of(date, ONE, date.plusMonths(1), BigDecimalConstants.TWELVE);

    when(sut.calculateNumerator(any(), any())).thenReturn(BigDecimalConstants.TWELVE);
    when(sut.calculateDenominator(any(), any())).thenReturn(TWO);
    when(sut.calculateCorrelation(any(), any())).thenReturn(BigDecimal.ONE);
    doCallRealMethod().when(sut).calculateNumerator(any(), any());

    final BigDecimal result = sut.calculateNumerator(map1, map2);

    assertEquals(toUserScale(BigDecimal.valueOf(146)), toUserScale(result));
  }

  @Test
  void shouldCalculateDenominator_whenCheckResult() {
    final var sut = mock(CorrelationCalculation.class);
    final var date = LocalDate.now();
    final var map1 = Map.of(date, TWO, date.plusMonths(1), BigDecimalConstants.TWELVE);
    final var map2 = Map.of(date, ONE, date.plusMonths(1), BigDecimalConstants.TWELVE);

    when(sut.getSumOfSquaredValues(any())).thenReturn(BigDecimalConstants.TWELVE);

    doCallRealMethod().when(sut).calculateDenominator(any(), any());
    final BigDecimal result = sut.calculateDenominator(map1, map2);

    assertEquals(toUserScale(BigDecimal.valueOf(12)), toUserScale(result));
  }

  @Test
  void shouldCalculateDenominator_whenVerifyGetSumOfSquaredValues() {
    final var sut = mock(CorrelationCalculation.class);
    final var map = mock(Map.class);

    when(sut.getSumOfSquaredValues(any())).thenReturn(BigDecimalConstants.TWELVE);

    doCallRealMethod().when(sut).calculateDenominator(any(), any());
    sut.calculateDenominator(map, map);

    verify(sut, times(2)).getSumOfSquaredValues(map);
  }

  @Test
  void shouldGetSumOfSquaredValues_whenCheckResults() {
    final var sut = mock(CorrelationCalculation.class);
    final var date = LocalDate.now();
    final var map = Map.of(date, TWO, date.plusMonths(1), BigDecimalConstants.TWELVE);

    when(sut.getSumOfSquaredValues(any())).thenReturn(BigDecimalConstants.TWELVE);

    doCallRealMethod().when(sut).getSumOfSquaredValues(any());
    final BigDecimal sumOfSquaredValues = sut.getSumOfSquaredValues(map);

    assertEquals(toUserScale(BigDecimal.valueOf(148)), toUserScale(sumOfSquaredValues));
  }

  @Test
  void shouldBuildCorrelationPeriodResult_whenMappingCorrelationValues() {
    final var sut = mock(CorrelationCalculation.class);
    final var usEtfHolding = new EtfHolding().setTicker("TEST").setType(HoldingType.US_ETF);
    final var mutualFundsHolding = new FundSeriesHolding().setFundServCode("TEST").setType(
        HoldingType.CANADA_MUTUAL_FUNDS);
    final var map = Map.of(mutualFundsHolding, BigDecimalConstants.TWELVE);

    doCallRealMethod().when(sut).mapToCorrelationPeriodResult(any(), anyInt(), any());
    final CorrelationPeriodResult correlationPeriodDTO = sut.mapToCorrelationPeriodResult(usEtfHolding, TWELVE, map);

    assertEquals(String.valueOf(TWELVE), correlationPeriodDTO.getPeriod());
    assertEquals("US_ETF_TEST", correlationPeriodDTO.getKey());
    assertEquals(1, correlationPeriodDTO.getCorrelations().size());
    assertEquals("CANADA_MUTUAL_FUNDS_TEST", correlationPeriodDTO.getCorrelations().get(0).getCorrelationKey());
    assertEquals(BigDecimal.valueOf(TWELVE), correlationPeriodDTO.getCorrelations().get(0).getValue());
  }

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    final var calculationDTO = mock(CalculationDTO.class);
    final var map = Map.of(new EtfHolding().setTicker("TEST").setType(HoldingType.US_ETF), mock(Map.class));
    final var portfolioBaseTotalReturn = Map.of(mock(Holding.class), map);
    final var sut = mock(CorrelationCalculation.class, withSettings()
        .useConstructor(calculationDTO, portfolioBaseTotalReturn, Set.of()));

    final var listMock = List.of(new CorrelationPeriodResult());
    final var pairs = Set.of(Pair.of("2000-01-12", listMock), Pair.of("2020-01-05", listMock));
    when(sut.setPeriod(anyString(), anyList())).thenReturn(listMock);

    doCallRealMethod().when(sut).defineResponseType(anySet());
    final CorrelationResult correlationResDTO = sut.defineResponseType(pairs);

    assertEquals(2, correlationResDTO.getCorrelationPeriods().size());
    Assertions.assertEquals(listMock.get(0), correlationResDTO.getCorrelationPeriods().get(0));
    Assertions.assertEquals(listMock.get(0), correlationResDTO.getCorrelationPeriods().get(1));
    assertEquals(1, correlationResDTO.getHoldingsKey().size());
  }

  @Test
  void shouldReturnEmptyList_whenFormattingEmptyCorrelationPeriods() {
    final var sut = mock(CorrelationCalculation.class);

    final List<CorrelationPeriodResult> expected = List.of();

    doCallRealMethod().when(sut).toUserFormat(any());
    final var actual = sut.toUserFormat(List.of());

    ComparisonUtils.compareCollections(expected, actual);
  }

  @Test
  void shouldReturnNull_whenFormattingNullCorrelationPeriods() {
    final var sut = mock(CorrelationCalculation.class);

    doCallRealMethod().when(sut).toUserFormat(any());
    final var actual = sut.toUserFormat(null);

    assertNull(actual);
  }

  @Test
  void shouldKeepValuesUnchanged_whenFormattingAlreadyScaledCorrelationPeriods() {
    final var sut = mock(CorrelationCalculation.class);

    final var correlationPeriodDTO = new CorrelationPeriodResult();
    correlationPeriodDTO.setCorrelations(List.of());
    final var argument = List.of(correlationPeriodDTO);

    final var correlationPeriodDTOExpected = new CorrelationPeriodResult();
    correlationPeriodDTOExpected.setCorrelations(List.of());
    final var expected = List.of(correlationPeriodDTOExpected);

    doCallRealMethod().when(sut).toUserFormat(any());
    final var actual = sut.toUserFormat(argument);

    assertEquals(expected, actual);
  }

  @Test
  void shouldRoundCorrelationValues_whenFormattingCorrelationPeriods() {
    final var sut = mock(CorrelationCalculation.class);

    final var correlationPeriodDTO = new CorrelationPeriodResult();
    final var correlationKeyValueResult = new CorrelationKeyValueResult().setValue(new BigDecimal("0.123456789112345"));
    correlationPeriodDTO.setCorrelations(List.of(correlationKeyValueResult));
    final var argument = List.of(correlationPeriodDTO);

    final var correlationPeriodDTOExpected = new CorrelationPeriodResult();
    final var correlationKeyValueResultExpected = new CorrelationKeyValueResult().setValue(new BigDecimal("0.1234567891"));
    correlationPeriodDTOExpected.setCorrelations(List.of(correlationKeyValueResultExpected));
    final var expected = List.of(correlationPeriodDTOExpected);

    doCallRealMethod().when(sut).toUserFormat(any());
    final var actual = sut.toUserFormat(argument);

    assertEquals(expected, actual);
  }

  @Test
  void shouldSetPeriod_whenCheckResult() {
    final var sut = mock(CorrelationCalculation.class);

    final String period = "SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE";
    final var correlationPeriodDTO = new CorrelationPeriodResult("20", null, null);
    final var periods = List.of(correlationPeriodDTO);

    doCallRealMethod().when(sut).setPeriod(anyString(), anyList());
    final var actual = sut.setPeriod(period, periods);

    assertEquals(period, actual.get(0).getPeriod());
  }

}
