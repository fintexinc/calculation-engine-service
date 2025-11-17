package com.fintex.ce.domain.calculation;

import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.response.MARRatioResDTO;
import com.fintex.ce.dto.response.core.TimeIntervalResDTO;
import com.fintex.ce.dto.response.maxdrawdown.MaxDrawdownDTO;
import com.fintex.ce.util.DecimalUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Set;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

class MarRatioCalculationTest {

    @Test
    void calculatePeriodForNumberOfMonths_verifyCalculatePeriodForNumberOfMonthsForTrailingTRAndMaxDrawdown() {
        //SETUP
        final var input = mock(CalculationDTO.class);
        final var trailingTTRCalculation = mock(TrailingTotalReturnsCalculation.class);
        final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);
        final var sut = mock(MarRatioCalculation.class, withSettings().useConstructor(input, Set.of(), trailingTTRCalculation, maxDrawdownCalculation));
        final var numberOfMonths = 12;
        final var maxDrawdownDTO = new MaxDrawdownDTO(null, new BigDecimal("0.111"), null, null, null);

        when(maxDrawdownCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(maxDrawdownDTO);
        when(trailingTTRCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(new BigDecimal("0.111"));

        doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
        //ACT
        sut.calculatePeriodForNumberOfMonths(numberOfMonths);

        //VERIFY
        verify(trailingTTRCalculation).calculatePeriodForNumberOfMonths(numberOfMonths);
        verify(maxDrawdownCalculation).calculatePeriodForNumberOfMonths(numberOfMonths);
    }

    @Test
    void calculatePeriodForNumberOfMonths_verifyStaticDivideAndAbs() {
        //SETUP
        try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
            final var input = mock(CalculationDTO.class);
            final var trailingTTRCalculation = mock(TrailingTotalReturnsCalculation.class);
            final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);
            final var sut = mock(MarRatioCalculation.class, withSettings().useConstructor(input, Set.of(), trailingTTRCalculation, maxDrawdownCalculation));
            final var numberOfMonths = 12;
            final var maxDrawdownDTO = new MaxDrawdownDTO(null, new BigDecimal("0.1112"), null, null, null);

            when(maxDrawdownCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(maxDrawdownDTO);
            when(trailingTTRCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(new BigDecimal("0.1113"));

            mockedDecimalUtils.when(() -> DecimalUtils.abs(any())).thenReturn(new BigDecimal("0.1112"));

            doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
            //ACT
            sut.calculatePeriodForNumberOfMonths(numberOfMonths);

            //VERIFY
            mockedDecimalUtils.verify(() -> DecimalUtils.abs(new BigDecimal("0.1112")));
            mockedDecimalUtils.verify(() -> DecimalUtils.divide(new BigDecimal("0.1113"), new BigDecimal("0.1112")));
        }
    }

    @Test
    void calculatePeriodForNumberOfMonths_checkResult() {
        //SETUP
        final var input = mock(CalculationDTO.class);
        final var trailingTTRCalculation = mock(TrailingTotalReturnsCalculation.class);
        final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);
        final var sut = mock(MarRatioCalculation.class, withSettings().useConstructor(input, Set.of(), trailingTTRCalculation, maxDrawdownCalculation));
        final var numberOfMonths = 12;
        final var maxDrawdownDTO = new MaxDrawdownDTO(null, null, null, null, null);

        when(maxDrawdownCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(maxDrawdownDTO);
        when(trailingTTRCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(new BigDecimal("0.111"));

        doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
        //ACT
        final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(numberOfMonths);

        //VERIFY
        Assertions.assertNull(actual);
    }

    @Test
    void calculatePeriodForNumberOfMonths_checkResult2() {
        //SETUP
        final var input = mock(CalculationDTO.class);
        final var trailingTTRCalculation = mock(TrailingTotalReturnsCalculation.class);
        final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);
        final var sut = mock(MarRatioCalculation.class, withSettings().useConstructor(input, Set.of(), trailingTTRCalculation, maxDrawdownCalculation));
        final var numberOfMonths = 12;
        final var maxDrawdownDTO = new MaxDrawdownDTO(null, new BigDecimal("0.112"), null, null, null);

        when(maxDrawdownCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(maxDrawdownDTO);
        when(trailingTTRCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(new BigDecimal("0.111"));

        doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
        //ACT
        final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(numberOfMonths);

        //VERIFY
        final BigDecimal expected = new BigDecimal("0.991071428571429");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void calculatePeriodForNumberOfMonths_checkResult3() {
        //SETUP
        final var input = mock(CalculationDTO.class);
        final var trailingTTRCalculation = mock(TrailingTotalReturnsCalculation.class);
        final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);
        final var sut = mock(MarRatioCalculation.class, withSettings().useConstructor(input, Set.of(), trailingTTRCalculation, maxDrawdownCalculation));
        final var numberOfMonths = 10;

        doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
        //ACT
        final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(numberOfMonths);

        //VERIFY
        assertNull(actual);
    }

    @Test
    void defineResponseType_verifyFormTimeIntervalResDTO() {
        //SETUP
        final var sut = mock(MarRatioCalculation.class);
        final var result = Set.of(
                Pair.of("2000-01-12", ZERO),
                Pair.of("2020-01-05", BigDecimal.ONE)
        );

        final Set<TimeIntervalResDTO> timeIntervals = Set.of(new TimeIntervalResDTO("2000-01-12", ONE));
        when(sut.formTimeIntervalResDTO(anySet())).thenReturn(timeIntervals);

        doCallRealMethod().when(sut).defineResponseType(result);
        //ACT
        sut.defineResponseType(result);

        //VERIFY
        verify(sut).formTimeIntervalResDTO(result);
    }

    @Test
    void defineResponseType_checkResult() {
        //SETUP
        final var sut = mock(MarRatioCalculation.class);
        final var result = Set.of(
                Pair.of("2020-01-05", BigDecimal.ONE),
                Pair.of("2000-01-12", ZERO)
        );

        final Set<TimeIntervalResDTO> expected = Set.of(
                new TimeIntervalResDTO("2000-01-12", ZERO),
                new TimeIntervalResDTO("2020-01-05", BigDecimal.ONE)
        );
        when(sut.formTimeIntervalResDTO(anySet())).thenReturn(expected);

        doCallRealMethod().when(sut).defineResponseType(anySet());
        //ACT
        final MARRatioResDTO actual = sut.defineResponseType(result);

        //VERIFY
        assertEquals(expected, actual.getMarRatio());
    }

    @Test
    void calculatePeriodForNumberOfMonths_returnNull_whenMaxDrawdownIsZero() {
        //SETUP
        final var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
        final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);

        final var sut = mock(MarRatioCalculation.class,
                withSettings().useConstructor(mock(CalculationDTO.class), Set.of(), trailingTotalReturnsCalculation, maxDrawdownCalculation));

        final MaxDrawdownDTO maxDrawdownDTO = new MaxDrawdownDTO().setValue(ZERO);
        when(maxDrawdownCalculation.calculatePeriodForNumberOfMonths(12)).thenReturn(maxDrawdownDTO);

        doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
        //ACT
        final BigDecimal actualResult = sut.calculatePeriodForNumberOfMonths(12);

        //VERIFY
        assertNull(actualResult);
    }

    @Test
    void calculatePeriodForNumberOfMonths_returnNull_whenMaxDrawdownIsNull() {
        //SETUP
        final var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
        final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);

        final var sut = mock(MarRatioCalculation.class,
                withSettings().useConstructor(mock(CalculationDTO.class), Set.of(), trailingTotalReturnsCalculation, maxDrawdownCalculation));

        final MaxDrawdownDTO maxDrawdownDTO = new MaxDrawdownDTO().setValue(null);
        when(maxDrawdownCalculation.calculatePeriodForNumberOfMonths(12)).thenReturn(maxDrawdownDTO);

        doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
        //ACT
        final BigDecimal actualResult = sut.calculatePeriodForNumberOfMonths(12);

        //VERIFY
        assertNull(actualResult);
    }

}