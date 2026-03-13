package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.MeanCalculation;
import com.fintex.ce.application.service.calculation.period.MeanCalculationServiceImpl;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.domain.exception.ReqValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.fintex.ce.domain.enumeration.ExceptionCode.ERR_RRC_TIP_001;
import static com.fintex.ce.domain.enumeration.ExceptionCode.ERR_RRC_TIP_002;
import static com.fintex.ce.domain.enumeration.Period.SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE;
import static com.fintex.ce.util.DecimalUtils.OUTPUT_SCALE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MeanCalculationServiceImplTest {

  @Test
  void shouldCalculationSpecificChecks_whenCheckResult() {
    // SETUP
    final MeanCalculationServiceImpl meanCalculationService = mock(MeanCalculationServiceImpl.class);

    final PeriodCommand p = mock(PeriodCommand.class);
    when(p.getPeriods()).thenReturn(Set.of("10"));

    doCallRealMethod().when(meanCalculationService).addSpecificChecks(any());
    // ACT
    final ReqValidationException e = assertThrows(ReqValidationException.class, () -> meanCalculationService
        .addSpecificChecks(p));

    // VERIFY
    assertEquals(ERR_RRC_TIP_001.getMessage(), e.getMessage());
  }

  @Test
  void shouldCalculationSpecificChecks_whenCheckResult2() {
    // SETUP
    final MeanCalculationServiceImpl meanCalculationService = mock(MeanCalculationServiceImpl.class);

    final PeriodCommand p = mock(PeriodCommand.class);
    when(p.getPeriods()).thenReturn(Set.of("YEAR_TO_DATE"));

    doCallRealMethod().when(meanCalculationService).addSpecificChecks(any());
    // ACT
    final ReqValidationException e = assertThrows(ReqValidationException.class, () -> meanCalculationService
        .addSpecificChecks(p));

    // VERIFY
    assertEquals(ERR_RRC_TIP_002.getMessage(), e.getMessage());
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
    final var calculationDTO = mock(CalculationDTO.class);
    final var expected = new MeanCalculation<>(calculationDTO, Set.of("12", "36", "60", "120")).setScale(OUTPUT_SCALE);

    when(sut.buildCalculationDto(any(), any())).thenReturn(calculationDTO);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    // ACT
    final MeanCalculation actual = sut.defineCalculationMethod(req);

    // VERIFY
    assertEquals(expected, actual);
  }

}
