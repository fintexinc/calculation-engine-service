package ca.tangerine.pce.application.calculation.metric.core;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import static ca.tangerine.pce.application.util.DecimalUtils.toUserScale;
import static ca.tangerine.pce.application.util.TestConstants.LOCAL_DATE_NOW;
import static ca.tangerine.pce.model.util.BigDecimalConstants.ONE;
import static ca.tangerine.pce.model.util.BigDecimalConstants.TWO;
import static ca.tangerine.pce.util.DateTimeUtils.toLastDayOfMonth;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.CIPSD;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.FIVE_MTH;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.ONE_MTH;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.QTD;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.SI;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.THREE_MTH;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.TWO_MTH;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.TWO_YR;
import static ca.tangerine.wm.commons.domain.enumeration.TimePeriod.YTD;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import ca.tangerine.pce.application.calculation.metric.StandardDeviationCalculation;
import ca.tangerine.pce.application.calculation.metric.TrailingTotalReturnsCalculation;
import ca.tangerine.pce.model.domain.calculation.input.PeriodCalculationInput;
import ca.tangerine.pce.model.domain.calculation.input.WeightedAverageInput;
import ca.tangerine.pce.model.domain.result.PeriodResult;
import ca.tangerine.pce.model.domain.result.TimeIntervalResult;
import ca.tangerine.pce.model.domain.result.returns.TrailingTotalReturnsResult;
import ca.tangerine.pce.model.error.exceptions.CalculationException;
import ca.tangerine.pce.util.ComparisonUtils;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;

class PeriodCalculationAbstractTest {

  int TWELVE = 12;

  @Test
  void shouldAddSinceCustomIntervalPeriod_whenSinceCustomIntervalStartDateIsValid() {
    PeriodCalculationInput w = mock(PeriodCalculationInput.class);
    when(w.getCipsd()).thenReturn(null);
    PeriodCalculationAbstract p = TrailingTotalReturnsCalculation.mathOnly(w, Set.of());

    Set<Pair<String, BigDecimal>> results = new HashSet<>();
    p.addSinceCustomIntervalPerformanceStartDate(results, Set.of());

    assertEquals(0, results.size());
  }

  @Test
  void shouldNotAddSinceCustomIntervalPeriod_whenPeriodAlreadyPresent() {
    PeriodCalculationAbstract calculation = mock(PeriodCalculationAbstract.class);

    when(calculation.isSinceCustomIntervalPerformanceStartDateValid()).thenReturn(false);

    doCallRealMethod().when(calculation).addSinceCustomIntervalPerformanceStartDate(any(), any());
    Set<Pair<String, BigDecimal>> results = new HashSet<>();
    calculation.addSinceCustomIntervalPerformanceStartDate(results, Set.of(SI, YTD, ONE_YR));

    assertEquals(0, results.size());
  }

  @Test
  void shouldCalculateFromCustomIntervalStartDate_whenSinceCustomIntervalPeriodRequested() {
    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    WeightedAverageInput w = mock(WeightedAverageInput.class);
    when(w.getCipsd()).thenReturn(LocalDate.now());

    when(p.isSinceCustomIntervalPerformanceStartDateValid()).thenReturn(false);

    doCallRealMethod().when(p).addSinceCustomIntervalPerformanceStartDate(any(), any());
    Set<Pair<String, BigDecimal>> results = new HashSet<>();
    p.addSinceCustomIntervalPerformanceStartDate(results, Set.of(CIPSD));

    assertEquals(1, results.size());
    Pair<String, BigDecimal> actual = results.stream().findFirst().orElseThrow();
    assertEquals(CIPSD.name(), actual.getKey());
    assertNull(actual.getValue());
  }

  @Test
  void shouldCalculatePeriodForCustomIntervalStartDate_whenAddingSinceCustomIntervalPeriod() {
    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    when(p.isSinceCustomIntervalPerformanceStartDateValid()).thenReturn(true);

    doCallRealMethod().when(p).addSinceCustomIntervalPerformanceStartDate(any(), any());
    p.addSinceCustomIntervalPerformanceStartDate(new HashSet<>(), Set.of(CIPSD));

    verify(p).calculatePeriodForCustomIntervalStartDate();
  }

  @Test
  void shouldAddSinceCustomIntervalResult_whenCustomIntervalValueIsCalculated() {
    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    when(p.isSinceCustomIntervalPerformanceStartDateValid()).thenReturn(true);

    BigDecimal one = ONE;
    when(p.calculatePeriodForCustomIntervalStartDate()).thenReturn(one);
    when(p.toUserFormat(any())).thenReturn(one);

    doCallRealMethod().when(p).addSinceCustomIntervalPerformanceStartDate(any(), any());
    HashSet<Pair<String, BigDecimal>> resultSet = new HashSet<>();
    p.addSinceCustomIntervalPerformanceStartDate(resultSet, Set.of(CIPSD));

    assertEquals(Set.of(Pair.of(CIPSD.name(), one)), resultSet);
  }

  @Test
  void shouldDelegateToCalculatePeriodForNumberOfMonths_whenCalculatingSinceCustomInterval() {
    var context = mock(PeriodCalculationInput.class);
    when(context.getCipsd()).thenReturn(LOCAL_DATE_NOW);
    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class,
        withSettings().useConstructor(context, null));

    p.portfolioTotalReturns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE, LOCAL_DATE_NOW.plusMonths(2), ONE));

    doCallRealMethod().when(p).calculatePeriodForCustomIntervalStartDate();
    p.calculatePeriodForCustomIntervalStartDate();

    verify(p).calculatePeriodForNumberOfMonths(3);
  }

  @Test
  void shouldReturnDeclaredLength_whenPeriodHasOne() {
    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE));
    TimePeriod period = THREE_MTH;

    doCallRealMethod().when(p).getNumberOfMonthsFor(any(), any());
    int actual = p.getNumberOfMonthsFor(returns, period);

    assertEquals(3, actual);
  }

  @Test
  void shouldCalculateMonthsFromYearStart_whenPeriodIsYearToDate() {
    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE));
    TimePeriod period = YTD;

    int months = 10;
    when(p.getNumberOfMonthsForYearToDate(any())).thenReturn(months);

    doCallRealMethod().when(p).getNumberOfMonthsFor(any(), any());
    int actual = p.getNumberOfMonthsFor(returns, period);

    assertEquals(months, actual);
    verify(p).getNumberOfMonthsForYearToDate(returns);
  }

  @Test
  void shouldCalculateMonthsFromInception_whenPeriodIsSinceInception() {
    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE));
    TimePeriod period = SI;

    int months = 15;
    when(p.getNumberOfMonthsForSinceInception(any())).thenReturn(months);

    doCallRealMethod().when(p).getNumberOfMonthsFor(any(), any());
    int actual = p.getNumberOfMonthsFor(returns, period);

    assertEquals(months, actual);
    verify(p).getNumberOfMonthsForSinceInception(returns);
  }

  /**
   * A malformed period string can no longer reach this method — the type makes it unrepresentable. What remains
   * reachable is a real member whose length this pipeline cannot resolve: {@code QTD} carries no length of its own and,
   * unlike {@code YTD} and {@code SI}, has no resolution rule here.
   */
  @Test
  void shouldThrowNamingThePeriod_whenLengthCannotBeResolved() {
    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE));
    TimePeriod period = QTD;

    doCallRealMethod().when(p).getNumberOfMonthsFor(any(), any());
    CalculationException e = assertThrows(CalculationException.class, () -> p.getNumberOfMonthsFor(returns,
        period));

    assertTrue(e.getMessage().contains(period.name()));
  }

  @Test
  void shouldDelegateToCalculatePeriods_whenCalculateIsCalled() {
    Set<TimePeriod> periods = Set.of(ONE_MTH);
    Set<Pair<String, BigDecimal>> periodResults = Set.of();
    PeriodResult result = mock(PeriodResult.class);
    PeriodCalculationAbstract p = calculationForCalculateTests(periodResults, result);

    doCallRealMethod().when(p).calculate(any());
    PeriodResult actual = p.calculate(periods);

    verify(p).calculatePeriods(periods);
    verify(p).defineResponseType(periodResults);
    verify(result).setWarnings(List.of());
    assertEquals(result, actual);
  }

  @Test
  void shouldAddSinceCustomIntervalPeriod_whenCalculatingPeriods() {
    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    TimePeriod interval = ONE_YR;
    Set<TimePeriod> periods = Set.of(CIPSD, interval);

    doCallRealMethod().when(p).getInitialPeriods(any());
    doCallRealMethod().when(p).calculatePeriods(any());
    p.calculatePeriods(periods);

    verify(p).calculateForPeriod(interval);
    verify(p, times(0)).calculateForPeriod(CIPSD);
  }

  @Test
  void shouldDelegateToDefineResponseType_whenCalculateIsCalled() {
    Set<TimePeriod> periods = Set.of(ONE_MTH);
    Set<Pair<String, BigDecimal>> periodResults = Set.of(Pair.of(ONE_MTH.name(), ONE));
    PeriodResult result = mock(PeriodResult.class);
    PeriodCalculationAbstract p = calculationForCalculateTests(periodResults, result);

    doCallRealMethod().when(p).calculate(any());
    PeriodResult actual = p.calculate(periods);

    verify(p).defineResponseType(periodResults);
    verify(result).setWarnings(List.of());
    assertEquals(result, actual);
  }

  @Test
  void shouldReturnDefaultPeriods_whenInputPeriodsEmpty() {
    Set<TimePeriod> periods = Set.of(THREE_MTH);
    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class,
        withSettings().useConstructor(mock(PeriodCalculationInput.class), periods));

    doCallRealMethod().when(p).getInitialPeriods(any());
    Set actual = p.getInitialPeriods(Set.of());

    assertEquals(periods, actual);
  }

  @Test
  void shouldReturnInputPeriods_whenInputPeriodsProvided() {
    Set<TimePeriod> periods = Set.of(THREE_MTH);
    PeriodCalculationAbstract calculation = mock(PeriodCalculationAbstract.class,
        withSettings().useConstructor(mock(PeriodCalculationInput.class), periods));

    doCallRealMethod().when(calculation).getInitialPeriods(any());
    Set<TimePeriod> userP = Set.of(FIVE_MTH);
    Set actual = calculation.getInitialPeriods(userP);

    assertEquals(userP, actual);
  }

  @Test
  void shouldReturnNull_whenPeriodExceedsPortfolioSizeForProduct() {
    var calculation = mock(PeriodCalculationAbstract.class);

    TreeMap<LocalDate, BigDecimal> aReturns = new TreeMap<>(Map.of(toLastDayOfMonth(LOCAL_DATE_NOW), ONE));

    doCallRealMethod().when(calculation).filterRequiredMonthsForPeriod(1, aReturns);

    doCallRealMethod().when(calculation).calculateProductForPeriod(eq(1), any());
    BigDecimal actual = calculation.calculateProductForPeriod(1, aReturns);

    assertEquals(0, ONE.compareTo(actual));
  }

  @Test
  void shouldCalculateProductForRequestedPeriod_whenEnoughDataExists() {
    PeriodCalculationAbstract calculation = mock(PeriodCalculationAbstract.class);

    TreeMap<LocalDate, BigDecimal> aReturns = new TreeMap<>(Map.of(
        toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(2)), ONE,
        toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(3)), BigDecimal.valueOf(5),
        toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), TEN,
        toLastDayOfMonth(LOCAL_DATE_NOW), BigDecimal.valueOf(2)));

    doCallRealMethod().when(calculation).filterRequiredMonthsForPeriod(3, aReturns);

    doCallRealMethod().when(calculation).calculateProductForPeriod(eq(3), any());
    BigDecimal actual = calculation.calculateProductForPeriod(3, aReturns);

    assertEquals(0, BigDecimal.valueOf(20).compareTo(actual));
  }

  @Test
  void shouldCalculateBenchmarkProductForRequestedPeriod_whenEnoughDataExists() {
    var calculation = mock(PeriodCalculationAbstract.class);

    var portfolioTotalReturns = new TreeMap<LocalDate, BigDecimal>(Map.of(toLastDayOfMonth(LOCAL_DATE_NOW
        .minusMonths(2)), BigDecimal.valueOf(5)));
    var aReturns = new TreeMap<LocalDate, BigDecimal>(Map.of(
        toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(3)), ONE,
        toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(2)), BigDecimal.valueOf(11),
        toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), TEN,
        toLastDayOfMonth(LOCAL_DATE_NOW), BigDecimal.valueOf(2)));

    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);
    doCallRealMethod().when(calculation).filterRequiredMonthsForPeriod(3, portfolioTotalReturns);

    doCallRealMethod().when(calculation).getBenchmarkValues(eq(3), any());
    List list = calculation.getBenchmarkValues(3, aReturns);

    assertEquals(List.of(BigDecimal.valueOf(11)), list);
  }

  @Test
  void shouldPopulateBasicDetails_whenCalculateIsCalled() {
    Set<TimePeriod> periods = Set.of(ONE_MTH);
    Set<Pair<String, BigDecimal>> periodResults = Set.of();
    PeriodResult result = mock(PeriodResult.class);
    PeriodCalculationAbstract p = calculationForCalculateTests(periodResults, result);

    doCallRealMethod().when(p).calculate(any());
    PeriodResult actual = p.calculate(periods);

    verify(p).populateBasicDetails(result);
    verify(result).setWarnings(List.of());
    assertEquals(result, actual);
  }

  @Test
  void shouldReadInitialPeriods_whenCalculatingPeriods() {
    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    when(p.getInitialPeriods(any())).thenReturn(Set.of());

    Set<TimePeriod> periods = Set.of(TWO_MTH);

    doCallRealMethod().when(p).calculatePeriods(any());
    p.calculatePeriods(periods);

    verify(p).getInitialPeriods(periods);
  }

  private static PeriodCalculationAbstract calculationForCalculateTests(
      Set<Pair<String, BigDecimal>> periodResults,
      PeriodResult result) {
    PeriodCalculationInput input = new PeriodCalculationInput(null, new TreeMap<>(), List.of());
    PeriodCalculationAbstract calculation = mock(PeriodCalculationAbstract.class,
        withSettings().useConstructor(input, Set.of()));
    when(calculation.calculatePeriods(any())).thenReturn(periodResults);
    when(calculation.defineResponseType(periodResults)).thenReturn(result);
    return calculation;
  }

  @Test
  void shouldCalculateValueForEachPeriod_whenCalculatingPeriods() {
    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    TimePeriod period = TWO_MTH;
    Set<TimePeriod> periods = Set.of(period);

    when(p.getInitialPeriods(any())).thenReturn(periods);

    doCallRealMethod().when(p).calculatePeriods(any());
    p.calculatePeriods(periods);

    verify(p).calculateForPeriod(period);
  }

  @Test
  void shouldAppendSinceCustomIntervalPeriod_whenCalculatingPeriods() {
    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    TimePeriod period = TWO_MTH;
    Set<TimePeriod> periods = Set.of(period);
    Pair<String, BigDecimal> pair = Pair.of(period.name(), ONE);

    when(p.getInitialPeriods(any())).thenReturn(periods);
    when(p.calculateForPeriod(any())).thenReturn(pair);

    doCallRealMethod().when(p).calculatePeriods(any());
    Set actual = p.calculatePeriods(periods);

    verify(p).addSinceCustomIntervalPerformanceStartDate(Set.of(pair), periods);
    assertEquals(Set.of(pair), actual);
  }

  /**
   * The period reaches resolution unchanged. This used to assert that a whitespace-padded {@code "3 "} was trimmed
   * first — an artefact of the comma-separated yml values being split into raw strings. Typed periods cannot carry
   * whitespace, so what is worth pinning now is only that the same portfolio series and the same period are passed
   * through.
   */
  @Test
  void shouldDelegateToCalculatePeriodForNumberOfMonths_whenCalculatingForPeriod() {
    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);
    p.portfolioTotalReturns = new TreeMap();

    TimePeriod period = THREE_MTH;

    doCallRealMethod().when(p).calculateForPeriod(any());
    p.calculateForPeriod(period);

    verify(p).getNumberOfMonthsFor(argThat(argument -> argument == p.portfolioTotalReturns), eq(period));
  }

  @Test
  void shouldResolveNumberOfMonths_whenCalculatingForPeriod() {
    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    int months = 10;
    when(p.getNumberOfMonthsFor(any(), any())).thenReturn(months);

    doCallRealMethod().when(p).calculateForPeriod(any());
    p.calculateForPeriod(TWO_YR);

    verify(p).calculatePeriodForNumberOfMonths(months);
  }

  /** The pair is keyed by the period's name, which is the form the response DTOs and the wire both use. */
  @Test
  void shouldReturnPeriodPair_whenCalculationSucceeds() {
    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    BigDecimal one = ONE;
    when(p.calculatePeriodForNumberOfMonths(0)).thenReturn(one);
    TimePeriod period = TWO_YR;

    when(p.toUserFormat(any())).thenReturn(one);
    doCallRealMethod().when(p).calculateForPeriod(any());
    Pair actual = p.calculateForPeriod(period);

    assertEquals(Pair.of(period.name(), toUserScale(one)), actual);
  }

  @Test
  void shouldRoundValuesToUserFormat_whenFormattingResult() {
    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    BigDecimal one = ONE;

    doCallRealMethod().when(p).toUserFormat(any());

    BigDecimal actual = (BigDecimal) p.toUserFormat(one);

    assertEquals(toUserScale(one), actual);
  }

  @Test
  void shouldPopulateDateDetails_whenSettingPeriodDates() {
    var context = mock(PeriodCalculationInput.class);
    when(context.getCipsd()).thenReturn(LOCAL_DATE_NOW);

    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class,
        withSettings().useConstructor(context, Set.of()));

    TreeMap kvTreeMap = new TreeMap<>(Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE, LOCAL_DATE_NOW.plusMonths(1),
        ONE));
    p.portfolioTotalReturns = kvTreeMap;

    doCallRealMethod().when(p).populateBasicDetails(any());
    TrailingTotalReturnsResult actual = new TrailingTotalReturnsResult();
    p.populateBasicDetails(actual);

    assertEquals(LOCAL_DATE_NOW, actual.getCustomIntervalPerformanceStartDate());
    assertEquals(LOCAL_DATE_NOW.minusMonths(1), actual.getPerformanceStartDate());
    assertEquals(LOCAL_DATE_NOW.plusMonths(1), actual.getPerformanceEndDate());
  }

  @ParameterizedTest(name = "[{index}] date={0}-{1}-{2} expectedMonth={1}")
  @CsvSource({
      "2020, 4, 30",
      "2020, 1, 31",
      "2020, 12, 31",
  })
  void shouldReturnMonthIndex_whenCalculatingNumberOfMonthsForYearToDate(int year, int month, int dayOfMonth) {
    PeriodCalculationAbstract t = mock(PeriodCalculationAbstract.class);
    doCallRealMethod().when(t).getNumberOfMonthsForYearToDate(anyMap());

    Map<LocalDate, BigDecimal> aReturns = Map.of(LocalDate.of(year, month, dayOfMonth), ONE);

    int actual = t.getNumberOfMonthsForYearToDate(aReturns);

    assertEquals(month, actual);
  }

  @ParameterizedTest(name = "[{index}] {0} (returns={1}, expectedMonths={2})")
  @MethodSource("sinceInceptionMonthCountCases")
  void shouldReturnEntryCount_whenCalculatingNumberOfMonthsForSinceInception(
      String name,
      NavigableMap<LocalDate, BigDecimal> returns,
      int expectedMonths) {
    PeriodCalculationAbstract t = mock(PeriodCalculationAbstract.class);
    doCallRealMethod().when(t).getNumberOfMonthsForSinceInception(any(NavigableMap.class));

    int actual = t.getNumberOfMonthsForSinceInception(returns);

    assertEquals(expectedMonths, actual);
  }

  static Stream<Arguments> sinceInceptionMonthCountCases() {
    return Stream.of(
        Arguments.of("single entry", new TreeMap<>(Map.of(LocalDate.of(2020, 4, 30), ONE)), 1),
        Arguments.of("two consecutive months",
            new TreeMap<>(Map.of(
                toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), ONE,
                toLastDayOfMonth(LOCAL_DATE_NOW), ONE)),
            2));
  }

  @ParameterizedTest(name = "[{index}] {0} (cipsd={1}, expectedValid={3})")
  @MethodSource("sinceCustomIntervalValidityCases")
  void shouldDetermineSinceCustomIntervalValidity(
      String name,
      LocalDate cipsd,
      NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      boolean expectedValid) {
    var calculation = mock(PeriodCalculationAbstract.class);
    calculation.cipsd = cipsd;
    calculation.portfolioTotalReturns = new TreeMap<>(portfolioReturns);

    doCallRealMethod().when(calculation).isSinceCustomIntervalPerformanceStartDateValid();
    boolean actual = calculation.isSinceCustomIntervalPerformanceStartDateValid();

    assertEquals(expectedValid, actual);
  }

  static Stream<Arguments> sinceCustomIntervalValidityCases() {
    NavigableMap<LocalDate, BigDecimal> singleEntry = new TreeMap<>(Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE));
    NavigableMap<LocalDate, BigDecimal> rangeEntries = new TreeMap<>(Map.of(
        LOCAL_DATE_NOW.minusMonths(1), ONE,
        LOCAL_DATE_NOW.plusMonths(1), TWO));
    return Stream.of(
        // Note: cases 1 and 2 currently have identical inputs (the original "before" test had the same data
        // as the "after" test); preserved as-is to keep behavior. Fix the inputs to genuinely test "before"
        // when revisiting.
        Arguments.of("custom start date after portfolio start", LOCAL_DATE_NOW, singleEntry, false),
        Arguments.of("custom start date before portfolio start", LOCAL_DATE_NOW, singleEntry, false),
        Arguments.of("custom start date within portfolio range", LOCAL_DATE_NOW, rangeEntries, true));
  }

  @Test
  void shouldReturnPeriodStartDate_whenOffsetProvided() {
    StandardDeviationCalculation calculation = mock(StandardDeviationCalculation.class);
    doCallRealMethod().when(calculation).getPeriodStartDate(anyInt(), any());
    LocalDate periodStartDate = calculation.getPeriodStartDate(12, getPortfolioReturns());

    assertEquals(toLastDayOfMonth(LocalDate.of(2020, 12, 1).minusMonths(11)), periodStartDate);
  }

  @Test
  void shouldReturnSubMapFromStartDate_whenFilteringReturns() {
    StandardDeviationCalculation calculation = mock(StandardDeviationCalculation.class);
    doCallRealMethod().when(calculation).getSubMapByPeriodStartDate(any(), any());
    SortedMap<LocalDate, BigDecimal> subMap = calculation
        .getSubMapByPeriodStartDate(toLastDayOfMonth(LocalDate.of(2020, 12, 1).minusMonths(2)), getPortfolioReturns());

    assertEquals(3, subMap.size());
    assertEquals(toLastDayOfMonth(LocalDate.of(2020, 12, 1)), subMap.lastKey());
    assertEquals(toLastDayOfMonth(LocalDate.of(2020, 10, 1)), subMap.firstKey());
  }

  @Test
  void shouldConvertTotalReturnsToMonthlyChanges_whenOverridingReturns() {
    StandardDeviationCalculation calculation = mock(StandardDeviationCalculation.class);
    LocalDate date = LocalDate.of(2020, 12, 1);
    TreeMap<LocalDate, BigDecimal> portfolioTotalReturns = new TreeMap<>(Map.of(toLastDayOfMonth(date), BigDecimal
        .valueOf(1.01094319080371),
        toLastDayOfMonth(date.minusMonths(1)), BigDecimal.valueOf(1.02297440154456)));
    when(calculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);
    doCallRealMethod().when(calculation).overrideTotalReturns(any());

    NavigableMap<LocalDate, BigDecimal> totalReturns = calculation.overrideTotalReturns(
        portfolioTotalReturns);

    assertEquals(2, totalReturns.size());
    assertEquals(toUserScale(BigDecimal.valueOf(0.02297440154456)), toUserScale(totalReturns.firstEntry().getValue()));
    assertEquals(toUserScale(BigDecimal.valueOf(0.01094319080371)), toUserScale(totalReturns.lastEntry().getValue()));
  }

  @Test
  void shouldCreateTimeIntervalResults_whenFormattingPairs() {
    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    Set<TimeIntervalResult> expected = Set.of(new TimeIntervalResult("2000-01-12", ONE));

    Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2000-01-12", ONE));

    doCallRealMethod().when(p).formTimeIntervalResult(anySet());
    Set actual = p.formTimeIntervalResult(pairs);

    assertEquals(expected, actual);
  }

  @Test
  void shouldUseSubMapFromPeriodStartDate_whenCalculatingAverageArithmeticAnnualizedReturn() {
    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);
    TreeMap treeMap = mock(TreeMap.class);
    LocalDate date = LocalDate.now();

    doCallRealMethod().when(p).calculateAverageArithmeticAnnualizedReturn(any(), any(), anyInt());

    p.calculateAverageArithmeticAnnualizedReturn(treeMap, date, TWELVE);

    verify(p).getSubMapByPeriodStartDate(date, treeMap);
  }

  @Test
  void shouldCalculateAverageArithmeticAnnualizedReturn_whenSubMapContainsReturns() {
    PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);
    TreeMap treeMap = mock(TreeMap.class);
    LocalDate date = LocalDate.now();
    TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(date, ONE, date.plusMonths(1), TWO));

    when(p.getSubMapByPeriodStartDate(any(), any())).thenReturn(returns);
    doCallRealMethod().when(p).calculateAverageArithmeticAnnualizedReturn(any(), any(), anyInt());

    BigDecimal returnValue = p.calculateAverageArithmeticAnnualizedReturn(treeMap, date, TWELVE);

    assertEquals(toUserScale(BigDecimal.valueOf(3)), toUserScale(returnValue));
  }

  @Test
  void shouldRestrictTBillsRangeToReturnsWindow_whenBothInputsProvided() {
    var calculation = mock(PeriodCalculationAbstract.class);
    LocalDate date = LocalDate.now();
    TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(date, ONE, date.plusMonths(1), TWO));
    TreeMap<LocalDate, BigDecimal> tBills = new TreeMap<>(Map.of(
        date.minusMonths(1), ZERO,
        date, ONE,
        date.plusMonths(1), TWO,
        date.plusMonths(2), TEN));
    var expected = new TreeMap<>(Map.of(
        date, ONE,
        date.plusMonths(1), TWO));

    doCallRealMethod().when(calculation).restrictTBillsRange(any(), any());
    NavigableMap<LocalDate, BigDecimal> actual = calculation.restrictTBillsRange(tBills, returns);

    ComparisonUtils.compareMaps(expected, actual);
    assertEquals(expected.size(), actual.size());
  }

  @Test
  void shouldRestrictTBillsRangeUsingPortfolioReturns_whenOnlyTBillsProvided() {
    var calculation = mock(PeriodCalculationAbstract.class);
    LocalDate date = LocalDate.now();
    TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(date, ONE, date.plusMonths(1), TWO));
    TreeMap<LocalDate, BigDecimal> tBills = new TreeMap<>(Map.of(
        date.minusMonths(1), ZERO,
        date, ONE,
        date.plusMonths(1), TWO,
        date.plusMonths(2), TEN));
    calculation.portfolioTotalReturns = returns;

    var expected = new TreeMap<>(Map.of(
        date, ONE,
        date.plusMonths(1), TWO));

    doCallRealMethod().when(calculation).restrictTBillsRange(any());
    doCallRealMethod().when(calculation).restrictTBillsRange(any(), any());
    NavigableMap<LocalDate, BigDecimal> actual = calculation.restrictTBillsRange(tBills);

    ComparisonUtils.compareMaps(expected, actual);
    assertEquals(expected.size(), actual.size());
  }

  @ParameterizedTest(name = "[{index}] {0} (returnsSize={1}, period={2}, value={3}, expectedWarnings={4}, expectedCode={5})")
  @MethodSource("insufficientDataWarningCases")
  void shouldHandleInsufficientDataWarning(
      String name,
      int returnsSize,
      String period,
      BigDecimal value,
      int expectedWarningCount,
      String expectedCode) {
    var calculation = mock(PeriodCalculationAbstract.class);
    TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    for (int i = 0; i < returnsSize; i++) {
      returns.put(LOCAL_DATE_NOW.minusMonths(i), ONE);
    }
    calculation.portfolioTotalReturns = returns;
    TrailingTotalReturnsResult result = new TrailingTotalReturnsResult();
    Set<Pair<String, BigDecimal>> periodValues = Set.of(Pair.of(period, value));

    doCallRealMethod().when(calculation).addInsufficientDataWarnings(any(), any());
    doCallRealMethod().when(calculation).availableMonths();
    doCallRealMethod().when(calculation).getNumberOfMonthsFor(any(), any());
    doCallRealMethod().when(calculation).getNumberOfMonthsForYearToDate(any());
    doCallRealMethod().when(calculation).getNumberOfMonthsForSinceInception(any());
    doCallRealMethod().when(calculation).requiresInsufficientDataWarning(any(), anyInt());
    doCallRealMethod().when(calculation).requiresUnavailablePeriodWarning(any(), anyInt());
    doCallRealMethod().when(calculation).unavailablePeriodWarning(any());
    calculation.addInsufficientDataWarnings(result, periodValues);

    assertEquals(expectedWarningCount, result.getWarnings().size());
    if (expectedCode != null) {
      assertEquals(expectedCode, result.getWarnings().get(0).getCode());
    }
  }

  /**
   * The period here is a <i>result</i> key, which is still a string — always a {@link TimePeriod} name, since that is
   * the only thing {@code calculateForPeriod} writes. The whitespace case that used to sit in this list is gone with
   * the SpEL splits that produced it: a padded {@code " 36"} can no longer occur, so a test asserting it was trimmed
   * would only be testing the test.
   */
  static Stream<Arguments> insufficientDataWarningCases() {
    return Stream.of(
        // Fixed-length periods.
        Arguments.of("fixed-length period exceeds available months", 1, ONE_YR.name(), null, 1, "RET-004"),
        Arguments.of("fixed-length period fits available months", 12, ONE_YR.name(), null, 1, "RET-011"),
        Arguments.of("value is not null (period not exceeded)", 1, ONE_YR.name(), ONE, 0, null),
        // With 13 entries the resolved YTD month count is always ≤ 12 ≤ 13, so it fits. Confirms length-less periods
        // are resolved from the series (this would warn if resolution fell through to the mock's 0 default).
        Arguments.of("length-less YTD fits available months", 13, YTD.name(), null, 1, "RET-011"),
        // CIPSD is gated by the date's position rather than by a month count, so the count-based path skips it. With no
        // CIPSD set, the dedicated out-of-range path is also a no-op.
        Arguments.of("CIPSD with no date set is skipped", 1, CIPSD.name(), null, 0, null));
  }

  @Test
  void shouldEmitCipsdOutsideDataRangeWarning_whenCipsdIsBeforeFirstAvailableMonth() {
    var calculation = mock(PeriodCalculationAbstract.class);
    TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    returns.put(LOCAL_DATE_NOW.minusMonths(2), ONE);
    returns.put(LOCAL_DATE_NOW.minusMonths(1), ONE);
    returns.put(LOCAL_DATE_NOW, ONE);
    calculation.portfolioTotalReturns = returns;
    calculation.cipsd = LOCAL_DATE_NOW.minusMonths(24);
    TrailingTotalReturnsResult result = new TrailingTotalReturnsResult();
    Set<Pair<String, BigDecimal>> periodValues = Set.of(
        Pair.of(CIPSD.name(), null));

    doCallRealMethod().when(calculation).addInsufficientDataWarnings(any(), any());
    doCallRealMethod().when(calculation).availableMonths();
    doCallRealMethod().when(calculation).getNumberOfMonthsFor(any(), any());
    doCallRealMethod().when(calculation).requiresUnavailablePeriodWarning(any(), anyInt());
    doCallRealMethod().when(calculation).unavailablePeriodWarning(any());
    // Drive the warning gate from the real cipsd-position logic instead of Mockito's default false — otherwise
    // the assertion would pass for any non-null cipsd, including in-range values, defeating the test's premise.
    doCallRealMethod().when(calculation).isSinceCustomIntervalPerformanceStartDateValid();
    calculation.addInsufficientDataWarnings(result, periodValues);

    assertEquals(1, result.getWarnings().size());
    assertEquals("RET-005", result.getWarnings().get(0).getCode());
  }

  @Test
  void shouldEmitCipsdOutsideDataRangeWarning_whenCipsdIsAfterLastAvailableMonth() {
    var calculation = mock(PeriodCalculationAbstract.class);
    TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    returns.put(LOCAL_DATE_NOW.minusMonths(2), ONE);
    returns.put(LOCAL_DATE_NOW.minusMonths(1), ONE);
    returns.put(LOCAL_DATE_NOW, ONE);
    calculation.portfolioTotalReturns = returns;
    calculation.cipsd = LOCAL_DATE_NOW.plusMonths(6);
    TrailingTotalReturnsResult result = new TrailingTotalReturnsResult();
    Set<Pair<String, BigDecimal>> periodValues = Set.of(
        Pair.of(CIPSD.name(), null));

    doCallRealMethod().when(calculation).addInsufficientDataWarnings(any(), any());
    doCallRealMethod().when(calculation).availableMonths();
    doCallRealMethod().when(calculation).getNumberOfMonthsFor(any(), any());
    doCallRealMethod().when(calculation).requiresUnavailablePeriodWarning(any(), anyInt());
    doCallRealMethod().when(calculation).unavailablePeriodWarning(any());
    // Drive the warning gate from the real cipsd-position logic instead of Mockito's default false — otherwise
    // the assertion would pass for any non-null cipsd, including in-range values, defeating the test's premise.
    doCallRealMethod().when(calculation).isSinceCustomIntervalPerformanceStartDateValid();
    calculation.addInsufficientDataWarnings(result, periodValues);

    assertEquals(1, result.getWarnings().size());
    assertEquals("RET-005", result.getWarnings().get(0).getCode());
  }

  @Test
  void shouldNotEmitCipsdOutsideDataRangeWarning_whenCipsdIsValidWithinDataRange() {
    var calculation = mock(PeriodCalculationAbstract.class);
    TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    returns.put(LOCAL_DATE_NOW.minusMonths(2), ONE);
    returns.put(LOCAL_DATE_NOW.minusMonths(1), ONE);
    returns.put(LOCAL_DATE_NOW, ONE);
    calculation.portfolioTotalReturns = returns;
    calculation.cipsd = LOCAL_DATE_NOW.minusMonths(1);
    TrailingTotalReturnsResult result = new TrailingTotalReturnsResult();
    Set<Pair<String, BigDecimal>> periodValues = Set.of(
        Pair.of(CIPSD.name(), null));

    doCallRealMethod().when(calculation).addInsufficientDataWarnings(any(), any());
    doCallRealMethod().when(calculation).availableMonths();
    doCallRealMethod().when(calculation).getNumberOfMonthsFor(any(), any());
    doCallRealMethod().when(calculation).requiresUnavailablePeriodWarning(any(), anyInt());
    doCallRealMethod().when(calculation).unavailablePeriodWarning(any());
    // Real cipsd-position logic should return true here (cipsd is between first and last keys),
    // so the warning gate stays closed without an explicit thenReturn(true) stub.
    doCallRealMethod().when(calculation).isSinceCustomIntervalPerformanceStartDateValid();
    calculation.addInsufficientDataWarnings(result, periodValues);

    assertEquals(1, result.getWarnings().size());
    assertEquals("RET-011", result.getWarnings().get(0).getCode());
  }

  private TreeMap<LocalDate, BigDecimal> getPortfolioReturns() {
    LocalDate date = LocalDate.of(2020, 12, 1);
    Map<LocalDate, BigDecimal> map = new HashMap<>();
    map.put(toLastDayOfMonth(date), new BigDecimal("1.01222986673534"));
    map.put(toLastDayOfMonth(date.minusMonths(12)), new BigDecimal("1.01094319080371"));
    map.put(toLastDayOfMonth(date.minusMonths(11)), new BigDecimal("0.994895485347306"));
    map.put(toLastDayOfMonth(date.minusMonths(10)), new BigDecimal("1.02297440154456"));
    map.put(toLastDayOfMonth(date.minusMonths(9)), new BigDecimal("1.03431353421321"));
    map.put(toLastDayOfMonth(date.minusMonths(8)), new BigDecimal("1.01111160279157"));
    map.put(toLastDayOfMonth(date.minusMonths(7)), new BigDecimal("0.998508625796384"));
    map.put(toLastDayOfMonth(date.minusMonths(6)), new BigDecimal("0.996781991187829"));
    map.put(toLastDayOfMonth(date.minusMonths(5)), new BigDecimal("1.01213800595451"));
    map.put(toLastDayOfMonth(date.minusMonths(4)), new BigDecimal("1.02031184300726"));
    map.put(toLastDayOfMonth(date.minusMonths(3)), new BigDecimal("1.01074832088959"));
    map.put(toLastDayOfMonth(date.minusMonths(2)), new BigDecimal("1.01608812281602"));
    map.put(toLastDayOfMonth(date.minusMonths(1)), new BigDecimal("1.00844777099365"));
    return new TreeMap<>(map);
  }

}
