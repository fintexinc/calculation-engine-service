package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.UpsideCaptureResult;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class UpsideCaptureMethodCalculationTest {

  @Test
  void shouldDefineResponseType_whenVerifyFormTimeIntervalResult() {
    final BenchmarkPeriodCalculationInput context = mock(BenchmarkPeriodCalculationInput.class);
    when(context.getWeightedAverageBenchmarkReturns()).thenReturn(new TreeMap<>());

    final var calculation = mock(UpsideCaptureCalculation.class, withSettings().useConstructor(context, null));

    final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05",
        BigDecimal.ONE));

    doCallRealMethod().when(calculation).defineResponseType(anySet());
    calculation.defineResponseType(pairs);

    verify(calculation).formTimeIntervalResult(pairs);
  }

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    final var calculation = mock(UpsideCaptureCalculation.class);

    final var pairs = Set.of(Pair.of("2009-01-01", ONE), Pair.of("2013-01-05", TEN));

    final var expected = Set.of(
        new TimeIntervalResult("2000-01-12", ZERO),
        new TimeIntervalResult("2020-01-05", BigDecimal.ONE));
    when(calculation.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(calculation).defineResponseType(anySet());
    final UpsideCaptureResult actual = calculation.defineResponseType(pairs);

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