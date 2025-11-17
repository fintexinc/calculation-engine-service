package com.fintex.ce.domain.calculation;

import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.response.StandardDeviationResDTO;
import com.fintex.ce.dto.response.core.TimeIntervalResDTO;
import com.fintex.ce.util.CalculationUtils;
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

import static com.fintex.ce.config.constant.BigDecimalConstants.ONE;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static com.fintex.ce.util.DecimalUtils.OUTPUT_SCALE;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
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
    void calculatePeriodForNumberOfMonths_verifyGetPeriodStartDate() {
        //SETUP
        final var sut = mock(StandardDeviationCalculation.class, withSettings().useConstructor(mock(CalculationDTO.class), Set.of()));
        final var treeMap = mock(TreeMap.class);

        when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
        when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
        when(treeMap.size()).thenReturn(TWELVE);

        doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt(), any());
        //ACT
        sut.calculatePeriodForNumberOfMonths(TWELVE, treeMap);

        //VERIFY
        verify(sut).getPeriodStartDate(12, treeMap);
    }

    @Test
    void calculatePeriodForNumberOfMonths_verifyCalculatePeriodForNumberOfMonths() {
        //SETUP
        final var sut = mock(StandardDeviationCalculation.class);
        final var returns = mock(TreeMap.class);

        when(sut.getPortfolioTotalReturns()).thenReturn(returns);

        doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
        //ACT
        sut.calculatePeriodForNumberOfMonths(TWELVE);

        //VERIFY
        verify(sut).calculatePeriodForNumberOfMonths(TWELVE, returns);
    }

    @Test
    void calculatePeriodForNumberOfMonths_verifyGetSubMapByPeriodStartDate() {
        //SETUP
        final var sut = mock(StandardDeviationCalculation.class);
        final var treeMap = mock(TreeMap.class);

        when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
        when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
        when(treeMap.size()).thenReturn(TWELVE);

        final var periodStartDate = LocalDate.now();
        when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);
        doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt(), any());

        //ACT
        sut.calculatePeriodForNumberOfMonths(TWELVE, treeMap);

        //VERIFY
        verify(sut).getSubMapByPeriodStartDate(periodStartDate, treeMap);
    }

    @Test
    void calculatePeriodForNumberOfMonths_checkResult() {
        //SETUP
        final var sut = mock(StandardDeviationCalculation.class);
        final var treeMap = mock(TreeMap.class);

        when(treeMap.size()).thenReturn(1);

        //ACT
        final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(TWELVE, treeMap);

        //VERIFY
        assertNull(actual);
    }

    @Test
    void calculatePeriodForNumberOfMonths_checkResult2() {
        //SETUP
        final var sut = mock(StandardDeviationCalculation.class);
        final var treeMap = mock(TreeMap.class);

        when(treeMap.size()).thenReturn(20);

        //ACT
        final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(ONE.intValue(), treeMap);

        //VERIFY
        assertNull(actual);
    }

    @Test
    void calculatePeriodForNumberOfMonths_verifyCalculateStandardDeviation() {
        //SETUP
        final var sut = mock(StandardDeviationCalculation.class);
        final var treeMap = mock(TreeMap.class);

        when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
        when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(treeMap);
        when(treeMap.size()).thenReturn(TWELVE);

        final LocalDate periodStartDate = LocalDate.now();
        when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(periodStartDate);

        doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt(), any());
        //ACT
        sut.calculatePeriodForNumberOfMonths(TWELVE, treeMap);

        //VERIFY
        verify(sut).calculateStandardDeviation(treeMap, TWELVE);
    }

    @Test
    void calculatePeriodForNumberOfMonths_checkResult_whenNumberOfMonthsBiggerThanReturnsSize() {
        //SETUP
        final var sut = mock(StandardDeviationCalculation.class);
        final var returns = mock(NavigableMap.class);

        when(returns.size()).thenReturn(11);
        doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt(), any(NavigableMap.class));

        //ACT
        final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(12, returns);

        //VERIFY
        assertNull(actual);
    }

    @Test
    void calculatePeriodForNumberOfMonths_checkResult_whenNumberOfMonthsBiggerLessThan12() {
        //SETUP
        final var sut = mock(StandardDeviationCalculation.class);

        doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt(), any(NavigableMap.class));

        //ACT
        final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(11, mock(NavigableMap.class));

        //VERIFY
        assertNull(actual);
    }

    @Test
    void calculateStandardDeviation_verifyCalculateNumerator() {
        //SETUP
        final var sut = mock(StandardDeviationCalculation.class);
        final var treeMap = new TreeMap();
        treeMap.put(LocalDate.now(), TEN);
        treeMap.put(LocalDate.now().minusMonths(1), TEN);
        treeMap.put(LocalDate.now().minusMonths(5), TEN);

        when(sut.calculateNumerator(any(), any())).thenReturn(BigDecimal.ONE);
        doCallRealMethod().when(sut).calculateStandardDeviation(any(), anyInt());

        //ACT
        sut.calculateStandardDeviation(treeMap, TWELVE);

        //VERIFY
        verify(sut).calculateNumerator(treeMap, TEN.setScale(15, RoundingMode.UNNECESSARY));
    }

    @Test
    void calculateStandardDeviation_checkResult() {
        //SETUP
        final var sut = mock(StandardDeviationCalculation.class);
        doCallRealMethod().when(sut).setScale(anyInt());
        sut.setScale(OUTPUT_SCALE);
        final var treeMap = new TreeMap();
        treeMap.put(LocalDate.now(), ONE);
        treeMap.put(LocalDate.now().minusMonths(1), TEN);
        treeMap.put(LocalDate.now().minusMonths(5), TEN);

        when(sut.calculateNumerator(any(), any())).thenReturn(BigDecimal.TEN);
        doCallRealMethod().when(sut).calculateStandardDeviation(any(), anyInt());
        //ACT
        final BigDecimal actual = sut.calculateStandardDeviation(treeMap, TWELVE);

        //VERIFY
        assertEquals(toUserScale(BigDecimal.valueOf(3.30289129537908)), actual);
    }

    @Test
    void calculateNumerator_checkResult() {
        //SETUP
        final var sut = mock(StandardDeviationCalculation.class);

        doCallRealMethod().when(sut).calculateNumerator(any(), any());
        doCallRealMethod().when(sut).overrideTotalReturns(any());

        //ACT
        final NavigableMap<LocalDate, BigDecimal> totalReturns = sut.overrideTotalReturns(map);
        final BigDecimal actual = sut.calculateNumerator(totalReturns, CalculationUtils.average(totalReturns));

        //VERIFY
        assertEquals(toUserScale(BigDecimal.valueOf(3.08915891708201E-05)), toUserScale(actual));
    }

    @Test
    void defineResponseType_checkResult() {
        //SETUP
        final var sut = mock(StandardDeviationCalculation.class);
        final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05", ONE));
        final var intervalResDto = new TimeIntervalResDTO("2000-01-12", ZERO);
        final var intervalResDto1 = new TimeIntervalResDTO("2020-01-05", ONE);
        final var expected = Set.of(intervalResDto, intervalResDto1);

        when(sut.formTimeIntervalResDTO(anySet())).thenReturn(expected);

        doCallRealMethod().when(sut).defineResponseType(anySet());
        //ACT
        final StandardDeviationResDTO actual = (StandardDeviationResDTO) sut.defineResponseType(pairs);

        //VERIFY
        assertEquals(expected, actual.getStandardDeviation());
    }

    @AfterAll
    static void tearDown() {
        map.clear();
    }

}
