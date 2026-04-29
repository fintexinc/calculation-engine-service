package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.RollingStandardDeviationCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.application.util.ReturnFactorScale.SCALE_OF_TWO;
import static com.fintex.ce.application.util.TestConstants.LOCAL_DATE_NOW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class RollingStandardDeviationCalculationServiceImplTest {

  @Test
  void shouldPerform_whenVerifyDefineCalculationMethod() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingStandardDeviationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var command = mock(RollingCalculationCommand.class);
    final var holdings = List.of(mock(PortfolioHolding.class));

    when(command.getHoldings()).thenReturn(holdings);
    when(sut.defineCalculationMethod(command)).thenReturn(mock(RollingStandardDeviationCalculation.class));

    doCallRealMethod().when(sut).perform(any());
    sut.perform(command);

    verify(sut).defineCalculationMethod(command);
  }

  @Test
  void shouldPerform_whenVerifyCalculate() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingStandardDeviationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var command = mock(RollingCalculationCommand.class);
    final var holdings = List.of(mock(PortfolioHolding.class));
    final var rollingCorrelationCalculation = mock(RollingStandardDeviationCalculation.class);
    final var rollingPeriods = Set.of("12");

    when(command.getHoldings()).thenReturn(holdings);
    when(sut.defineCalculationMethod(command)).thenReturn(rollingCorrelationCalculation);
    when(command.getRollingPeriods()).thenReturn(rollingPeriods);

    doCallRealMethod().when(sut).perform(any());
    sut.perform(command);

    verify(rollingCorrelationCalculation).calculate(rollingPeriods);
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildPeriodCalculationInput() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingStandardDeviationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var command = mock(RollingCalculationCommand.class);
    final var input = mock(BenchmarkPeriodCalculationInput.class);

    when(sut.buildPeriodCalculationInput(any(), any())).thenReturn(input);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    sut.defineCalculationMethod(command);

    verify(sut).buildPeriodCalculationInput(command, SCALE_OF_TWO);
  }

  @Test
  void shouldBuildCalculationDto_whenCheckResult() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingStandardDeviationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var command = mock(RollingCalculationCommand.class);
    final var map = mock(TreeMap.class);

    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any())).thenReturn(map);

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(), any());
    final PeriodCalculationInput actual = sut.buildPeriodCalculationInput(command, SCALE_OF_TWO);

    final var expected = new PeriodCalculationInput().setWeightedAveragePortfolioReturns(map);
    assertEquals(expected, actual);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetPortfolioMonthlyReturns() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingStandardDeviationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var holdings = mock(List.class);
    final var command = mock(RollingCalculationCommand.class);

    when(command.getHoldings()).thenReturn(holdings);
    when(command.getCurrency()).thenReturn(Currency.CAD);

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(), any());
    final PeriodCalculationInput actual = sut.buildPeriodCalculationInput(command, SCALE_OF_TWO);

    verify(monthlyReturnsService).getPortfolioMonthlyReturns(holdings, Currency.CAD, SCALE_OF_TWO);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetWeightedAverageWithCpsdAndCpedValidation() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingStandardDeviationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var holdings = mock(List.class);
    final var command = mock(RollingCalculationCommand.class);
    when(command.getHoldings()).thenReturn(holdings);
    when(command.getCustomPsd()).thenReturn(LOCAL_DATE_NOW);
    when(command.getCustomPed()).thenReturn(LOCAL_DATE_NOW.plusMonths(1));

    final ReturnsAggregate monthlyReturnsAggregate = mock(ReturnsAggregate.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturnsAggregate);

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(), any());
    final PeriodCalculationInput actual = sut.buildPeriodCalculationInput(command, SCALE_OF_TWO);

    verify(monthlyReturnsService).getWeightedAverageWithCpsdAndCpedValidation(monthlyReturnsAggregate, LOCAL_DATE_NOW,
        LOCAL_DATE_NOW.plusMonths(1));
  }

}