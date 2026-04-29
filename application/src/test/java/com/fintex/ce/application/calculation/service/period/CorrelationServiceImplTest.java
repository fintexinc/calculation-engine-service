package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.CorrelationCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class CorrelationServiceImplTest {

  @Test
  void shouldDefineCalculationMethod_whenVerifyGetPortfolioMonthlyReturns() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(CorrelationServiceImpl.class,
        withSettings().useConstructor(Set.of(), monthlyReturnsService));

    final ReturnsAggregate monthlyReturnsAggregate = mock(ReturnsAggregate.class, RETURNS_DEEP_STUBS);
    final Map baseTotalReturns = mock(Map.class);
    final NavigableMap portfolioTotalReturns = mock(NavigableMap.class);

    final var command = mock(PeriodCommand.class);
    final List holdings = mock(List.class);
    when(command.getHoldings()).thenReturn(holdings);
    when(command.getCustomIntervalPsd()).thenReturn(LocalDate.now());
    when(command.getCustomPed()).thenReturn(LocalDate.now().minusMonths(1));
    when(command.getCurrency()).thenReturn(Currency.CAD);

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturnsAggregate);

    when(monthlyReturnsAggregate
        .validateCped(command.getCustomPed())
        .validateReturns()
        .cutByCpedIfCpedEmptyCutByPed(command.getCustomPed())
        .fxRatesApplied()
        .getReturnsMap()).thenReturn(baseTotalReturns);

    when(monthlyReturnsAggregate.cutByPsd().getWeightedAverage()).thenReturn(portfolioTotalReturns);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    sut.defineCalculationMethod(command);

    verify(monthlyReturnsService).getPortfolioMonthlyReturns(holdings, Currency.CAD,
        ReturnFactorScale.SCALE_OF_TWO);
  }

  @Test
  void shouldDefineCalculationMethod_whenCheckResult() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(CorrelationServiceImpl.class,
        withSettings().useConstructor(Set.of(), monthlyReturnsService));

    final ReturnsAggregate monthlyReturnsAggregate = mock(ReturnsAggregate.class, RETURNS_DEEP_STUBS);
    final Map baseTotalReturns = mock(Map.class);
    final NavigableMap portfolioTotalReturns = mock(NavigableMap.class);

    final var command = mock(PeriodCommand.class);
    final List holdings = mock(List.class);
    when(command.getHoldings()).thenReturn(holdings);
    when(command.getCustomIntervalPsd()).thenReturn(LocalDate.now());
    when(command.getCustomPed()).thenReturn(LocalDate.now().minusMonths(1));
    when(command.getCurrency()).thenReturn(Currency.CAD);

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturnsAggregate);

    when(monthlyReturnsAggregate
        .validateCped(command.getCustomPed())
        .cutByCpedIfCpedEmptyCutByPed(command.getCustomPed())
        .fxRatesApplied()
        .getReturnsMap()).thenReturn(baseTotalReturns);

    when(monthlyReturnsAggregate.cutByPsd().getWeightedAverage()).thenReturn(portfolioTotalReturns);

    final PeriodCalculationInput context = new PeriodCalculationInput()
        .setCipsd(command.getCustomIntervalPsd())
        .setWeightedAveragePortfolioReturns(portfolioTotalReturns);
    final var expected = new CorrelationCalculation(context, baseTotalReturns, Set.of());

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    final var actual = sut.defineCalculationMethod(command);

    assertEquals(expected, actual);
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyReqDtoSetReqCurrencyToCashHolding() {
    final MonthlyReturnsService monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(CorrelationServiceImpl.class,
        withSettings().useConstructor(null, monthlyReturnsService));
    final var command = mock(PeriodCommand.class);
    PeriodCalculationInput context = mock(PeriodCalculationInput.class);

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any()))
        .thenReturn(mock(ReturnsAggregate.class, RETURNS_SELF));

    when(sut.buildPeriodCalculationInput(any(), any())).thenReturn(context);
    when(context.getCipsd()).thenReturn(LocalDate.now());

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    sut.defineCalculationMethod(command);

    verify(command).setReqCurrencyToCashHolding();
  }

  @Test
  void shouldPerform_whenVerifyDefineCalculationMethod() {
    final var sut = mock(CorrelationServiceImpl.class, withSettings().useConstructor(Set.of(), null));

    final var command = mock(PeriodCommand.class);

    when(sut.defineCalculationMethod(any())).thenReturn(mock(CorrelationCalculation.class));
    doCallRealMethod().when(sut).perform(any());

    sut.perform(command);

    verify(sut).defineCalculationMethod(command);
  }

  @Test
  void shouldPerform_whenVerifyCalculate() {
    final var sut = mock(CorrelationServiceImpl.class, withSettings().useConstructor(Set.of(), null));

    final var command = mock(PeriodCommand.class);
    final var set = mock(Set.class);
    final var correlationCalculation = mock(CorrelationCalculation.class);

    when(command.getPeriods()).thenReturn(set);
    when(sut.defineCalculationMethod(any())).thenReturn(correlationCalculation);
    doCallRealMethod().when(sut).perform(any());

    sut.perform(command);

    verify(correlationCalculation).calculate(set);
  }
}