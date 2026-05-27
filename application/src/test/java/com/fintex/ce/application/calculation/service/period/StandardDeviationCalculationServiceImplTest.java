package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.StandardDeviationCalculation;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.fintex.ce.model.util.BigDecimalConstants.OUTPUT_SCALE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class StandardDeviationCalculationServiceImplTest {

  @Test
  void shouldDefineCalculationMethod_whenCheckResult() {
    var service = mock(StandardDeviationCalculationServiceImpl.class, withSettings()
        .useConstructor(null, null, Set.of("12", "36", "60", "120")));
    var req = mock(PeriodCommand.class);
    var context = mock(PeriodCalculationInput.class);
    var expected = StandardDeviationCalculation.builder()
        .input(context)
        .defaultPeriods(Set.of("12", "36", "60", "120"))
        .scale(OUTPUT_SCALE)
        .build();

    when(service.buildPeriodCalculationInput(any(), any())).thenReturn(context);

    doCallRealMethod().when(service).defineCalculationMethod(any());
    StandardDeviationCalculation actual = service.defineCalculationMethod(req);

    assertEquals(expected, actual);
  }

}