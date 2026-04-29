package com.fintex.ce.application.calculation.service.period.core;

import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class PeriodBenchmarkAbstractServiceTest {

  @Test
  void buildWeightedAverageInputDto_verifyСutArgumentToTheSameEndDateWhenPedIsGreater() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(PeriodBenchmarkAbstractService.class);
    sut.monthlyReturnsService = monthlyReturnsService;

    final var portfolioHoldings = mock(List.class);
    final var benchmarkHoldings = mock(List.class);
    final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;

    final PeriodCommand req = new PeriodCommand();
    req.setHoldings(portfolioHoldings);
    req.setBenchmarkHoldings(benchmarkHoldings);
    req.setCurrency(Currency.CAD);
    req.setCustomPed(LocalDate.now());
    req.setCustomIntervalPsd(LocalDate.now().minusMonths(1));

    final ReturnsAggregate portfolioMonthlyReturnsAggregate = mock(ReturnsAggregate.class);
    final ReturnsAggregate benchmarkMonthlyReturnsAggregate = mock(ReturnsAggregate.class);
    final ReturnsAggregate portfolio1 = mock(ReturnsAggregate.class);
    final ReturnsAggregate benchmark1 = mock(ReturnsAggregate.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(
        portfolioMonthlyReturnsAggregate);
    when(monthlyReturnsService.getBenchmarkMonthlyReturns(anyList(), any(), any())).thenReturn(
        benchmarkMonthlyReturnsAggregate);
    when(portfolioMonthlyReturnsAggregate.cutArgumentToTheSameEndDate(benchmarkMonthlyReturnsAggregate)).thenReturn(
        portfolio1);
    when(benchmarkMonthlyReturnsAggregate.cutArgumentToTheSameEndDate(portfolioMonthlyReturnsAggregate)).thenReturn(
        benchmark1);

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(), any());
    sut.buildPeriodCalculationInput(req, returnFactorScale);

    verify(portfolioMonthlyReturnsAggregate).cutArgumentToTheSameEndDate(benchmarkMonthlyReturnsAggregate);
    verify(benchmarkMonthlyReturnsAggregate).cutArgumentToTheSameEndDate(portfolioMonthlyReturnsAggregate);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenVerifyGetPortfolioMonthlyReturns() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(PeriodBenchmarkAbstractService.class);
    sut.monthlyReturnsService = monthlyReturnsService;

    final var portfolioHoldings = mock(List.class);
    final var benchmarkHoldings = mock(List.class);
    final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;

    final PeriodCommand req = new PeriodCommand();
    req.setHoldings(portfolioHoldings);
    req.setBenchmarkHoldings(benchmarkHoldings);
    req.setCurrency(Currency.CAD);
    req.setCustomPed(LocalDate.now());
    req.setCustomIntervalPsd(LocalDate.now().minusMonths(1));

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(mock(
        ReturnsAggregate.class));
    when(monthlyReturnsService.getBenchmarkMonthlyReturns(anyList(), any(), any())).thenReturn(mock(
        ReturnsAggregate.class));

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(), any());
    sut.buildPeriodCalculationInput(req, returnFactorScale);

    verify(monthlyReturnsService).getPortfolioMonthlyReturns(portfolioHoldings, Currency.CAD, returnFactorScale);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenVerifyGetBenchmarkMonthlyReturns() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(PeriodBenchmarkAbstractService.class, withSettings().useConstructor(monthlyReturnsService,
        null));

    final var portfolioHoldings = mock(List.class);
    final var benchmarkHoldings = mock(List.class);
    final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;

    final PeriodCommand req = new PeriodCommand();
    req.setHoldings(portfolioHoldings);
    req.setBenchmarkHoldings(benchmarkHoldings);
    req.setCurrency(Currency.CAD);
    req.setCustomPed(LocalDate.now());
    req.setCustomIntervalPsd(LocalDate.now().minusMonths(1));

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(mock(
        ReturnsAggregate.class));
    when(monthlyReturnsService.getBenchmarkMonthlyReturns(anyList(), any(), any())).thenReturn(mock(
        ReturnsAggregate.class));

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(), any());
    sut.buildPeriodCalculationInput(req, returnFactorScale);

    verify(monthlyReturnsService).getBenchmarkMonthlyReturns(benchmarkHoldings, Currency.CAD, returnFactorScale);
  }

}