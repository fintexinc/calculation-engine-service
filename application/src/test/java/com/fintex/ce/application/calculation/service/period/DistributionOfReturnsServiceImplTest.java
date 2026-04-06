package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.Returns;
import com.fintex.ce.domain.constant.BigDecimalConstants;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.dto.command.DistributionOfReturnsCommand;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
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

    final var reqDTO = mock(DistributionOfReturnsCommand.class);
    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(reqDTO.getCurrency()).thenReturn(CurrencyType.CAD);

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());
    sut.buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);

    verify(monthlyReturnsService).getPortfolioMonthlyReturns(reqDTO.getHoldings(), CurrencyType.CAD,
        ReturnFactorScale.SCALE_OF_TWO);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetWeightedAverageWithCpsdAndCpedValidation() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(DistributionOfReturnsServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));
    final List holdings = mock(List.class);
    final Returns monthlyReturns = mock(Returns.class);

    final var reqDTO = mock(DistributionOfReturnsCommand.class);
    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(reqDTO.getCurrency()).thenReturn(CurrencyType.CAD);
    when(reqDTO.getCustomPsd()).thenReturn(LocalDate.now());
    when(reqDTO.getCustomPed()).thenReturn(LocalDate.now().minusMonths(1));

    when(monthlyReturnsService.getPortfolioMonthlyReturns(reqDTO.getHoldings(), reqDTO.getCurrency(),
        ReturnFactorScale.SCALE_OF_TWO)).thenReturn(monthlyReturns);

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());
    sut.buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);

    verify(monthlyReturnsService).getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, reqDTO.getCustomPsd(),
        reqDTO.getCustomPed());
  }

  @Test
  void shouldBuildCalculationDto_whenCheckResult() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(DistributionOfReturnsServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));
    final List holdings = mock(List.class);
    final Returns monthlyReturns = mock(Returns.class);
    final NavigableMap portfolioTotalReturns = mock(NavigableMap.class);

    final var reqDTO = mock(DistributionOfReturnsCommand.class);
    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(reqDTO.getCurrency()).thenReturn(CurrencyType.CAD);
    when(reqDTO.getCustomPsd()).thenReturn(LocalDate.now());
    when(reqDTO.getCustomPed()).thenReturn(LocalDate.now().minusMonths(1));
    when(reqDTO.getCustomPed()).thenReturn(LocalDate.now().minusMonths(2));

    when(monthlyReturnsService.getPortfolioMonthlyReturns(reqDTO.getHoldings(), reqDTO.getCurrency(),
        ReturnFactorScale.SCALE_OF_TWO)).thenReturn(monthlyReturns);
    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, reqDTO.getCustomPsd(), reqDTO
        .getCustomPed())).thenReturn(portfolioTotalReturns);

    final CalculationDTO expected = new CalculationDTO();
    expected.setCipsd(reqDTO.getCustomIntervalPsd());
    expected.setWeightedAveragePortfolioReturns(portfolioTotalReturns);

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());
    final CalculationDTO actual = sut.buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);

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