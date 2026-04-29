package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.RollingCorrelationCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.application.util.ReturnFactorScale.SCALE_OF_TWO;
import static com.fintex.ce.application.util.TestConstants.LOCAL_DATE_NOW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class RollingCorrelationCalculationServiceImplTest {

  @Test
  void shouldPerform_whenVerifyDefineCalculationMethod() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var command = mock(RollingCalculationCommand.class);
    final var holdings = List.of(mock(PortfolioHolding.class));

    when(command.getHoldings()).thenReturn(holdings);
    when(sut.defineCalculationMethod(command)).thenReturn(mock(RollingCorrelationCalculation.class));

    doCallRealMethod().when(sut).perform(any());
    sut.perform(command);

    verify(sut).defineCalculationMethod(command);

  }

  @Test
  void shouldPerform_whenVerifyCalculate() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var command = mock(RollingCalculationCommand.class);
    final var holdings = List.of(mock(PortfolioHolding.class));
    final var rollingCorrelationCalculation = mock(RollingCorrelationCalculation.class);
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
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var command = mock(RollingCalculationCommand.class);
    final var input = mock(BenchmarkPeriodCalculationInput.class);

    when(sut.buildPeriodCalculationInput(any(), any())).thenReturn(input);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    sut.defineCalculationMethod(command);

    verify(sut).buildPeriodCalculationInput(command, SCALE_OF_TWO);
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyGetBaseTotalReturns() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var command = mock(RollingCalculationCommand.class);
    final var input = mock(BenchmarkPeriodCalculationInput.class);

    when(sut.buildPeriodCalculationInput(any(), any())).thenReturn(input);

    doCallRealMethod().when(sut).defineCalculationMethod(any());

    sut.defineCalculationMethod(command);

    verify(sut).getBaseTotalReturns(command);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetPortfolioMonthlyReturns() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var command = mock(RollingCalculationCommand.class);
    final var holdings = mock(List.class);
    when(command.getHoldings()).thenReturn(holdings);
    when(command.getCurrency()).thenReturn(Currency.CAD);

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(mock(
        ReturnsAggregate.class));
    when(monthlyReturnsService.getBenchmarkMonthlyReturns(anyList(), any(), any())).thenReturn(mock(
        ReturnsAggregate.class));
    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(RollingCalculationCommand.class), any(
        ReturnFactorScale.class));

    sut.buildPeriodCalculationInput(command, SCALE_OF_TWO);

    verify(monthlyReturnsService).getPortfolioMonthlyReturns(holdings, Currency.CAD, SCALE_OF_TWO);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetBenchmarkMonthlyReturns() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var command = mock(RollingCalculationCommand.class);
    final var holdings = mock(List.class);
    when(command.getBenchmarkHoldings()).thenReturn(holdings);
    when(command.getCurrency()).thenReturn(Currency.CAD);

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(mock(
        ReturnsAggregate.class));
    when(monthlyReturnsService.getBenchmarkMonthlyReturns(anyList(), any(), any())).thenReturn(mock(
        ReturnsAggregate.class));
    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(RollingCalculationCommand.class), any(
        ReturnFactorScale.class));

    sut.buildPeriodCalculationInput(command, SCALE_OF_TWO);

    verify(monthlyReturnsService).getBenchmarkMonthlyReturns(holdings, Currency.CAD, SCALE_OF_TWO);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyPortfolioMonthlyReturnsCutArgumentToTheSameEndDateWhenPedIsGreater() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var command = mock(RollingCalculationCommand.class);
    final var holdings = mock(List.class);
    when(command.getBenchmarkHoldings()).thenReturn(holdings);
    when(command.getCurrency()).thenReturn(Currency.CAD);

    final var portfolioMonthlyReturns = mock(ReturnsAggregate.class);
    final var benchmarkMonthlyReturns = mock(ReturnsAggregate.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(portfolioMonthlyReturns);
    when(monthlyReturnsService.getBenchmarkMonthlyReturns(anyList(), any(), any())).thenReturn(benchmarkMonthlyReturns);

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(RollingCalculationCommand.class), any(
        ReturnFactorScale.class));

    sut.buildPeriodCalculationInput(command, SCALE_OF_TWO);

    verify(portfolioMonthlyReturns).cutArgumentToTheSameEndDate(benchmarkMonthlyReturns);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyBenchmarkMonthlyReturnsCutArgumentToTheSameEndDateWhenPedIsGreater() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var command = mock(RollingCalculationCommand.class);
    final var holdings = mock(List.class);
    when(command.getBenchmarkHoldings()).thenReturn(holdings);
    when(command.getCurrency()).thenReturn(Currency.CAD);

    final var portfolioMonthlyReturns = mock(ReturnsAggregate.class);
    final var benchmarkMonthlyReturns = mock(ReturnsAggregate.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(portfolioMonthlyReturns);
    when(monthlyReturnsService.getBenchmarkMonthlyReturns(anyList(), any(), any())).thenReturn(benchmarkMonthlyReturns);

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(RollingCalculationCommand.class), any(
        ReturnFactorScale.class));

    sut.buildPeriodCalculationInput(command, SCALE_OF_TWO);

    verify(benchmarkMonthlyReturns).cutArgumentToTheSameEndDate(portfolioMonthlyReturns);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyPortfolioMonthlyReturnsGetWeightedAverageWithCpsdAndCpedValidation() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var command = mock(RollingCalculationCommand.class);
    when(command.getCustomPsd()).thenReturn(LOCAL_DATE_NOW);
    when(command.getCustomPed()).thenReturn(LOCAL_DATE_NOW.plusMonths(1));

    final var portfolioMonthlyReturns = mock(ReturnsAggregate.class);
    final var benchmarkMonthlyReturns = mock(ReturnsAggregate.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(portfolioMonthlyReturns);
    when(monthlyReturnsService.getBenchmarkMonthlyReturns(anyList(), any(), any())).thenReturn(benchmarkMonthlyReturns);

    final var portfolioMonthlyReturnsAfterCut = mock(ReturnsAggregate.class);
    when(portfolioMonthlyReturns.cutArgumentToTheSameEndDate(any())).thenReturn(portfolioMonthlyReturnsAfterCut);

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(RollingCalculationCommand.class), any(
        ReturnFactorScale.class));

    sut.buildPeriodCalculationInput(command, SCALE_OF_TWO);

    verify(monthlyReturnsService).getWeightedAverageWithCpsdAndCpedValidation(
        same(portfolioMonthlyReturns), eq(LOCAL_DATE_NOW), eq(LOCAL_DATE_NOW.plusMonths(1)));
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyBenchmarkMonthlyReturnsGetWeightedAverageWithCpsdAndCpedValidation() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var command = mock(RollingCalculationCommand.class);
    when(command.getCustomPsd()).thenReturn(LOCAL_DATE_NOW);
    when(command.getCustomPed()).thenReturn(LOCAL_DATE_NOW.plusMonths(1));

    final var portfolioMonthlyReturns = mock(ReturnsAggregate.class);
    final var benchmarkMonthlyReturns = mock(ReturnsAggregate.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(portfolioMonthlyReturns);
    when(monthlyReturnsService.getBenchmarkMonthlyReturns(anyList(), any(), any())).thenReturn(benchmarkMonthlyReturns);

    final var benchmarkMonthlyReturnsAfterCut = mock(ReturnsAggregate.class);
    when(benchmarkMonthlyReturns.cutArgumentToTheSameEndDate(any())).thenReturn(benchmarkMonthlyReturnsAfterCut);

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(RollingCalculationCommand.class), any(
        ReturnFactorScale.class));

    sut.buildPeriodCalculationInput(command, SCALE_OF_TWO);

    verify(monthlyReturnsService).getWeightedAverageWithCpsdAndCpedValidation(
        same(benchmarkMonthlyReturns), eq(LOCAL_DATE_NOW), eq(LOCAL_DATE_NOW.plusMonths(1)));
  }

  @Test
  void shouldBuildCalculationDto_whenCheckResult() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var command = mock(RollingCalculationCommand.class);
    when(command.getCustomPsd()).thenReturn(LOCAL_DATE_NOW);
    when(command.getCustomPed()).thenReturn(LOCAL_DATE_NOW.plusMonths(1));
    when(command.getCustomIntervalPsd()).thenReturn(LOCAL_DATE_NOW.plusDays(3));

    final var portfolioMonthlyReturns = mock(ReturnsAggregate.class);
    final var benchmarkMonthlyReturns = mock(ReturnsAggregate.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(portfolioMonthlyReturns);
    when(monthlyReturnsService.getBenchmarkMonthlyReturns(anyList(), any(), any())).thenReturn(benchmarkMonthlyReturns);

    final var portfolioMonthlyReturnsAfterCut = mock(ReturnsAggregate.class);
    final var benchmarkMonthlyReturnsAfterCut = mock(ReturnsAggregate.class);
    when(portfolioMonthlyReturns.cutArgumentToTheSameEndDate(any())).thenReturn(benchmarkMonthlyReturnsAfterCut);
    when(benchmarkMonthlyReturns.cutArgumentToTheSameEndDate(any())).thenReturn(portfolioMonthlyReturnsAfterCut);

    final var portfolioTotalReturns = mock(TreeMap.class);
    final var benchmarkTotalReturns = mock(TreeMap.class);
    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(
        portfolioMonthlyReturns, LOCAL_DATE_NOW, LOCAL_DATE_NOW.plusMonths(1)))
        .thenReturn(portfolioTotalReturns);
    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(
        benchmarkMonthlyReturns, LOCAL_DATE_NOW, LOCAL_DATE_NOW.plusMonths(1)))
        .thenReturn(benchmarkTotalReturns);

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(RollingCalculationCommand.class), any(
        ReturnFactorScale.class));

    final var actual = sut.buildPeriodCalculationInput(command, SCALE_OF_TWO);

    final var expected = new BenchmarkPeriodCalculationInput()
        .setWeightedAverageBenchmarkReturns(benchmarkTotalReturns)
        .setWeightedAveragePortfolioReturns(portfolioTotalReturns)
        .setCipsd(LOCAL_DATE_NOW.plusDays(3));

    assertEquals(expected, actual);
  }

  @Test
  void shouldGetBaseTotalReturns_whenVerifyGetPortfolioMonthlyReturns() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var holdings = mock(List.class);
    final var command = mock(RollingCalculationCommand.class);
    when(command.getHoldings()).thenReturn(holdings);
    when(command.getCurrency()).thenReturn(Currency.CAD);

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(mock(
        ReturnsAggregate.class,
        RETURNS_SELF));

    doCallRealMethod().when(sut).getBaseTotalReturns(any());

    sut.getBaseTotalReturns(command);

    verify(monthlyReturnsService).getPortfolioMonthlyReturns(same(holdings), eq(Currency.CAD), eq(SCALE_OF_TWO));
  }

  @Test
  void shouldGetBaseTotalReturns_whenVerifyGetMonthlyReturns() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var holdings = mock(List.class);
    final var command = mock(RollingCalculationCommand.class);
    when(command.getHoldings()).thenReturn(holdings);
    when(command.getCurrency()).thenReturn(Currency.CAD);

    final ReturnsAggregate monthlyReturnsAggregate = mock(ReturnsAggregate.class, RETURNS_SELF);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturnsAggregate);

    doCallRealMethod().when(sut).getBaseTotalReturns(any());

    sut.getBaseTotalReturns(command);

    verify(monthlyReturnsAggregate).getReturnsMap();
  }

  @Test
  void shouldGetBaseTotalReturns_whenCheckResult() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var holdings = mock(List.class);
    final var command = mock(RollingCalculationCommand.class);
    when(command.getHoldings()).thenReturn(holdings);
    when(command.getCurrency()).thenReturn(Currency.CAD);

    final ReturnsAggregate monthlyReturnsAggregate = mock(ReturnsAggregate.class, RETURNS_DEEP_STUBS);
    final Map baseTotalReturn = mock(Map.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturnsAggregate);
    when(monthlyReturnsAggregate
        .validateCped(command.getCustomPed())
        .cutByCpedIfCpedEmptyCutByPed(command.getCustomPed())
        .fxRatesApplied()
        .getReturnsMap()).thenReturn(baseTotalReturn);

    doCallRealMethod().when(sut).getBaseTotalReturns(any());

    final Map<PortfolioHolding, Map<LocalDate, BigDecimal>> actual = sut.getBaseTotalReturns(command);

    assertSame(baseTotalReturn, actual);
  }

}