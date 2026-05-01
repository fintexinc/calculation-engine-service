package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.Growth10KCalculation;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.application.validation.PortfolioCpedDataValidation;
import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.returns.Growth10KResult;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.ce.model.error.Warning;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.fintex.ce.application.util.TestConstants.LOCAL_DATE_NOW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class GrowthOf10KCalculationServiceImplTest {

  @Test
  void shouldPerform_whenVerifyBuildWeightedAverageInput() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(GrowthOf10KCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));

    final var command = mock(ReturnCommand.class);
    final var holdings = List.of(mock(PortfolioHolding.class));
    final var context = mock(PeriodCalculationInput.class);
    final var calculation = mock(Growth10KCalculation.class);

    when(command.getHoldings()).thenReturn(holdings);
    when(sut.buildPeriodCalculationInput(any())).thenReturn(context);
    when(context.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>());
    when(sut.buildGrowth10kCalculation(any(), any())).thenReturn(calculation);

    doCallRealMethod().when(sut).perform(command);
    sut.perform(command);

    verify(sut).buildPeriodCalculationInput(command);
  }

  @Test
  void shouldGrowth10KCalculation_whenCalculateCheckResult() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(GrowthOf10KCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));

    final var command = mock(ReturnCommand.class);
    final var holdings = List.of(mock(PortfolioHolding.class));
    when(command.getHoldings()).thenReturn(holdings);
    when(command.getCurrency()).thenReturn(Currency.CAD);
    when(command.getCustomPed()).thenReturn(LOCAL_DATE_NOW);

    final var growth10KCalculation = mock(Growth10KCalculation.class, withSettings()
        .useConstructor(new TreeMap<>(), DateRange.UNBOUNDED, false, List.of()));
    final var result = mock(Growth10KResult.class);

    final var context = mock(PeriodCalculationInput.class);
    when(sut.buildPeriodCalculationInput(any())).thenReturn(context);
    when(context.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>());

    when(sut.buildGrowth10kCalculation(command, context)).thenReturn(growth10KCalculation);
    when(growth10KCalculation.calculate()).thenReturn(result);

    doCallRealMethod().when(sut).perform(any());
    final Growth10KResult actual = sut.perform(command);

    assertSame(result, actual);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenVerifyGetPortfolioMonthlyReturns() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(GrowthOf10KCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));

    final List<PortfolioHolding> holdings = List.of(mock(PortfolioHolding.class));

    final ReturnsAggregate monthlyReturnsAggregate = mock(ReturnsAggregate.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(any(), any(), any())).thenReturn(monthlyReturnsAggregate);
    when(monthlyReturnsAggregate.setCpedDataValidation(any())).thenReturn(monthlyReturnsAggregate);
    when(monthlyReturnsAggregate.setCpsdDataValidation(any())).thenReturn(monthlyReturnsAggregate);

    final ReturnCommand command = mock(ReturnCommand.class);
    when(command.getCurrency()).thenReturn(Currency.CAD);
    when(command.getHoldings()).thenReturn(holdings);
    when(command.getCustomPed()).thenReturn(LOCAL_DATE_NOW);
    when(command.getCustomPsd()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any());
    sut.buildPeriodCalculationInput(command);

    verify(monthlyReturnsService).getPortfolioMonthlyReturns(
        holdings,
        Currency.CAD,
        ReturnFactorScale.SCALE_OF_TWO);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenCheckResult() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(GrowthOf10KCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));
    final var holdings = List.of(mock(PortfolioHolding.class));
    final ReturnsAggregate monthlyReturnsAggregate = mock(ReturnsAggregate.class);

    final NavigableMap portfolioTotalReturns = mock(NavigableMap.class);
    final var expected = PeriodCalculationInput.builder()
        .weightedAveragePortfolioReturns(portfolioTotalReturns)
        .warnings(List
            .of())
        .build();
    when(monthlyReturnsService.getPortfolioMonthlyReturns(any(), any(), any())).thenReturn(monthlyReturnsAggregate);
    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any())).thenReturn(
        portfolioTotalReturns);
    when(monthlyReturnsAggregate.setCpedDataValidation(any())).thenReturn(monthlyReturnsAggregate);
    when(monthlyReturnsAggregate.setCpsdDataValidation(any())).thenReturn(monthlyReturnsAggregate);

    final var command = mock(ReturnCommand.class);
    when(command.getCurrency()).thenReturn(Currency.CAD);
    when(command.getHoldings()).thenReturn(holdings);
    when(command.getCustomPed()).thenReturn(LOCAL_DATE_NOW);
    when(command.getCustomPsd()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any());
    final PeriodCalculationInput actual = sut.buildPeriodCalculationInput(command);

    assertEquals(expected, actual);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenCheckResultWithWarnings() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(GrowthOf10KCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));
    final var holdings = List.of(mock(PortfolioHolding.class));
    final ReturnsAggregate monthlyReturnsAggregate = mock(ReturnsAggregate.class);
    final var warnings = List.of(mock(Warning.class));

    final NavigableMap portfolioTotalReturns = mock(NavigableMap.class);
    final var expected = PeriodCalculationInput.builder()
        .weightedAveragePortfolioReturns(portfolioTotalReturns)
        .warnings(warnings)
        .build();
    when(monthlyReturnsService.getPortfolioMonthlyReturns(any(), any(), any())).thenReturn(monthlyReturnsAggregate);
    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any())).thenReturn(
        portfolioTotalReturns);
    when(monthlyReturnsAggregate.setCpedDataValidation(any())).thenReturn(monthlyReturnsAggregate);
    when(monthlyReturnsAggregate.setCpsdDataValidation(any())).thenReturn(monthlyReturnsAggregate);
    when(monthlyReturnsAggregate.getErrorsAsWarnings()).thenReturn(warnings);

    final var command = mock(ReturnCommand.class);
    when(command.getCurrency()).thenReturn(Currency.CAD);
    when(command.getHoldings()).thenReturn(holdings);
    when(command.getCustomPed()).thenReturn(LOCAL_DATE_NOW);
    when(command.getCustomPsd()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any());
    final PeriodCalculationInput actual = sut.buildPeriodCalculationInput(command);

    assertEquals(expected, actual);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenVerifyGetWeightedAverageWithCpsdAndCpedValidation() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(GrowthOf10KCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));

    final var holdings = List.of(mock(PortfolioHolding.class));
    final NavigableMap portfolioTotalReturns = mock(NavigableMap.class);
    final ReturnsAggregate monthlyReturnsAggregate = mock(ReturnsAggregate.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturnsAggregate);
    when(monthlyReturnsAggregate.setCpedDataValidation(any())).thenReturn(monthlyReturnsAggregate);
    when(monthlyReturnsAggregate.setCpsdDataValidation(any())).thenReturn(monthlyReturnsAggregate);

    final var command = mock(ReturnCommand.class);
    when(command.getCurrency()).thenReturn(Currency.CAD);
    when(command.getHoldings()).thenReturn(holdings);
    when(command.getCustomPed()).thenReturn(LOCAL_DATE_NOW);
    when(command.getCustomPsd()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any());
    sut.buildPeriodCalculationInput(command);

    verify(monthlyReturnsService).getWeightedAverageWithCpsdAndCpedValidation(monthlyReturnsAggregate,
        LOCAL_DATE_NOW.minusMonths(2), LOCAL_DATE_NOW);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenVerifyMonthlyReturnsSetValidation() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(GrowthOf10KCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));

    final var holdings = List.of(mock(PortfolioHolding.class));
    final NavigableMap portfolioTotalReturns = mock(NavigableMap.class);
    final ReturnsAggregate monthlyReturnsAggregate = mock(ReturnsAggregate.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturnsAggregate);
    when(monthlyReturnsAggregate.setCpedDataValidation(any())).thenReturn(monthlyReturnsAggregate);
    when(monthlyReturnsAggregate.setCpsdDataValidation(any())).thenReturn(monthlyReturnsAggregate);

    final var command = mock(ReturnCommand.class);
    when(command.getCurrency()).thenReturn(Currency.CAD);
    when(command.getHoldings()).thenReturn(holdings);
    when(command.getCustomPed()).thenReturn(LOCAL_DATE_NOW);
    when(command.getCustomPsd()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any());
    sut.buildPeriodCalculationInput(command);

    verify(monthlyReturnsAggregate).setCpedDataValidation(new PortfolioCpedDataValidation());
    verify(monthlyReturnsAggregate).setCpsdDataValidation(new PortfolioCpsdDataValidation());
  }

  @Test
  void shouldBuildGrowth10kCalculation_whenVerifyConstructionGrowth10KCalculation() {
    try (var mockedGrowth10KCalculation = Mockito.mockConstruction(Growth10KCalculation.class)) {
      final var sut = mock(GrowthOf10KCalculationServiceImpl.class);
      final var command = mock(ReturnCommand.class);
      final var context = mock(PeriodCalculationInput.class);

      doCallRealMethod().when(sut).buildGrowth10kCalculation(any(), any());

      final Growth10KCalculation actual = sut.buildGrowth10kCalculation(command, context);

      verify(context).getWeightedAveragePortfolioReturns();

      final List<Growth10KCalculation> constructed = mockedGrowth10KCalculation.constructed();

      assertEquals(1, constructed.size());
      assertTrue(constructed.contains(actual));
    }
  }

  @Test
  void shouldBuildGrowth10kCalculation_whenVerifyCustomDatesUsed() {
    try (var mockedGrowth10KCalculation = Mockito.mockConstruction(Growth10KCalculation.class)) {
      final var sut = mock(GrowthOf10KCalculationServiceImpl.class);
      final var command = mock(ReturnCommand.class);
      final var context = mock(PeriodCalculationInput.class);

      doCallRealMethod().when(sut).buildGrowth10kCalculation(any(), any());

      sut.buildGrowth10kCalculation(command, context);

      verify(command).getCustomPsd();
      verify(command).getCustomPed();
    }
  }

}
