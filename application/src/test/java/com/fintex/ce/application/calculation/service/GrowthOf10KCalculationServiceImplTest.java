package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.Growth10KCalculation;
import com.fintex.ce.application.returns.Returns;
import com.fintex.ce.application.validation.PortfolioCpedDataValidation;
import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.dto.command.ReturnCommand;
import com.fintex.ce.domain.model.CommonDates;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.Growth10KResult;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.fintex.ce.application.util.TestConstants.LOCAL_DATE_NOW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class GrowthOf10KCalculationServiceImplTest {

  @Test
  void shouldPerform_whenVerifyBuildWeightedAverageInputDto() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(GrowthOf10KCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));

    final var returnReqDTO = mock(ReturnCommand.class);
    final var holdings = List.of(mock(Holding.class));
    final var calculationDTO = mock(CalculationDTO.class);
    final var calculation = mock(Growth10KCalculation.class);

    when(returnReqDTO.getHoldings()).thenReturn(holdings);
    when(sut.buildCalculationDto(any())).thenReturn(calculationDTO);
    when(calculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>());
    when(sut.buildGrowth10kCalculation(any(), any())).thenReturn(calculation);

    doCallRealMethod().when(sut).perform(returnReqDTO);
    sut.perform(returnReqDTO);

    verify(sut).buildCalculationDto(returnReqDTO);

  }

  @Test
  void shouldGrowth10KCalculation_whenCalculateCheckResult() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(GrowthOf10KCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));

    final var returnReqDTO = mock(ReturnCommand.class);
    final var holdings = List.of(mock(Holding.class));
    when(returnReqDTO.getHoldings()).thenReturn(holdings);
    when(returnReqDTO.getCurrency()).thenReturn(CurrencyType.CAD);
    when(returnReqDTO.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);

    final var growth10KCalculation = mock(Growth10KCalculation.class, withSettings()
        .useConstructor(new TreeMap<>(), mock(CommonDates.class), false, List.of()));
    final var resDTO = mock(Growth10KResult.class);

    final var calculationDTO = mock(CalculationDTO.class);
    when(sut.buildCalculationDto(any())).thenReturn(calculationDTO);
    when(calculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>());

    when(sut.buildGrowth10kCalculation(returnReqDTO, calculationDTO)).thenReturn(growth10KCalculation);
    when(growth10KCalculation.calculate()).thenReturn(resDTO);

    doCallRealMethod().when(sut).perform(any());
    final Growth10KResult actual = sut.perform(returnReqDTO);

    assertSame(resDTO, actual);

  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenVerifyGetPortfolioMonthlyReturns() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(GrowthOf10KCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));

    final List<Holding> holdings = List.of(mock(Holding.class));

    final Returns monthlyReturns = mock(Returns.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(any(), any(), any())).thenReturn(monthlyReturns);
    when(monthlyReturns.setCpedDataValidation(any())).thenReturn(monthlyReturns);
    when(monthlyReturns.setCpsdDataValidation(any())).thenReturn(monthlyReturns);

    final ReturnCommand returnReqDTO = mock(ReturnCommand.class);
    when(returnReqDTO.getCurrency()).thenReturn(CurrencyType.CAD);
    when(returnReqDTO.getHoldings()).thenReturn(holdings);
    when(returnReqDTO.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
    when(returnReqDTO.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildCalculationDto(any());
    sut.buildCalculationDto(returnReqDTO);

    verify(monthlyReturnsService).getPortfolioMonthlyReturns(
        holdings,
        CurrencyType.CAD,
        ReturnFactorScale.SCALE_OF_TWO);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenCheckResult() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(GrowthOf10KCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));
    final var holdings = List.of(mock(Holding.class));
    final Returns monthlyReturns = mock(Returns.class);

    final NavigableMap portfolioTotalReturns = mock(NavigableMap.class);
    final var expected = new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioTotalReturns).setWarnings(List
        .of());
    when(monthlyReturnsService.getPortfolioMonthlyReturns(any(), any(), any())).thenReturn(monthlyReturns);
    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any())).thenReturn(
        portfolioTotalReturns);
    when(monthlyReturns.setCpedDataValidation(any())).thenReturn(monthlyReturns);
    when(monthlyReturns.setCpsdDataValidation(any())).thenReturn(monthlyReturns);

    final var returnReqDTO = mock(ReturnCommand.class);
    when(returnReqDTO.getCurrency()).thenReturn(CurrencyType.CAD);
    when(returnReqDTO.getHoldings()).thenReturn(holdings);
    when(returnReqDTO.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
    when(returnReqDTO.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildCalculationDto(any());
    final CalculationDTO actual = sut.buildCalculationDto(returnReqDTO);

    assertEquals(expected, actual);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenCheckResultWithWarnings() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(GrowthOf10KCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));
    final var holdings = List.of(mock(Holding.class));
    final Returns monthlyReturns = mock(Returns.class);
    final var warnings = List.of(mock(Warning.class));

    final NavigableMap portfolioTotalReturns = mock(NavigableMap.class);
    final var expected = new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioTotalReturns).setWarnings(
        warnings);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(any(), any(), any())).thenReturn(monthlyReturns);
    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any())).thenReturn(
        portfolioTotalReturns);
    when(monthlyReturns.setCpedDataValidation(any())).thenReturn(monthlyReturns);
    when(monthlyReturns.setCpsdDataValidation(any())).thenReturn(monthlyReturns);
    when(monthlyReturns.getErrorsAsWarnings()).thenReturn(warnings);

    final var returnReqDTO = mock(ReturnCommand.class);
    when(returnReqDTO.getCurrency()).thenReturn(CurrencyType.CAD);
    when(returnReqDTO.getHoldings()).thenReturn(holdings);
    when(returnReqDTO.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
    when(returnReqDTO.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildCalculationDto(any());
    final CalculationDTO actual = sut.buildCalculationDto(returnReqDTO);

    assertEquals(expected, actual);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenVerifyGetWeightedAverageWithCpsdAndCpedValidation() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(GrowthOf10KCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));

    final var holdings = List.of(mock(Holding.class));
    final NavigableMap portfolioTotalReturns = mock(NavigableMap.class);
    final Returns monthlyReturns = mock(Returns.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturns);
    when(monthlyReturns.setCpedDataValidation(any())).thenReturn(monthlyReturns);
    when(monthlyReturns.setCpsdDataValidation(any())).thenReturn(monthlyReturns);

    final var returnReqDTO = mock(ReturnCommand.class);
    when(returnReqDTO.getCurrency()).thenReturn(CurrencyType.CAD);
    when(returnReqDTO.getHoldings()).thenReturn(holdings);
    when(returnReqDTO.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
    when(returnReqDTO.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildCalculationDto(any());
    sut.buildCalculationDto(returnReqDTO);

    verify(monthlyReturnsService).getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns,
        LOCAL_DATE_NOW.minusMonths(2), LOCAL_DATE_NOW);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenVerifyMonthlyReturnsSetValidation() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(GrowthOf10KCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));

    final var holdings = List.of(mock(Holding.class));
    final NavigableMap portfolioTotalReturns = mock(NavigableMap.class);
    final Returns monthlyReturns = mock(Returns.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturns);
    when(monthlyReturns.setCpedDataValidation(any())).thenReturn(monthlyReturns);
    when(monthlyReturns.setCpsdDataValidation(any())).thenReturn(monthlyReturns);

    final var returnReqDTO = mock(ReturnCommand.class);
    when(returnReqDTO.getCurrency()).thenReturn(CurrencyType.CAD);
    when(returnReqDTO.getHoldings()).thenReturn(holdings);
    when(returnReqDTO.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
    when(returnReqDTO.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildCalculationDto(any());
    sut.buildCalculationDto(returnReqDTO);

    verify(monthlyReturns).setCpedDataValidation(new PortfolioCpedDataValidation());
    verify(monthlyReturns).setCpsdDataValidation(new PortfolioCpsdDataValidation());
  }

  @Test
  void shouldBuildGrowth10kCalculation_whenVerifyConstructionGrowth10KCalculation() {
    try (var mockedGrowth10KCalculation = Mockito.mockConstruction(Growth10KCalculation.class);
        var mockedCommonDates = Mockito.mockConstruction(CommonDates.class)) {
      final var sut = mock(GrowthOf10KCalculationServiceImpl.class);
      final var returnReqDTO = mock(ReturnCommand.class);
      final var calculationDTO = mock(CalculationDTO.class);

      doCallRealMethod().when(sut).buildGrowth10kCalculation(any(), any());

      final Growth10KCalculation actual = sut.buildGrowth10kCalculation(returnReqDTO, calculationDTO);

      verify(calculationDTO).getWeightedAveragePortfolioReturns();

      final List<Growth10KCalculation> constructed = mockedGrowth10KCalculation.constructed();

      assertEquals(1, constructed.size());
      assertTrue(constructed.contains(actual));
    }
  }

  @Test
  void shouldBuildGrowth10kCalculation_whenVerifyConstructionCommonDates() {
    try (var mockedCommonDates = Mockito.mockConstruction(CommonDates.class)) {
      final var sut = mock(GrowthOf10KCalculationServiceImpl.class);
      final var returnReqDTO = mock(ReturnCommand.class);
      final var calculationDTO = mock(CalculationDTO.class);

      doCallRealMethod().when(sut).buildGrowth10kCalculation(any(), any());

      final Growth10KCalculation actual = sut.buildGrowth10kCalculation(returnReqDTO, calculationDTO);

      verify(returnReqDTO).getCustomPerformanceStartDate();
      verify(returnReqDTO).getCustomPerformanceEndDate();

      final List<CommonDates> constructed = mockedCommonDates.constructed();

      assertEquals(1, constructed.size());
    }
  }

}