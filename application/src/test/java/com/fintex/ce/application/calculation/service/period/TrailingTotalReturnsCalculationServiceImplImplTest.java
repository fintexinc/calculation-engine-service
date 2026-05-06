package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class TrailingTotalReturnsCalculationServiceImplImplTest {

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildPeriodCalculationInput() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var set = mock(Set.class);
    final var service = mock(TrailingTotalReturnsCalculationServiceImpl.class,
        withSettings().useConstructor(monthlyReturnsService, set));

    final PeriodCommand req = mock(PeriodCommand.class);

    when(service.buildPeriodCalculationInput(req, ReturnFactorScale.SCALE_OF_TWO)).thenReturn(
        new PeriodCalculationInput());

    doCallRealMethod().when(service).defineCalculationMethod(req);
    // ACT
    service.defineCalculationMethod(req);

    // VERIFY
    verify(service).buildPeriodCalculationInput(req, ReturnFactorScale.SCALE_OF_TWO);
  }

}