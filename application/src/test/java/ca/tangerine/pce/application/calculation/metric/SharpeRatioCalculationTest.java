package ca.tangerine.pce.application.calculation.metric;

import ca.tangerine.pce.application.calculation.metric.core.PeriodCalculationAbstract;
import ca.tangerine.pce.model.domain.calculation.input.PeriodCalculationInput;
import ca.tangerine.pce.model.domain.result.TimeIntervalResult;
import ca.tangerine.pce.model.domain.result.risk.SharpeRatioResult;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.exceptions.CalculationException;
import ca.tangerine.pce.model.util.BigDecimalConstants;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static ca.tangerine.pce.application.util.DecimalUtils.toUserScale;
import static ca.tangerine.pce.model.util.BigDecimalConstants.ONE;
import static ca.tangerine.pce.model.util.BigDecimalConstants.TWO;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.FIVE_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.TEN_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.TWENTY_YR;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SharpeRatioCalculationTest {

  int TWELVE = 12;

  @Test
  void shouldThrowMissingTBillRate_whenTBillWindowHasGap() {
    NavigableMap<LocalDate, BigDecimal> portfolioReturns = new TreeMap<>();
    NavigableMap<LocalDate, BigDecimal> tBills = new TreeMap<>();
    for (int i = 0; i < 12; i++) {
      LocalDate month = LocalDate.of(2025, 1, 31).plusMonths(i);
      portfolioReturns.put(month, ONE);
      if (i != 4) {
        tBills.put(month, valueOf(0.001));
      }
    }
    PeriodCalculationInput input = new PeriodCalculationInput();
    input.setWeightedAveragePortfolioReturns(portfolioReturns);
    var calculation = new SharpeRatioCalculation(input, Set.of(), tBills,
        new StandardDeviationCalculation<>(input, Set.of()));

    CalculationException ex = assertThrows(CalculationException.class,
        () -> calculation.calculatePeriodForNumberOfMonths(12));
    assertEquals(ErrorCode.MISSING_TBILL_RATE, ex.getErrorCode());
    assertEquals("Missing T-Bill rate for date " + LocalDate.of(2025, 1, 31).plusMonths(4), ex.getMessage());
    assertEquals(Map.of("param-1", LocalDate.of(2025, 1, 31).plusMonths(4)), ex.getMetadata());
  }

  /**
   * Regression: restricting T-Bills to the portfolio range at construction must not call {@code firstKey()/lastKey()}
   * on an empty portfolio series (which throws {@link java.util.NoSuchElementException} -> HTTP 500). An empty return
   * series must degrade to the insufficient-data {@code null} path, not fail during object construction.
   */
  @Test
  void shouldReturnNull_whenConstructedWithEmptyPortfolioReturns() {
    PeriodCalculationInput input = new PeriodCalculationInput();
    input.setWeightedAveragePortfolioReturns(new TreeMap<>());
    NavigableMap<LocalDate, BigDecimal> tBills = new TreeMap<>();
    tBills.put(LocalDate.of(2025, 1, 31), valueOf(0.001));

    var calculation = new SharpeRatioCalculation(input, Set.of(), tBills,
        new StandardDeviationCalculation<>(input, Set.of()));

    assertNull(calculation.calculatePeriodForNumberOfMonths(12));
  }

  /**
   * Regression test for a bug where {@code calculatePeriodForNumberOfMonths(numberOfMonths, returns)} annualized the
   * risk-free rate off the constructor-restricted {@code tBills} field (bounded to the full portfolio range) instead of
   * bounding it to the passed rolling {@code returns} window. For a historical window (one that ends before the
   * portfolio's last date), that let the risk-free sum spill past the window end while still dividing by
   * {@code numberOfMonths}, overstating the annualized risk-free rate.
   */
  @Test
  void shouldUseWindowBoundedRiskFreeRate_whenComputingHistoricalWindow() {
    NavigableMap<LocalDate, BigDecimal> portfolioReturns = new TreeMap<>();
    NavigableMap<LocalDate, BigDecimal> tBillsFull = new TreeMap<>();
    LocalDate start = LocalDate.of(2023, 1, 31);
    for (int i = 0; i < 24; i++) {
      LocalDate month = start.plusMonths(i);
      portfolioReturns.put(month, valueOf(1.01 + (i % 2 == 0 ? 0.002 : -0.001)));
      tBillsFull.put(month, valueOf(0.001));
    }

    NavigableMap<LocalDate, BigDecimal> tBillsWindowOnly = new TreeMap<>();
    NavigableMap<LocalDate, BigDecimal> windowReturns = new TreeMap<>();
    for (int i = 0; i < 12; i++) {
      LocalDate month = start.plusMonths(i);
      tBillsWindowOnly.put(month, valueOf(0.001));
      windowReturns.put(month, portfolioReturns.get(month));
    }
    assertTrue(windowReturns.lastKey().isBefore(portfolioReturns.lastKey()));

    PeriodCalculationInput input = PeriodCalculationInput.builder()
        .weightedAveragePortfolioReturns(portfolioReturns)
        .build();
    var calculationWithFullTBills = new SharpeRatioCalculation(input, Set.of(), tBillsFull,
        new StandardDeviationCalculation<>(input, Set.of()));
    var calculationWithWindowTBills = new SharpeRatioCalculation(input, Set.of(), tBillsWindowOnly,
        new StandardDeviationCalculation<>(input, Set.of()));

    BigDecimal resultWithFullTBills = calculationWithFullTBills.calculatePeriodForNumberOfMonths(TWELVE,
        windowReturns);
    BigDecimal resultWithWindowTBills = calculationWithWindowTBills.calculatePeriodForNumberOfMonths(TWELVE,
        windowReturns);

    assertNotNull(resultWithFullTBills);
    assertNotNull(resultWithWindowTBills);
    assertEquals(0, resultWithFullTBills.compareTo(resultWithWindowTBills));
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetPeriodStartDate() {
    var calculation = mock(SharpeRatioCalculation.class);
    var returns = mock(TreeMap.class);

    when(returns.size()).thenReturn(TWELVE);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(new TreeMap<>());

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt(), any());
    calculation.calculatePeriodForNumberOfMonths(TWELVE, returns);

    verify(calculation).getPeriodStartDate(TWELVE, returns);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenWindowCoveredComputesWithoutThrowing() {
    var calculation = mock(SharpeRatioCalculation.class);
    var returns = mock(TreeMap.class);
    var date = LocalDate.now();

    when(returns.size()).thenReturn(TWELVE);
    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(new TreeMap<>());
    when(calculation.calculateAverageArithmeticAnnualizedReturn(any(), any(), anyInt())).thenReturn(ONE);
    when(calculation.getStandardDeviation(anyInt(), any())).thenReturn(ONE);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt(), any());
    calculation.calculatePeriodForNumberOfMonths(TWELVE, returns);

    verify(calculation).calculateSharpeRatio(ONE, ONE, ONE);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculateAverageArithmeticAnnualizedReturn() {
    var calculation = mock(SharpeRatioCalculation.class);
    var returns = mock(TreeMap.class);
    var date = LocalDate.now();

    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(returns.size()).thenReturn(TWELVE);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(new TreeMap<>());

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt(), any());
    calculation.calculatePeriodForNumberOfMonths(TWELVE, returns);

    verify(calculation).calculateAverageArithmeticAnnualizedReturn(returns, date, TWELVE);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculationOfAnnualizedRiskFreeRate() {
    var calculation = mock(SharpeRatioCalculation.class);
    var returns = mock(TreeMap.class);
    var date = LocalDate.now();
    var restrictedTBills = mock(NavigableMap.class);

    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(returns.size()).thenReturn(TWELVE);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(new TreeMap<>());
    when(calculation.restrictTBillsRange(any(), any())).thenReturn(restrictedTBills);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt(), any());
    calculation.calculatePeriodForNumberOfMonths(TWELVE, returns);

    verify(calculation).calculateAverageArithmeticAnnualizedReturn(restrictedTBills, date, TWELVE);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetStandardDeviation() {
    var calculation = mock(SharpeRatioCalculation.class);
    var returns = mock(TreeMap.class);
    var date = LocalDate.now();

    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(returns.size()).thenReturn(TWELVE);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(new TreeMap<>());

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt(), any());
    calculation.calculatePeriodForNumberOfMonths(TWELVE, returns);

    verify(calculation).getStandardDeviation(TWELVE, returns);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculateSharpeRatio() {
    var calculation = mock(SharpeRatioCalculation.class);
    var returns = mock(TreeMap.class);
    var date = LocalDate.now();
    var one = ONE;

    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(date);
    when(calculation.calculateAverageArithmeticAnnualizedReturn(any(), any(), anyInt())).thenReturn(one);
    when(calculation.getStandardDeviation(anyInt(), any())).thenReturn(one);
    when(returns.size()).thenReturn(TWELVE);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(new TreeMap<>());

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt(), any());
    calculation.calculatePeriodForNumberOfMonths(TWELVE, returns);

    verify(calculation).calculateSharpeRatio(one, one, one);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResultWhenPortfolioSizeIsLessThanTwelve() {
    var calculation = mock(SharpeRatioCalculation.class);
    var treeMap = mock(TreeMap.class);

    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    BigDecimal resultValue = calculation.calculatePeriodForNumberOfMonths(ONE.intValue());

    assertNull(resultValue);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResult() {
    var calculation = mock(SharpeRatioCalculation.class);
    var returns = mock(TreeMap.class);

    when(returns.size()).thenReturn(TWELVE);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt(), any());
    BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(ONE.intValue(), returns);

    assertNull(actual);
  }

  @Test
  void shouldCalculateSharpeRatio_whenCheckResult() {
    var calculation = mock(SharpeRatioCalculation.class);

    doCallRealMethod().when(calculation).calculateSharpeRatio(any(), any(), any());

    BigDecimal returnValue = calculation.calculateSharpeRatio(TEN, TWO, TEN);

    assertEquals(toUserScale(valueOf(0.8)), toUserScale(returnValue));
  }

  @Test
  void shouldCalculateSharpeRatio_whenCheckResult2() {
    var calculation = mock(SharpeRatioCalculation.class);

    doCallRealMethod().when(calculation).calculateSharpeRatio(any(), any(), any());

    BigDecimal returnValue = calculation.calculateSharpeRatio(TEN, TWO, ZERO);

    assertNull(returnValue);
  }

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    var calculation = mock(SharpeRatioCalculation.class);
    Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05",
        BigDecimal.ONE));
    TimeIntervalResult interval1 = new TimeIntervalResult("2000-01-12", ZERO);
    TimeIntervalResult interval2 = new TimeIntervalResult("2020-01-05", BigDecimal.ONE);
    Set<TimeIntervalResult> expected = Set.of(interval1, interval2);
    when(calculation.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(calculation).defineResponseType(anySet());
    SharpeRatioResult result = calculation.defineResponseType(pairs);

    assertEquals(expected, result.getSharpeRatio());
  }

  @Test
  void shouldGetStandardDeviation_whenVerifyCalculateExcessReturn() {
    try (var mockedPeriodCalculationAbstract = Mockito.mockStatic(PeriodCalculationAbstract.class)) {
      var calculation = mock(SharpeRatioCalculation.class);
      NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>();
      returns.put(LocalDate.now().minusMonths(1), ONE);
      returns.put(LocalDate.now().minusMonths(2), TEN);
      returns.put(LocalDate.now().minusMonths(3), BigDecimalConstants.TWELVE);

      calculation.tBills = new TreeMap<>();
      calculation.standardDeviationCalculation = mock(StandardDeviationCalculation.class);

      when(calculation.standardDeviationCalculation.calculatePeriodForNumberOfMonths(anyInt(), any())).thenReturn(ONE);

      doCallRealMethod().when(calculation).getStandardDeviation(anyInt(), any());
      calculation.getStandardDeviation(TWELVE, returns);

      mockedPeriodCalculationAbstract.verify(() -> PeriodCalculationAbstract.calculateExcessReturn(returns,
          calculation.tBills));
    }
  }

  @Test
  void shouldGetStandardDeviation_whenVerifyPeriodForNumberOfMonths() {
    try (var mockedPeriodCalculationAbstract = Mockito.mockStatic(PeriodCalculationAbstract.class)) {
      var calculation = mock(SharpeRatioCalculation.class);
      var periodCalculationAbstract = mock(PeriodCalculationAbstract.class);

      NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>();
      returns.put(LocalDate.now().minusMonths(1), ONE);
      returns.put(LocalDate.now().minusMonths(2), TEN);
      returns.put(LocalDate.now().minusMonths(3), BigDecimalConstants.TWELVE);

      calculation.tBills = new TreeMap<>();
      calculation.standardDeviationCalculation = mock(StandardDeviationCalculation.class);

      mockedPeriodCalculationAbstract.when(() -> PeriodCalculationAbstract.calculateExcessReturn(any(), any()))
          .thenReturn((TreeMap) returns);
      when(calculation.standardDeviationCalculation.calculatePeriodForNumberOfMonths(anyInt(), any())).thenReturn(ONE);

      doCallRealMethod().when(calculation).getStandardDeviation(anyInt(), any());
      calculation.getStandardDeviation(TWELVE, returns);

      verify(calculation.standardDeviationCalculation).calculatePeriodForNumberOfMonths(eq(TWELVE), any());
    }
  }

  @Test
  void shouldComputeSharpeRatio_whenPeriodIs240AndHistoryExceeds240Months() {
    NavigableMap<LocalDate, BigDecimal> returns = patternedFactorReturns(TWENTY_YR.getMonths() + ONE_YR.getMonths());
    var calculation = sharpeRatioCalculation(returns, Set.of());

    SharpeRatioResult result = calculation.calculate(Set.of(TWENTY_YR));

    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getPerformanceStartDate()).isEqualTo(returns.firstKey());
    assertThat(result.getPerformanceEndDate()).isEqualTo(returns.lastKey());
    assertThat(result.getSharpeRatio()).hasSize(1);
    assertThat(period(result, TWENTY_YR).value()).isNotNull().isPositive();
  }

  @Test
  void shouldIncludeTwentyYearPeriodByDefault_whenNoPeriodsRequested() {
    NavigableMap<LocalDate, BigDecimal> returns = patternedFactorReturns(TWENTY_YR.getMonths() + ONE_YR.getMonths());
    var calculation = sharpeRatioCalculation(returns, Set.of(ONE_YR, TEN_YR, TWENTY_YR));

    SharpeRatioResult result = calculation.calculate(Set.of());

    assertThat(result.getSharpeRatio())
        .extracting(TimeIntervalResult::period)
        .contains(TEN_YR.name(), TWENTY_YR.name());
    assertThat(period(result, TWENTY_YR).value()).isNotNull();
  }

  @Test
  void shouldReturnNullWithInsufficientDataWarning_whenTwentyYearExceedsAvailableHistory() {
    NavigableMap<LocalDate, BigDecimal> returns = patternedFactorReturns(FIVE_YR.getMonths());
    var calculation = sharpeRatioCalculation(returns, Set.of());

    SharpeRatioResult result = calculation.calculate(Set.of(ONE_YR, TWENTY_YR));

    assertThat(period(result, TWENTY_YR).value()).isNull();
    assertThat(period(result, ONE_YR).value()).isNotNull();
    assertThat(result.getWarnings()).singleElement().satisfies(warning -> {
      assertThat(warning.getCode()).isEqualTo(ErrorCode.INSUFFICIENT_MONTHLY_RETURNS_FOR_PERIOD.getCode());
      assertThat(warning.getMessage())
          .contains(String.valueOf(TWENTY_YR.getMonths()))
          .contains(String.valueOf(FIVE_YR.getMonths()));
    });
  }

  private static SharpeRatioCalculation sharpeRatioCalculation(NavigableMap<LocalDate, BigDecimal> returns,
      Set<TimePeriod> defaultPeriods) {
    NavigableMap<LocalDate, BigDecimal> tBills = new TreeMap<>();
    returns.keySet().forEach(month -> tBills.put(month, valueOf(0.001)));
    PeriodCalculationInput input = new PeriodCalculationInput();
    input.setWeightedAveragePortfolioReturns(returns);
    return new SharpeRatioCalculation(input, defaultPeriods, tBills,
        new StandardDeviationCalculation<>(input, defaultPeriods));
  }

  /** Non-degenerate month-end factor series: the alternating step keeps the excess-return standard deviation > 0. */
  private static NavigableMap<LocalDate, BigDecimal> patternedFactorReturns(int count) {
    NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    LocalDate first = LocalDate.of(2005, 1, 31);
    for (int i = 0; i < count; i++) {
      LocalDate month = first.plusMonths(i);
      returns.put(month.withDayOfMonth(month.lengthOfMonth()), valueOf(1.01 + (i % 2 == 0 ? 0.002 : -0.001)));
    }
    return returns;
  }

  private static TimeIntervalResult period(SharpeRatioResult result, TimePeriod period) {
    return result.getSharpeRatio().stream()
        .filter(interval -> period.name().equals(interval.period()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Missing period " + period));
  }

}
