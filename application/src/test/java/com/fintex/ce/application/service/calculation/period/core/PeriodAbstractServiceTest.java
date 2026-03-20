package com.fintex.ce.application.service.calculation.period.core;

import com.fintex.ce.application.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.model.result.PeriodResult;
import com.fintex.ce.monthlyreturns.Returns;
import com.fintex.ce.util.ReturnFactorScale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PeriodAbstractServiceTest {

  @Test
  void shouldPerform_whenVerifyDefineCalculationMethod() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(PeriodAbstractService.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var dto = mock(PeriodCommand.class);
    when(sut.defineCalculationMethod(any())).thenReturn(mock(PeriodCalculationAbstract.class));

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(dto);

    // VERIFY
    verify(sut).defineCalculationMethod(dto);
  }

  @Test
  void shouldPerform_whenVerifyCalculate() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(PeriodAbstractService.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var dto = mock(PeriodCommand.class);
    final var periods = Set.of("e");
    when(dto.getPeriods()).thenReturn(periods);

    final var pCalculation = mock(PeriodCalculationAbstract.class);
    when(sut.defineCalculationMethod(any())).thenReturn(pCalculation);

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(dto);

    // VERIFY
    verify(pCalculation).calculate(periods);
  }

  @Test
  void shouldPerform_whenCheckResult() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(PeriodAbstractService.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var dto = mock(PeriodCommand.class);
    final var periods = Set.of("e");
    when(dto.getPeriods()).thenReturn(periods);

    final var pCalculation = mock(PeriodCalculationAbstract.class);
    when(sut.defineCalculationMethod(any())).thenReturn(pCalculation);

    final var expected = mock(PeriodResult.class);
    when(pCalculation.calculate(any())).thenReturn(expected);

    doCallRealMethod().when(sut).perform(any());
    // ACT
    final PeriodResult actual = sut.perform(dto);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

  @Test
  void shouldBuildCalculationDto_whenCheckResult() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(PeriodAbstractService.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var req = mock(PeriodCommand.class);
    final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;
    final var monthlyReturns = mock(Returns.class);
    final var portfolioTotalReturns = mock(NavigableMap.class);

    when(req.getHoldings()).thenReturn(new ArrayList<>());
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(req.getCustomPed()).thenReturn(LocalDate.now().plusMonths(5));
    when(req.getCustomIntervalPsd()).thenReturn(LocalDate.now().minusMonths(3));

    when(monthlyReturnsService.getPortfolioMonthlyReturns(req.getHoldings(), Currency.CAD,
        ReturnFactorScale.SCALE_OF_TWO)).thenReturn(monthlyReturns);
    when(monthlyReturnsService.getWeightedAverageWithCpedValidation(monthlyReturns, req.getCustomPed())).thenReturn(
        portfolioTotalReturns);

    final CalculationDTO expected = new CalculationDTO();
    expected.setCipsd(req.getCustomIntervalPsd());
    expected.setWeightedAveragePortfolioReturns(portfolioTotalReturns);

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());
    // ACT
    final CalculationDTO actual = sut.buildCalculationDto(req, returnFactorScale);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetPortfolioMonthlyReturns() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(PeriodAbstractService.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var req = mock(PeriodCommand.class);
    final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;
    final var monthlyReturns = mock(Returns.class);
    final var portfolioTotalReturns = mock(NavigableMap.class);

    when(req.getHoldings()).thenReturn(new ArrayList<>());
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(req.getCustomPed()).thenReturn(LocalDate.now().plusMonths(5));
    when(req.getCustomIntervalPsd()).thenReturn(LocalDate.now().minusMonths(3));

    when(monthlyReturnsService.getPortfolioMonthlyReturns(req.getHoldings(), Currency.CAD,
        ReturnFactorScale.SCALE_OF_TWO)).thenReturn(monthlyReturns);
    when(monthlyReturnsService.getWeightedAverageWithCpedValidation(monthlyReturns, req.getCustomPed())).thenReturn(
        portfolioTotalReturns);

    final CalculationDTO expected = new CalculationDTO();
    expected.setCipsd(req.getCustomIntervalPsd());
    expected.setWeightedAveragePortfolioReturns(portfolioTotalReturns);

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());
    // ACT
    sut.buildCalculationDto(req, returnFactorScale);

    // VERIFY
    verify(monthlyReturnsService).getPortfolioMonthlyReturns(req.getHoldings(), Currency.CAD,
        ReturnFactorScale.SCALE_OF_TWO);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetWeightedAverageWithCpedValidation() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(PeriodAbstractService.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var req = mock(PeriodCommand.class);
    final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;
    final var monthlyReturns = mock(Returns.class);
    final var portfolioTotalReturns = mock(NavigableMap.class);

    when(req.getHoldings()).thenReturn(new ArrayList<>());
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(req.getCustomPed()).thenReturn(LocalDate.now().plusMonths(5));
    when(req.getCustomIntervalPsd()).thenReturn(LocalDate.now().minusMonths(3));

    when(monthlyReturnsService.getPortfolioMonthlyReturns(req.getHoldings(), Currency.CAD,
        ReturnFactorScale.SCALE_OF_TWO)).thenReturn(monthlyReturns);
    when(monthlyReturnsService.getWeightedAverageWithCpedValidation(monthlyReturns, req.getCustomPed())).thenReturn(
        portfolioTotalReturns);

    final CalculationDTO expected = new CalculationDTO();
    expected.setCipsd(req.getCustomIntervalPsd());
    expected.setWeightedAveragePortfolioReturns(portfolioTotalReturns);

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());
    // ACT
    sut.buildCalculationDto(req, returnFactorScale);

    // VERIFY
    verify(monthlyReturnsService).getWeightedAverageWithCpedValidation(monthlyReturns, req.getCustomPed());
  }

}