package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.CorrelationCalculation;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.monthlyreturns.Returns;
import com.fintex.ce.util.ReturnFactorScale;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CorrelationServiceImplTest {

  @Test
  void shouldDefineCalculationMethod_whenVerifyGetPortfolioMonthlyReturns() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(CorrelationServiceImpl.class,
        withSettings().useConstructor(Set.of(), monthlyReturnsService));

    final Returns monthlyReturns = mock(Returns.class, RETURNS_DEEP_STUBS);
    final Map baseTotalReturns = mock(Map.class);
    final NavigableMap portfolioTotalReturns = mock(NavigableMap.class);

    final var reqDTO = mock(PeriodCommand.class);
    final List holdings = mock(List.class);
    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(reqDTO.getCustomIntervalPsd()).thenReturn(LocalDate.now());
    when(reqDTO.getCustomPed()).thenReturn(LocalDate.now().minusMonths(1));
    when(reqDTO.getCurrency()).thenReturn(Currency.CAD);

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturns);

    when(monthlyReturns
        .validateCped(reqDTO.getCustomPed())
        .validateReturns()
        .cutByCpedIfCpedEmptyCutByPed(reqDTO.getCustomPed())
        .fxRatesApplied()
        .getReturnsMap()).thenReturn(baseTotalReturns);

    when(monthlyReturns.cutByPsd().getWeightedAverage()).thenReturn(portfolioTotalReturns);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    // ACT
    sut.defineCalculationMethod(reqDTO);

    // VERIFY
    verify(monthlyReturnsService).getPortfolioMonthlyReturns(holdings, Currency.CAD, ReturnFactorScale.SCALE_OF_TWO);
  }

  @Test
  void shouldDefineCalculationMethod_whenCheckResult() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(CorrelationServiceImpl.class,
        withSettings().useConstructor(Set.of(), monthlyReturnsService));

    final Returns monthlyReturns = mock(Returns.class, RETURNS_DEEP_STUBS);
    final Map baseTotalReturns = mock(Map.class);
    final NavigableMap portfolioTotalReturns = mock(NavigableMap.class);

    final var reqDTO = mock(PeriodCommand.class);
    final List holdings = mock(List.class);
    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(reqDTO.getCustomIntervalPsd()).thenReturn(LocalDate.now());
    when(reqDTO.getCustomPed()).thenReturn(LocalDate.now().minusMonths(1));
    when(reqDTO.getCurrency()).thenReturn(Currency.CAD);

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturns);

    when(monthlyReturns
        .validateCped(reqDTO.getCustomPed())
        .cutByCpedIfCpedEmptyCutByPed(reqDTO.getCustomPed())
        .fxRatesApplied()
        .getReturnsMap()).thenReturn(baseTotalReturns);

    when(monthlyReturns.cutByPsd().getWeightedAverage()).thenReturn(portfolioTotalReturns);

    final CalculationDTO calculationDTO = new CalculationDTO()
        .setCipsd(reqDTO.getCustomIntervalPsd())
        .setWeightedAveragePortfolioReturns(portfolioTotalReturns);
    final var expected = new CorrelationCalculation(calculationDTO, baseTotalReturns, Set.of());

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    // ACT
    final var actual = sut.defineCalculationMethod(reqDTO);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyReqDtoSetReqCurrencyToCashHolding() {
    // SETUP
    final MonthlyReturnsService monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(CorrelationServiceImpl.class,
        withSettings().useConstructor(null, monthlyReturnsService));
    final var reqDTO = mock(PeriodCommand.class);
    CalculationDTO inputDTO = mock(CalculationDTO.class);

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any()))
        .thenReturn(mock(Returns.class, RETURNS_SELF));

    when(sut.buildCalculationDto(any(), any())).thenReturn(inputDTO);
    when(inputDTO.getCipsd()).thenReturn(LocalDate.now());

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    // ACT
    sut.defineCalculationMethod(reqDTO);

    // VERIFY
    verify(reqDTO).setReqCurrencyToCashHolding();
  }

  @Test
  void shouldPerform_whenVerifyDefineCalculationMethod() {
    // SETUP
    final var sut = mock(CorrelationServiceImpl.class, withSettings().useConstructor(Set.of(), null));

    final var periodsReqDTO = mock(PeriodCommand.class);

    when(sut.defineCalculationMethod(any())).thenReturn(mock(CorrelationCalculation.class));
    doCallRealMethod().when(sut).perform(any());

    // ACT
    sut.perform(periodsReqDTO);

    // VERIFY
    verify(sut).defineCalculationMethod(periodsReqDTO);
  }

  @Test
  void shouldPerform_whenVerifyCalculate() {
    // SETUP
    final var sut = mock(CorrelationServiceImpl.class, withSettings().useConstructor(Set.of(), null));

    final var periodsReqDTO = mock(PeriodCommand.class);
    final var set = mock(Set.class);
    final var correlationCalculation = mock(CorrelationCalculation.class);

    when(periodsReqDTO.getPeriods()).thenReturn(set);
    when(sut.defineCalculationMethod(any())).thenReturn(correlationCalculation);
    doCallRealMethod().when(sut).perform(any());

    // ACT
    sut.perform(periodsReqDTO);

    // VERIFY
    verify(correlationCalculation).calculate(set);
  }
}