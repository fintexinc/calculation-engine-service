package ca.tangerine.pce.application.calculation.metric;

import ca.tangerine.pce.application.util.CalculationUtils;
import ca.tangerine.pce.model.domain.calculation.input.PeriodCalculationInput;
import ca.tangerine.pce.model.domain.result.TimeIntervalResult;
import ca.tangerine.pce.model.domain.result.risk.StandardDeviationResult;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ca.tangerine.pce.application.util.DecimalUtils.toUserScale;
import static ca.tangerine.pce.model.util.BigDecimalConstants.ONE;
import static ca.tangerine.pce.model.util.BigDecimalConstants.OUTPUT_SCALE;
import static ca.tangerine.pce.util.DateTimeUtils.toLastDayOfMonth;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.SIX_MTH;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class StandardDeviationCalculationTest {

  private final int TWELVE = 12;

  private static TreeMap<LocalDate, BigDecimal> map;

  @BeforeAll
  static void setUp() {
    final LocalDate date = LocalDate.of(2020, 12, 1);
    map = new TreeMap<>();
    map.put(toLastDayOfMonth(date.minusMonths(3)), new BigDecimal("1.01074832088959"));
    map.put(toLastDayOfMonth(date.minusMonths(2)), new BigDecimal("1.01608812281602"));
    map.put(toLastDayOfMonth(date.minusMonths(1)), new BigDecimal("1.00844777099365"));
    map.put(toLastDayOfMonth(date), new BigDecimal("1.01222986673534"));
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetPeriodStartDate() {
    final var calculation = mock(StandardDeviationCalculation.class, withSettings().useConstructor(mock(
        PeriodCalculationInput.class),
        Set.of()));
    final var treeMap = mock(TreeMap.class);

    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);
    stubCompleteCoverage(treeMap);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt(), any());
    calculation.calculatePeriodForNumberOfMonths(TWELVE, treeMap);

    verify(calculation).getPeriodStartDate(12, treeMap);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculatePeriodForNumberOfMonths() {
    final var calculation = mock(StandardDeviationCalculation.class);
    final var returns = mock(TreeMap.class);

    when(calculation.getPortfolioTotalReturns()).thenReturn(returns);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(TWELVE);

    verify(calculation).calculatePeriodForNumberOfMonths(TWELVE, returns);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyGetSubMapByPeriodStartDate() {
    final var calculation = mock(StandardDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);

    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);
    stubCompleteCoverage(treeMap);

    final var periodStartDate = LocalDate.now();
    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);
    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt(), any());

    calculation.calculatePeriodForNumberOfMonths(TWELVE, treeMap);

    verify(calculation).getSubMapByPeriodStartDate(periodStartDate, treeMap);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResult() {
    final var calculation = mock(StandardDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);

    when(treeMap.size()).thenReturn(1);

    final BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(TWELVE, treeMap);

    assertNull(actual);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResult2() {
    final var calculation = mock(StandardDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);

    when(treeMap.size()).thenReturn(20);

    final BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(ONE.intValue(), treeMap);

    assertNull(actual);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenVerifyCalculateStandardDeviation() {
    final var calculation = mock(StandardDeviationCalculation.class);
    final var treeMap = mock(TreeMap.class);

    when(calculation.getPortfolioTotalReturns()).thenReturn(treeMap);
    when(calculation.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
    when(treeMap.size()).thenReturn(TWELVE);
    stubCompleteCoverage(treeMap);

    final LocalDate periodStartDate = LocalDate.now();
    when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt(), any());
    calculation.calculatePeriodForNumberOfMonths(TWELVE, treeMap);

    verify(calculation).calculateStandardDeviation(treeMap, TWELVE);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResultWhenNumberOfMonthsBiggerThanReturnsSize() {
    final var calculation = mock(StandardDeviationCalculation.class);
    final var returns = mock(NavigableMap.class);

    when(returns.size()).thenReturn(11);
    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt(), any(NavigableMap.class));

    final BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(12, returns);

    assertNull(actual);
  }

  @Test
  void shouldCalculatePeriodForNumberOfMonths_whenCheckResultWhenNumberOfMonthsBiggerLessThan12() {
    final var calculation = mock(StandardDeviationCalculation.class);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt(), any(NavigableMap.class));

    final BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(11, mock(NavigableMap.class));

    assertNull(actual);
  }

  /**
   * Pins that portfolio calendar gaps no longer raise an error: the window keeps values 3..10, 13, 14, so the
   * annualized sample deviation is sqrt(120.9 / 11) * sqrt(12).
   */
  @Test
  void shouldCalculateAvailableReturns_whenPortfolioSeriesContainsCalendarGaps() {
    TreeMap<LocalDate, BigDecimal> returns = monthlyReturnsWithIncreasingValues(LocalDate.parse("2024-01-31"), 14);
    returns.remove(LocalDate.parse("2024-11-30"));
    returns.remove(LocalDate.parse("2024-12-31"));
    StandardDeviationCalculation<StandardDeviationResult> calculation = new StandardDeviationCalculation<>(
        new PeriodCalculationInput(null, returns), Set.of());

    BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(TWELVE);

    assertThat(actual).isCloseTo(new BigDecimal("11.484376739331938"), within(new BigDecimal("0.000000000001")));
  }

  @Test
  void shouldReturnNullStandardDeviation_whenRequestedPeriodIsLessThanTwelveMonths() {
    final var calculation = new StandardDeviationCalculation<StandardDeviationResult>(new PeriodCalculationInput(map),
        Set.of());

    final StandardDeviationResult actual = calculation.calculate(Set.of(SIX_MTH));

    assertEquals(Set.of(new TimeIntervalResult(SIX_MTH.name(), null)), actual.getStandardDeviation());
    assertEquals(map.firstKey(), actual.getPerformanceStartDate());
    assertEquals(map.lastKey(), actual.getPerformanceEndDate());
    assertNull(actual.getCustomIntervalPerformanceStartDate());
  }

  @Test
  void shouldCalculateStandardDeviation_whenVerifyCalculateNumerator() {
    final var calculation = mock(StandardDeviationCalculation.class);
    final var treeMap = new TreeMap();
    treeMap.put(LocalDate.now(), TEN);
    treeMap.put(LocalDate.now().minusMonths(1), TEN);
    treeMap.put(LocalDate.now().minusMonths(5), TEN);

    when(calculation.calculateNumerator(any(), any())).thenReturn(BigDecimal.ONE);
    doCallRealMethod().when(calculation).calculateStandardDeviation(any(), anyInt());

    calculation.calculateStandardDeviation(treeMap, TWELVE);

    verify(calculation).calculateNumerator(treeMap, TEN.setScale(15, RoundingMode.UNNECESSARY));
  }

  @Test
  void shouldCalculateStandardDeviation_whenCheckResult() {
    final var calculation = mock(StandardDeviationCalculation.class);
    doCallRealMethod().when(calculation).setScale(anyInt());
    calculation.setScale(OUTPUT_SCALE);
    final var treeMap = new TreeMap();
    treeMap.put(LocalDate.now(), ONE);
    treeMap.put(LocalDate.now().minusMonths(1), TEN);
    treeMap.put(LocalDate.now().minusMonths(5), TEN);

    when(calculation.calculateNumerator(any(), any())).thenReturn(BigDecimal.TEN);
    doCallRealMethod().when(calculation).calculateStandardDeviation(any(), anyInt());
    final BigDecimal actual = calculation.calculateStandardDeviation(treeMap, TWELVE);

    assertEquals(toUserScale(BigDecimal.valueOf(3.30289129537908)), actual);
  }

  @Test
  void shouldCalculateNumerator_whenCheckResult() {
    final var calculation = mock(StandardDeviationCalculation.class);

    doCallRealMethod().when(calculation).calculateNumerator(any(), any());
    doCallRealMethod().when(calculation).overrideTotalReturns(any());

    final NavigableMap<LocalDate, BigDecimal> totalReturns = calculation.overrideTotalReturns(map);
    final BigDecimal actual = calculation.calculateNumerator(totalReturns, CalculationUtils.average(totalReturns));

    assertEquals(toUserScale(BigDecimal.valueOf(3.08915891708201E-05)), toUserScale(actual));
  }

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    final var calculation = mock(StandardDeviationCalculation.class);
    final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05", ONE));
    final var interval1 = new TimeIntervalResult("2000-01-12", ZERO);
    final var interval2 = new TimeIntervalResult("2020-01-05", ONE);
    final var expected = Set.of(interval1, interval2);

    when(calculation.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(calculation).defineResponseType(anySet());
    final StandardDeviationResult actual = (StandardDeviationResult) calculation.defineResponseType(pairs);

    assertEquals(expected, actual.getStandardDeviation());
  }

  private static TreeMap<LocalDate, BigDecimal> monthlyReturnsWithIncreasingValues(LocalDate startDate, int months) {
    return IntStream.range(0, months)
        .boxed()
        .collect(Collectors.toMap(month -> toLastDayOfMonth(startDate.plusMonths(month)),
            month -> BigDecimal.valueOf(month + 1L), (left, right) -> right, TreeMap::new));
  }

  private static void stubCompleteCoverage(NavigableMap<LocalDate, BigDecimal> returns) {
    when(returns.lastKey()).thenReturn(LocalDate.parse("2024-12-31"));
    when(returns.containsKey(any(LocalDate.class))).thenReturn(true);
  }

  @AfterAll
  static void tearDown() {
    map.clear();
  }

}
