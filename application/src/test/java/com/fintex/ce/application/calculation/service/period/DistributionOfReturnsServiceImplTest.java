package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.dto.command.DistributionOfReturnsCommand;
import com.fintex.ce.model.util.BigDecimalConstants;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class DistributionOfReturnsServiceImplTest {

  @Test
  void shouldBuildCalculationDto_whenVerifyGetPortfolioTotalReturns() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(DistributionOfReturnsServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));
    final List holdings = mock(List.class);

    final var command = mock(DistributionOfReturnsCommand.class);
    when(command.getHoldings()).thenReturn(holdings);
    when(command.getCurrency()).thenReturn(Currency.CAD);

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(), any());
    sut.buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO);

    verify(monthlyReturnsService).getPortfolioMonthlyReturns(command.getHoldings(), Currency.CAD,
        ReturnFactorScale.SCALE_OF_TWO);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetWeightedAverageWithCpsdAndCpedValidation() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(DistributionOfReturnsServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));
    final List holdings = mock(List.class);
    final ReturnsAggregate monthlyReturnsAggregate = mock(ReturnsAggregate.class);

    final var command = mock(DistributionOfReturnsCommand.class);
    when(command.getHoldings()).thenReturn(holdings);
    when(command.getCurrency()).thenReturn(Currency.CAD);
    when(command.getCustomPsd()).thenReturn(LocalDate.now());
    when(command.getCustomPed()).thenReturn(LocalDate.now().minusMonths(1));

    when(monthlyReturnsService.getPortfolioMonthlyReturns(command.getHoldings(), command.getCurrency(),
        ReturnFactorScale.SCALE_OF_TWO)).thenReturn(monthlyReturnsAggregate);

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(), any());
    sut.buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO);

    verify(monthlyReturnsService).getWeightedAverageWithCpsdAndCpedValidation(monthlyReturnsAggregate, command
        .getCustomPsd(),
        command.getCustomPed());
  }

  @Test
  void shouldBuildCalculationDto_whenCheckResult() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(DistributionOfReturnsServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));
    final List holdings = mock(List.class);
    final ReturnsAggregate monthlyReturnsAggregate = mock(ReturnsAggregate.class);
    final NavigableMap portfolioTotalReturns = mock(NavigableMap.class);

    final var command = mock(DistributionOfReturnsCommand.class);
    when(command.getHoldings()).thenReturn(holdings);
    when(command.getCurrency()).thenReturn(Currency.CAD);
    when(command.getCustomPsd()).thenReturn(LocalDate.now());
    when(command.getCustomPed()).thenReturn(LocalDate.now().minusMonths(1));
    when(command.getCustomPed()).thenReturn(LocalDate.now().minusMonths(2));

    when(monthlyReturnsService.getPortfolioMonthlyReturns(command.getHoldings(), command.getCurrency(),
        ReturnFactorScale.SCALE_OF_TWO)).thenReturn(monthlyReturnsAggregate);
    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(monthlyReturnsAggregate, command
        .getCustomPsd(), command
            .getCustomPed())).thenReturn(portfolioTotalReturns);

    final PeriodCalculationInput expected = new PeriodCalculationInput();
    expected.setCipsd(command.getCustomIntervalPsd());
    expected.setWeightedAveragePortfolioReturns(portfolioTotalReturns);

    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(), any());
    final PeriodCalculationInput actual = sut.buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO);

    assertEquals(expected, actual);
  }

  private TreeMap<LocalDate, BigDecimal> getPortfolioTotalReturns() {
    final var portfolioTotalReturns = new TreeMap<LocalDate, BigDecimal>();
    portfolioTotalReturns.put(LocalDate.now().minusMonths(8), BigDecimalConstants.HUNDRED);
    portfolioTotalReturns.put(LocalDate.now().minusMonths(9), BigDecimalConstants.TWELVE);
    portfolioTotalReturns.put(LocalDate.now().minusMonths(10), BigDecimalConstants.TEN_THOUSAND);
    portfolioTotalReturns.put(LocalDate.now().minusMonths(11), BigDecimalConstants.HUNDRED);
    portfolioTotalReturns.put(LocalDate.now().minusMonths(12), BigDecimalConstants.HUNDRED);
    portfolioTotalReturns.put(LocalDate.now().minusMonths(13), BigDecimalConstants.HUNDRED);
    portfolioTotalReturns.put(LocalDate.now().minusMonths(14), BigDecimalConstants.HUNDRED);
    portfolioTotalReturns.put(LocalDate.now().minusMonths(15), BigDecimalConstants.HUNDRED);
    portfolioTotalReturns.put(LocalDate.now().minusMonths(16), BigDecimalConstants.HUNDRED);
    portfolioTotalReturns.put(LocalDate.now().minusMonths(17), BigDecimalConstants.HUNDRED);
    portfolioTotalReturns.put(LocalDate.now().minusMonths(18), BigDecimalConstants.HUNDRED);
    portfolioTotalReturns.put(LocalDate.now().minusMonths(19), BigDecimalConstants.HUNDRED);
    return portfolioTotalReturns;
  }
}