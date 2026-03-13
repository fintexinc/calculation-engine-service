package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.DownsideCaptureCalculation;
import com.fintex.ce.port.input.result.DownsideCaptureResult;
import com.fintex.ce.port.input.result.core.TimeIntervalResult;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import static com.fintex.ce.domain.constant.BigDecimalConstants.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

class DownsideCaptureCalculationTest {

  @Test
  void shouldDefineResponseType_whenVerifyFormTimeIntervalResult() {
    final DownsideCaptureCalculation alpha = mock(DownsideCaptureCalculation.class);

    final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05",
        BigDecimal.ONE));

    doCallRealMethod().when(alpha).defineResponseType(anySet());
    alpha.defineResponseType(pairs);

    verify(alpha).formTimeIntervalResult(pairs);
  }

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    final DownsideCaptureCalculation t = mock(DownsideCaptureCalculation.class);

    final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2009-01-01", ONE), Pair.of("2013-01-05", TEN));

    final Set<TimeIntervalResult> expected = Set.of(
        new TimeIntervalResult("2009-01-01", ZERO),
        new TimeIntervalResult("2013-01-05", TEN));
    when(t.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(t).defineResponseType(anySet());
    final DownsideCaptureResult actual = t.defineResponseType(pairs);

    assertEquals(expected, actual.getDownsideCapture());
  }

  @Test
  void shouldFilterCaptureExpression_whenCheckResult() {
    final DownsideCaptureCalculation t = mock(DownsideCaptureCalculation.class);

    final Map.Entry<LocalDate, BigDecimal> entry = new AbstractMap.SimpleEntry<>(
        LocalDate.now(), new BigDecimal(String.valueOf(BigDecimal.ONE.subtract(TEN))));

    doCallRealMethod().when(t).filterCaptureExpression(any());
    final boolean actual = t.filterCaptureExpression(entry);

    assertTrue(actual);
  }

  @Test
  void shouldFilterCaptureExpression_whenCheckResult1() {
    final DownsideCaptureCalculation t = mock(DownsideCaptureCalculation.class);

    final Map.Entry<LocalDate, BigDecimal> entry = new AbstractMap.SimpleEntry<>(
        LocalDate.now(), new BigDecimal(String.valueOf(BigDecimal.ONE.subtract(ONE))));

    doCallRealMethod().when(t).filterCaptureExpression(any());
    final boolean actual = t.filterCaptureExpression(entry);

    assertFalse(actual);
  }
}