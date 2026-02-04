package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.application.service.calculation.period.TrailingTotalReturnsCalculationServiceImpl;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.util.ReturnFactorScale;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class TrailingTotalReturnsCalculationServiceImplImplTest {

  @Test
  void defineCalculationMethod_verifyBuildCalculationDto() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var set = mock(Set.class);
    final var sut = mock(TrailingTotalReturnsCalculationServiceImpl.class,
        withSettings().useConstructor(monthlyReturnsService, set));

    final PeriodCommand req = mock(PeriodCommand.class);

    when(sut.buildCalculationDto(req, ReturnFactorScale.SCALE_OF_TWO)).thenReturn(new CalculationDTO());

    doCallRealMethod().when(sut).defineCalculationMethod(req);
    // ACT
    sut.defineCalculationMethod(req);

    // VERIFY
    verify(sut).buildCalculationDto(req, ReturnFactorScale.SCALE_OF_TWO);
  }

}