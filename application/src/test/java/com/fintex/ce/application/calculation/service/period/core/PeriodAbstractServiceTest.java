package com.fintex.ce.application.calculation.service.period.core;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class PeriodAbstractServiceTest {

  @Test
  void shouldPerform_whenVerifyDefineCalculationMethod() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(PeriodAbstractService.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var dto = mock(PeriodCommand.class);
    when(sut.defineCalculationMethod(any())).thenReturn(mock(PeriodCalculationAbstract.class));

    doCallRealMethod().when(sut).perform(any());
    sut.perform(dto);

    verify(sut).defineCalculationMethod(dto);
  }

  @Test
  void shouldPerform_whenVerifyCalculate() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(PeriodAbstractService.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var dto = mock(PeriodCommand.class);
    final var periods = Set.of("e");
    when(dto.getPeriods()).thenReturn(periods);

    final var pCalculation = mock(PeriodCalculationAbstract.class);
    when(sut.defineCalculationMethod(any())).thenReturn(pCalculation);

    doCallRealMethod().when(sut).perform(any());
    sut.perform(dto);

    verify(pCalculation).calculate(periods);
  }

  @Test
  void shouldPerform_whenCheckResult() {
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
    final PeriodResult actual = sut.perform(dto);

    Assertions.assertSame(expected, actual);
  }

  @Test
  void shouldBuildCalculationDto_whenCheckResult() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(PeriodAbstractService.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var req = mock(PeriodCommand.class);
    final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;
    final var monthlyReturns = mock(ReturnsAggregate.class);
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
    final CalculationDTO actual = sut.buildCalculationDto(req, returnFactorScale);

    assertEquals(expected, actual);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetPortfolioMonthlyReturns() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(PeriodAbstractService.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var req = mock(PeriodCommand.class);
    final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;
    final var monthlyReturns = mock(ReturnsAggregate.class);
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
    sut.buildCalculationDto(req, returnFactorScale);

    verify(monthlyReturnsService).getPortfolioMonthlyReturns(req.getHoldings(), Currency.CAD,
        ReturnFactorScale.SCALE_OF_TWO);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetWeightedAverageWithCpedValidation() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(PeriodAbstractService.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var req = mock(PeriodCommand.class);
    final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;
    final var monthlyReturns = mock(ReturnsAggregate.class);
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
    sut.buildCalculationDto(req, returnFactorScale);

    verify(monthlyReturnsService).getWeightedAverageWithCpedValidation(monthlyReturns, req.getCustomPed());
  }

}