package com.fintex.ce.application.calculation.metric.core;

import com.fintex.ce.application.calculation.metric.BetaCalculation;
import com.fintex.ce.application.calculation.metric.ExcessReturnsCalculation;
import com.fintex.ce.application.calculation.metric.TrailingTotalReturnsCalculation;
import com.fintex.ce.application.util.ComparisonUtils;
import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.returns.TrailingTotalReturnsResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
import com.fintex.ce.model.dto.calculation.WeightedAverageInputDTO;
import com.fintex.ce.model.error.exceptions.CalculationException;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

import static com.fintex.ce.application.util.TestConstants.LOCAL_DATE_NOW;
import static com.fintex.ce.model.domain.enumeration.Period.SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE;
import static com.fintex.ce.model.domain.enumeration.Period.SINCE_PERFORMANCE_START_DATE;
import static com.fintex.ce.model.domain.enumeration.Period.YEAR_TO_DATE;
import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static com.fintex.ce.model.util.BigDecimalConstants.TWO;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

class PeriodCalculationAbstractTest {

  final int TWELVE = 12;

  @Test
  void shouldAddSinceCustomIntervalPeriod_whenSinceCustomIntervalStartDateIsValid() {
    final CalculationDTO w = mock(CalculationDTO.class);
    when(w.getCipsd()).thenReturn(null);
    final PeriodCalculationAbstract p = new TrailingTotalReturnsCalculation(w, Set.of());

    final Set<Pair<String, BigDecimal>> results = new HashSet<>();
    p.addSinceCustomIntervalPerformanceStartDate(results, Set.of());

    assertEquals(0, results.size());
  }

  @Test
  void shouldNotAddSinceCustomIntervalPeriod_whenPeriodAlreadyPresent() {
    final PeriodCalculationAbstract sut = mock(PeriodCalculationAbstract.class);

    when(sut.isSinceCustomIntervalPerformanceStartDateValid()).thenReturn(false);

    doCallRealMethod().when(sut).addSinceCustomIntervalPerformanceStartDate(any(), any());
    final Set<Pair<String, BigDecimal>> results = new HashSet<>();
    sut.addSinceCustomIntervalPerformanceStartDate(results, Set.of(SINCE_PERFORMANCE_START_DATE.name(), YEAR_TO_DATE
        .name(), "12"));

    assertEquals(0, results.size());
  }

  @Test
  void shouldCalculateFromCustomIntervalStartDate_whenSinceCustomIntervalPeriodRequested() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    final WeightedAverageInputDTO w = mock(WeightedAverageInputDTO.class);
    when(w.getCipsd()).thenReturn(LocalDate.now());

    when(p.isSinceCustomIntervalPerformanceStartDateValid()).thenReturn(false);

    doCallRealMethod().when(p).addSinceCustomIntervalPerformanceStartDate(any(), any());
    final Set<Pair<String, BigDecimal>> results = new HashSet<>();
    p.addSinceCustomIntervalPerformanceStartDate(results, Set.of(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name()));

    assertEquals(1, results.size());
    final Pair<String, BigDecimal> actual = results.stream().findFirst().orElseThrow();
    assertEquals(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name(), actual.getKey());
    assertNull(actual.getValue());
  }

  @Test
  void shouldCalculatePeriodForCustomIntervalStartDate_whenAddingSinceCustomIntervalPeriod() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    when(p.isSinceCustomIntervalPerformanceStartDateValid()).thenReturn(true);

    doCallRealMethod().when(p).addSinceCustomIntervalPerformanceStartDate(any(), any());
    p.addSinceCustomIntervalPerformanceStartDate(new HashSet<>(), Set.of(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE
        .name()));

    verify(p).calculatePeriodForCustomIntervalStartDate();
  }

  @Test
  void shouldAddSinceCustomIntervalResult_whenCustomIntervalValueIsCalculated() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    when(p.isSinceCustomIntervalPerformanceStartDateValid()).thenReturn(true);

    final BigDecimal one = ONE;
    when(p.calculatePeriodForCustomIntervalStartDate()).thenReturn(one);
    when(p.toUserFormat(any())).thenReturn(one);

    doCallRealMethod().when(p).addSinceCustomIntervalPerformanceStartDate(any(), any());
    final HashSet<Pair<String, BigDecimal>> resultSet = new HashSet<>();
    p.addSinceCustomIntervalPerformanceStartDate(resultSet, Set.of(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE
        .name()));

    assertEquals(Set.of(Pair.of(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name(), one)), resultSet);
  }

  @Test
  void shouldDelegateToCalculatePeriodForNumberOfMonths_whenCalculatingSinceCustomInterval() {
    final var inputDTO = mock(CalculationDTO.class);
    when(inputDTO.getCipsd()).thenReturn(LOCAL_DATE_NOW);
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class,
        withSettings().useConstructor(inputDTO, null));

    p.portfolioTotalReturns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE, LOCAL_DATE_NOW.plusMonths(2), ONE));

    doCallRealMethod().when(p).calculatePeriodForCustomIntervalStartDate();
    p.calculatePeriodForCustomIntervalStartDate();

    verify(p).calculatePeriodForNumberOfMonths(3);
  }

  @Test
  void shouldReturnProvidedMonths_whenPeriodIsNumeric() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    final NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE));
    final String period = "3";

    doCallRealMethod().when(p).getNumberOfMonthsFor(any(), any());
    final int actual = p.getNumberOfMonthsFor(returns, period);

    assertEquals(3, actual);
  }

  @Test
  void shouldCalculateMonthsFromYearStart_whenPeriodIsYearToDate() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    final NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE));
    final String period = YEAR_TO_DATE.name();

    final int months = 10;
    when(p.getNumberOfMonthsForYearToDate(any())).thenReturn(months);

    doCallRealMethod().when(p).getNumberOfMonthsFor(any(), any());
    final int actual = p.getNumberOfMonthsFor(returns, period);

    assertEquals(months, actual);
    verify(p).getNumberOfMonthsForYearToDate(returns);
  }

  @Test
  void shouldCalculateMonthsFromInception_whenPeriodIsSinceInception() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    final NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE));
    final String period = SINCE_PERFORMANCE_START_DATE.name();

    final int months = 15;
    when(p.getNumberOfMonthsForSinceInception(any())).thenReturn(months);

    doCallRealMethod().when(p).getNumberOfMonthsFor(any(), any());
    final int actual = p.getNumberOfMonthsFor(returns, period);

    assertEquals(months, actual);
    verify(p).getNumberOfMonthsForSinceInception(returns);
  }

  @Test
  void shouldReturnNull_whenPeriodFormatIsUnsupported() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    final NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE));
    final String period = "TEST";

    doCallRealMethod().when(p).getNumberOfMonthsFor(any(), any());
    final CalculationException e = assertThrows(CalculationException.class, () -> p.getNumberOfMonthsFor(returns,
        period));

    assertTrue(e.getMessage().contains(period));
  }

  @Test
  void shouldDelegateToCalculatePeriods_whenCalculateIsCalled() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    final Set<String> periods = Set.of("1");

    doCallRealMethod().when(p).calculate(any());
    p.calculate(periods);

    verify(p).calculatePeriods(periods);
  }

  @Test
  void shouldAddSinceCustomIntervalPeriod_whenCalculatingPeriods() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    final String interval = "12";
    final Set<String> periods = Set.of(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name(), interval);

    doCallRealMethod().when(p).getInitialPeriods(any());
    doCallRealMethod().when(p).calculatePeriods(any());
    p.calculatePeriods(periods);

    verify(p).calculateForPeriod(interval);
    verify(p, times(0)).calculateForPeriod(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name());
  }

  @Test
  void shouldDelegateToDefineResponseType_whenCalculateIsCalled() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    final Set<String> periods = Set.of("1");
    final Set<Object> periodsR = Set.of(mock(Object.class));

    when(p.calculatePeriods(any())).thenReturn(periodsR);

    doCallRealMethod().when(p).calculate(any());
    p.calculate(periods);

    verify(p).defineResponseType(periodsR);
  }

  @Test
  void shouldReturnDefaultPeriods_whenInputPeriodsEmpty() {
    final Set<String> periods = Set.of("3");
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class,
        withSettings().useConstructor(mock(CalculationDTO.class), periods));

    doCallRealMethod().when(p).getInitialPeriods(any());
    final Set actual = p.getInitialPeriods(Set.of());

    assertEquals(periods, actual);
  }

  @Test
  void shouldReturnInputPeriods_whenInputPeriodsProvided() {
    final Set<String> periods = Set.of("3");
    final PeriodCalculationAbstract sut = mock(PeriodCalculationAbstract.class,
        withSettings().useConstructor(mock(CalculationDTO.class), periods));

    doCallRealMethod().when(sut).getInitialPeriods(any());
    final Set<String> userP = Set.of("5");
    final Set actual = sut.getInitialPeriods(userP);

    assertEquals(userP, actual);
  }

  @Test
  void shouldReturnNull_whenPeriodExceedsPortfolioSizeForProduct() {
    final var sut = mock(PeriodCalculationAbstract.class);

    final TreeMap<LocalDate, BigDecimal> aReturns = new TreeMap<>(Map.of(toLastDayOfMonth(LOCAL_DATE_NOW), ONE));

    doCallRealMethod().when(sut).filterRequiredMonthsForPeriod(1, aReturns);

    doCallRealMethod().when(sut).calculateProductForPeriod(eq(1), any());
    final BigDecimal actual = sut.calculateProductForPeriod(1, aReturns);

    assertEquals(0, ONE.compareTo(actual));
  }

  @Test
  void shouldCalculateProductForRequestedPeriod_whenEnoughDataExists() {
    final PeriodCalculationAbstract sut = mock(PeriodCalculationAbstract.class);

    final TreeMap<LocalDate, BigDecimal> aReturns = new TreeMap<>(Map.of(
        toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(2)), ONE,
        toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(3)), BigDecimal.valueOf(5),
        toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), TEN,
        toLastDayOfMonth(LOCAL_DATE_NOW), BigDecimal.valueOf(2)));

    doCallRealMethod().when(sut).filterRequiredMonthsForPeriod(3, aReturns);

    doCallRealMethod().when(sut).calculateProductForPeriod(eq(3), any());
    final BigDecimal actual = sut.calculateProductForPeriod(3, aReturns);

    assertEquals(0, BigDecimal.valueOf(20).compareTo(actual));
  }

  @Test
  void shouldCalculateBenchmarkProductForRequestedPeriod_whenEnoughDataExists() {
    final var sut = mock(PeriodCalculationAbstract.class);

    final var portfolioTotalReturns = new TreeMap<LocalDate, BigDecimal>(Map.of(toLastDayOfMonth(LOCAL_DATE_NOW
        .minusMonths(2)), BigDecimal.valueOf(5)));
    final var aReturns = new TreeMap<LocalDate, BigDecimal>(Map.of(
        toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(3)), ONE,
        toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(2)), BigDecimal.valueOf(11),
        toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), TEN,
        toLastDayOfMonth(LOCAL_DATE_NOW), BigDecimal.valueOf(2)));

    when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);
    doCallRealMethod().when(sut).filterRequiredMonthsForPeriod(3, portfolioTotalReturns);

    doCallRealMethod().when(sut).getBenchmarkValues(eq(3), any());
    final List list = sut.getBenchmarkValues(3, aReturns);

    assertEquals(List.of(BigDecimal.valueOf(11)), list);
  }

  @Test
  void shouldPopulateBasicDetails_whenCalculateIsCalled() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    final Set<String> periods = Set.of("1");
    final PeriodResult resDTO = mock(PeriodResult.class);
    when(p.defineResponseType(any())).thenReturn(resDTO);

    doCallRealMethod().when(p).calculate(any());
    final PeriodResult actual = p.calculate(periods);

    verify(p).populateBasicDetails(resDTO);
    assertEquals(resDTO, actual);
  }

  @Test
  void shouldReadInitialPeriods_whenCalculatingPeriods() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    when(p.getInitialPeriods(any())).thenReturn(Set.of());

    final Set<String> periods = Set.of("2");

    doCallRealMethod().when(p).calculatePeriods(any());
    p.calculatePeriods(periods);

    verify(p).getInitialPeriods(periods);
  }

  @Test
  void shouldCalculateValueForEachPeriod_whenCalculatingPeriods() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    final String period = "2";
    final Set<String> periods = Set.of(period);

    when(p.getInitialPeriods(any())).thenReturn(periods);

    doCallRealMethod().when(p).calculatePeriods(any());
    p.calculatePeriods(periods);

    verify(p).calculateForPeriod(period);
  }

  @Test
  void shouldAppendSinceCustomIntervalPeriod_whenCalculatingPeriods() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    final String period = "2";
    final Set<String> periods = Set.of(period);
    final Pair<String, BigDecimal> pair = Pair.of(period, ONE);

    when(p.getInitialPeriods(any())).thenReturn(periods);
    when(p.calculateForPeriod(any())).thenReturn(pair);

    doCallRealMethod().when(p).calculatePeriods(any());
    final Set actual = p.calculatePeriods(periods);

    verify(p).addSinceCustomIntervalPerformanceStartDate(Set.of(pair), periods);
    assertEquals(Set.of(pair), actual);
  }

  @Test
  void shouldDelegateToCalculatePeriodForNumberOfMonths_whenCalculatingForPeriod() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);
    p.portfolioTotalReturns = new TreeMap();

    String period = "3 ";

    doCallRealMethod().when(p).calculateForPeriod(any());
    p.calculateForPeriod(period);

    verify(p).getNumberOfMonthsFor(argThat(argument -> argument == p.portfolioTotalReturns), eq(period.trim()));
  }

  @Test
  void shouldResolveNumberOfMonths_whenCalculatingForPeriod() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    final int months = 10;
    when(p.getNumberOfMonthsFor(any(), any())).thenReturn(months);

    doCallRealMethod().when(p).calculateForPeriod(any());
    p.calculateForPeriod("32");

    verify(p).calculatePeriodForNumberOfMonths(months);
  }

  @Test
  void shouldReturnPeriodPair_whenCalculationSucceeds() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    final BigDecimal one = ONE;
    when(p.calculatePeriodForNumberOfMonths(0)).thenReturn(one);
    final String period = "32";

    when(p.toUserFormat(any())).thenReturn(one);
    doCallRealMethod().when(p).calculateForPeriod(any());
    final Pair actual = p.calculateForPeriod(period);

    assertEquals(Pair.of(period, toUserScale(one)), actual);
  }

  @Test
  void shouldRoundValuesToUserFormat_whenFormattingResult() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    final BigDecimal one = ONE;

    doCallRealMethod().when(p).toUserFormat(any());

    final BigDecimal actual = (BigDecimal) p.toUserFormat(one);

    assertEquals(toUserScale(one), actual);
  }

  @Test
  void shouldPopulateDateDetails_whenSettingPeriodDates() {
    final var inputDTO = mock(CalculationDTO.class);
    when(inputDTO.getCipsd()).thenReturn(LOCAL_DATE_NOW);

    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class,
        withSettings().useConstructor(inputDTO, Set.of()));

    final TreeMap kvTreeMap = new TreeMap<>(Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE, LOCAL_DATE_NOW.plusMonths(1),
        ONE));
    p.portfolioTotalReturns = kvTreeMap;

    doCallRealMethod().when(p).populateBasicDetails(any());
    final TrailingTotalReturnsResult actual = new TrailingTotalReturnsResult();
    p.populateBasicDetails(actual);

    assertEquals(LOCAL_DATE_NOW, actual.getCustomIpsd());
    assertEquals(LOCAL_DATE_NOW.minusMonths(1), actual.getPsd());
    assertEquals(LOCAL_DATE_NOW.plusMonths(1), actual.getPed());
  }

  @ParameterizedTest
  @CsvSource({
      "2020, 4, 30",
      "2020, 1, 31",
      "2020, 12, 31",
  })
  void shouldReturnMonthIndex_whenCalculatingNumberOfMonthsForYearToDate(int year, int month, int dayOfMonth) {
    final PeriodCalculationAbstract t = mock(PeriodCalculationAbstract.class);
    doCallRealMethod().when(t).getNumberOfMonthsForYearToDate(anyMap());

    final Map<LocalDate, BigDecimal> aReturns = Map.of(LocalDate.of(year, month, dayOfMonth), ONE);

    final int actual = t.getNumberOfMonthsForYearToDate(aReturns);

    assertEquals(month, actual);
  }

  @Test
  void shouldCalculateYearToDateMonths_whenCurrentDateAfterYearStart() {
    final PeriodCalculationAbstract t = mock(PeriodCalculationAbstract.class);
    doCallRealMethod().when(t).getNumberOfMonthsForSinceInception(any(NavigableMap.class));

    final NavigableMap<LocalDate, BigDecimal> aReturns = new TreeMap<>(Map.of(LocalDate.of(2020, 4, 30), ONE));

    final int actual = t.getNumberOfMonthsForSinceInception(aReturns);

    assertEquals(1, actual);
  }

  @Test
  void shouldCalculateYearToDateMonths_whenCurrentDateInJanuary() {
    final PeriodCalculationAbstract t = mock(PeriodCalculationAbstract.class);
    doCallRealMethod().when(t).getNumberOfMonthsForSinceInception(any(NavigableMap.class));

    final NavigableMap<LocalDate, BigDecimal> aReturns = new TreeMap<>(Map.of(
        toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), ONE,
        toLastDayOfMonth(LOCAL_DATE_NOW), ONE));

    final int actual = t.getNumberOfMonthsForSinceInception(aReturns);

    assertEquals(2, actual);
  }

  @Test
  void shouldAddSinceCustomIntervalPeriod_whenCustomStartDateAfterPortfolioStart() {
    final var input = mock(CalculationDTO.class);
    when(input.getCipsd()).thenReturn(LOCAL_DATE_NOW);
    final Map<LocalDate, BigDecimal> returns = Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE);
    final var portfolioTotalReturns = new TreeMap<>(returns);
    when(input.getWeightedAveragePortfolioReturns()).thenReturn(portfolioTotalReturns);

    final var sut = mock(PeriodCalculationAbstract.class,
        withSettings().useConstructor(input, null));

    doCallRealMethod().when(sut).isSinceCustomIntervalPerformanceStartDateValid();
    final boolean actual = sut.isSinceCustomIntervalPerformanceStartDateValid();

    assertFalse(actual);
  }

  @Test
  void shouldAddSinceCustomIntervalPeriod_whenCustomStartDateBeforePortfolioStart() {
    final var input = mock(CalculationDTO.class);
    when(input.getCipsd()).thenReturn(LOCAL_DATE_NOW);
    final Map<LocalDate, BigDecimal> returns = Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE);
    final var portfolioTotalReturns = new TreeMap<>(returns);
    when(input.getWeightedAveragePortfolioReturns()).thenReturn(portfolioTotalReturns);

    final var sut = mock(PeriodCalculationAbstract.class, withSettings().useConstructor(input, null));

    doCallRealMethod().when(sut).isSinceCustomIntervalPerformanceStartDateValid();
    final boolean actual = sut.isSinceCustomIntervalPerformanceStartDateValid();

    assertFalse(actual);
  }

  @Test
  void shouldAddSinceCustomIntervalPeriod_whenCustomStartDateEqualsPortfolioStart() {
    final var input = mock(CalculationDTO.class);
    final var sut = mock(PeriodCalculationAbstract.class, withSettings().useConstructor(input, null));
    final Map<LocalDate, BigDecimal> returns = Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE, LOCAL_DATE_NOW.plusMonths(1),
        TWO);
    sut.cipsd = LOCAL_DATE_NOW;
    sut.portfolioTotalReturns = new TreeMap<>(returns);

    doCallRealMethod().when(sut).isSinceCustomIntervalPerformanceStartDateValid();
    final boolean actual = sut.isSinceCustomIntervalPerformanceStartDateValid();

    assertTrue(actual);
  }

  @Test
  void shouldReturnPeriodStartDate_whenOffsetProvided() {
    final ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
    doCallRealMethod().when(excessReturnsCalculation).getPeriodStartDate(anyInt(), any());
    final LocalDate periodStartDate = excessReturnsCalculation.getPeriodStartDate(12, getPortfolioReturns());

    assertEquals(toLastDayOfMonth(LocalDate.of(2020, 12, 1).minusMonths(11)), periodStartDate);
  }

  @Test
  void shouldReturnSubMapFromStartDate_whenFilteringReturns() {
    final ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
    doCallRealMethod().when(excessReturnsCalculation).getSubMapByPeriodStartDate(any(), any());
    final SortedMap<LocalDate, BigDecimal> subMap = excessReturnsCalculation
        .getSubMapByPeriodStartDate(toLastDayOfMonth(LocalDate.of(2020, 12, 1).minusMonths(2)), getPortfolioReturns());

    assertEquals(3, subMap.size());
    assertEquals(toLastDayOfMonth(LocalDate.of(2020, 12, 1)), subMap.lastKey());
    assertEquals(toLastDayOfMonth(LocalDate.of(2020, 10, 1)), subMap.firstKey());
  }

  @Test
  void shouldConvertTotalReturnsToMonthlyChanges_whenOverridingReturns() {
    final BetaCalculation betaCalculation = mock(BetaCalculation.class);
    final LocalDate date = LocalDate.of(2020, 12, 1);
    final TreeMap<LocalDate, BigDecimal> portfolioTotalReturns = new TreeMap<>(Map.of(toLastDayOfMonth(date), BigDecimal
        .valueOf(1.01094319080371),
        toLastDayOfMonth(date.minusMonths(1)), BigDecimal.valueOf(1.02297440154456)));
    when(betaCalculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);
    doCallRealMethod().when(betaCalculation).overrideTotalReturns(any());

    final NavigableMap<LocalDate, BigDecimal> totalReturns = betaCalculation.overrideTotalReturns(
        portfolioTotalReturns);

    assertEquals(2, totalReturns.size());
    assertEquals(toUserScale(BigDecimal.valueOf(0.02297440154456)), toUserScale(totalReturns.firstEntry().getValue()));
    assertEquals(toUserScale(BigDecimal.valueOf(0.01094319080371)), toUserScale(totalReturns.lastEntry().getValue()));
  }

  @Test
  void shouldCreateTimeIntervalResults_whenFormattingPairs() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

    final Set<TimeIntervalResult> expected = Set.of(new TimeIntervalResult("2000-01-12", ONE));

    final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2000-01-12", ONE));

    doCallRealMethod().when(p).formTimeIntervalResult(anySet());
    final Set actual = p.formTimeIntervalResult(pairs);

    assertEquals(expected, actual);
  }

  @Test
  void shouldUseSubMapFromPeriodStartDate_whenCalculatingAverageArithmeticAnnualizedReturn() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);
    final TreeMap treeMap = mock(TreeMap.class);
    final LocalDate date = LocalDate.now();

    doCallRealMethod().when(p).calculateAverageArithmeticAnnualizedReturn(any(), any(), anyInt());

    p.calculateAverageArithmeticAnnualizedReturn(treeMap, date, TWELVE);

    verify(p).getSubMapByPeriodStartDate(date, treeMap);
  }

  @Test
  void shouldCalculateAverageArithmeticAnnualizedReturn_whenSubMapContainsReturns() {
    final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);
    final TreeMap treeMap = mock(TreeMap.class);
    final LocalDate date = LocalDate.now();
    final TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(date, ONE, date.plusMonths(1), TWO));

    when(p.getSubMapByPeriodStartDate(any(), any())).thenReturn(returns);
    doCallRealMethod().when(p).calculateAverageArithmeticAnnualizedReturn(any(), any(), anyInt());

    final BigDecimal returnValue = p.calculateAverageArithmeticAnnualizedReturn(treeMap, date, TWELVE);

    assertEquals(toUserScale(BigDecimal.valueOf(3)), toUserScale(returnValue));
  }

  @Test
  void shouldRestrictTBillsRangeToReturnsWindow_whenBothInputsProvided() {
    final var sut = mock(PeriodCalculationAbstract.class);
    final LocalDate date = LocalDate.now();
    final TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(date, ONE, date.plusMonths(1), TWO));
    final TreeMap<LocalDate, BigDecimal> tBills = new TreeMap<>(Map.of(
        date.minusMonths(1), ZERO,
        date, ONE,
        date.plusMonths(1), TWO,
        date.plusMonths(2), TEN));
    final var expected = new TreeMap<>(Map.of(
        date, ONE,
        date.plusMonths(1), TWO));

    doCallRealMethod().when(sut).restrictTBillsRange(any(), any());
    final NavigableMap<LocalDate, BigDecimal> actual = sut.restrictTBillsRange(tBills, returns);

    ComparisonUtils.compareMaps(expected, actual);
    assertEquals(expected.size(), actual.size());
  }

  @Test
  void shouldRestrictTBillsRangeUsingPortfolioReturns_whenOnlyTBillsProvided() {
    final var sut = mock(PeriodCalculationAbstract.class);
    final LocalDate date = LocalDate.now();
    final TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(date, ONE, date.plusMonths(1), TWO));
    final TreeMap<LocalDate, BigDecimal> tBills = new TreeMap<>(Map.of(
        date.minusMonths(1), ZERO,
        date, ONE,
        date.plusMonths(1), TWO,
        date.plusMonths(2), TEN));
    sut.portfolioTotalReturns = returns;

    final var expected = new TreeMap<>(Map.of(
        date, ONE,
        date.plusMonths(1), TWO));

    doCallRealMethod().when(sut).restrictTBillsRange(any());
    doCallRealMethod().when(sut).restrictTBillsRange(any(), any());
    final NavigableMap<LocalDate, BigDecimal> actual = sut.restrictTBillsRange(tBills);

    ComparisonUtils.compareMaps(expected, actual);
    assertEquals(expected.size(), actual.size());
  }

  private TreeMap<LocalDate, BigDecimal> getPortfolioReturns() {
    final LocalDate date = LocalDate.of(2020, 12, 1);
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