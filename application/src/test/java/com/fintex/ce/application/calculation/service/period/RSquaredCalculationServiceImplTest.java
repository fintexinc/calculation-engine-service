package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.domain.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.TBillsFetcher;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.sm.model.domain.enumeration.CurrencyType;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.TreeMap;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class RSquaredCalculationServiceImplTest {

  RSquaredCalculationServiceImplTest() {
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildCalculationDto() {
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var sut = mock(RSquaredCalculationServiceImpl.class, withSettings().useConstructor(null, tBillsFetcher,
        null));

    final var benchmarkCalculationDTO = mock(BenchmarkCalculationDTO.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>();

    final var req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(CurrencyType.CAD);
    when(tBillsFetcher.fetch(any())).thenReturn(new TreeMap<>());
    when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
    when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
    when(benchmarkCalculationDTO.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(sut).defineCalculationMethod(req);
    sut.defineCalculationMethod(req);

    verify(sut).buildCalculationDto(req, ReturnFactorScale.SCALE_OF_TWO);
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyLoadTBillsFor() {
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var sut = mock(RSquaredCalculationServiceImpl.class, withSettings()
        .useConstructor(null, tBillsFetcher, null));

    final var benchmarkCalculationDTO = mock(BenchmarkCalculationDTO.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>();

    final var req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(CurrencyType.CAD);
    when(tBillsFetcher.fetch(any())).thenReturn(new TreeMap<>());
    when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
    when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
    when(benchmarkCalculationDTO.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(sut).defineCalculationMethod(req);
    sut.defineCalculationMethod(req);

    verify(tBillsFetcher).fetch(CurrencyType.CAD);
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyCalculateExcessReturn() {
    try (var mockedPeriodCalculationAbstract = Mockito.mockStatic(PeriodCalculationAbstract.class)) {
      final var tBillsFetcher = mock(TBillsFetcher.class);
      final var sut = mock(RSquaredCalculationServiceImpl.class, withSettings().useConstructor(null, tBillsFetcher,
          null));

      final var benchmarkCalculationDTO = mock(BenchmarkCalculationDTO.class);
      final TreeMap<LocalDate, BigDecimal> treeMap = new TreeMap<>();
      final var req = mock(PeriodCommand.class);

      when(req.getCurrency()).thenReturn(CurrencyType.CAD);
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