package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.DownsideCaptureResult;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled("metric unsupported")
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