package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.result.risk.UpsideCaptureResult;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.Map;
import java.util.TreeMap;

import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@Disabled("metric unsupported")
class UpsideCaptureMethodCalculationTest {

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    final var calculation = mock(UpsideCaptureCalculation.class);

    final Map<String, BigDecimal> periods = Map.of("2009-01-01", ONE, "2013-01-05", TEN);

    final Map<String, BigDecimal> expected = Map.of("2009-01-01", ONE, "2013-01-05", TEN);

    doCallRealMethod().when(calculation).defineResponseType(anyMap());
    final UpsideCaptureResult actual = calculation.defineResponseType(periods);

    assertEquals(expected, actual.getUpsideCapture());
  }

  @Test
  void shouldFilterCaptureExpression_whenCheckResult() {
    final BenchmarkPeriodCalculationInput context = mock(BenchmarkPeriodCalculationInput.class);
    when(context.getWeightedAverageBenchmarkReturns()).thenReturn(new TreeMap<>());

    final var calculation = mock(UpsideCaptureCalculation.class, withSettings().useConstructor(context, null));

    final var entry = new AbstractMap.SimpleEntry<>(LocalDate.now(), new BigDecimal(String.valueOf(BigDecimal.ONE)));

    doCallRealMethod().when(calculation).filterCaptureExpression(any());
    final boolean actual = calculation.filterCaptureExpression(entry);

    assertTrue(actual);
  }

  @Test
  void shouldFilterCaptureExpression_whenCheckResult1() {
    final BenchmarkPeriodCalculationInput context = mock(BenchmarkPeriodCalculationInput.class);
    when(context.getWeightedAverageBenchmarkReturns()).thenReturn(new TreeMap<>());

    final var calculation = mock(UpsideCaptureCalculation.class, withSettings().useConstructor(context, null));

    final Map.Entry<LocalDate, BigDecimal> entry = new AbstractMap.SimpleEntry<>(
        LocalDate.now(), new BigDecimal(String.valueOf(BigDecimal.ONE.subtract(HUNDRED))));

    doCallRealMethod().when(calculation).filterCaptureExpression(any());
    final boolean actual = calculation.filterCaptureExpression(entry);

    assertFalse(actual);
  }

}