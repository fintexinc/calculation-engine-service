package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.MeanCalculation;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.exceptions.CalculationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.fintex.ce.model.domain.enumeration.Period.SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE;
import static com.fintex.ce.model.error.ErrorCode.TIME_INTERVAL_PERIOD_CONTAINS_YEAR_TO_DATE;
import static com.fintex.ce.model.error.ErrorCode.TIME_INTERVAL_PERIOD_LESS_THAN_12;
import static com.fintex.ce.model.util.BigDecimalConstants.OUTPUT_SCALE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class MeanCalculationServiceImplTest {

  @Test
  void shouldCalculationSpecificChecks_whenCheckResult() {
    // SETUP
    final MeanCalculationServiceImpl meanCalculationService = mock(MeanCalculationServiceImpl.class);

    final PeriodCommand p = mock(PeriodCommand.class);
    when(p.getPeriods()).thenReturn(Set.of("10"));

    doCallRealMethod().when(meanCalculationService).addSpecificChecks(any());
    // ACT
    final CalculationException e = assertThrows(CalculationException.class, () -> meanCalculationService
        .addSpecificChecks(p));

    // VERIFY
    assertEquals(TIME_INTERVAL_PERIOD_LESS_THAN_12.getMessage(), e.getMessage());
  }

  @Test
  void shouldCalculationSpecificChecks_whenCheckResult2() {
    // SETUP
    final MeanCalculationServiceImpl meanCalculationService = mock(MeanCalculationServiceImpl.class);

    final PeriodCommand p = mock(PeriodCommand.class);
    when(p.getPeriods()).thenReturn(Set.of("YEAR_TO_DATE"));

    doCallRealMethod().when(meanCalculationService).addSpecificChecks(any());
    // ACT
    final CalculationException e = assertThrows(CalculationException.class, () -> meanCalculationService
        .addSpecificChecks(p));

    // VERIFY
    assertEquals(TIME_INTERVAL_PERIOD_CONTAINS_YEAR_TO_DATE.getMessage(), e.getMessage());
  }

  @Test
  void shouldCalculationSpecificChecks_whenCheckResult3() {
    // SETUP
    final MeanCalculationServiceImpl meanCalculationService = mock(MeanCalculationServiceImpl.class);

    final PeriodCommand p = mock(PeriodCommand.class);
    when(p.getPeriods()).thenReturn(Set.of("12", "14", "22", "64", SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE
        .name()));

    doCallRealMethod().when(meanCalculationService).addSpecificChecks(any());
    // ACT
    Assertions.assertDoesNotThrow(() -> meanCalculationService.addSpecificChecks(p));

    // VERIFY
  }

  @Test
  void shouldDefineCalculationMethod_whenCheckResult() {
    // SETUP
    final var sut = mock(MeanCalculationServiceImpl.class, withSettings()
        .useConstructor(null, Set.of("12", "36", "60", "120")));
    final var req = mock(PeriodCommand.class);
    final var context = mock(PeriodCalculationInput.class);
    final var expected = new MeanCalculation<>(context, Set.of("12", "36", "60", "120")).setScale(OUTPUT_SCALE);

    when(sut.buildPeriodCalculationInput(any(), any())).thenReturn(context);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    // ACT
    final MeanCalculation actual = sut.defineCalculationMethod(req);

    // VERIFY
    assertEquals(expected, actual);
  }

}
