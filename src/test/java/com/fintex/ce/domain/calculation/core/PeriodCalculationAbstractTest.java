package com.fintex.ce.domain.calculation.core;

import com.fintex.ce.config.constant.BigDecimalConstants;
import com.fintex.ce.domain.calculation.BetaCalculation;
import com.fintex.ce.domain.calculation.ExcessReturnsCalculation;
import com.fintex.ce.domain.calculation.TrailingTotalReturnsCalculation;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.calculation.WeightedAverageInputDTO;
import com.fintex.ce.dto.response.TrailingTotalReturnsResDTO;
import com.fintex.ce.dto.response.core.PeriodResDTO;
import com.fintex.ce.dto.response.core.TimeIntervalResDTO;
import com.fintex.ce.exception.ReqValidationException;
import com.fintex.ce.util.ComparisonUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
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

import static com.fintex.ce.config.constant.BigDecimalConstants.TWO;
import static com.fintex.ce.config.enumeration.Period.*;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.TestConstants.LOCAL_DATE_NOW;
import static java.math.BigDecimal.*;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

class PeriodCalculationAbstractTest {

    final int TWELVE = 12;

    @Test
    void addSinceCustomIntervalPerformanceStartDate_verifyIsSinceCustomIntervalPerformanceStartDateValid() {
        //SETUP
        final CalculationDTO w = mock(CalculationDTO.class);
        when(w.getCipsd()).thenReturn(null);
        final PeriodCalculationAbstract p = new TrailingTotalReturnsCalculation(w, Set.of());

        //ACT
        final Set<Pair<String, BigDecimal>> results = new HashSet<>();
        p.addSinceCustomIntervalPerformanceStartDate(results, Set.of());

        //VERIFY
        assertEquals(0, results.size());
    }

    @Test
    void addSinceCustomIntervalPerformanceStartDate_verifyPeriodIsNotPresent() {
        //SETUP
        final PeriodCalculationAbstract sut = mock(PeriodCalculationAbstract.class);

        when(sut.isSinceCustomIntervalPerformanceStartDateValid()).thenReturn(false);

        doCallRealMethod().when(sut).addSinceCustomIntervalPerformanceStartDate(any(), any());
        //ACT
        final Set<Pair<String, BigDecimal>> results = new HashSet<>();
        sut.addSinceCustomIntervalPerformanceStartDate(results, Set.of(SINCE_PERFORMANCE_START_DATE.name(), YEAR_TO_DATE.name(), "12"));

        //VERIFY
        assertEquals(0, results.size());
    }

    @Test
    void addSinceCustomIntervalPerformanceStartDate_nullIsUsed() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

        final WeightedAverageInputDTO w = mock(WeightedAverageInputDTO.class);
        when(w.getCipsd()).thenReturn(LocalDate.now());

        when(p.isSinceCustomIntervalPerformanceStartDateValid()).thenReturn(false);

        doCallRealMethod().when(p).addSinceCustomIntervalPerformanceStartDate(any(), any());
        //ACT
        final Set<Pair<String, BigDecimal>> results = new HashSet<>();
        p.addSinceCustomIntervalPerformanceStartDate(results, Set.of(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name()));

        //VERIFY
        assertEquals(1, results.size());
        final Pair<String, BigDecimal> actual = results.stream().findFirst().orElseThrow();
        assertEquals(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name(), actual.getKey());
        assertNull(actual.getValue());
    }

    @Test
    void addSinceCustomIntervalPerformanceStartDate_verifyCalculatePeriodForCustomIntervalStartDate() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

        when(p.isSinceCustomIntervalPerformanceStartDateValid()).thenReturn(true);

        doCallRealMethod().when(p).addSinceCustomIntervalPerformanceStartDate(any(), any());
        //ACT
        p.addSinceCustomIntervalPerformanceStartDate(new HashSet<>(), Set.of(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name()));

        //VERIFY
        verify(p).calculatePeriodForCustomIntervalStartDate();
    }

    @Test
    void addSinceCustomIntervalPerformanceStartDate_checkResult() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

        when(p.isSinceCustomIntervalPerformanceStartDateValid()).thenReturn(true);

        final BigDecimal one = ONE;
        when(p.calculatePeriodForCustomIntervalStartDate()).thenReturn(one);
        when(p.toUserFormat(any())).thenReturn(one);

        doCallRealMethod().when(p).addSinceCustomIntervalPerformanceStartDate(any(), any());
        //ACT
        final HashSet<Pair<String, BigDecimal>> resultSet = new HashSet<>();
        p.addSinceCustomIntervalPerformanceStartDate(resultSet, Set.of(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name()));

        //VERIFY
        assertEquals(Set.of(Pair.of(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name(), one)), resultSet);
    }

    @Test
    void calculatePeriodForCustomIntervalStartDate_verifyCalculatePeriodForNumberOfMonths() {
        //SETUP
        final var inputDTO = mock(CalculationDTO.class);
        when(inputDTO.getCipsd()).thenReturn(LOCAL_DATE_NOW);
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class,
                withSettings().useConstructor(inputDTO, null));

        p.portfolioTotalReturns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE, LOCAL_DATE_NOW.plusMonths(2), ONE));

        doCallRealMethod().when(p).calculatePeriodForCustomIntervalStartDate();
        //ACT
        p.calculatePeriodForCustomIntervalStartDate();

        //VERIFY
        verify(p).calculatePeriodForNumberOfMonths(3);
    }

    @Test
    void getNumberOfMonthsFor_checkForNumber() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

        final NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE));
        final String period = "3";

        doCallRealMethod().when(p).getNumberOfMonthsFor(any(), any());
        //ACT
        final int actual = p.getNumberOfMonthsFor(returns, period);

        //VERIFY
        assertEquals(3, actual);
    }

    @Test
    void getNumberOfMonthsFor_checkForYearToDay() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

        final NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE));
        final String period = YEAR_TO_DATE.name();

        final int months = 10;
        when(p.getNumberOfMonthsForYearToDate(any())).thenReturn(months);

        doCallRealMethod().when(p).getNumberOfMonthsFor(any(), any());
        //ACT
        final int actual = p.getNumberOfMonthsFor(returns, period);

        //VERIFY
        assertEquals(months, actual);
        verify(p).getNumberOfMonthsForYearToDate(returns);
    }

    @Test
    void getNumberOfMonthsFor_checkForSinceInceptionDay() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

        final NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE));
        final String period = SINCE_PERFORMANCE_START_DATE.name();

        final int months = 15;
        when(p.getNumberOfMonthsForSinceInception(any())).thenReturn(months);

        doCallRealMethod().when(p).getNumberOfMonthsFor(any(), any());
        //ACT
        final int actual = p.getNumberOfMonthsFor(returns, period);

        //VERIFY
        assertEquals(months, actual);
        verify(p).getNumberOfMonthsForSinceInception(returns);
    }

    @Test
    void getNumberOfMonthsFor_checkForIncorrect() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

        final NavigableMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE));
        final String period = "TEST";

        doCallRealMethod().when(p).getNumberOfMonthsFor(any(), any());
        //ACT
        final ReqValidationException e = assertThrows(ReqValidationException.class, () -> p.getNumberOfMonthsFor(returns, period));

        //VERIFY
        assertTrue(e.getMessage().contains(period));
    }

    @Test
    void calculate_verifyCalculatePeriods() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

        final Set<String> periods = Set.of("1");

        doCallRealMethod().when(p).calculate(any());
        //ACT
        p.calculate(periods);

        //VERIFY
        verify(p).calculatePeriods(periods);
    }

    @Test
    void calculatePeriods_verifyCIPSD() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

        final String interval = "12";
        final Set<String> periods = Set.of(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name(), interval);

        doCallRealMethod().when(p).getInitialPeriods(any());
        doCallRealMethod().when(p).calculatePeriods(any());
        //ACT
        p.calculatePeriods(periods);

        //VERIFY
        verify(p).calculateForPeriod(interval);
        verify(p, times(0)).calculateForPeriod(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name());
    }

    @Test
    void calculate_verifyDefineResponseType() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

        final Set<String> periods = Set.of("1");
        final Set<Object> periodsR = Set.of(mock(Object.class));

        when(p.calculatePeriods(any())).thenReturn(periodsR);

        doCallRealMethod().when(p).calculate(any());
        //ACT
        p.calculate(periods);

        //VERIFY
        verify(p).defineResponseType(periodsR);
    }

    @Test
    void getInitialPeriods_checkResultDefault() {
        //SETUP
        final Set<String> periods = Set.of("3");
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class,
                withSettings().useConstructor(mock(CalculationDTO.class), periods));

        doCallRealMethod().when(p).getInitialPeriods(any());
        //ACT
        final Set actual = p.getInitialPeriods(Set.of());

        //VERIFY
        assertEquals(periods, actual);
    }

    @Test
    void getInitialPeriods_checkResultUserEntered() {
        //SETUP
        final Set<String> periods = Set.of("3");
        final PeriodCalculationAbstract sut = mock(PeriodCalculationAbstract.class,
                withSettings().useConstructor(mock(CalculationDTO.class), periods));

        doCallRealMethod().when(sut).getInitialPeriods(any());
        //ACT
        final Set<String> userP = Set.of("5");
        final Set actual = sut.getInitialPeriods(userP);

        //VERIFY
        assertEquals(userP, actual);
    }

    @Test
    void calculateProductForPeriod_verifyForPeriod1() {
        //SETUP
        final var sut = mock(PeriodCalculationAbstract.class);

        final TreeMap<LocalDate, BigDecimal> aReturns = new TreeMap<>(Map.of(toLastDayOfMonth(LOCAL_DATE_NOW), ONE));

        doCallRealMethod().when(sut).filterRequiredMonthsForPeriod(1, aReturns);

        doCallRealMethod().when(sut).calculateProductForPeriod(eq(1), any());
        //ACT
        final BigDecimal actual = sut.calculateProductForPeriod(1, aReturns);

        //VERIFY
        assertEquals(0, ONE.compareTo(actual));
    }

    @Test
    void calculateProductForPeriod_verifyForPeriod3() {
        //SETUP
        final PeriodCalculationAbstract sut = mock(PeriodCalculationAbstract.class);

        final TreeMap<LocalDate, BigDecimal> aReturns = new TreeMap<>(Map.of(
                toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(2)), ONE,
                toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(3)), BigDecimal.valueOf(5),
                toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), TEN,
                toLastDayOfMonth(LOCAL_DATE_NOW), BigDecimal.valueOf(2)));

        doCallRealMethod().when(sut).filterRequiredMonthsForPeriod(3, aReturns);

        doCallRealMethod().when(sut).calculateProductForPeriod(eq(3), any());
        //ACT
        final BigDecimal actual = sut.calculateProductForPeriod(3, aReturns);

        //VERIFY
        assertEquals(0, BigDecimal.valueOf(20).compareTo(actual));
    }

    @Test
    void calculateBenchmarkProductForPeriod_verifyForPeriod3() {
        //SETUP
        final var sut = mock(PeriodCalculationAbstract.class);

        final var portfolioTotalReturns = new TreeMap<LocalDate, BigDecimal>(Map.of(toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(2)), BigDecimal.valueOf(5)));
        final var aReturns = new TreeMap<LocalDate, BigDecimal>(Map.of(
                toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(3)), ONE,
                toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(2)), BigDecimal.valueOf(11),
                toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), TEN,
                toLastDayOfMonth(LOCAL_DATE_NOW), BigDecimal.valueOf(2)));

        when(sut.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);
        doCallRealMethod().when(sut).filterRequiredMonthsForPeriod(3, portfolioTotalReturns);

        doCallRealMethod().when(sut).getBenchmarkValues(eq(3), any());
        //ACT
        final List list = sut.getBenchmarkValues(3, aReturns);

        //VERIFY
        assertEquals(List.of(BigDecimal.valueOf(11)), list);
    }

    @Test
    void calculate_verifyPopulateBasicDetails() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

        final Set<String> periods = Set.of("1");
        final PeriodResDTO resDTO = mock(PeriodResDTO.class);
        when(p.defineResponseType(any())).thenReturn(resDTO);

        doCallRealMethod().when(p).calculate(any());
        //ACT
        final PeriodResDTO actual = p.calculate(periods);

        //VERIFY
        verify(p).populateBasicDetails(resDTO);
        assertEquals(resDTO, actual);
    }

    @Test
    void calculatePeriods_verifyGetInitialPeriods() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

        when(p.getInitialPeriods(any())).thenReturn(Set.of());

        final Set<String> periods = Set.of("2");

        doCallRealMethod().when(p).calculatePeriods(any());
        //ACT
        p.calculatePeriods(periods);

        //VERIFY
        verify(p).getInitialPeriods(periods);
    }

    @Test
    void calculatePeriods_verifyCalculateForPeriod() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

        final String period = "2";
        final Set<String> periods = Set.of(period);

        when(p.getInitialPeriods(any())).thenReturn(periods);


        doCallRealMethod().when(p).calculatePeriods(any());
        //ACT
        p.calculatePeriods(periods);

        //VERIFY
        verify(p).calculateForPeriod(period);
    }

    @Test
    void calculatePeriods_verifyAddSinceCustomIntervalPerformanceStartDate() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

        final String period = "2";
        final Set<String> periods = Set.of(period);
        final Pair<String, BigDecimal> pair = Pair.of(period, ONE);

        when(p.getInitialPeriods(any())).thenReturn(periods);
        when(p.calculateForPeriod(any())).thenReturn(pair);

        doCallRealMethod().when(p).calculatePeriods(any());
        //ACT
        final Set actual = p.calculatePeriods(periods);

        //VERIFY
        verify(p).addSinceCustomIntervalPerformanceStartDate(Set.of(pair), periods);
        assertEquals(Set.of(pair), actual);
    }

    @Test
    void calculateForPeriod_verifyCalculatePeriodForNumberOfMonths() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);
        p.portfolioTotalReturns = new TreeMap();

        String period = "3 ";

        doCallRealMethod().when(p).calculateForPeriod(any());
        //ACT
        p.calculateForPeriod(period);

        //VERIFY
        verify(p).getNumberOfMonthsFor(argThat(argument -> argument == p.portfolioTotalReturns), eq(period.trim()));
    }

    @Test
    void calculateForPeriod_verifyGetNumberOfMonthsFor() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

        final int months = 10;
        when(p.getNumberOfMonthsFor(any(), any())).thenReturn(months);

        doCallRealMethod().when(p).calculateForPeriod(any());
        //ACT
        p.calculateForPeriod("32");

        //VERIFY
        verify(p).calculatePeriodForNumberOfMonths(months);
    }

    @Test
    void calculateForPeriod_checkResult() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

        final BigDecimal one = ONE;
        when(p.calculatePeriodForNumberOfMonths(0)).thenReturn(one);
        final String period = "32";

        when(p.toUserFormat(any())).thenReturn(one);
        doCallRealMethod().when(p).calculateForPeriod(any());
        //ACT
        final Pair actual = p.calculateForPeriod(period);

        //VERIFY
        assertEquals(Pair.of(period, toUserScale(one)), actual);
    }

    @Test
    void toUserFormat_checkResult() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

        final BigDecimal one = ONE;

        doCallRealMethod().when(p).toUserFormat(any());
        //ACT

        final BigDecimal actual = (BigDecimal) p.toUserFormat(one);

        //VERIFY
        assertEquals(toUserScale(one), actual);
    }

    @Test
    void populateDatesDetails_fieldsSetProperly() {
        //SETUP
        final var inputDTO = mock(CalculationDTO.class);
        when(inputDTO.getCipsd()).thenReturn(LOCAL_DATE_NOW);

        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class,
                withSettings().useConstructor(inputDTO, Set.of()));

        final TreeMap kvTreeMap = new TreeMap<>(Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE, LOCAL_DATE_NOW.plusMonths(1), ONE));
        p.portfolioTotalReturns = kvTreeMap;

        doCallRealMethod().when(p).populateBasicDetails(any());
        //ACT
        final TrailingTotalReturnsResDTO actual = new TrailingTotalReturnsResDTO();
        p.populateBasicDetails(actual);

        //VERIFY
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
    void getNumberOfMonthsForYearToDate(int year, int month, int dayOfMonth) {
        //SETUP
        final PeriodCalculationAbstract t = mock(PeriodCalculationAbstract.class);
        doCallRealMethod().when(t).getNumberOfMonthsForYearToDate(anyMap());

        final Map<LocalDate, BigDecimal> aReturns = Map.of(LocalDate.of(year, month, dayOfMonth), ONE);

        //ACT
        final int actual = t.getNumberOfMonthsForYearToDate(aReturns);

        //VERIFY
        assertEquals(month, actual);
    }

    @Test
    void getNumberOfMonthsForYearToDate_validateGetNumberOfMonthsForSinceInception() {
        //SETUP
        final PeriodCalculationAbstract t = mock(PeriodCalculationAbstract.class);
        doCallRealMethod().when(t).getNumberOfMonthsForSinceInception(any(NavigableMap.class));

        final NavigableMap<LocalDate, BigDecimal> aReturns = new TreeMap<>(Map.of(LocalDate.of(2020, 4, 30), ONE));

        //ACT
        final int actual = t.getNumberOfMonthsForSinceInception(aReturns);

        //VERIFY
        assertEquals(1, actual);
    }

    @Test
    void getNumberOfMonthsForYearToDate_validateGetNumberOfMonthsForSinceInception2() {
        //SETUP
        final PeriodCalculationAbstract t = mock(PeriodCalculationAbstract.class);
        doCallRealMethod().when(t).getNumberOfMonthsForSinceInception(any(NavigableMap.class));

        final NavigableMap<LocalDate, BigDecimal> aReturns = new TreeMap<>(Map.of(
                toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), ONE,
                toLastDayOfMonth(LOCAL_DATE_NOW), ONE));

        //ACT
        final int actual = t.getNumberOfMonthsForSinceInception(aReturns);

        //VERIFY
        assertEquals(2, actual);
    }

    @Test
    void addSinceCustomIntervalPerformanceStartDate_verifyCspsdIsGreater() {
        //SETUP
        final var input = mock(CalculationDTO.class);
        when(input.getCipsd()).thenReturn(LOCAL_DATE_NOW);
        final Map<LocalDate, BigDecimal> returns = Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE);
        final var portfolioTotalReturns = new TreeMap<>(returns);
        when(input.getWeightedAveragePortfolioReturns()).thenReturn(portfolioTotalReturns);

        final var sut = mock(PeriodCalculationAbstract.class,
                withSettings().useConstructor(input, null));

        doCallRealMethod().when(sut).isSinceCustomIntervalPerformanceStartDateValid();
        //ACT
        final boolean actual = sut.isSinceCustomIntervalPerformanceStartDateValid();

        //VERIFY
        assertFalse(actual);
    }

    @Test
    void addSinceCustomIntervalPerformanceStartDate_verifyCspsdIsLess() {
        //SETUP
        final var input = mock(CalculationDTO.class);
        when(input.getCipsd()).thenReturn(LOCAL_DATE_NOW);
        final Map<LocalDate, BigDecimal> returns = Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE);
        final var portfolioTotalReturns = new TreeMap<>(returns);
        when(input.getWeightedAveragePortfolioReturns()).thenReturn(portfolioTotalReturns);

        final var sut = mock(PeriodCalculationAbstract.class, withSettings().useConstructor(input, null));

        doCallRealMethod().when(sut).isSinceCustomIntervalPerformanceStartDateValid();
        //ACT
        final boolean actual = sut.isSinceCustomIntervalPerformanceStartDateValid();

        //VERIFY
        assertFalse(actual);
    }

    @Test
    void addSinceCustomIntervalPerformanceStartDate_verifyCspsdIsEqualTo() {
        //SETUP
        final var input = mock(CalculationDTO.class);
        final var sut = mock(PeriodCalculationAbstract.class, withSettings().useConstructor(input, null));
        final Map<LocalDate, BigDecimal> returns = Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE, LOCAL_DATE_NOW.plusMonths(1), TWO);
        sut.cipsd = LOCAL_DATE_NOW;
        sut.portfolioTotalReturns = new TreeMap<>(returns);

        doCallRealMethod().when(sut).isSinceCustomIntervalPerformanceStartDateValid();
        //ACT
        final boolean actual = sut.isSinceCustomIntervalPerformanceStartDateValid();

        //VERIFY
        assertTrue(actual);
    }

    @Test
    void getPeriodStartDate_checkResult() {
        //SETUP
        final ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
        doCallRealMethod().when(excessReturnsCalculation).getPeriodStartDate(anyInt(), any());
        //ACT
        final LocalDate periodStartDate = excessReturnsCalculation.getPeriodStartDate(12, getPortfolioReturns());

        //VERIFY
        assertEquals(toLastDayOfMonth(LocalDate.of(2020, 12, 1).minusMonths(11)), periodStartDate);
    }

    @Test
    void getSubMapByPeriodStartDate_checkResult() {
        //SETUP
        final ExcessReturnsCalculation excessReturnsCalculation = mock(ExcessReturnsCalculation.class);
        doCallRealMethod().when(excessReturnsCalculation).getSubMapByPeriodStartDate(any(), any());
        //ACT
        final SortedMap<LocalDate, BigDecimal> subMap = excessReturnsCalculation
                .getSubMapByPeriodStartDate(toLastDayOfMonth(LocalDate.of(2020, 12, 1).minusMonths(2)), getPortfolioReturns());

        //VERIFY
        assertEquals(3, subMap.size());
        assertEquals(toLastDayOfMonth(LocalDate.of(2020, 12, 1)), subMap.lastKey());
        assertEquals(toLastDayOfMonth(LocalDate.of(2020, 10, 1)), subMap.firstKey());
    }

    @Test
    void overrideTotalReturns_checkResult() {
        //SETUP
        final BetaCalculation betaCalculation = mock(BetaCalculation.class);
        final LocalDate date = LocalDate.of(2020, 12, 1);
        final TreeMap<LocalDate, BigDecimal> portfolioTotalReturns = new TreeMap<>(Map.of(toLastDayOfMonth(date), BigDecimal.valueOf(1.01094319080371),
                toLastDayOfMonth(date.minusMonths(1)), BigDecimal.valueOf(1.02297440154456)));
        when(betaCalculation.getPortfolioTotalReturns()).thenReturn(portfolioTotalReturns);
        doCallRealMethod().when(betaCalculation).overrideTotalReturns(any());

        //ACT
        final NavigableMap<LocalDate, BigDecimal> totalReturns = betaCalculation.overrideTotalReturns(portfolioTotalReturns);

        //VERIFY
        assertEquals(2, totalReturns.size());
        assertEquals(toUserScale(BigDecimal.valueOf(0.02297440154456)), toUserScale(totalReturns.firstEntry().getValue()));
        assertEquals(toUserScale(BigDecimal.valueOf(0.01094319080371)), toUserScale(totalReturns.lastEntry().getValue()));
    }

    @Test
    void formTimeIntervalResDTO_checkResult() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);

        final Set<TimeIntervalResDTO> expected = Set.of(new TimeIntervalResDTO("2000-01-12", ONE));

        final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2000-01-12", ONE));

        doCallRealMethod().when(p).formTimeIntervalResDTO(anySet());
        //ACT
        final Set actual = p.formTimeIntervalResDTO(pairs);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void calculateAverageArithmeticAnnualizedReturn_verifyGetSubMapByPeriodStartDate() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);
        final TreeMap treeMap = mock(TreeMap.class);
        final LocalDate date = LocalDate.now();

        doCallRealMethod().when(p).calculateAverageArithmeticAnnualizedReturn(any(), any(), anyInt());

        //ACT
        p.calculateAverageArithmeticAnnualizedReturn(treeMap, date, TWELVE);

        //VERIFY
        verify(p).getSubMapByPeriodStartDate(date, treeMap);
    }

    @Test
    void calculateAverageArithmeticAnnualizedReturn_checkResult() {
        //SETUP
        final PeriodCalculationAbstract p = mock(PeriodCalculationAbstract.class);
        final TreeMap treeMap = mock(TreeMap.class);
        final LocalDate date = LocalDate.now();
        final TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(date, BigDecimalConstants.ONE, date.plusMonths(1), TWO));

        when(p.getSubMapByPeriodStartDate(any(),any())).thenReturn(returns);
        doCallRealMethod().when(p).calculateAverageArithmeticAnnualizedReturn(any(), any(), anyInt());

        //ACT
        final BigDecimal returnValue = p.calculateAverageArithmeticAnnualizedReturn(treeMap, date, TWELVE);

        //VERIFY
        assertEquals(toUserScale(BigDecimal.valueOf(3)), toUserScale(returnValue));
    }

    @Test
    void restrictTBillsRange_checkResult() {
        //SETUP
        final var sut = mock(PeriodCalculationAbstract.class);
        final LocalDate date = LocalDate.now();
        final TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(date, BigDecimalConstants.ONE, date.plusMonths(1), TWO));
        final TreeMap<LocalDate, BigDecimal> tBills = new TreeMap<>(Map.of(
                date.minusMonths(1), ZERO,
                date, BigDecimalConstants.ONE,
                date.plusMonths(1), TWO,
                date.plusMonths(2), TEN)
        );
        final var expected = new TreeMap<>(Map.of(
                date, BigDecimalConstants.ONE,
                date.plusMonths(1), TWO)
        );

        doCallRealMethod().when(sut).restrictTBillsRange(any(), any());
        //ACT
        final NavigableMap<LocalDate, BigDecimal> actual = sut.restrictTBillsRange(tBills, returns);

        //VERIFY
        Assertions.assertNotNull(actual);
        ComparisonUtils.compareMaps(expected, actual);
    }

    @Test
    void restrictTBillsRangeWithOneParam_checkResult() {
        //SETUP
        final var sut = mock(PeriodCalculationAbstract.class);
        final LocalDate date = LocalDate.now();
        final TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(date, BigDecimalConstants.ONE, date.plusMonths(1), TWO));
        final TreeMap<LocalDate, BigDecimal> tBills = new TreeMap<>(Map.of(
                date.minusMonths(1), ZERO,
                date, BigDecimalConstants.ONE,
                date.plusMonths(1), TWO,
                date.plusMonths(2), TEN)
        );
        sut.portfolioTotalReturns = returns;

        final var expected = new TreeMap<>(Map.of(
                date, BigDecimalConstants.ONE,
                date.plusMonths(1), TWO)
        );

        doCallRealMethod().when(sut).restrictTBillsRange(any());
        doCallRealMethod().when(sut).restrictTBillsRange(any(), any());
        //ACT
        final NavigableMap<LocalDate, BigDecimal> actual = sut.restrictTBillsRange(tBills);

        //VERIFY
        Assertions.assertNotNull(actual);
        ComparisonUtils.compareMaps(expected, actual);
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