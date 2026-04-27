package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.RollingSharpeRatioCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
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

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var holdings = List.of(mock(PortfolioHolding.class));

    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(reqDTO.getCurrency()).thenReturn(Currency.CAD);
    when(sut.defineCalculationMethod(reqDTO)).thenReturn(mock(RollingSharpeRatioCalculation.class));

    doCallRealMethod().when(sut).perform(any());
    sut.perform(reqDTO);

    verify(sut).defineCalculationMethod(reqDTO);

  }

  @Test
  void shouldPerform_whenVerifyCalculate() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingSharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, tBillsFetcher, defaultPeriods));

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var holdings = List.of(mock(PortfolioHolding.class));
    final var rollingCorrelationCalculation = mock(RollingSharpeRatioCalculation.class);
    final var rollingPeriods = Set.of("12");

    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(sut.defineCalculationMethod(reqDTO)).thenReturn(rollingCorrelationCalculation);
    when(reqDTO.getRollingPeriods()).thenReturn(rollingPeriods);

    doCallRealMethod().when(sut).perform(any());
    sut.perform(reqDTO);

    verify(rollingCorrelationCalculation).calculate(rollingPeriods);
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildCalculationDto() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingSharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, tBillsFetcher, defaultPeriods));

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var input = mock(BenchmarkCalculationDTO.class);

    when(sut.buildCalculationDto(any(), any())).thenReturn(input);
    when(reqDTO.getCurrency()).thenReturn(Currency.CAD);
    when(tBillsFetcher.fetch(any())).thenReturn(new TreeMap<>());

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    sut.defineCalculationMethod(reqDTO);

    verify(sut).buildCalculationDto(reqDTO, SCALE_OF_ONE);
  }

  @Test
  void shouldBuildCalculationDto_whenCheckResult() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(RollingSharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, mock(TBillsFetcher.class), Set.of()));

    final TreeMap portfolioBaseTotalReturn = mock(TreeMap.class);
    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any())).thenReturn(
        portfolioBaseTotalReturn);

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());

    final CalculationDTO actual = sut.buildCalculationDto(mock(RollingCalculationCommand.class), SCALE_OF_TWO);

    final CalculationDTO expected = new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioBaseTotalReturn);
    assertEquals(expected, actual);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetPortfolioMonthlyReturns() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(RollingSharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, mock(TBillsFetcher.class), Set.of()));

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var holdings = mock(List.class);

    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(reqDTO.getCurrency()).thenReturn(Currency.CAD);

    sut.buildCalculationDto(reqDTO, SCALE_OF_TWO);

    verify(monthlyReturnsService).getPortfolioMonthlyReturns(holdings, Currency.CAD, SCALE_OF_TWO);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetWeightedAverageWithCpsdAndCpedValidation() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(RollingSharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, mock(TBillsFetcher.class), Set.of()));

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var holdings = mock(List.class);
    when(reqDTO.getCustomPsd()).thenReturn(LOCAL_DATE_NOW);
    when(reqDTO.getCustomPed()).thenReturn(LOCAL_DATE_NOW.plusMonths(1));

    final var monthlyReturns = mock(ReturnsAggregate.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturns);

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());

    sut.buildCalculationDto(reqDTO, SCALE_OF_TWO);

    verify(monthlyReturnsService).getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, LOCAL_DATE_NOW,
        LOCAL_DATE_NOW.plusMonths(1));
  }

}