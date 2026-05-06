package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.MaxDrawdownEntry;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.MarRatioResult;

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
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class MarRatioCalculationTest {

  @Test
  void shouldDelegateToDependencies_whenCalculatingPeriod() {
    final var input = mock(PeriodCalculationInput.class);
    final var trailingTTRCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);
    final var calculation = mock(MarRatioCalculation.class, withSettings().useConstructor(input, Set.of(),
        trailingTTRCalculation, maxDrawdownCalculation));
    final var numberOfMonths = 12;
    final var maxDrawdown = new MaxDrawdownEntry(null, new BigDecimal("0.111"), null, null, null);

    when(maxDrawdownCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(maxDrawdown);
    when(trailingTTRCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(new BigDecimal("0.111"));

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    calculation.calculatePeriodForNumberOfMonths(numberOfMonths);

    verify(trailingTTRCalculation).calculatePeriodForNumberOfMonths(numberOfMonths);
    verify(maxDrawdownCalculation).calculatePeriodForNumberOfMonths(numberOfMonths);
  }

  @Test
  void shouldUseAbsoluteMaxDrawdownAndDivide_whenBothInputsPresent() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
      final var input = mock(PeriodCalculationInput.class);
      final var trailingTTRCalculation = mock(TrailingTotalReturnsCalculation.class);
      final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);
      final var calculation = mock(MarRatioCalculation.class, withSettings().useConstructor(input, Set.of(),
          trailingTTRCalculation, maxDrawdownCalculation));
      final var numberOfMonths = 12;
      final var maxDrawdown = new MaxDrawdownEntry(null, new BigDecimal("0.1112"), null, null, null);

      when(maxDrawdownCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(maxDrawdown);
      when(trailingTTRCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(new BigDecimal("0.1113"));

      mockedDecimalUtils.when(() -> DecimalUtils.abs(any())).thenReturn(new BigDecimal("0.1112"));

      doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
      calculation.calculatePeriodForNumberOfMonths(numberOfMonths);

      mockedDecimalUtils.verify(() -> DecimalUtils.abs(new BigDecimal("0.1112")));
      mockedDecimalUtils.verify(() -> DecimalUtils.divide(new BigDecimal("0.1113"), new BigDecimal("0.1112")));
    }
  }

  @Test
  void shouldReturnNull_whenMaxDrawdownValueIsNull() {
    final var input = mock(PeriodCalculationInput.class);
    final var trailingTTRCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);
    final var calculation = mock(MarRatioCalculation.class, withSettings().useConstructor(input, Set.of(),
        trailingTTRCalculation, maxDrawdownCalculation));
    final var numberOfMonths = 12;
    final var maxDrawdown = new MaxDrawdownEntry(null, null, null, null, null);

    when(maxDrawdownCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(maxDrawdown);
    when(trailingTTRCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(new BigDecimal("0.111"));

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(numberOfMonths);

    Assertions.assertNull(actual);
  }

  @Test
  void shouldReturnMarRatio_whenTrailingReturnAndMaxDrawdownPresent() {
    final var input = mock(PeriodCalculationInput.class);
    final var trailingTTRCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);
    final var calculation = mock(MarRatioCalculation.class, withSettings().useConstructor(input, Set.of(),
        trailingTTRCalculation, maxDrawdownCalculation));
    final var numberOfMonths = 12;
    final var maxDrawdown = new MaxDrawdownEntry(null, new BigDecimal("0.112"), null, null, null);

    when(maxDrawdownCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(maxDrawdown);
    when(trailingTTRCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(new BigDecimal("0.111"));

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(numberOfMonths);

    final BigDecimal expected = new BigDecimal("0.991071428571429");
    Assertions.assertEquals(expected, actual);
  }

  @Test
  void shouldReturnNull_whenPeriodIsLessThanTwelve() {
    final var input = mock(PeriodCalculationInput.class);
    final var trailingTTRCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);
    final var calculation = mock(MarRatioCalculation.class, withSettings().useConstructor(input, Set.of(),
        trailingTTRCalculation, maxDrawdownCalculation));
    final var numberOfMonths = 10;

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal actual = calculation.calculatePeriodForNumberOfMonths(numberOfMonths);

    assertNull(actual);
  }

  @Test
  void shouldDelegateToFormTimeIntervalResult_whenDefiningResponseType() {
    final var calculation = mock(MarRatioCalculation.class);
    final var result = Set.of(
        Pair.of("2000-01-12", ZERO),
        Pair.of("2020-01-05", BigDecimal.ONE));

    final Set<TimeIntervalResult> timeIntervals = Set.of(new TimeIntervalResult("2000-01-12", ONE));
    when(calculation.formTimeIntervalResult(anySet())).thenReturn(timeIntervals);

    doCallRealMethod().when(calculation).defineResponseType(result);
    calculation.defineResponseType(result);

    verify(calculation).formTimeIntervalResult(result);
  }

  @Test
  void shouldMapIntervals_whenDefiningResponseType() {
    final var calculation = mock(MarRatioCalculation.class);
    final var result = Set.of(
        Pair.of("2020-01-05", BigDecimal.ONE),
        Pair.of("2000-01-12", ZERO));

    final Set<TimeIntervalResult> expected = Set.of(
        new TimeIntervalResult("2000-01-12", ZERO),
        new TimeIntervalResult("2020-01-05", BigDecimal.ONE));
    when(calculation.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(calculation).defineResponseType(anySet());
    final MarRatioResult actual = calculation.defineResponseType(result);

    assertEquals(expected, actual.getMarRatio());
  }

  @Test
  void shouldReturnNull_whenMaxDrawdownIsZero() {
    final var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);

    final var calculation = mock(MarRatioCalculation.class,
        withSettings().useConstructor(mock(PeriodCalculationInput.class), Set.of(), trailingTotalReturnsCalculation,
            maxDrawdownCalculation));

    final MaxDrawdownEntry maxDrawdown = new MaxDrawdownEntry(null, ZERO, null, null, null);
    when(maxDrawdownCalculation.calculatePeriodForNumberOfMonths(12)).thenReturn(maxDrawdown);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal actualResult = calculation.calculatePeriodForNumberOfMonths(12);

    assertNull(actualResult);
  }

  @Test
  void shouldReturnNull_whenMaxDrawdownIsNull() {
    final var trailingTotalReturnsCalculation = mock(TrailingTotalReturnsCalculation.class);
    final var maxDrawdownCalculation = mock(MaxDrawdownCalculation.class);

    final var calculation = mock(MarRatioCalculation.class,
        withSettings().useConstructor(mock(PeriodCalculationInput.class), Set.of(), trailingTotalReturnsCalculation,
            maxDrawdownCalculation));

    final MaxDrawdownEntry maxDrawdown = new MaxDrawdownEntry(null, null, null, null, null);
    when(maxDrawdownCalculation.calculatePeriodForNumberOfMonths(12)).thenReturn(maxDrawdown);

    doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());
    final BigDecimal actualResult = calculation.calculatePeriodForNumberOfMonths(12);

    assertNull(actualResult);
  }

}
