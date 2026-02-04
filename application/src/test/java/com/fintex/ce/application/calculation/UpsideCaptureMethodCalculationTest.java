package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.UpsideCaptureCalculation;
import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.application.result.UpsideCaptureResult;
import com.fintex.ce.application.result.core.TimeIntervalResult;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.domain.constant.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.domain.constant.BigDecimalConstants.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

class UpsideCaptureMethodCalculationTest {

  @Test
  void defineResponseType_verifyFormTimeIntervalResult() {
    // SETUP
    final BenchmarkCalculationDTO calculationDTO = mock(BenchmarkCalculationDTO.class);
    when(calculationDTO.getWeightedAverageBenchmarkReturns()).thenReturn(new TreeMap<>());

    final var sut = mock(UpsideCaptureCalculation.class, withSettings().useConstructor(calculationDTO, null));

    final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05",
        BigDecimal.ONE));

    doCallRealMethod().when(sut).defineResponseType(anySet());
    // ACT
    sut.defineResponseType(pairs);

    // VERIFY
    verify(sut).formTimeIntervalResult(pairs);
  }

  @Test
  void defineResponseType_checkResult() {
    // SETUP
    final var sut = mock(UpsideCaptureCalculation.class);

    final var pairs = Set.of(Pair.of("2009-01-01", ONE), Pair.of("2013-01-05", TEN));

    final var expected = Set.of(
        new TimeIntervalResult("2000-01-12", ZERO),
        new TimeIntervalResult("2020-01-05", BigDecimal.ONE));
    when(sut.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(sut).defineResponseType(anySet());
    // ACT
    final UpsideCaptureResult actual = sut.defineResponseType(pairs);

    // VERIFY
    assertEquals(expected, actual.getUpsideCapture());
  }

  @Test
  void filterCaptureExpression_checkResult() {
    // SETUP
    final BenchmarkCalculationDTO calculationDTO = mock(BenchmarkCalculationDTO.class);
    when(calculationDTO.getWeightedAverageBenchmarkReturns()).thenReturn(new TreeMap<>());

    final var sut = mock(UpsideCaptureCalculation.class, withSettings().useConstructor(calculationDTO, null));

    final var entry = new AbstractMap.SimpleEntry<>(LocalDate.now(), new BigDecimal(String.valueOf(BigDecimal.ONE)));

    doCallRealMethod().when(sut).filterCaptureExpression(any());
    // ACT
    final boolean actual = sut.filterCaptureExpression(entry);

    // VERIFY
    assertTrue(actual);
  }

  @Test
  void filterCaptureExpression_checkResult1() {
    // SETUP
    final BenchmarkCalculationDTO calculationDTO = mock(BenchmarkCalculationDTO.class);
    when(calculationDTO.getWeightedAverageBenchmarkReturns()).thenReturn(new TreeMap<>());

    final var sut = mock(UpsideCaptureCalculation.class, withSettings().useConstructor(calculationDTO, null));

    final Map.Entry<LocalDate, BigDecimal> entry = new AbstractMap.SimpleEntry<>(
        LocalDate.now(), new BigDecimal(String.valueOf(BigDecimal.ONE.subtract(HUNDRED))));

    doCallRealMethod().when(sut).filterCaptureExpression(any());
    // ACT
    final boolean actual = sut.filterCaptureExpression(entry);

    // VERIFY
    assertFalse(actual);
  }

}