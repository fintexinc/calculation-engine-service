package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.result.risk.InformationRatioResult;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.TreeMap;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class InformationRatioCalculationServiceImplTest {

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildPeriodCalculationInput() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriod = mock(Set.class);
    final var command = mock(PeriodCommand.class);
    final var context = mock(BenchmarkPeriodCalculationInput.class);

    final var sut = mock(InformationRatioCalculationServiceImpl.class,
        withSettings().useConstructor(monthlyReturnsService, defaultPeriod));

    when(context.getCipsd()).thenReturn(LocalDate.MIN);
    when(context.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap());
    when(sut.buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO))
        .thenReturn(context);
    doCallRealMethod().when(sut).defineCalculationMethod(any());

    // ACT
    final PeriodCalculationAbstract<InformationRatioResult, ?> actual = sut.defineCalculationMethod(command);

    // VERIFY
    verify(sut).buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO);
  }

}