package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.MeanCalculation;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.fintex.ce.model.util.BigDecimalConstants.OUTPUT_SCALE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class MeanCalculationServiceImplTest {

  @Test
  void shouldBuildMeanCalculation_whenPerformIsCalled() {
    var service = mock(MeanCalculationServiceImpl.class, withSettings()
        .useConstructor(null, null, Set.of("12", "36", "60", "120")));
    var req = mock(PeriodCommand.class);
    var context = mock(PeriodCalculationInput.class);

    when(service.buildPeriodCalculationInput(any(), any(), any())).thenReturn(context);

    doCallRealMethod().when(service).perform(any(), any());
    List<Object> constructorArgs = new ArrayList<>();
    try (var ignored = mockConstruction(MeanCalculation.class,
        (mocked, ctx) -> constructorArgs.addAll(ctx.arguments()))) {
      service.perform(req, PortfolioBenchmarkReturns.EMPTY);
    }

    assertEquals(List.of(context, Set.of("12", "36", "60", "120"), OUTPUT_SCALE), constructorArgs);
  }

}
