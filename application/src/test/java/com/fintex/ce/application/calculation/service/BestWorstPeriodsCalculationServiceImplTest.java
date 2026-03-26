package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.BestWorstPeriodCalculation;
import com.fintex.ce.application.returns.Returns;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.dto.command.BestWorstPeriodsCommand;
import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.BestWorstPeriodsResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import static com.fintex.ce.application.util.TestConstants.LOCAL_DATE_NOW;
import static com.fintex.ce.util.ReturnFactorScale.SCALE_OF_TWO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class BestWorstPeriodsCalculationServiceImplTest {

  @Test
  void shouldPerform_whenCheckResult() {
    // SETUP
    final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class, withSettings()
        .useConstructor((MonthlyReturnsService) null));

    final var calculationDTO = mock(CalculationDTO.class);
    final var bestWorstPeriodCommand = mock(BestWorstPeriodsCommand.class);
    final var holdings = List.of(mock(Holding.class));
    final var portfolioTotalReturns = mock(TreeMap.class);
    final BestWorstPeriodCalculation bestWorstPeriodCalculation = mock(BestWorstPeriodCalculation.class, withSettings()
        .useConstructor(portfolioTotalReturns, Set.of()));

    final BestWorstPeriodsResult expected = mock(BestWorstPeriodsResult.class);
    when(bestWorstPeriodCalculation.calculate()).thenReturn(expected);
    when(sut.buildWeightedAverageInputDto(any())).thenReturn(calculationDTO);
    when(calculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>());
    when(bestWorstPeriodCommand.getHoldings()).thenReturn(holdings);
    when(sut.buildBestWorstPeriodCalculation(any(), any())).thenReturn(bestWorstPeriodCalculation);

    doCallRealMethod().when(sut).perform(bestWorstPeriodCommand);
    // ACT
    final BestWorstPeriodsResult actual = sut.perform(bestWorstPeriodCommand);

    // VERIFY
    assertEquals(expected, actual);

  }

  @Test
  void shouldPerform_whenVerifyBuildWeightedAverageInputDto() {
    // SETUP
    final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class, withSettings()
        .useConstructor((MonthlyReturnsService) null));

    final var calculationDTO = mock(CalculationDTO.class);
    final var portfolioTotalReturns = mock(TreeMap.class);
    final var bestWorstPeriodCommand = mock(BestWorstPeriodsCommand.class);
    final var holdings = List.of(mock(Holding.class));
    final BestWorstPeriodCalculation bestWorstPeriodCalculation = mock(BestWorstPeriodCalculation.class, withSettings()
        .useConstructor(portfolioTotalReturns, Set.of()));

    when(sut.buildWeightedAverageInputDto(any())).thenReturn(calculationDTO);
    when(calculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>());
    when(bestWorstPeriodCommand.getHoldings()).thenReturn(holdings);
    when(sut.buildBestWorstPeriodCalculation(any(), any())).thenReturn(bestWorstPeriodCalculation);

    doCallRealMethod().when(sut).perform(bestWorstPeriodCommand);
    // ACT
    sut.perform(bestWorstPeriodCommand);

    // VERIFY
    verify(sut).buildWeightedAverageInputDto(bestWorstPeriodCommand);

  }

  @Test
  void shouldPerform_whenVerifyBuildCalculation() {
    // SETUP
    final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class, withSettings()
        .useConstructor((MonthlyReturnsService) null));

    final var calculationDTO = mock(CalculationDTO.class);
    final var portfolioTotalReturns = mock(TreeMap.class);
    final var holdings = List.of(mock(Holding.class));
    final var bestWorstPeriodCommand = mock(BestWorstPeriodsCommand.class);
    final BestWorstPeriodCalculation bestWorstPeriodCalculation = mock(BestWorstPeriodCalculation.class, withSettings()
        .useConstructor(portfolioTotalReturns, Set.of()));

    when(bestWorstPeriodCommand.getHoldings()).thenReturn(holdings);
    when(sut.buildWeightedAverageInputDto(any())).thenReturn(calculationDTO);
    when(calculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>());
    when(sut.buildBestWorstPeriodCalculation(any(), any())).thenReturn(bestWorstPeriodCalculation);

    doCallRealMethod().when(sut).perform(bestWorstPeriodCommand);
    // ACT
    sut.perform(bestWorstPeriodCommand);

    // VERIFY
    verify(sut).buildBestWorstPeriodCalculation(bestWorstPeriodCommand, calculationDTO);
  }

  @Test
  void shouldBuildBestWorstPeriodCalculation_whenVerifyGetPeriods() {
    // SETUP
    final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class);

    final var calculationDTO = mock(CalculationDTO.class);
    final var portfolioTotalReturns = mock(TreeMap.class);
    final var holdings = List.of(mock(Holding.class));
    final var bestWorstPeriodCommand = mock(BestWorstPeriodsCommand.class);

    when(bestWorstPeriodCommand.getHoldings()).thenReturn(holdings);
    when(sut.buildWeightedAverageInputDto(any())).thenReturn(calculationDTO);
    when(calculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>());

    doCallRealMethod().when(sut).buildBestWorstPeriodCalculation(any(), any());
    // ACT
    sut.buildBestWorstPeriodCalculation(bestWorstPeriodCommand, calculationDTO);

    // VERIFY
    verify(sut).getPeriods(bestWorstPeriodCommand);
  }

  @Test
  void shouldBuildBestWorstPeriodCalculation_whenCheckResult() {
    // SETUP
    final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class);

    final var calculationDTO = mock(CalculationDTO.class);
    final var holdings = List.of(mock(Holding.class));
    final var bestWorstPeriodCommand = mock(BestWorstPeriodsCommand.class);

    when(bestWorstPeriodCommand.getHoldings()).thenReturn(holdings);
    when(sut.buildWeightedAverageInputDto(any())).thenReturn(calculationDTO);
    when(calculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>(Map.of(LocalDate.now(),
        BigDecimal.ONE)));
    final BestWorstPeriodCalculation expected = new BestWorstPeriodCalculation(calculationDTO
        .getWeightedAveragePortfolioReturns(),
        sut.getPeriods(bestWorstPeriodCommand));

    doCallRealMethod().when(sut).buildBestWorstPeriodCalculation(any(), any());
    // ACT
    final BestWorstPeriodCalculation actual = sut.buildBestWorstPeriodCalculation(bestWorstPeriodCommand,
        calculationDTO);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGetPeriods_whenCheckResultWithCustomPeriods() {
    // SETUP
    final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class, withSettings()
        .useConstructor((MonthlyReturnsService) null));

    final var bestWorstPeriodCommand = mock(BestWorstPeriodsCommand.class);
    final var customPeriods = Set.of(12L, 24L);
    when(bestWorstPeriodCommand.getBestWorstTimeIntervalPeriods()).thenReturn(customPeriods);

    doCallRealMethod().when(sut).getPeriods(any());
    // ACT
    final Set<Long> periods = sut.getPeriods(bestWorstPeriodCommand);

    // VERIFY
    assertSame(customPeriods, periods);

  }

  @Test
  void shouldGetPeriods_whenCheckResultWithoutCustomPeriods() {
    // SETUP
    final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class, withSettings()
        .useConstructor((MonthlyReturnsService) null));

    final var bestWorstPeriodCommand = mock(BestWorstPeriodsCommand.class);
    sut.defaultPeriods = Set.of(12L, 24L, 36L);

    when(bestWorstPeriodCommand.getBestWorstTimeIntervalPeriods()).thenReturn(null);

    doCallRealMethod().when(sut).getPeriods(any());
    // ACT
    final Set<Long> periods = sut.getPeriods(bestWorstPeriodCommand);

    // VERIFY
    assertSame(sut.defaultPeriods, periods);

  }

  @Test
  void shouldBestWorstPeriodCalculation_whenCalculateCheckResult() throws Exception {
    // SETUP
    final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class, withSettings()
        .useConstructor((MonthlyReturnsService) null));

    final var calculationDTO = mock(CalculationDTO.class);
    when(sut.buildWeightedAverageInputDto(any())).thenReturn(calculationDTO);
    when(calculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>());

    final var bestWorstPeriodCommand = mock(BestWorstPeriodsCommand.class);
    final var holdings = List.of(mock(Holding.class));
    final var bestWorstPeriodCalculation = mock(BestWorstPeriodCalculation.class);
    final var resDTO = mock(BestWorstPeriodsResult.class);

    when(bestWorstPeriodCommand.getHoldings()).thenReturn(holdings);
    when(bestWorstPeriodCommand.getCurrency()).thenReturn(Currency.CAD);
    when(bestWorstPeriodCommand.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
    when(bestWorstPeriodCommand.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(1));
    when(sut.buildBestWorstPeriodCalculation(any(), any())).thenReturn(bestWorstPeriodCalculation);
    when(bestWorstPeriodCalculation.calculate()).thenReturn(resDTO);

    doCallRealMethod().when(sut).perform(any());
    // ACT
    final BestWorstPeriodsResult bestWorstPeriodsResponseDTO = sut.perform(bestWorstPeriodCommand);

    // VERIFY
    assertSame(resDTO, bestWorstPeriodsResponseDTO);

  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenVerifyGetPortfolioMonthlyReturns() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));

    final var holdings = List.of(mock(Holding.class));

    final var bestWorstPeriodCommand = mock(BestWorstPeriodsCommand.class);
    when(bestWorstPeriodCommand.getCurrency()).thenReturn(Currency.CAD);
    when(bestWorstPeriodCommand.getHoldings()).thenReturn(holdings);
    when(bestWorstPeriodCommand.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
    when(bestWorstPeriodCommand.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildWeightedAverageInputDto(any());
    // ACT
    sut.buildWeightedAverageInputDto(bestWorstPeriodCommand);

    // VERIFY
    verify(monthlyReturnsService).getPortfolioMonthlyReturns(holdings, Currency.CAD, SCALE_OF_TWO);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenVerifyGetWeightedAverageWithCpsdAndCpedValidation() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));

    final var holdings = List.of(mock(Holding.class));

    final var monthlyReturns = mock(Returns.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(any(), any(), any())).thenReturn(monthlyReturns);
    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any())).thenReturn(mock(
        NavigableMap.class));

    final var bestWorstPeriodCommand = mock(BestWorstPeriodsCommand.class);
    when(bestWorstPeriodCommand.getCurrency()).thenReturn(Currency.CAD);
    when(bestWorstPeriodCommand.getHoldings()).thenReturn(holdings);
    when(bestWorstPeriodCommand.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
    when(bestWorstPeriodCommand.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildWeightedAverageInputDto(any());
    // ACT
    sut.buildWeightedAverageInputDto(bestWorstPeriodCommand);

    // VERIFY
    verify(monthlyReturnsService).getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns,
        LOCAL_DATE_NOW.minusMonths(2), LOCAL_DATE_NOW);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenCheckResult() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(BestWorstPeriodsCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService));

    final var holdings = List.of(mock(Holding.class));

    final var portfolioTotalReturns = mock(NavigableMap.class);
    final var expected = new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioTotalReturns);
    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any())).thenReturn(
        portfolioTotalReturns);

    final var bestWorstPeriodCommand = mock(BestWorstPeriodsCommand.class);
    when(bestWorstPeriodCommand.getCurrency()).thenReturn(Currency.CAD);
    when(bestWorstPeriodCommand.getHoldings()).thenReturn(holdings);
    when(bestWorstPeriodCommand.getCustomPerformanceEndDate()).thenReturn(LOCAL_DATE_NOW);
    when(bestWorstPeriodCommand.getCustomPerformanceStartDate()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildWeightedAverageInputDto(any());
    // ACT
    final CalculationDTO actual = sut.buildWeightedAverageInputDto(bestWorstPeriodCommand);

    // VERIFY
    assertEquals(expected, actual);
  }

}
