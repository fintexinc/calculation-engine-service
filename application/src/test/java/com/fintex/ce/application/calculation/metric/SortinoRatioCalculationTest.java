package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.SortinoRatioResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static com.fintex.ce.model.util.BigDecimalConstants.TWO;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class SortinoRatioCalculationTest {

  final int TWELVE = 12;

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenNumberOfMonthGreaterThanTBillsResultNull() {
    final var sut = mock(SortinoRatioCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var tBills = mock(TreeMap.class);
    sut.tBills = tBills;

    when(tBills.size()).thenReturn(20);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(100);
    when(sut.calculateSortinoRatio(any(), any(), any())).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(100);

    assertNull(actual);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetPeriodStartDate() {
    final var sut = mock(SortinoRatioCalculation.class);
    final var treeMap = mock(TreeMap.class);

    sut.tBills = treeMap;

    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sut).getPeriodStartDate(TWELVE, treeMap);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculateAverageArithmeticAnnualizedReturn() {
    final var sut = mock(SortinoRatioCalculation.class);
    final var downsideDeviationCalculation = mock(DownsideDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var date = LocalDate.now();

    downsideDeviationCalculation.tBills = new TreeMap();
    sut.tBills = treeMap;

    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);
    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sut, times(2)).calculateAverageArithmeticAnnualizedReturn(any(), eq(date), eq(TWELVE));
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetDownsideDeviation() {
    final var sortinoRatioCalculation = mock(SortinoRatioCalculation.class);
    final var downsideDeviationCalculation = mock(DownsideDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var date = LocalDate.now();

    downsideDeviationCalculation.tBills = new TreeMap();
    sortinoRatioCalculation.tBills = treeMap;

    when(sortinoRatioCalculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);
    when(sortinoRatioCalculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(sortinoRatioCalculation.calculateAverageArithmeticAnnualizedReturn(new TreeMap<>(), date, TWELVE)).thenReturn(
        BigDecimal.ONE);

    doCallRealMethod().when(sortinoRatioCalculation).calculatePeriodForNumberOfMonths(anyInt());
    sortinoRatioCalculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sortinoRatioCalculation).getDownsideDeviation(TWELVE);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculateSortinoRatio() {
    final var sortinoRatioCalculation = mock(SortinoRatioCalculation.class);
    final var downsideDeviationCalculation = mock(DownsideDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var date = LocalDate.now();

    downsideDeviationCalculation.tBills = new TreeMap();
    sortinoRatioCalculation.tBills = treeMap;

    when(sortinoRatioCalculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);
    when(sortinoRatioCalculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(sortinoRatioCalculation.calculateAverageArithmeticAnnualizedReturn(any(), any(), anyInt())).thenReturn(ONE);
    when(sortinoRatioCalculation.getDownsideDeviation(anyInt())).thenReturn(TEN);
    doCallRealMethod().when(sortinoRatioCalculation).calculatePeriodForNumberOfMonths(anyInt());

    sortinoRatioCalculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(sortinoRatioCalculation).calculateSortinoRatio(ONE, ONE, TEN);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResultWhenPortfolioSizeIsLessThanTwelve() {
    final var sortinoRatioCalculation = mock(SortinoRatioCalculation.class);
    final var treeMap = mock(TreeMap.class);

    sortinoRatioCalculation.tBills = treeMap;

    when(sortinoRatioCalculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    doCallRealMethod().when(sortinoRatioCalculation).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal resultValue = sortinoRatioCalculation.calculatePeriodForNumberOfMonths(ONE.intValue());

    assertNull(resultValue);
  }

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    final var sut = mock(SortinoRatioCalculation.class);
    final var pairs = Set.of(Pair.of("2015-01-01", ZERO), Pair.of("2018-02-02", ONE));

    final var intervalResDto = new TimeIntervalResult("2015-01-01", ZERO);
    final var intervalResDto1 = new TimeIntervalResult("2018-02-02", ONE);
    final var expected = Set.of(intervalResDto, intervalResDto1);

    when(sut.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(sut).defineResponseType(anySet());
    final SortinoRatioResult sortinoRatioResDTO = sut.defineResponseType(pairs);

    assertEquals(expected, sortinoRatioResDTO.getSortinoRatio());
  }

  @Test
  void shouldCalculateSortinoRatio_whenCheckResult() {
    final var sut = mock(SortinoRatioCalculation.class);

    doCallRealMethod().when(sut).calculateSortinoRatio(any(), any(), any());
    final BigDecimal returnValue = sut.calculateSortinoRatio(TEN, TWO, TWO);

    assertEquals(toUserScale(BigDecimal.valueOf(4)), toUserScale(returnValue));
  }

  @Test
  void shouldCalculateSortinoRatio_whenCheckResult2() {
    final var sut = mock(SortinoRatioCalculation.class);

    doCallRealMethod().when(sut).calculateSortinoRatio(any(), any(), any());
    final BigDecimal returnValue = sut.calculateSortinoRatio(TEN, ONE, TEN);

    assertEquals(toUserScale(BigDecimal.valueOf(0.9)), toUserScale(returnValue));
  }

  @Test
  void shouldCalculateSortinoRatio_whenCheckResult3() {
    final var sut = mock(SortinoRatioCalculation.class);

    doCallRealMethod().when(sut).calculateSortinoRatio(any(), any(), any());
    final BigDecimal returnValue = sut.calculateSortinoRatio(TEN, ONE, ZERO);

    assertNull(returnValue);
  }

  @Test
  void shouldGetDownsideDeviation_whenCheckResult() {
    final var downsideDeviationCalculation = mock(DownsideDeviationCalculation.class);
    final var calculationDTO = new CalculationDTO().setWeightedAveragePortfolioReturns(new TreeMap<>(Map.of(LocalDate
        .now(), TEN)));
    final var sut = mock(SortinoRatioCalculation.class, withSettings()
        .useConstructor(calculationDTO, Set.of(), new TreeMap<>(Map.of(LocalDate.now(), TEN)),
            downsideDeviationCalculation));

    when(downsideDeviationCalculation.calculatePeriodForNumberOfMonths(TEN.intValue())).thenReturn(BigDecimal.ONE);

    doCallRealMethod().when(sut).getDownsideDeviation(anyInt());
    final BigDecimal actual = sut.getDownsideDeviation(TEN.intValue());

    assertEquals(ONE, actual);
  }
}
