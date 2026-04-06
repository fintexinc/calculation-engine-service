package com.fintex.ce.application.calculation.service.period.core;

import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.Returns;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
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
    req.setCurrency(CurrencyType.CAD);
    req.setCustomPed(LocalDate.now());
    req.setCustomIntervalPsd(LocalDate.now().minusMonths(1));

    final Returns portfolioMonthlyReturns = mock(Returns.class);
    final Returns benchmarkMonthlyReturns = mock(Returns.class);
    final Returns portfolio1 = mock(Returns.class);
    final Returns benchmark1 = mock(Returns.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(portfolioMonthlyReturns);
    when(monthlyReturnsService.getBenchmarkMonthlyReturns(anyList(), any(), any())).thenReturn(benchmarkMonthlyReturns);
    when(portfolioMonthlyReturns.cutArgumentToTheSameEndDate(benchmarkMonthlyReturns)).thenReturn(portfolio1);
    when(benchmarkMonthlyReturns.cutArgumentToTheSameEndDate(portfolioMonthlyReturns)).thenReturn(benchmark1);

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());
    sut.buildCalculationDto(req, returnFactorScale);

    verify(portfolioMonthlyReturns).cutArgumentToTheSameEndDate(benchmarkMonthlyReturns);
    verify(benchmarkMonthlyReturns).cutArgumentToTheSameEndDate(portfolioMonthlyReturns);
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
    req.setCurrency(CurrencyType.CAD);
    req.setCustomPed(LocalDate.now());
    req.setCustomIntervalPsd(LocalDate.now().minusMonths(1));

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(mock(Returns.class));
    when(monthlyReturnsService.getBenchmarkMonthlyReturns(anyList(), any(), any())).thenReturn(mock(Returns.class));

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());
    sut.buildCalculationDto(req, returnFactorScale);

    verify(monthlyReturnsService).getPortfolioMonthlyReturns(portfolioHoldings, CurrencyType.CAD, returnFactorScale);
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
    req.setCurrency(CurrencyType.CAD);
    req.setCustomPed(LocalDate.now());
    req.setCustomIntervalPsd(LocalDate.now().minusMonths(1));

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(mock(Returns.class));
    when(monthlyReturnsService.getBenchmarkMonthlyReturns(anyList(), any(), any())).thenReturn(mock(Returns.class));

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());
    sut.buildCalculationDto(req, returnFactorScale);

    verify(monthlyReturnsService).getBenchmarkMonthlyReturns(benchmarkHoldings, CurrencyType.CAD, returnFactorScale);
  }

}