package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.port.output.TBillsPort;
import com.fintex.ce.application.calculation.RollingSharpeRatioCalculation;
import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.RollingCalculationCommand;
import com.fintex.ce.monthlyreturns.Returns;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.util.ReturnFactorScale.SCALE_OF_ONE;
import static com.fintex.ce.util.ReturnFactorScale.SCALE_OF_TWO;
import static com.fintex.ce.util.TestConstants.LOCAL_DATE_NOW;
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
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var tBillsCacheStorage = mock(TBillsPort.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingSharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, tBillsCacheStorage, defaultPeriods));

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var holdings = List.of(mock(Holding.class));

    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(reqDTO.getCurrency()).thenReturn(Currency.CAD);
    when(sut.defineCalculationMethod(reqDTO)).thenReturn(mock(RollingSharpeRatioCalculation.class));

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(reqDTO);

    // VERIFY
    verify(sut).defineCalculationMethod(reqDTO);

  }

  @Test
  void shouldPerform_whenVerifyCalculate() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var tBillsCacheStorage = mock(TBillsPort.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingSharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, tBillsCacheStorage, defaultPeriods));

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var holdings = List.of(mock(Holding.class));
    final var rollingCorrelationCalculation = mock(RollingSharpeRatioCalculation.class);
    final var rollingPeriods = Set.of("12");

    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(sut.defineCalculationMethod(reqDTO)).thenReturn(rollingCorrelationCalculation);
    when(reqDTO.getRollingPeriods()).thenReturn(rollingPeriods);

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(reqDTO);

    // VERIFY
    verify(rollingCorrelationCalculation).calculate(rollingPeriods);
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildCalculationDto() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var tBillsCacheStorage = mock(TBillsPort.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingSharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, tBillsCacheStorage, defaultPeriods));

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var input = mock(BenchmarkCalculationDTO.class);

    when(sut.buildCalculationDto(any(), any())).thenReturn(input);
    when(reqDTO.getCurrency()).thenReturn(Currency.CAD);
    when(tBillsCacheStorage.loadTBillsFor(any())).thenReturn(new TreeMap<>());

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    // ACT
    sut.defineCalculationMethod(reqDTO);

    // VERIFY
    verify(sut).buildCalculationDto(reqDTO, SCALE_OF_ONE);
  }

  @Test
  void shouldBuildCalculationDto_whenCheckResult() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(RollingSharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, mock(TBillsPort.class), Set.of()));

    final TreeMap portfolioBaseTotalReturn = mock(TreeMap.class);
    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any())).thenReturn(
        portfolioBaseTotalReturn);

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());

    // ACT
    final CalculationDTO actual = sut.buildCalculationDto(mock(RollingCalculationCommand.class), SCALE_OF_TWO);

    // VERIFY
    final CalculationDTO expected = new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioBaseTotalReturn);
    assertEquals(expected, actual);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetPortfolioMonthlyReturns() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(RollingSharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, mock(TBillsPort.class), Set.of()));

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var holdings = mock(List.class);

    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(reqDTO.getCurrency()).thenReturn(Currency.CAD);

    // ACT
    sut.buildCalculationDto(reqDTO, SCALE_OF_TWO);

    // VERIFY
    verify(monthlyReturnsService).getPortfolioMonthlyReturns(holdings, Currency.CAD, SCALE_OF_TWO);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetWeightedAverageWithCpsdAndCpedValidation() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(RollingSharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, mock(TBillsPort.class), Set.of()));

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var holdings = mock(List.class);
    when(reqDTO.getCustomPsd()).thenReturn(LOCAL_DATE_NOW);
    when(reqDTO.getCustomPed()).thenReturn(LOCAL_DATE_NOW.plusMonths(1));

    final var monthlyReturns = mock(Returns.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturns);

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());

    // ACT
    sut.buildCalculationDto(reqDTO, SCALE_OF_TWO);

    // VERIFY
    verify(monthlyReturnsService).getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, LOCAL_DATE_NOW,
        LOCAL_DATE_NOW.plusMonths(1));
  }

}