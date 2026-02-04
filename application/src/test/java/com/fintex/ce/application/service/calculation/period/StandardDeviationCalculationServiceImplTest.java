package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.StandardDeviationCalculation;
import com.fintex.ce.application.service.calculation.period.StandardDeviationCalculationServiceImpl;
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

class StandardDeviationCalculationServiceImplTest {

  @Test
  void calculationSpecificChecks_checkResult() {
    // SETUP
    final StandardDeviationCalculationServiceImpl standardDeviationCalculationService = mock(
        StandardDeviationCalculationServiceImpl.class);

    final PeriodCommand p = mock(PeriodCommand.class);
    when(p.getPeriods()).thenReturn(Set.of("10"));

    doCallRealMethod().when(standardDeviationCalculationService).addSpecificChecks(any());
    // ACT
    final ReqValidationException e = assertThrows(ReqValidationException.class,
        () -> standardDeviationCalculationService.addSpecificChecks(p));

    // VERIFY
    assertEquals(ERR_RRC_TIP_001.getMessage(), e.getMessage());
  }

  @Test
  void calculationSpecificChecks_checkResult2() {
    // SETUP
    final StandardDeviationCalculationServiceImpl standardDeviationCalculationService = mock(
        StandardDeviationCalculationServiceImpl.class);

    final PeriodCommand p = mock(PeriodCommand.class);
    when(p.getPeriods()).thenReturn(Set.of("YEAR_TO_DATE"));

    doCallRealMethod().when(standardDeviationCalculationService).addSpecificChecks(any());
    // ACT
    final ReqValidationException e = assertThrows(ReqValidationException.class,
        () -> standardDeviationCalculationService.addSpecificChecks(p));

    // VERIFY
    assertEquals(ERR_RRC_TIP_002.getMessage(), e.getMessage());
  }

  @Test
  void calculationSpecificChecks_checkResult3() {
    // SETUP
    final StandardDeviationCalculationServiceImpl standardDeviationCalculationService = mock(
        StandardDeviationCalculationServiceImpl.class);

    final PeriodCommand p = mock(PeriodCommand.class);
    when(p.getPeriods()).thenReturn(Set.of("12", "14", "22", "64", SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE
        .name()));

    doCallRealMethod().when(standardDeviationCalculationService).addSpecificChecks(any());
    // ACT
    Assertions.assertDoesNotThrow(() -> standardDeviationCalculationService.addSpecificChecks(p));

    // VERIFY
  }

  @Test
  void defineCalculationMethod_checkResult() {
    // SETUP
    final var sut = mock(StandardDeviationCalculationServiceImpl.class, withSettings()
        .useConstructor(null, Set.of("12", "36", "60", "120")));
    final var req = mock(PeriodCommand.class);
    final var calculationDTO = mock(CalculationDTO.class);
    final var expected = new StandardDeviationCalculation(calculationDTO, Set.of("12", "36", "60", "120")).setScale(
        OUTPUT_SCALE);

    when(sut.buildCalculationDto(any(), any())).thenReturn(calculationDTO);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    // ACT
    final StandardDeviationCalculation actual = sut.defineCalculationMethod(req);

    // VERIFY
    assertEquals(expected, actual);
  }

}
