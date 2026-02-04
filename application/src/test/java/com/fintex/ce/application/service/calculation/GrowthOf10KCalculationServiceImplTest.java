package com.fintex.ce.application.service.calculation;

import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.application.calculation.Growth10KCalculation;
import com.fintex.ce.application.validation.PortfolioCpedDataValidation;
import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.monthlyreturns.Returns;
import com.fintex.ce.domain.model.CommonDates;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.application.command.ReturnCommand;
import com.fintex.ce.application.result.Growth10KResult;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.application.service.calculation.GrowthOf10KCalculationServiceImpl;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.util.ReturnFactorScale;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.util.TestConstants.LOCAL_DATE_NOW;
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
  void perform_verifyBuildWeightedAverageInputDto() {
    // SETUP
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
    // ACT
    sut.perform(returnReqDTO);

    // VERIFY
    verify(sut).buildCalculationDto(returnReqDTO);

  }

  @Test
  void growth10KCalculation_calculateCheckResult() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(GrowthOf10KCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));

    final var returnReqDTO = mock(ReturnCommand.class);
    final var holdings = List.of(mock(Holding.class));
    when(returnReqDTO.getHoldings()).thenReturn(holdings);
    when(returnReqDTO.getCurrency()).thenReturn(Currency.CAD);
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
    // ACT
    final Growth10KResult actual = sut.perform(returnReqDTO);

    // VERIFY
    assertSame(resDTO, actual);

  }

  @Test
  void buildWeightedAverageInputDto_verifyGetPortfolioMonthlyReturns() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(GrowthOf10KCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));

    final List<Holding> holdings = List.of(mock(Holding.class));

    final Returns monthlyReturns = mock(Returns.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(any(), any(), any())).thenReturn(monthlyReturns);
    when(monthlyReturns.setCpedDataValidation(any())).thenReturn(monthlyReturns);
    when(monthlyReturns.setCpsdDataValidation(any())).thenReturn(monthlyReturns);

    final ReturnCommand returnReqDTO = mock(ReturnCommand.class);
    when(returnReqDTO.getCurrency()).thenReturn(Currency.CAD);
    when(returnReqDTO.getHoldings()).thenReturn(holdings);
    when(returnReqDTO.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
    when(returnReqDTO.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildCalculationDto(any());
    // ACT
    sut.buildCalculationDto(returnReqDTO);

    // VERIFY
    verify(monthlyReturnsService).getPortfolioMonthlyReturns(
        holdings,
        Currency.CAD,
        ReturnFactorScale.SCALE_OF_TWO);
  }

  @Test
  void buildWeightedAverageInputDto_checkResult() {
    // SETUP
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
    when(returnReqDTO.getCurrency()).thenReturn(Currency.CAD);
    when(returnReqDTO.getHoldings()).thenReturn(holdings);
    when(returnReqDTO.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
    when(returnReqDTO.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildCalculationDto(any());
    // ACT
    final CalculationDTO actual = sut.buildCalculationDto(returnReqDTO);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void buildWeightedAverageInputDto_checkResultWithWarnings() {
    // SETUP
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
    when(returnReqDTO.getCurrency()).thenReturn(Currency.CAD);
    when(returnReqDTO.getHoldings()).thenReturn(holdings);
    when(returnReqDTO.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
    when(returnReqDTO.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildCalculationDto(any());
    // ACT
    final CalculationDTO actual = sut.buildCalculationDto(returnReqDTO);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void buildWeightedAverageInputDto_verifyGetWeightedAverageWithCpsdAndCpedValidation() {
    // SETUP
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
    when(returnReqDTO.getCurrency()).thenReturn(Currency.CAD);
    when(returnReqDTO.getHoldings()).thenReturn(holdings);
    when(returnReqDTO.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
    when(returnReqDTO.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildCalculationDto(any());
    // ACT
    sut.buildCalculationDto(returnReqDTO);

    // VERIFY
    verify(monthlyReturnsService).getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns,
        LOCAL_DATE_NOW.minusMonths(2), LOCAL_DATE_NOW);
  }

  @Test
  void buildWeightedAverageInputDto_verifyMonthlyReturnsSetValidation() {
    // SETUP
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
    when(returnReqDTO.getCurrency()).thenReturn(Currency.CAD);
    when(returnReqDTO.getHoldings()).thenReturn(holdings);
    when(returnReqDTO.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
    when(returnReqDTO.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildCalculationDto(any());
    // ACT
    sut.buildCalculationDto(returnReqDTO);

    // VERIFY
    verify(monthlyReturns).setCpedDataValidation(new PortfolioCpedDataValidation());
    verify(monthlyReturns).setCpsdDataValidation(new PortfolioCpsdDataValidation());
  }

  @Test
  void buildGrowth10kCalculation_verifyConstructionGrowth10KCalculation() {
    try (var mockedGrowth10KCalculation = Mockito.mockConstruction(Growth10KCalculation.class);
        var mockedCommonDates = Mockito.mockConstruction(CommonDates.class)) {
      // SETUP
      final var sut = mock(GrowthOf10KCalculationServiceImpl.class);
      final var returnReqDTO = mock(ReturnCommand.class);
      final var calculationDTO = mock(CalculationDTO.class);

      doCallRealMethod().when(sut).buildGrowth10kCalculation(any(), any());

      // ACT
      final Growth10KCalculation actual = sut.buildGrowth10kCalculation(returnReqDTO, calculationDTO);

      // VERIFY
      verify(calculationDTO).getWeightedAveragePortfolioReturns();

      final List<Growth10KCalculation> constructed = mockedGrowth10KCalculation.constructed();

      assertEquals(1, constructed.size());
      assertTrue(constructed.contains(actual));
    }
  }

  @Test
  void buildGrowth10kCalculation_verifyConstructionCommonDates() {
    try (var mockedCommonDates = Mockito.mockConstruction(CommonDates.class)) {
      // SETUP
      final var sut = mock(GrowthOf10KCalculationServiceImpl.class);
      final var returnReqDTO = mock(ReturnCommand.class);
      final var calculationDTO = mock(CalculationDTO.class);

      doCallRealMethod().when(sut).buildGrowth10kCalculation(any(), any());

      // ACT
      final Growth10KCalculation actual = sut.buildGrowth10kCalculation(returnReqDTO, calculationDTO);

      // VERIFY
      verify(returnReqDTO).getCustomPerformanceStartDate();
      verify(returnReqDTO).getCustomPerformanceEndDate();

      final List<CommonDates> constructed = mockedCommonDates.constructed();

      assertEquals(1, constructed.size());
    }
  }

}