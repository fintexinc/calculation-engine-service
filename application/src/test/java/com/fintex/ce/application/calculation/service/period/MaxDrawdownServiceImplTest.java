package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.Growth10KCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class MaxDrawdownServiceImplTest {

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildCalculationDto() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(MaxDrawdownServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var benchmarkCalculationDTO = mock(CalculationDTO.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>(Map.of(LocalDate.now(),
        BigDecimal.TEN));

    final var req = mock(PeriodCommand.class);
    when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
    when(req.getCurrency()).thenReturn(CurrencyType.CAD);
    when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(sut).defineCalculationMethod(req);
    sut.defineCalculationMethod(req);

    verify(sut).buildCalculationDto(req, ReturnFactorScale.SCALE_OF_TWO);
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyInitializeGrowthOf10KMap() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(MaxDrawdownServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var benchmarkCalculationDTO = mock(CalculationDTO.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>(Map.of(LocalDate.now(),
        BigDecimal.TEN));

    final var req = mock(PeriodCommand.class);
    when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
    when(req.getCurrency()).thenReturn(CurrencyType.CAD);
    when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(sut).defineCalculationMethod(req);
    sut.defineCalculationMethod(req);

    verify(sut).initializeGrowthOf10KMap(eq(benchmarkCalculationDTO), any());
  }

  @Test
  void shouldInitializeGrowthOf10KMap_whenCheckResult() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(MaxDrawdownServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var inputDTO = mock(CalculationDTO.class);
    final var weightedAverageReturns = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.TEN));
    final var growth10KCalculation = new Growth10KCalculation(null, null, false);

    when(inputDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(sut).initializeGrowthOf10KMap(any(), any());
    final NavigableMap<LocalDate, BigDecimal> actual = sut.initializeGrowthOf10KMap(inputDTO, growth10KCalculation);

    assertNotNull(actual.entrySet().stream().findFirst());
  }

  @Test
  void shouldInitializeGrowthOf10KMap_whenCheckResult2() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(MaxDrawdownServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var inputDTO = mock(CalculationDTO.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>();
    final var growth10KCalculation = new Growth10KCalculation(null, null, false);

    when(inputDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(sut).initializeGrowthOf10KMap(any(), any());
    final NavigableMap<LocalDate, BigDecimal> actual = sut.initializeGrowthOf10KMap(inputDTO, growth10KCalculation);

    assertFalse(actual.entrySet().stream().findFirst().isPresent());
  }
}
