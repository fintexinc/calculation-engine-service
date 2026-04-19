package com.fintex.ce.application.calculation.service.period.core;

import com.fintex.ce.application.calculation.service.period.UpsideCaptureCalculationServiceImpl;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.exceptions.CalculationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.fintex.ce.model.domain.enumeration.Period.SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE;
import static com.fintex.ce.model.error.ErrorCode.TIME_INTERVAL_PERIOD_CONTAINS_YEAR_TO_DATE;
import static com.fintex.ce.model.error.ErrorCode.TIME_INTERVAL_PERIOD_LESS_THAN_12;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpDownSideCalculationTest {

  @Test
  void shouldCalculationSpecificChecks_whenCheckResult() {
    // SETUP
    final UpsideCaptureCalculationServiceImpl u = mock(UpsideCaptureCalculationServiceImpl.class);

    final PeriodCommand p = mock(PeriodCommand.class);
    when(p.getPeriods()).thenReturn(Set.of("11"));

    doCallRealMethod().when(u).addSpecificChecks(any());
    // ACT
    final CalculationException e = assertThrows(CalculationException.class, () -> u.addSpecificChecks(p));

    // VERIFY
    assertEquals(TIME_INTERVAL_PERIOD_LESS_THAN_12.getMessage(), e.getMessage());
  }

  @Test
  void shouldCalculationSpecificChecks_whenCheckResult2() {
    // SETUP
    final UpsideCaptureCalculationServiceImpl u = mock(UpsideCaptureCalculationServiceImpl.class);

    final PeriodCommand p = mock(PeriodCommand.class);
    when(p.getPeriods()).thenReturn(Set.of("YEAR_TO_DATE"));

    doCallRealMethod().when(u).addSpecificChecks(any());
    // ACT
    final CalculationException e = assertThrows(CalculationException.class, () -> u.addSpecificChecks(p));

    // VERIFY
    assertEquals(TIME_INTERVAL_PERIOD_CONTAINS_YEAR_TO_DATE.getMessage(), e.getMessage());
  }

  @Test
  void shouldCalculationSpecificChecks_whenCheckResult3() {
    // SETUP
    final UpsideCaptureCalculationServiceImpl u = mock(UpsideCaptureCalculationServiceImpl.class);

    final PeriodCommand p = mock(PeriodCommand.class);
    when(p.getPeriods()).thenReturn(Set.of("12", SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name()));

    doCallRealMethod().when(u).addSpecificChecks(any());
    // ACT
    Assertions.assertDoesNotThrow(() -> u.addSpecificChecks(p));

    // VERIFY
  }

  @Test
  void shouldCalculationSpecificChecks_whenCheckResult4() {
    // SETUP
    final UpsideCaptureCalculationServiceImpl u = mock(UpsideCaptureCalculationServiceImpl.class);

    final PeriodCommand p = mock(PeriodCommand.class);
    when(p.getPeriods()).thenReturn(Set.of());

    doCallRealMethod().when(u).addSpecificChecks(p);
    // ACT
    Assertions.assertDoesNotThrow(() -> u.addSpecificChecks(p));

    // VERIFY
  }

}