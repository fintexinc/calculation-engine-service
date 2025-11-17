package com.fintex.ce.domain.calculation;

import com.fintex.ce.dto.response.DownsideCaptureResDTO;
import com.fintex.ce.dto.response.core.TimeIntervalResDTO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import static com.fintex.ce.config.constant.BigDecimalConstants.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

class DownsideCaptureCalculationTest {

    @Test
    void defineResponseType_verifyFormTimeIntervalResDTO() {
        //SETUP
        final DownsideCaptureCalculation alpha = mock(DownsideCaptureCalculation.class);

        final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05", BigDecimal.ONE));

        doCallRealMethod().when(alpha).defineResponseType(anySet());
        //ACT
        alpha.defineResponseType(pairs);

        //VERIFY
        verify(alpha).formTimeIntervalResDTO(pairs);
    }

    @Test
    void defineResponseType_checkResult() {
        //SETUP
        final DownsideCaptureCalculation t = mock(DownsideCaptureCalculation.class);

        final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2009-01-01", ONE), Pair.of("2013-01-05", TEN));

        final Set<TimeIntervalResDTO> expected = Set.of(
                new TimeIntervalResDTO("2009-01-01", ZERO),
                new TimeIntervalResDTO("2013-01-05", TEN)
        );
        when(t.formTimeIntervalResDTO(anySet())).thenReturn(expected);

        doCallRealMethod().when(t).defineResponseType(anySet());
        //ACT
        final DownsideCaptureResDTO actual = t.defineResponseType(pairs);

        //VERIFY
        assertEquals(expected, actual.getDownsideCapture());
    }

    @Test
    void filterCaptureExpression_checkResult() {
        //SETUP
        final DownsideCaptureCalculation t = mock(DownsideCaptureCalculation.class);

        final Map.Entry<LocalDate, BigDecimal> entry = new AbstractMap.SimpleEntry<>(
                LocalDate.now(), new BigDecimal(String.valueOf(BigDecimal.ONE.subtract(TEN)))
        );

        doCallRealMethod().when(t).filterCaptureExpression(any());
        //ACT
        final boolean actual = t.filterCaptureExpression(entry);

        //VERIFY
        assertTrue(actual);
    }

    @Test
    void filterCaptureExpression_checkResult1() {
        //SETUP
        final DownsideCaptureCalculation t = mock(DownsideCaptureCalculation.class);

        final Map.Entry<LocalDate, BigDecimal> entry = new AbstractMap.SimpleEntry<>(
                LocalDate.now(), new BigDecimal(String.valueOf(BigDecimal.ONE.subtract(ONE)))
        );

        doCallRealMethod().when(t).filterCaptureExpression(any());
        //ACT
        final boolean actual = t.filterCaptureExpression(entry);

        //VERIFY
        assertFalse(actual);
    }
}