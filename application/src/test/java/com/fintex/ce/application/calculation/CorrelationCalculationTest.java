package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.CorrelationCalculation;
import com.fintex.ce.domain.constant.BigDecimalConstants;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.result.CorrelationResult;
import com.fintex.ce.port.input.result.correlation.CorrelationKeyValueResult;
import com.fintex.ce.port.input.result.correlation.CorrelationPeriodResult;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
  void calculatePeriodForNumberOfMonths_verifyCalculatePortfolioBaseTotalReturnValuesByPeriod_whenHoldingHasEnoughReturns() {
    // SETUP
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

    // ACT
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    verify(sut).calculatePortfolioBaseTotalReturnValuesByPeriod(TWELVE, map);
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyCalculatePortfolioBaseTotalReturnValuesByPeriod_whenHoldingHasNoTEnoughReturns() {
    // SETUP
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

    // ACT
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    verify(sut, times(0)).calculatePortfolioBaseTotalReturnValuesByPeriod(TWELVE, map);
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyGetCorrelationPeriodResult_whenPortfolioReturnHasEnoughReturns() {
    // SETUP
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

    // ACT
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    verify(sut).getCorrelationPeriod(any(), any(), eq(TWELVE));
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyGetCorrelationPeriodResult_whenPortfolioReturnHasNotEnoughReturns() {
    // SETUP
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

    // ACT
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    verify(sut, times(0)).getCorrelationPeriod(any(), any(), eq(TWELVE));
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyHasEnoughReturns() {
    // SETUP
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
    // ACT
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    verify(sut).hasEnoughReturns(eq(TWELVE),
        argThat(entry -> entry.getKey().equals(holding) && entry.getValue().equals(map)));
  }

  @Test
  void calculatePeriodForNumberOfMonths_checkResult() {
    // SETUP
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
    // ACT
    final List<CorrelationPeriodResult> correlationPeriodDTOS = sut.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    assertEquals(1, correlationPeriodDTOS.size());
    assertEquals(correlationPeriodDTO, correlationPeriodDTOS.get(0));
  }

  @Test
  void calculatePeriodForNumberOfMonths_checkResultWhenHoldingHasNotEnoughReturns() {
    // SETUP
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

    // ACT
    final List<CorrelationPeriodResult> correlationPeriodDTOS = sut.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    assertTrue(correlationPeriodDTOS.isEmpty());
  }

  @Test
  void calculatePortfolioBaseTotalReturnValuesByPeriod_verifyGetPeriodStartDate() {
    // SETUP
    final var sut = mock(CorrelationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var map = mock(Map.class);

    when(treeMap.size()).thenReturn(BigDecimal.ONE.intValue());
    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);

    doCallRealMethod().when(sut).calculatePortfolioBaseTotalReturnValuesByPeriod(anyInt(), anyMap());
    // ACT
    sut.calculatePortfolioBaseTotalReturnValuesByPeriod(TWELVE, map);

    // VERIFY
    verify(sut).getPeriodStartDate(TWELVE, new TreeMap<>(map));
  }

  @Test
  void calculatePortfolioBaseTotalReturnValuesByPeriod_verifyGetSubMapByPeriodStartDate() {
    // SETUP
    final var sut = mock(CorrelationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var map = mock(Map.class);
    final var date = LocalDate.now();

    when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(BigDecimal.ONE.intValue());
    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);

    doCallRealMethod().when(sut).calculatePortfolioBaseTotalReturnValuesByPeriod(anyInt(), anyMap());
    // ACT
    sut.calculatePortfolioBaseTotalReturnValuesByPeriod(TWELVE, map);

    // VERIFY
    verify(sut).getSubMapByPeriodStartDate(date, new TreeMap<>(map));
  }

  @Test
  void calculatePortfolioBaseTotalReturnValuesByPeriod_checkResult() {
    // SETUP
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
    // ACT
    final TreeMap<LocalDate, BigDecimal> result = new TreeMap<>(sut.calculatePortfolioBaseTotalReturnValuesByPeriod(
        TWELVE, map));

    // VERIFY
    assertEquals(2, result.size());
    assertEquals(toUserScale(BigDecimal.valueOf(5)), toUserScale(result.firstEntry().getValue()));
    assertEquals(toUserScale(BigDecimal.valueOf(-5)), toUserScale(result.lastEntry().getValue()));
  }

  @Test
  void getCorrelationPeriod_verifyCalculateCorrelation() {
    // SETUP
    final var sut = mock(CorrelationCalculation.class);
    final var date = LocalDate.now();
    final var map = Map.of(date, ONE);
    final var usEtfHolding = new Holding().setType(HoldingType.US_ETF);
    final var mutualFundsHolding = new Holding().setType(HoldingType.CANADA_MUTUAL_FUNDS);
    final var holdings = Map.of(usEtfHolding, map, mutualFundsHolding, map);

    when(sut.calculateCorrelation(any(), any())).thenReturn(BigDecimal.ONE);

    doCallRealMethod().when(sut).getCorrelationPeriod(any(), any(), anyInt());
    // ACT
    sut.getCorrelationPeriod(mutualFundsHolding, holdings, TWELVE);

    // VERIFY
    verify(sut).calculateCorrelation(map, map);
  }

  @Test
  void getCorrelationPeriod_verifyMapToCorrelationPeriodResult() {
    // SETUP
    final var sut = mock(CorrelationCalculation.class);
    final var date = LocalDate.now();
    final var map = Map.of(date, ONE);
    final var usEtfHolding = new Holding().setType(HoldingType.US_ETF);
    final var mutualFundsHolding = new Holding().setType(HoldingType.CANADA_MUTUAL_FUNDS);
    final var holdings = Map.of(usEtfHolding, map, mutualFundsHolding, map);

    when(sut.calculateCorrelation(any(), any())).thenReturn(BigDecimal.ONE);

    doCallRealMethod().when(sut).getCorrelationPeriod(any(), any(), anyInt());
    // ACT
    sut.getCorrelationPeriod(mutualFundsHolding, holdings, TWELVE);

    // VERIFY
    verify(sut).mapToCorrelationPeriodResult(eq(mutualFundsHolding), eq(TWELVE), any());
  }

  @Test
  void calculateCorrelation_verifyCalculateNumerator() {
    // SETUP
    final var sut = mock(CorrelationCalculation.class);
    final var map = mock(Map.class);

    when(sut.calculateNumerator(any(), any())).thenReturn(BigDecimal.ONE);
    when(sut.calculateDenominator(any(), any())).thenReturn(BigDecimal.ONE);
    when(sut.calculateCorrelation(any(), any())).thenReturn(BigDecimal.ONE);
    doCallRealMethod().when(sut).calculateCorrelation(any(), any());

    // ACT
    sut.calculateCorrelation(map, map);

    // VERIFY
    verify(sut).calculateNumerator(map, map);
  }

  @Test
  void calculateCorrelation_verifyCalculateDenominator() {
    // SETUP
    final var sut = mock(CorrelationCalculation.class);
    final var map = mock(Map.class);

    when(sut.calculateNumerator(any(), any())).thenReturn(BigDecimal.ONE);
    when(sut.calculateDenominator(any(), any())).thenReturn(BigDecimal.ONE);
    when(sut.calculateCorrelation(any(), any())).thenReturn(BigDecimal.ONE);

    doCallRealMethod().when(sut).calculateCorrelation(any(), any());
    // ACT
    sut.calculateCorrelation(map, map);

    // VERIFY
    verify(sut).calculateDenominator(map, map);
  }

  @Test
  void calculateCorrelation_checkResult() {
    // SETUP
    final var sut = mock(CorrelationCalculation.class);
    final var map = mock(Map.class);

    when(sut.calculateNumerator(any(), any())).thenReturn(BigDecimalConstants.TWELVE);
    when(sut.calculateDenominator(any(), any())).thenReturn(TWO);
    when(sut.calculateCorrelation(any(), any())).thenReturn(BigDecimal.ONE);
    doCallRealMethod().when(sut).calculateCorrelation(any(), any());

    // ACT
    final BigDecimal result = sut.calculateCorrelation(map, map);

    // VERIFY
    assertEquals(toUserScale(BigDecimal.valueOf(6)), toUserScale(result));
  }

  @Test
  void calculateNumerator_checkResult() {
    // SETUP
    final var sut = mock(CorrelationCalculation.class);
    final var date = LocalDate.now();
    final var map1 = Map.of(date, TWO, date.plusMonths(1), BigDecimalConstants.TWELVE);
    final var map2 = Map.of(date, ONE, date.plusMonths(1), BigDecimalConstants.TWELVE);

    when(sut.calculateNumerator(any(), any())).thenReturn(BigDecimalConstants.TWELVE);
    when(sut.calculateDenominator(any(), any())).thenReturn(TWO);
    when(sut.calculateCorrelation(any(), any())).thenReturn(BigDecimal.ONE);
    doCallRealMethod().when(sut).calculateNumerator(any(), any());

    // ACT
    final BigDecimal result = sut.calculateNumerator(map1, map2);

    // VERIFY
    assertEquals(toUserScale(BigDecimal.valueOf(146)), toUserScale(result));
  }

  @Test
  void calculateDenominator_checkResult() {
    // SETUP
    final var sut = mock(CorrelationCalculation.class);
    final var date = LocalDate.now();
    final var map1 = Map.of(date, TWO, date.plusMonths(1), BigDecimalConstants.TWELVE);
    final var map2 = Map.of(date, ONE, date.plusMonths(1), BigDecimalConstants.TWELVE);

    when(sut.getSumOfSquaredValues(any())).thenReturn(BigDecimalConstants.TWELVE);

    doCallRealMethod().when(sut).calculateDenominator(any(), any());
    // ACT
    final BigDecimal result = sut.calculateDenominator(map1, map2);

    // VERIFY
    assertEquals(toUserScale(BigDecimal.valueOf(12)), toUserScale(result));
  }

  @Test
  void calculateDenominator_verifyGetSumOfSquaredValues() {
    // SETUP
    final var sut = mock(CorrelationCalculation.class);
    final var map = mock(Map.class);

    when(sut.getSumOfSquaredValues(any())).thenReturn(BigDecimalConstants.TWELVE);

    doCallRealMethod().when(sut).calculateDenominator(any(), any());
    // ACT
    sut.calculateDenominator(map, map);

    // VERIFY
    verify(sut, times(2)).getSumOfSquaredValues(map);
  }

  @Test
  void getSumOfSquaredValues_checkResults() {
    // SETUP
    final var sut = mock(CorrelationCalculation.class);
    final var date = LocalDate.now();
    final var map = Map.of(date, TWO, date.plusMonths(1), BigDecimalConstants.TWELVE);

    when(sut.getSumOfSquaredValues(any())).thenReturn(BigDecimalConstants.TWELVE);

    doCallRealMethod().when(sut).getSumOfSquaredValues(any());
    // ACT
    final BigDecimal sumOfSquaredValues = sut.getSumOfSquaredValues(map);

    // VERIFY
    assertEquals(toUserScale(BigDecimal.valueOf(148)), toUserScale(sumOfSquaredValues));
  }

  @Test
  void mapToCorrelationPeriodResult_checkResult() {
    // SETUP
    final var sut = mock(CorrelationCalculation.class);
    final var usEtfHolding = new EtfHolding().setTicker("TEST").setType(HoldingType.US_ETF);
    final var mutualFundsHolding = new FundSeriesHolding().setFundServCode("TEST").setType(
        HoldingType.CANADA_MUTUAL_FUNDS);
    final var map = Map.of(mutualFundsHolding, BigDecimalConstants.TWELVE);

    doCallRealMethod().when(sut).mapToCorrelationPeriodResult(any(), anyInt(), any());
    // ACT
    final CorrelationPeriodResult correlationPeriodDTO = sut.mapToCorrelationPeriodResult(usEtfHolding, TWELVE, map);

    // VERIFY
    assertNotNull(correlationPeriodDTO);
    assertEquals(String.valueOf(TWELVE), correlationPeriodDTO.getPeriod());
    assertEquals("US_ETF_TEST", correlationPeriodDTO.getKey());
    assertEquals(1, correlationPeriodDTO.getCorrelations().size());
    assertEquals("CANADA_MUTUAL_FUNDS_TEST", correlationPeriodDTO.getCorrelations().get(0).getCorrelationKey());
    assertEquals(BigDecimal.valueOf(TWELVE), correlationPeriodDTO.getCorrelations().get(0).getValue());
  }

  @Test
  void defineResponseType_checkResult() {
    // SETUP
    final var calculationDTO = mock(CalculationDTO.class);
    final var map = Map.of(new EtfHolding().setTicker("TEST").setType(HoldingType.US_ETF), mock(Map.class));
    final var portfolioBaseTotalReturn = Map.of(mock(Holding.class), map);
    final var sut = mock(CorrelationCalculation.class, withSettings()
        .useConstructor(calculationDTO, portfolioBaseTotalReturn, Set.of()));

    final var listMock = List.of(new CorrelationPeriodResult());
    final var pairs = Set.of(Pair.of("2000-01-12", listMock), Pair.of("2020-01-05", listMock));
    when(sut.setPeriod(anyString(), anyList())).thenReturn(listMock);

    doCallRealMethod().when(sut).defineResponseType(anySet());
    // ACT
    final CorrelationResult correlationResDTO = sut.defineResponseType(pairs);

    // VERIFY
    assertEquals(2, correlationResDTO.getCorrelationPeriods().size());
    Assertions.assertEquals(listMock.get(0), correlationResDTO.getCorrelationPeriods().get(0));
    Assertions.assertEquals(listMock.get(0), correlationResDTO.getCorrelationPeriods().get(1));
    assertEquals(1, correlationResDTO.getHoldingsKey().size());
  }

  @Test
  void toUserFormat_checkResult() {
    // SETUP
    final var sut = mock(CorrelationCalculation.class);

    final List<CorrelationPeriodResult> expected = List.of();

    doCallRealMethod().when(sut).toUserFormat(any());
    // ACT
    final var actual = sut.toUserFormat(List.of());

    // VERIFY
    assertNotNull(actual);
    ComparisonUtils.compareCollections(expected, actual);
  }

  @Test
  void toUserFormat_checkResult1() {
    // SETUP
    final var sut = mock(CorrelationCalculation.class);

    final List<CorrelationPeriodResult> expected = null;

    doCallRealMethod().when(sut).toUserFormat(any());
    // ACT
    final var actual = sut.toUserFormat(null);

    // VERIFY
    assertNull(actual);
  }

  @Test
  void toUserFormat_checkResult2() {
    // SETUP
    final var sut = mock(CorrelationCalculation.class);

    final var correlationPeriodDTO = new CorrelationPeriodResult();
    correlationPeriodDTO.setCorrelations(List.of());
    final var argument = List.of(correlationPeriodDTO);

    final var correlationPeriodDTOExpected = new CorrelationPeriodResult();
    correlationPeriodDTOExpected.setCorrelations(List.of());
    final var expected = List.of(correlationPeriodDTOExpected);

    doCallRealMethod().when(sut).toUserFormat(any());
    // ACT
    final var actual = sut.toUserFormat(argument);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void toUserFormat_checkResult3() {
    // SETUP
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
    // ACT
    final var actual = sut.toUserFormat(argument);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void setPeriod_checkResult() {
    // SETUP
    final var sut = mock(CorrelationCalculation.class);

    final String period = "SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE";
    final var correlationPeriodDTO = new CorrelationPeriodResult("20", null, null);
    final var periods = List.of(correlationPeriodDTO);

    doCallRealMethod().when(sut).setPeriod(anyString(), anyList());
    // ACT
    final var actual = sut.setPeriod(period, periods);

    // VERIFY
    assertEquals(period, actual.get(0).getPeriod());
  }

}
