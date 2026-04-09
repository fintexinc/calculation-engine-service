package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.domain.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.TBillsFetcher;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.sm.model.domain.enumeration.CurrencyType.CAD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class TreynorRatioServiceImplTest {

  TreynorRatioServiceImplTest() {
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildCalculationDto() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var sut = mock(TreynorRatioServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, tBillsFetcher, Set.of()));

    final var benchmarkCalculationDTO = mock(BenchmarkCalculationDTO.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>();

    final var req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(CAD);
    when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
    when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
    when(benchmarkCalculationDTO.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);
    when(tBillsFetcher.fetch(any())).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(sut).defineCalculationMethod(req);
    sut.defineCalculationMethod(req);

    verify(sut, times(2)).buildCalculationDto(eq(req), any());
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyLoadTBillsFor() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var sut = mock(TreynorRatioServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, tBillsFetcher, Set.of()));

    final var benchmarkCalculationDTO = mock(BenchmarkCalculationDTO.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>();

    final var req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(CAD);
    when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
    when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
    when(benchmarkCalculationDTO.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);
    when(tBillsFetcher.fetch(any())).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(sut).defineCalculationMethod(req);
    sut.defineCalculationMethod(req);

    verify(tBillsFetcher).fetch(CAD);
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyCalculateExcessReturn() {
    try (var mockedPeriodCalculationAbstract = Mockito.mockStatic(PeriodCalculationAbstract.class)) {
      final var monthlyReturnsService = mock(MonthlyReturnsService.class);
      final var tBillsFetcher = mock(TBillsFetcher.class);
      final var sut = mock(TreynorRatioServiceImpl.class, withSettings()
          .useConstructor(monthlyReturnsService, tBillsFetcher, Set.of()));

      final var benchmarkCalculationDTO = mock(BenchmarkCalculationDTO.class);
      final TreeMap<LocalDate, BigDecimal> treeMap = new TreeMap<>();
      final var req = mock(PeriodCommand.class);

      when(req.getCurrency()).thenReturn(CAD);
      when(tBillsFetcher.fetch(any())).thenReturn(treeMap);
      when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
      when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(treeMap);
      when(benchmarkCalculationDTO.getWeightedAverageBenchmarkReturns()).thenReturn(treeMap);

      doCallRealMethod().when(sut).defineCalculationMethod(req);
      sut.defineCalculationMethod(req);

      mockedPeriodCalculationAbstract.verify(() -> PeriodCalculationAbstract.calculateExcessReturn(treeMap, treeMap),
          Mockito.times(2));
    }
  }
}