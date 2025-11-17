package com.fintex.ce.domain.calculation;

import com.fintex.ce.config.constant.BigDecimalConstants;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.response.MaxDrawdownResDTO;
import com.fintex.ce.dto.response.maxdrawdown.MaxDrawdownDTO;
import com.fintex.ce.util.DecimalUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.AbstractMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fintex.ce.config.constant.BigDecimalConstants.ONE;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class MaxDrawdownCalculationTest {

    final int TWELVE = 12;

    @Test
    void getPeriodStartDateWithOneMonthOffset_verifyGetPeriodStartDate() {
        //SETUP
        final var growth10K = mock(TreeMap.class);
        final var input = mock(CalculationDTO.class);
        final var sut = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K, null));

        final var numberOfMonths = 12;
        final var nowDate = LocalDate.now();

        when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(nowDate);

        doCallRealMethod().when(sut).getPeriodStartDateWithOneMonthOffset(anyInt());
        //ACT
        sut.getPeriodStartDateWithOneMonthOffset(numberOfMonths);

        //VERIFY
        verify(sut).getPeriodStartDate(numberOfMonths, growth10K);
    }

    @Test
    void getPeriodStartDateWithOneMonthOffset_checkResult() {
        //SETUP
        final var sut = mock(MaxDrawdownCalculation.class);

        final var numberOfMonths = 12;
        final var nowDate = LocalDate.now();
        final var expected = nowDate.minusMonths(1);

        when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(nowDate);

        doCallRealMethod().when(sut).getPeriodStartDateWithOneMonthOffset(anyInt());
        //ACT
        final var actual = sut.getPeriodStartDateWithOneMonthOffset(numberOfMonths);

        //VERIFY
        assertEquals(expected, actual);
    }


    @Test
    void getDrawDownStartDateWithOneMonthOffset_checkResult() {
        //SETUP
        final var sut = mock(MaxDrawdownCalculation.class);

        final var nowDate = LocalDate.now();
        final Map.Entry<LocalDate, BigDecimal> argument = new AbstractMap.SimpleEntry<>(nowDate, TEN);

        doCallRealMethod().when(sut).getDrawDownStartDateWithOneMonthOffset(any());
        //ACT
        final var actual = sut.getDrawDownStartDateWithOneMonthOffset(argument);

        //VERIFY
        assertEquals(nowDate.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth()), actual);
    }

    @Test
    void calculatePeriodForNumberOfMonths_verifyGetPeriodStartDateWithOneMonthOffset() {
        //SETUP
        final var growth10K = mock(TreeMap.class);
        final var input = mock(CalculationDTO.class);
        final Function<BigDecimal, BigDecimal> scaleFunction = DecimalUtils::toUserScale;
        final var sut = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K, scaleFunction));

        final var treeMap = mock(TreeMap.class);
        final var entry = mock(Map.Entry.class);

        when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
        when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(mock(SortedMap.class));
        when(sut.getMaxDrawdownValue(any())).thenReturn(entry);
        when(sut.getPeakValue(any(), any())).thenReturn(entry);
        when(treeMap.size()).thenReturn(TWELVE);
        when(entry.getValue()).thenReturn(ONE);

        doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
        //ACT
        sut.calculatePeriodForNumberOfMonths(TWELVE);

        //VERIFY
        verify(sut).getPeriodStartDateWithOneMonthOffset(12);
    }

    @Test
    void calculatePeriodForNumberOfMonths_verifyGetSubMapByPeriodStartDate() {
        //SETUP
        final var growth10K = mock(TreeMap.class);
        final var input = mock(CalculationDTO.class);
        final Function<BigDecimal, BigDecimal> scaleFunction = DecimalUtils::toUserScale;
        final var sut = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K, scaleFunction));

        final var treeMap = mock(TreeMap.class);
        final var entry = mock(Map.Entry.class);
        final var date = LocalDate.now();

        when(entry.getValue()).thenReturn(ONE);
        when(sut.getPeriodStartDateWithOneMonthOffset(anyInt())).thenReturn(date);
        when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
        when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(mock(SortedMap.class));
        when(sut.getMaxDrawdownValue(any())).thenReturn(entry);
        when(sut.getPeakValue(any(), any())).thenReturn(entry);
        when(treeMap.size()).thenReturn(TWELVE);
        doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

        //ACT
        sut.calculatePeriodForNumberOfMonths(TWELVE);

        //VERIFY
        verify(sut).getSubMapByPeriodStartDate(date, growth10K);
    }

    @Test
    void calculatePeriodForNumberOfMonths_verifyCalculateMaxDrawdownValues() {
        //SETUP
        final var growth10K = mock(TreeMap.class);
        final var input = mock(CalculationDTO.class);
        final Function<BigDecimal, BigDecimal> scaleFunction = DecimalUtils::toUserScale;
        final var sut = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K, scaleFunction));

        final var treeMap = mock(TreeMap.class);
        final var sortedMap = mock(SortedMap.class);
        final var entry = mock(Map.Entry.class);
        final LocalDate date = LocalDate.now();

        when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
        when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
        when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
        when(sut.getMaxDrawdownValue(any())).thenReturn(entry);
        when(sut.getPeakValue(any(), any())).thenReturn(entry);
        when(treeMap.size()).thenReturn(TWELVE);
        when(entry.getValue()).thenReturn(ONE);
        doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

        //ACT
        sut.calculatePeriodForNumberOfMonths(TWELVE);

        //VERIFY
        verify(sut).calculateMaxDrawdownValues(new TreeMap<>(sortedMap));
    }

    @Test
    void calculatePeriodForNumberOfMonths_verifyGetMaxDrawdownValue() {
        //SETUP
        final var growth10K = mock(TreeMap.class);
        final var input = mock(CalculationDTO.class);
        final Function<BigDecimal, BigDecimal> scaleFunction = DecimalUtils::toUserScale;
        final var sut = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K, scaleFunction));

        final var treeMap = mock(TreeMap.class);
        final var sortedMap = mock(SortedMap.class);
        final var entry = mock(Map.Entry.class);
        final var date = LocalDate.now();

        when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
        when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
        when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
        when(sut.getMaxDrawdownValue(any())).thenReturn(entry);
        when(sut.calculateMaxDrawdownValues(any())).thenReturn(treeMap);
        when(sut.getPeakValue(any(), any())).thenReturn(entry);
        when(treeMap.size()).thenReturn(TWELVE);
        when(entry.getValue()).thenReturn(ONE);
        doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

        //ACT
        sut.calculatePeriodForNumberOfMonths(TWELVE);

        //VERIFY
        verify(sut).getMaxDrawdownValue(treeMap);
    }

    @Test
    void calculatePeriodForNumberOfMonths_verifyGetPeakValue() {
        //SETUP
        final var growth10K = mock(TreeMap.class);
        final var input = mock(CalculationDTO.class);
        final Function<BigDecimal, BigDecimal> scaleFunction = DecimalUtils::toUserScale;
        final var sut = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K, scaleFunction));

        final var treeMap = mock(TreeMap.class);
        final var sortedMap = mock(SortedMap.class);
        final var entry = mock(Map.Entry.class);
        final var date = LocalDate.now();

        when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
        when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
        when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
        when(sut.getMaxDrawdownValue(any())).thenReturn(entry);
        when(sut.calculateMaxDrawdownValues(any())).thenReturn(treeMap);
        when(sut.getPeakValue(any(), any())).thenReturn(entry);
        when(treeMap.size()).thenReturn(TWELVE);
        when(entry.getValue()).thenReturn(ONE);
        doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

        //ACT
        sut.calculatePeriodForNumberOfMonths(TWELVE);

        //VERIFY
        verify(sut).getPeakValue(treeMap, entry);
    }

    @Test
    void calculatePeriodForNumberOfMonths_verifyGetRecoveryTimeValue() {
        //SETUP
        final var growth10K = mock(TreeMap.class);
        final var input = mock(CalculationDTO.class);
        final Function<BigDecimal, BigDecimal> scaleFunction = DecimalUtils::toUserScale;
        final var sut = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K, scaleFunction));

        final var treeMap = mock(TreeMap.class);
        final var sortedMap = mock(SortedMap.class);
        final var entry = mock(Map.Entry.class);
        final LocalDate date = LocalDate.now();

        when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
        when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
        when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
        when(sut.getMaxDrawdownValue(any())).thenReturn(entry);
        when(sut.calculateMaxDrawdownValues(any())).thenReturn(treeMap);
        when(sut.getPeakValue(any(), any())).thenReturn(entry);
        when(treeMap.size()).thenReturn(TWELVE);
        when(entry.getValue()).thenReturn(ONE);
        doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

        //ACT
        sut.calculatePeriodForNumberOfMonths(TWELVE);

        //VERIFY
        verify(sut).getRecoveryTimeValue(treeMap, entry, entry);
    }

    @Test
    void calculatePeriodForNumberOfMonths_checkResultWhenPortfolioTotalReturnsSizeLessThanPeriod() {
        //SETUP
        final var growth10K = mock(TreeMap.class);
        final var input = mock(CalculationDTO.class);
        final var sut = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K, null));

        final var treeMap = mock(TreeMap.class);
        final var sortedMap = mock(SortedMap.class);
        final var entry = mock(Map.Entry.class);
        final var date = LocalDate.now();


        when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
        when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
        when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
        when(sut.getMaxDrawdownValue(any())).thenReturn(entry);
        when(sut.calculateMaxDrawdownValues(any())).thenReturn(treeMap);
        when(sut.getPeakValue(any(), any())).thenReturn(entry);
        when(treeMap.size()).thenReturn(ONE.intValue());
        when(entry.getValue()).thenReturn(ONE);
        doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

        //ACT
        final MaxDrawdownDTO maxDrawDownDTO = sut.calculatePeriodForNumberOfMonths(TWELVE);

        //VERIFY
        assertEquals(String.valueOf(TWELVE), maxDrawDownDTO.getTimeIntervalPeriod());
        assertNull(maxDrawDownDTO.getDrawdownTroughDate());
        assertNull(maxDrawDownDTO.getDrawdownStartDate());
        assertNull(maxDrawDownDTO.getRecoveryTime());
        assertNull(maxDrawDownDTO.getValue());
    }

    @Test
    void calculatePeriodForNumberOfMonths_checkResultWhengetMaxDrawdownValueReturnsZero() {
        //SETUP
        final var growth10K = mock(TreeMap.class);
        final var input = mock(CalculationDTO.class);
        final var sut = mock(MaxDrawdownCalculation.class, withSettings().useConstructor(input, Set.of(), growth10K, null));

        final var treeMap = mock(TreeMap.class);
        final var sortedMap = mock(SortedMap.class);
        final var entry = mock(Map.Entry.class);
        final var date = LocalDate.now();

        when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(date);
        when(sut.getPortfolioTotalReturns()).thenReturn(treeMap);
        when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(sortedMap);
        when(sut.getMaxDrawdownValue(any()))
                .thenReturn(new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.valueOf(0, 00000000000))).firstEntry());
        when(sut.calculateMaxDrawdownValues(any())).thenReturn(treeMap);
        when(sut.getPeakValue(any(), any())).thenReturn(entry);
        when(treeMap.size()).thenReturn(ONE.intValue());
        doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

        //ACT
        final MaxDrawdownDTO maxDrawDownDTO = sut.calculatePeriodForNumberOfMonths(TWELVE);

        //VERIFY
        assertEquals(String.valueOf(TWELVE), maxDrawDownDTO.getTimeIntervalPeriod());
        assertNull(maxDrawDownDTO.getDrawdownTroughDate());
        assertNull(maxDrawDownDTO.getDrawdownStartDate());
        assertNull(maxDrawDownDTO.getRecoveryTime());
        assertNull(maxDrawDownDTO.getValue());
    }

    @Test
    void defineResponseType_checkResult() {
        //SETUP
        final var sut = mock(MaxDrawdownCalculation.class);
        final var pairs = Set.of(Pair.of("12", new MaxDrawdownDTO()), Pair.of("22", new MaxDrawdownDTO()));
        final var expected = pairs.stream().map(Pair::getValue).collect(Collectors.toList());

        doCallRealMethod().when(sut).defineResponseType(anySet());
        //ACT
        final MaxDrawdownResDTO actual = sut.defineResponseType(pairs);

        //VERIFY
        assertEquals(expected, actual.getMaxDrawdown());
    }

    @Test
    void calculateMaxDrawdownValues_verifyGetSubMapFromFirstKeyToCustomDate() {
        //SETUP
        final var sut = mock(MaxDrawdownCalculation.class);
        final var date = LocalDate.now();
        final var growth10KByPeriod = new TreeMap<>(Map.of(date, ONE));
        when(sut.getSubMapFromFirstKeyToCustomDate(any(), any())).thenReturn(growth10KByPeriod);

        doCallRealMethod().when(sut).calculateMaxDrawdownValues(any());
        //ACT
        sut.calculateMaxDrawdownValues(growth10KByPeriod);

        //VERIFY
        verify(sut, times(1)).getSubMapFromFirstKeyToCustomDate(growth10KByPeriod, date);
    }

    //
    @Test
    void calculateMaxDrawdownValues_checkResult() {
        //SETUP
        final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);
        final var date = LocalDate.now();
        final var growth10KByPeriod = new TreeMap<>(Map.of(date, ONE, date.minusMonths(1), TEN, date.minusMonths(2), BigDecimalConstants.TWELVE));
        when(maxDrawdownCalculation.getSubMapFromFirstKeyToCustomDate(any(), any())).thenReturn(growth10KByPeriod);

        doCallRealMethod().when(maxDrawdownCalculation).calculateMaxDrawdownValues(any());
        //ACT
        final NavigableMap<LocalDate, BigDecimal> results = maxDrawdownCalculation.calculateMaxDrawdownValues(growth10KByPeriod);

        //VERIFY
        assertEquals(3, results.size());
        assertEquals(date.minusMonths(2), results.firstKey());
        assertEquals(toUserScale(BigDecimal.valueOf(0)), toUserScale(results.firstEntry().getValue()));
        assertEquals(date, results.lastKey());
        assertEquals(toUserScale(BigDecimal.valueOf(-0.916666666666667)), toUserScale(results.lastEntry().getValue()));
    }

    @Test
    void getMaxDrawdownValue_checkResult() {
        //SETUP
        final var sut = mock(MaxDrawdownCalculation.class);
        final var date = LocalDate.now();
        final var maximumDrawdownMap = Map.of(date, ONE, date.minusMonths(1), TEN, date.minusMonths(2), BigDecimalConstants.TWELVE, date.plusMonths(3), ONE);
        final var growth10KByPeriod = new TreeMap<>(maximumDrawdownMap);

        doCallRealMethod().when(sut).getMaxDrawdownValue(any());
        //ACT
        final Map.Entry<LocalDate, BigDecimal> maxDrawdownValue = sut.getMaxDrawdownValue(growth10KByPeriod);

        //VERIFY
        assertEquals(date, maxDrawdownValue.getKey());
        assertEquals(BigDecimal.ONE, maxDrawdownValue.getValue());
    }

    @Test
    void getPeakValue_verifyGetSubMapFromFirstKeyToCustomDate() {
        //SETUP
        final var sut = mock(MaxDrawdownCalculation.class);
        final var date = LocalDate.now();
        final var map = Map.of(date, ONE, date.minusMonths(1), TEN, date.minusMonths(2), BigDecimalConstants.TWELVE, date.plusMonths(3), ONE);
        final var maximumDrawdownMap = new TreeMap<>(map);
        when(sut.getSubMapFromFirstKeyToCustomDate(any(), any())).thenReturn(maximumDrawdownMap);

        doCallRealMethod().when(sut).getPeakValue(any(), any());
        //ACT
        sut.getPeakValue(maximumDrawdownMap, maximumDrawdownMap.lastEntry());

        //VERIFY
        verify(sut).getSubMapFromFirstKeyToCustomDate(maximumDrawdownMap, maximumDrawdownMap.lastKey());
    }

    @Test
    void getPeakValue_checkResult() {
        //SETUP
        final var sut = mock(MaxDrawdownCalculation.class);
        final var date = LocalDate.now();
        final var map = Map.of(date, ONE, date.minusMonths(1), TEN, date.minusMonths(2), BigDecimalConstants.TWELVE, date.plusMonths(3), ONE);
        final var maximumDrawdownMap = new TreeMap<>(map);
        when(sut.getSubMapFromFirstKeyToCustomDate(any(), any())).thenReturn(maximumDrawdownMap);

        doCallRealMethod().when(sut).getPeakValue(any(), any());
        //ACT
        final Map.Entry<LocalDate, BigDecimal> peakValue = sut.getPeakValue(maximumDrawdownMap, maximumDrawdownMap.lastEntry());

        //VERIFY
        assertEquals(date.minusMonths(2), peakValue.getKey());
        assertEquals(BigDecimalConstants.TWELVE, peakValue.getValue());
    }

    @Test
    void getRecoveryTimeValue_verifyGetSubMapByPeriodStartDate() {
        //SETUP
        final var sut = mock(MaxDrawdownCalculation.class);
        final var date = LocalDate.now();
        final var map = Map.of(date, BigDecimalConstants.TWELVE, date.minusMonths(1), TEN, date.minusMonths(2), ONE);
        final var maximumDrawdownMap = new TreeMap<>(map);
        when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(maximumDrawdownMap);

        doCallRealMethod().when(sut).getRecoveryTimeValue(any(), any(), any());
        //ACT
        sut.getRecoveryTimeValue(maximumDrawdownMap, maximumDrawdownMap.firstEntry(), maximumDrawdownMap.lastEntry());

        //VERIFY
        verify(sut).getSubMapByPeriodStartDate(maximumDrawdownMap.firstEntry().getKey(), maximumDrawdownMap);
    }

    @Test
    void getRecoveryTimeValue_checkResult() {
        //SETUP
        final var sut = mock(MaxDrawdownCalculation.class);
        final var date = LocalDate.now();
        final var map = Map.of(date, BigDecimalConstants.TWELVE, date.minusMonths(1), TEN, date.minusMonths(2), ONE);
        final var maximumDrawdownMap = new TreeMap<>(map);
        when(sut.getSubMapByPeriodStartDate(any(), any())).thenReturn(maximumDrawdownMap);

        doCallRealMethod().when(sut).getRecoveryTimeValue(any(), any(), any());
        //ACT
        final Integer recoveryTimeValue = sut.getRecoveryTimeValue(maximumDrawdownMap, maximumDrawdownMap.firstEntry(), maximumDrawdownMap.lastEntry());

        //VERIFY
        assertEquals(2, recoveryTimeValue);
    }

    @Test
    void getSubMapFromFirstKeyToCustomDate_checkResult() {
        //SETUP
        final var sut = mock(MaxDrawdownCalculation.class);
        final var date = LocalDate.now();
        final var map = Map.of(date, BigDecimalConstants.TWELVE, date.minusMonths(1), TEN, date.minusMonths(2), ONE);
        final var maximumDrawdownMap = new TreeMap<>(map);
        doCallRealMethod().when(sut).getSubMapFromFirstKeyToCustomDate(any(), any());

        //ACT
        final NavigableMap<LocalDate, BigDecimal> result = sut.getSubMapFromFirstKeyToCustomDate(maximumDrawdownMap, date.minusMonths(1));

        //VERIFY
        assertEquals(2, result.size());
    }


}
