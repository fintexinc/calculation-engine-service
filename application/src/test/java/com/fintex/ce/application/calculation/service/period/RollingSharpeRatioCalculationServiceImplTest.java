package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.RollingSharpeRatioCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.ce.port.webclient.sm.TBillsFetcher;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.application.util.ReturnFactorScale.SCALE_OF_ONE;
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

class RollingSharpeRatioCalculationServiceImplTest {

  @Test
  void shouldPerform_whenVerifyDefineCalculationMethod() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingSharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, tBillsFetcher, defaultPeriods));

    final var command = mock(RollingCalculationCommand.class);
    final var holdings = List.of(mock(PortfolioHolding.class));

    when(command.getHoldings()).thenReturn(holdings);
    when(command.getCurrency()).thenReturn(Currency.CAD);
    when(sut.defineCalculationMethod(command)).thenReturn(mock(RollingSharpeRatioCalculation.class));

    doCallRealMethod().when(sut).perform(any());
    sut.perform(command);

    verify(sut).defineCalculationMethod(command);

  }

  @Test
  void shouldPerform_whenVerifyCalculate() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingSharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, tBillsFetcher, defaultPeriods));

    final var command = mock(RollingCalculationCommand.class);
    final var holdings = List.of(mock(PortfolioHolding.class));
    final var rollingCorrelationCalculation = mock(RollingSharpeRatioCalculation.class);
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
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingSharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, tBillsFetcher, defaultPeriods));

    final var command = mock(RollingCalculationCommand.class);
    final var input = mock(BenchmarkPeriodCalculationInput.class);

    when(sut.buildPeriodCalculationInput(any(), any())).thenReturn(input);
    when(command.getCurrency()).thenReturn(Currency.CAD);
    when(tBillsFetcher.fetch(any())).thenReturn(new TreeMap<>());

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    sut.defineCalculationMethod(command);

    verify(sut).buildPeriodCalculationInput(command, SCALE_OF_ONE);
  }

  @Test
  void shouldBuildCalculationDto_whenCheckResult() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(RollingSharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, mock(TBillsFetcher.class), Set.of()));

    final TreeMap portfolioBaseTotalReturn = mock(TreeMap.class);
    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any())).thenReturn(
        portfolioBaseTotalReturn);

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(), any());

    final PeriodCalculationInput actual = sut.buildPeriodCalculationInput(mock(RollingCalculationCommand.class),
        SCALE_OF_TWO);

    final PeriodCalculationInput expected = new PeriodCalculationInput(portfolioBaseTotalReturn);
    assertEquals(expected, actual);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetPortfolioMonthlyReturns() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(RollingSharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, mock(TBillsFetcher.class), Set.of()));

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(), any());

    final var command = mock(RollingCalculationCommand.class);
    final var holdings = mock(List.class);

    when(command.getHoldings()).thenReturn(holdings);
    when(command.getCurrency()).thenReturn(Currency.CAD);

    sut.buildPeriodCalculationInput(command, SCALE_OF_TWO);

    verify(monthlyReturnsService).getPortfolioMonthlyReturns(holdings, Currency.CAD, SCALE_OF_TWO);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetWeightedAverageWithCpsdAndCpedValidation() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(RollingSharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, mock(TBillsFetcher.class), Set.of()));

    final var command = mock(RollingCalculationCommand.class);
    final var holdings = mock(List.class);
    when(command.getCustomPsd()).thenReturn(LOCAL_DATE_NOW);
    when(command.getCustomPed()).thenReturn(LOCAL_DATE_NOW.plusMonths(1));

    final var monthlyReturns = mock(ReturnsAggregate.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturns);

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(), any());

    sut.buildPeriodCalculationInput(command, SCALE_OF_TWO);

    verify(monthlyReturnsService).getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, LOCAL_DATE_NOW,
        LOCAL_DATE_NOW.plusMonths(1));
  }

}