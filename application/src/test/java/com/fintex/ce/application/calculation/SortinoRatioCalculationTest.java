package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.DownsideDeviationCalculation;
import com.fintex.ce.application.calculation.SortinoRatioCalculation;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.application.result.SortinoRatioResult;
import com.fintex.ce.application.result.core.TimeIntervalResult;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.domain.constant.BigDecimalConstants.ONE;
import static com.fintex.ce.domain.constant.BigDecimalConstants.TWO;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class SortinoRatioCalculationTest {

  final int TWELVE = 12;

  @Test
  void calculatePeriodForNumberOfMonths_numberOfMonthGreaterThanTBillsResultNull() {
    // SETUP
    final var sut = mock(SortinoRatioCalculation.class);
    final var treeMap = mock(TreeMap.class);
    final var tBills = mock(TreeMap.class);
    sut.tBills = tBills;

    when(tBills.size()).thenReturn(20);
    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(100);
    when(sut.calculateSortinoRatio(any(), any(), any())).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(100);

    // VERIFY
    assertNull(actual);
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyGetPeriodStartDate() {
    // SETUP
    final var sut = mock(SortinoRatioCalculation.class);
    final var treeMap = mock(TreeMap.class);

    sut.tBills = treeMap;

    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    verify(sut).getPeriodStartDate(TWELVE, treeMap);
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyCalculateAverageArithmeticAnnualizedReturn() {
    // SETUP
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
    // ACT
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    verify(sut, times(2)).calculateAverageArithmeticAnnualizedReturn(any(), eq(date), eq(TWELVE));
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyGetDownsideDeviation() {
    // SETUP
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
    // ACT
    sortinoRatioCalculation.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    verify(sortinoRatioCalculation).getDownsideDeviation(TWELVE);
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyCalculateSortinoRatio() {
    // SETUP
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

    // ACT
    sortinoRatioCalculation.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    verify(sortinoRatioCalculation).calculateSortinoRatio(ONE, ONE, TEN);
  }

  @Test
  void calculatePeriodForNumberOfMonths_checkResultWhenPortfolioSizeIsLessThanTwelve() {
    // SETUP
    final var sortinoRatioCalculation = mock(SortinoRatioCalculation.class);
    final var treeMap = mock(TreeMap.class);

    sortinoRatioCalculation.tBills = treeMap;

    when(sortinoRatioCalculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    doCallRealMethod().when(sortinoRatioCalculation).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final BigDecimal resultValue = sortinoRatioCalculation.calculatePeriodForNumberOfMonths(ONE.intValue());

    // VERIFY
    assertNull(resultValue);
  }

  @Test
  void defineResponseType_checkResult() {
    // SETUP
    final var sut = mock(SortinoRatioCalculation.class);
    final var pairs = Set.of(Pair.of("2015-01-01", ZERO), Pair.of("2018-02-02", ONE));

    final var intervalResDto = new TimeIntervalResult("2015-01-01", ZERO);
    final var intervalResDto1 = new TimeIntervalResult("2018-02-02", ONE);
    final var expected = Set.of(intervalResDto, intervalResDto1);

    when(sut.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(sut).defineResponseType(anySet());
    // ACT
    final SortinoRatioResult sortinoRatioResDTO = sut.defineResponseType(pairs);

    // VERIFY
    assertEquals(expected, sortinoRatioResDTO.getSortinoRatio());
  }

  @Test
  void calculateSortinoRatio_checkResult() {
    // SETUP
    final var sut = mock(SortinoRatioCalculation.class);

    doCallRealMethod().when(sut).calculateSortinoRatio(any(), any(), any());
    // ACT
    final BigDecimal returnValue = sut.calculateSortinoRatio(TEN, TWO, TWO);

    // VERIFY
    assertEquals(toUserScale(BigDecimal.valueOf(4)), toUserScale(returnValue));
  }

  @Test
  void calculateSortinoRatio_checkResult2() {
    // SETUP
    final var sut = mock(SortinoRatioCalculation.class);

    doCallRealMethod().when(sut).calculateSortinoRatio(any(), any(), any());
    // ACT
    final BigDecimal returnValue = sut.calculateSortinoRatio(TEN, ONE, TEN);

    // VERIFY
    assertEquals(toUserScale(BigDecimal.valueOf(0.9)), toUserScale(returnValue));
  }

  @Test
  void calculateSortinoRatio_checkResult3() {
    // SETUP
    final var sut = mock(SortinoRatioCalculation.class);

    doCallRealMethod().when(sut).calculateSortinoRatio(any(), any(), any());
    // ACT
    final BigDecimal returnValue = sut.calculateSortinoRatio(TEN, ONE, ZERO);

    // VERIFY
    assertNull(returnValue);
  }

  @Test
  void getDownsideDeviation_checkResult() {
    // SETUP
    final var downsideDeviationCalculation = mock(DownsideDeviationCalculation.class);
    final var calculationDTO = new CalculationDTO().setWeightedAveragePortfolioReturns(new TreeMap<>(Map.of(LocalDate
        .now(), TEN)));
    final var sut = mock(SortinoRatioCalculation.class, withSettings()
        .useConstructor(calculationDTO, Set.of(), new TreeMap<>(Map.of(LocalDate.now(), TEN)),
            downsideDeviationCalculation));

    when(downsideDeviationCalculation.calculatePeriodForNumberOfMonths(TEN.intValue())).thenReturn(BigDecimal.ONE);

    doCallRealMethod().when(sut).getDownsideDeviation(anyInt());
    // ACT
    final BigDecimal actual = sut.getDownsideDeviation(TEN.intValue());

    // VERIFY
    assertEquals(ONE, actual);
  }
}