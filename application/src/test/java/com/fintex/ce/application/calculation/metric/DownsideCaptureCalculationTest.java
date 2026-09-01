package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.result.risk.DownsideCaptureResult;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.Map;

import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

@Disabled("metric unsupported")
class DownsideCaptureCalculationTest {

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    final DownsideCaptureCalculation t = mock(DownsideCaptureCalculation.class);

    final Map<String, BigDecimal> periods = Map.of("2009-01-01", ONE, "2013-01-05", TEN);

    final Map<String, BigDecimal> expected = Map.of("2009-01-01", ONE, "2013-01-05", TEN);

    doCallRealMethod().when(t).defineResponseType(anyMap());
    final DownsideCaptureResult actual = t.defineResponseType(periods);

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