package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.domain.constant.BigDecimalConstants;
import com.fintex.ce.domain.model.result.SharpeRatioResult;
import com.fintex.ce.domain.model.result.core.TimeIntervalResult;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.domain.constant.BigDecimalConstants.ONE;
import static com.fintex.ce.domain.constant.BigDecimalConstants.TWO;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SharpeRatioCalculationTest {

  final int TWELVE = 12;

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenNumberOfMonthGreaterThanTBillsResultNull() {
    final var sut = mock(SharpeRatioCalculation.class);
    final var returns = mock(TreeMap.class);
    final var tBills = mock(TreeMap.class);
    sut.tBills = tBills;

    when(tBills.size()).thenReturn(20);
    when(returns.size()).thenReturn(100);
    when(sut.calculateSharpeRatio(any(), any(), any())).thenReturn(TEN);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt(), any());
    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(100, returns);

    assertNull(actual);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetPeriodStartDate() {
    final var sut = mock(SharpeRatioCalculation.class);
    final var returns = mock(TreeMap.class);
    final var tBills = mock(TreeMap.class);
    sut.tBills = tBills;

    when(tBills.size()).thenReturn(TWELVE);
    when(returns.size()).thenReturn(TWELVE);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt(), any());
    sut.calculatePeriodForNumberOfMonths(TWELVE, returns);

    verify(sut).getPeriodStartDate(TWELVE, returns);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculateAverageArithmeticAnnualizedReturn() {
    final var sut = mock(SharpeRatioCalculation.class);
    final var returns = mock(TreeMap.class);
    final var date = LocalDate.now();
    final var tBills = mock(TreeMap.class);
    sut.tBills = tBills;

    when(tBills.size()).thenReturn(TWELVE);
    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(returns.size()).thenReturn(TWELVE);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt(), any());
    sut.calculatePeriodForNumberOfMonths(TWELVE, returns);

    verify(sut).calculateAverageArithmeticAnnualizedReturn(returns, date, TWELVE);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculationOfAnnualizedRiskFreeRate() {
    final var sut = mock(SharpeRatioCalculation.class);
    final var returns = mock(TreeMap.class);
    final var date = LocalDate.now();
    final var restrictedTBills = mock(NavigableMap.class);
    final var tBills = mock(TreeMap.class);
    sut.tBills = tBills;

    when(tBills.size()).thenReturn(TWELVE);
    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(returns.size()).thenReturn(TWELVE);
    when(sut.restrictTBillsRange(any(), any())).thenReturn(restrictedTBills);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt(), any());
    sut.calculatePeriodForNumberOfMonths(TWELVE, returns);

    verify(sut).calculateAverageArithmeticAnnualizedReturn(restrictedTBills, date, TWELVE);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetStandardDeviation() {
    final var sut = mock(SharpeRatioCalculation.class);
    final var returns = mock(TreeMap.class);
    final var date = LocalDate.now();
    final var tBills = mock(TreeMap.class);
    sut.tBills = tBills;

    when(tBills.size()).thenReturn(TWELVE);
    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(returns.size()).thenReturn(TWELVE);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt(), any());
    sut.calculatePeriodForNumberOfMonths(TWELVE, returns);

    verify(sut).getStandardDeviation(TWELVE, returns);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculateSharpeRatio() {
    final var sut = mock(SharpeRatioCalculation.class);
    final var returns = mock(TreeMap.class);
    final var date = LocalDate.now();
    final var one = ONE;
    final var tBills = mock(TreeMap.class);
    sut.tBills = tBills;

    when(tBills.size()).thenReturn(TWELVE);
    when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(sut.calculateAverageArithmeticAnnualizedReturn(any(), any(), anyInt())).thenReturn(one);
    when(sut.getStandardDeviation(anyInt(), any())).thenReturn(one);
    when(returns.size()).thenReturn(TWELVE);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt(), any());
    sut.calculatePeriodForNumberOfMonths(TWELVE, returns);

    verify(sut).calculateSharpeRatio(one, one, one);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResultWhenPortfolioSizeIsLessThanTwelve() {
    final var sut = mock(SharpeRatioCalculation.class);
    final var treeMap = mock(TreeMap.class);

    when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal resultValue = sut.calculatePeriodForNumberOfMonths(ONE.intValue());

    assertNull(resultValue);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResult() {
    final var sut = mock(SharpeRatioCalculation.class);
    final var returns = mock(TreeMap.class);
    final var tBills = mock(TreeMap.class);
    sut.tBills = tBills;

    when(tBills.size()).thenReturn(TWELVE);
    when(returns.size()).thenReturn(TWELVE);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt(), any());
    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(ONE.intValue(), returns);

    assertNull(actual);
  }

  @Test
  void shouldCalculateSharpeRatio_whenCheckResult() {
    final var sut = mock(SharpeRatioCalculation.class);

    doCallRealMethod().when(sut).calculateSharpeRatio(any(), any(), any());

    final BigDecimal returnValue = sut.calculateSharpeRatio(TEN, TWO, TEN);

    assertEquals(toUserScale(valueOf(0.8)), toUserScale(returnValue));
  }

  @Test
  void shouldCalculateSharpeRatio_whenCheckResult2() {
    final var sut = mock(SharpeRatioCalculation.class);

    doCallRealMethod().when(sut).calculateSharpeRatio(any(), any(), any());

    final BigDecimal returnValue = sut.calculateSharpeRatio(TEN, TWO, ZERO);

    assertNull(returnValue);
  }

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    final var sut = mock(SharpeRatioCalculation.class);
    final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05",
        BigDecimal.ONE));
    final TimeIntervalResult intervalResDto = new TimeIntervalResult("2000-01-12", ZERO);
    final TimeIntervalResult intervalResDto1 = new TimeIntervalResult("2020-01-05", BigDecimal.ONE);
    final Set<TimeIntervalResult> expected = Set.of(intervalResDto, intervalResDto1);
    when(sut.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(sut).defineResponseType(anySet());
    final SharpeRatioResult sharpeRatioResDTO = sut.defineResponseType(pairs);

    assertEquals(expected, sharpeRatioResDTO.getSharpeRatio());
  }

  @Test
  void shouldGetStandardDeviation_whenVerifyCalculateExcessReturn() {
    try (var mockedPeriodCalculationAbstract = Mockito.mockStatic(PeriodCalculationAbstract.class)) {
      final var sut = mock(SharpeRatioCalculation.class);
      final NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>();
      returns.put(LocalDate.now().minusMonths(1), ONE);
      returns.put(LocalDate.now().minusMonths(2), TEN);
      returns.put(LocalDate.now().minusMonths(3), BigDecimalConstants.TWELVE);

      sut.tBills = new TreeMap<>();
      sut.standardDeviationCalculation = mock(StandardDeviationCalculation.class);

      when(sut.standardDeviationCalculation.calculatePeriodForNumberOfMonths(anyInt(), any())).thenReturn(ONE);

      doCallRealMethod().when(sut).getStandardDeviation(anyInt(), any());
      sut.getStandardDeviation(TWELVE, returns);

      mockedPeriodCalculationAbstract.verify(() -> PeriodCalculationAbstract.calculateExcessReturn(returns,
          sut.tBills));
    }
  }

  @Test
  void shouldGetStandardDeviation_whenVerifyPeriodForNumberOfMonths() {
    try (var mockedPeriodCalculationAbstract = Mockito.mockStatic(PeriodCalculationAbstract.class)) {
      final var sut = mock(SharpeRatioCalculation.class);
      final var periodCalculationAbstract = mock(PeriodCalculationAbstract.class);

      final NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>();
      returns.put(LocalDate.now().minusMonths(1), ONE);
      returns.put(LocalDate.now().minusMonths(2), TEN);
      returns.put(LocalDate.now().minusMonths(3), BigDecimalConstants.TWELVE);

      sut.tBills = new TreeMap<>();
      sut.standardDeviationCalculation = mock(StandardDeviationCalculation.class);

      mockedPeriodCalculationAbstract.when(() -> PeriodCalculationAbstract.calculateExcessReturn(any(), any()))
          .thenReturn((TreeMap) returns);
      when(sut.standardDeviationCalculation.calculatePeriodForNumberOfMonths(anyInt(), any())).thenReturn(ONE);

      doCallRealMethod().when(sut).getStandardDeviation(anyInt(), any());
      sut.getStandardDeviation(TWELVE, returns);

      verify(sut.standardDeviationCalculation).calculatePeriodForNumberOfMonths(eq(TWELVE), any());
    }
  }

}
