package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.BetaCalculation;
import com.fintex.ce.application.calculation.TreynorRatioCalculation;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.application.result.TreynorRatioResult;
import com.fintex.ce.application.result.core.TimeIntervalResult;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.domain.constant.BigDecimalConstants.ONE;
import static com.fintex.ce.domain.constant.BigDecimalConstants.TWO;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class TreynorRatioCalculationTest {

  final int TWELVE = 12;

  @Test
  void calculatePeriodForNumberOfMonths_verifyGetPeriodStartDate() {
    // SETUP
    final var beta = mock(BetaCalculation.class);
    final var tBills = mock(TreeMap.class);
    final var sut = mock(TreynorRatioCalculation.class,
        withSettings().useConstructor(mock(CalculationDTO.class), mock(Set.class), tBills, beta));

    when(sut.getPortfolioTotalReturns()).thenReturn(tBills);
    when(tBills.size()).thenReturn(TWELVE);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    verify(sut).getPeriodStartDate(TWELVE, tBills);
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyCalculateAverageArithmeticAnnualizedReturn() {
    // SETUP
    final var tBills = mock(TreeMap.class);
    final var beta = mock(BetaCalculation.class);
    final var sut = mock(TreynorRatioCalculation.class,
        withSettings().useConstructor(mock(CalculationDTO.class), mock(Set.class), tBills, beta));
    final var treeMap = mock(TreeMap.class);
    final var date = LocalDate.now();

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
  void calculatePeriodForNumberOfMonths_verifyBetaCalculatePerioForNumberOfMonths() {
    // SETUP
    final var tBills = mock(TreeMap.class);
    final var beta = mock(BetaCalculation.class);
    final var sut = mock(TreynorRatioCalculation.class,
        withSettings().useConstructor(mock(CalculationDTO.class), mock(Set.class), tBills, beta));
    final var treeMap = mock(TreeMap.class);
    final var date = LocalDate.now();

    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);
    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    verify(beta).calculatePeriodForNumberOfMonths(TWELVE);
  }

  @Test
  void calculatePeriodForNumberOfMonths_returnNullWhenBetaReturnNull() {
    // SETUP
    final var tBills = mock(TreeMap.class);
    final var beta = mock(BetaCalculation.class);
    final var sut = mock(TreynorRatioCalculation.class,
        withSettings().useConstructor(mock(CalculationDTO.class), mock(Set.class), tBills, beta));
    final var treeMap = mock(TreeMap.class);
    final var date = LocalDate.now();

    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);
    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(beta.calculatePeriodForNumberOfMonths(TWELVE)).thenReturn(null);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final var actual = sut.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    assertNull(actual);
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyCalculateTreynorRatio() {
    // SETUP
    final var tBills = mock(TreeMap.class);
    final var beta = mock(BetaCalculation.class);
    final var sut = mock(TreynorRatioCalculation.class,
        withSettings().useConstructor(mock(CalculationDTO.class), mock(Set.class), tBills, beta));
    final var treeMap = mock(TreeMap.class);
    final var date = LocalDate.now();

    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);
    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(sut.calculateAverageArithmeticAnnualizedReturn(any(), any(), anyInt())).thenReturn(TEN);
    when(beta.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(ONE);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    sut.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    verify(sut).calculateTreynorRatio(TEN, TEN, ONE);
  }

  @Test
  void calculatePeriodForNumberOfMonths_checkResult() {
    // SETUP
    final var sut = mock(TreynorRatioCalculation.class);
    final var treeMap = mock(TreeMap.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(10);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(TWELVE);

    // VERIFY
    assertNull(actual);
  }

  @Test
  void defineResponseType_checkResult() {
    // SETUP
    final var sut = mock(TreynorRatioCalculation.class);
    final var result = Set.of(Pair.of("2015-01-01", ZERO), Pair.of("2018-02-02", ONE));

    final var intervalResDto = new TimeIntervalResult("2015-01-01", ZERO);
    final var intervalResDto1 = new TimeIntervalResult("2018-02-02", ONE);
    final var expected = Set.of(intervalResDto, intervalResDto1);

    when(sut.formTimeIntervalResult(anySet())).thenReturn(expected);
    doCallRealMethod().when(sut).defineResponseType(anySet());

    // ACT
    final TreynorRatioResult sortinoRatioResDTO = sut.defineResponseType(result);

    // VERIFY
    assertEquals(expected, sortinoRatioResDTO.getTreynorRatio());
  }

  @Test
  void calculateTreynorRatio_checkResult() {
    // SETUP
    final var sut = mock(TreynorRatioCalculation.class);

    doCallRealMethod().when(sut).calculateTreynorRatio(any(), any(), any());
    // ACT
    final BigDecimal returnValue = sut.calculateTreynorRatio(TEN, TWO, TEN);

    // VERIFY
    assertEquals(toUserScale(BigDecimal.valueOf(0.8)), toUserScale(returnValue));
  }

}