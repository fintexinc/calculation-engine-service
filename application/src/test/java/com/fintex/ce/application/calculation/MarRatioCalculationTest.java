package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.MarRatioCalculation;
import com.fintex.ce.application.calculation.MaxDrawdownCalculation;
import com.fintex.ce.application.calculation.TrailingTotalReturnsCalculation;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.application.result.MARRatioResult;
import com.fintex.ce.application.result.core.TimeIntervalResult;
import com.fintex.ce.application.result.core.MaxDrawdownEntry;
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
    // SETUP
    final var input = mock(CalculationDTO.class);
    final var trailingTTRCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);
    final var sut = mock(MarRatioCalculation.class, withSettings().useConstructor(input, Set.of(),
        trailingTTRCalculation, maxDrawdownCalculation));
    final var numberOfMonths = 12;
    final var maxDrawdownDTO = new MaxDrawdownEntry(null, new BigDecimal("0.111"), null, null, null);

    when(maxDrawdownCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(maxDrawdownDTO);
    when(trailingTTRCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(new BigDecimal("0.111"));

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    sut.calculatePeriodForNumberOfMonths(numberOfMonths);

    // VERIFY
    verify(trailingTTRCalculation).calculatePeriodForNumberOfMonths(numberOfMonths);
    verify(maxDrawdownCalculation).calculatePeriodForNumberOfMonths(numberOfMonths);
  }

  @Test
  void calculatePeriodForNumberOfMonths_verifyStaticDivideAndAbs() {
    // SETUP
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
      final var input = mock(CalculationDTO.class);
      final var trailingTTRCalculation = mock(TrailingTotalReturnsCalculation.class);
      final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);
      final var sut = mock(MarRatioCalculation.class, withSettings().useConstructor(input, Set.of(),
          trailingTTRCalculation, maxDrawdownCalculation));
      final var numberOfMonths = 12;
      final var maxDrawdownDTO = new MaxDrawdownEntry(null, new BigDecimal("0.1112"), null, null, null);

      when(maxDrawdownCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(maxDrawdownDTO);
      when(trailingTTRCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(new BigDecimal("0.1113"));

      mockedDecimalUtils.when(() -> DecimalUtils.abs(any())).thenReturn(new BigDecimal("0.1112"));

      doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
      // ACT
      sut.calculatePeriodForNumberOfMonths(numberOfMonths);

      // VERIFY
      mockedDecimalUtils.verify(() -> DecimalUtils.abs(new BigDecimal("0.1112")));
      mockedDecimalUtils.verify(() -> DecimalUtils.divide(new BigDecimal("0.1113"), new BigDecimal("0.1112")));
    }
  }

  @Test
  void calculatePeriodForNumberOfMonths_checkResult() {
    // SETUP
    final var input = mock(CalculationDTO.class);
    final var trailingTTRCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);
    final var sut = mock(MarRatioCalculation.class, withSettings().useConstructor(input, Set.of(),
        trailingTTRCalculation, maxDrawdownCalculation));
    final var numberOfMonths = 12;
    final var maxDrawdownDTO = new MaxDrawdownEntry(null, null, null, null, null);

    when(maxDrawdownCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(maxDrawdownDTO);
    when(trailingTTRCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(new BigDecimal("0.111"));

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(numberOfMonths);

    // VERIFY
    Assertions.assertNull(actual);
  }

  @Test
  void calculatePeriodForNumberOfMonths_checkResult2() {
    // SETUP
    final var input = mock(CalculationDTO.class);
    final var trailingTTRCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);
    final var sut = mock(MarRatioCalculation.class, withSettings().useConstructor(input, Set.of(),
        trailingTTRCalculation, maxDrawdownCalculation));
    final var numberOfMonths = 12;
    final var maxDrawdownDTO = new MaxDrawdownEntry(null, new BigDecimal("0.112"), null, null, null);

    when(maxDrawdownCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(maxDrawdownDTO);
    when(trailingTTRCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(new BigDecimal("0.111"));

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(numberOfMonths);

    // VERIFY
    final BigDecimal expected = new BigDecimal("0.991071428571429");
    Assertions.assertEquals(expected, actual);
  }

  @Test
  void calculatePeriodForNumberOfMonths_checkResult3() {
    // SETUP
    final var input = mock(CalculationDTO.class);
    final var trailingTTRCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);
    final var sut = mock(MarRatioCalculation.class, withSettings().useConstructor(input, Set.of(),
        trailingTTRCalculation, maxDrawdownCalculation));
    final var numberOfMonths = 10;

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final BigDecimal actual = sut.calculatePeriodForNumberOfMonths(numberOfMonths);

    // VERIFY
    assertNull(actual);
  }

  @Test
  void defineResponseType_verifyFormTimeIntervalResult() {
    // SETUP
    final var sut = mock(MarRatioCalculation.class);
    final var result = Set.of(
        Pair.of("2000-01-12", ZERO),
        Pair.of("2020-01-05", BigDecimal.ONE));

    final Set<TimeIntervalResult> timeIntervals = Set.of(new TimeIntervalResult("2000-01-12", ONE));
    when(sut.formTimeIntervalResult(anySet())).thenReturn(timeIntervals);

    doCallRealMethod().when(sut).defineResponseType(result);
    // ACT
    sut.defineResponseType(result);

    // VERIFY
    verify(sut).formTimeIntervalResult(result);
  }

  @Test
  void defineResponseType_checkResult() {
    // SETUP
    final var sut = mock(MarRatioCalculation.class);
    final var result = Set.of(
        Pair.of("2020-01-05", BigDecimal.ONE),
        Pair.of("2000-01-12", ZERO));

    final Set<TimeIntervalResult> expected = Set.of(
        new TimeIntervalResult("2000-01-12", ZERO),
        new TimeIntervalResult("2020-01-05", BigDecimal.ONE));
    when(sut.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(sut).defineResponseType(anySet());
    // ACT
    final MARRatioResult actual = sut.defineResponseType(result);

    // VERIFY
    assertEquals(expected, actual.getMarRatio());
  }

  @Test
  void calculatePeriodForNumberOfMonths_returnNull_whenMaxDrawdownIsZero() {
    // SETUP
    final var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);

    final var sut = mock(MarRatioCalculation.class,
        withSettings().useConstructor(mock(CalculationDTO.class), Set.of(), trailingTotalReturnsCalculation,
            maxDrawdownCalculation));

    final MaxDrawdownEntry maxDrawdownDTO = new MaxDrawdownEntry().setValue(ZERO);
    when(maxDrawdownCalculation.calculatePeriodForNumberOfMonths(12)).thenReturn(maxDrawdownDTO);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final BigDecimal actualResult = sut.calculatePeriodForNumberOfMonths(12);

    // VERIFY
    assertNull(actualResult);
  }

  @Test
  void calculatePeriodForNumberOfMonths_returnNull_whenMaxDrawdownIsNull() {
    // SETUP
    final var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);

    final var sut = mock(MarRatioCalculation.class,
        withSettings().useConstructor(mock(CalculationDTO.class), Set.of(), trailingTotalReturnsCalculation,
            maxDrawdownCalculation));

    final MaxDrawdownEntry maxDrawdownDTO = new MaxDrawdownEntry().setValue(null);
    when(maxDrawdownCalculation.calculatePeriodForNumberOfMonths(12)).thenReturn(maxDrawdownDTO);

    doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());
    // ACT
    final BigDecimal actualResult = sut.calculatePeriodForNumberOfMonths(12);

    // VERIFY
    assertNull(actualResult);
  }

}