package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.RollingStandardDeviationCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.Returns;
import com.fintex.ce.domain.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.dto.command.RollingCalculationCommand;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.application.util.TestConstants.LOCAL_DATE_NOW;
import static com.fintex.ce.util.ReturnFactorScale.SCALE_OF_TWO;
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

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var holdings = List.of(mock(Holding.class));

    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(sut.defineCalculationMethod(reqDTO)).thenReturn(mock(RollingStandardDeviationCalculation.class));

    doCallRealMethod().when(sut).perform(any());
    sut.perform(reqDTO);

    verify(sut).defineCalculationMethod(reqDTO);
  }

  @Test
  void shouldPerform_whenVerifyCalculate() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingStandardDeviationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var holdings = List.of(mock(Holding.class));
    final var rollingCorrelationCalculation = mock(RollingStandardDeviationCalculation.class);
    final var rollingPeriods = Set.of("12");

    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(sut.defineCalculationMethod(reqDTO)).thenReturn(rollingCorrelationCalculation);
    when(reqDTO.getRollingPeriods()).thenReturn(rollingPeriods);

    doCallRealMethod().when(sut).perform(any());
    sut.perform(reqDTO);

    verify(rollingCorrelationCalculation).calculate(rollingPeriods);
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildCalculationDto() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingStandardDeviationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var input = mock(BenchmarkCalculationDTO.class);

    when(sut.buildCalculationDto(any(), any())).thenReturn(input);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    sut.defineCalculationMethod(reqDTO);

    verify(sut).buildCalculationDto(reqDTO, SCALE_OF_TWO);
  }

  @Test
  void shouldBuildCalculationDto_whenCheckResult() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingStandardDeviationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var map = mock(TreeMap.class);

    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any())).thenReturn(map);

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());
    final CalculationDTO actual = sut.buildCalculationDto(reqDTO, SCALE_OF_TWO);

    final var expected = new CalculationDTO().setWeightedAveragePortfolioReturns(map);
    assertEquals(expected, actual);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetPortfolioMonthlyReturns() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingStandardDeviationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var holdings = mock(List.class);
    final var reqDTO = mock(RollingCalculationCommand.class);

    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(reqDTO.getCurrency()).thenReturn(CurrencyType.CAD);

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());
    final CalculationDTO actual = sut.buildCalculationDto(reqDTO, SCALE_OF_TWO);

    verify(monthlyReturnsService).getPortfolioMonthlyReturns(holdings, CurrencyType.CAD, SCALE_OF_TWO);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetWeightedAverageWithCpsdAndCpedValidation() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingStandardDeviationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var holdings = mock(List.class);
    final var reqDTO = mock(RollingCalculationCommand.class);
    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(reqDTO.getCustomPsd()).thenReturn(LOCAL_DATE_NOW);
    when(reqDTO.getCustomPed()).thenReturn(LOCAL_DATE_NOW.plusMonths(1));

    final Returns monthlyReturns = mock(Returns.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturns);

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());
    final CalculationDTO actual = sut.buildCalculationDto(reqDTO, SCALE_OF_TWO);

    verify(monthlyReturnsService).getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, LOCAL_DATE_NOW,
        LOCAL_DATE_NOW.plusMonths(1));
  }

}