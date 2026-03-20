package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.port.TBillsFetcher;
import com.fintex.ce.util.ReturnFactorScale;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class AlphaCalculationServiceImplTest {

  AlphaCalculationServiceImplTest() {
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildCalculationDto() {
    // SETUP
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var sut = mock(AlphaCalculationServiceImpl.class, withSettings()
        .useConstructor(null, tBillsFetcher, null));
    final var benchmarkCalculationDTO = mock(BenchmarkCalculationDTO.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>();

    final var req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(tBillsFetcher.fetch(any())).thenReturn(new TreeMap<>());
    when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
    when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
    when(benchmarkCalculationDTO.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(sut).defineCalculationMethod(req);
    // ACT
    sut.defineCalculationMethod(req);

    // VERIFY
    verify(sut).buildCalculationDto(req, ReturnFactorScale.SCALE_OF_ONE);
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyLoadTBillsFor() {
    // SETUP
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var sut = mock(AlphaCalculationServiceImpl.class, withSettings()
        .useConstructor(null, tBillsFetcher, null));
    final var benchmarkCalculationDTO = mock(BenchmarkCalculationDTO.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>();

    final var req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(tBillsFetcher.fetch(any())).thenReturn(new TreeMap<>());
    when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
    when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
    when(benchmarkCalculationDTO.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(sut).defineCalculationMethod(req);
    // ACT
    sut.defineCalculationMethod(req);

    // VERIFY
    verify(tBillsFetcher).fetch(Currency.CAD);
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyCalculateExcessReturn() {
    try (var mockedPeriodCalculationAbstract = Mockito.mockStatic(PeriodCalculationAbstract.class)) {
      // SETUP
      final var tBillsFetcher = mock(TBillsFetcher.class);
      final var sut = mock(AlphaCalculationServiceImpl.class, withSettings()
          .useConstructor(null, tBillsFetcher, null));

      final var benchmarkCalculationDTO = mock(BenchmarkCalculationDTO.class);
      final TreeMap<LocalDate, BigDecimal> treeMap = new TreeMap<>();
      final var req = mock(PeriodCommand.class);

      when(req.getCurrency()).thenReturn(Currency.CAD);
      when(tBillsFetcher.fetch(any())).thenReturn(treeMap);
      when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
      when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(treeMap);
      when(benchmarkCalculationDTO.getWeightedAverageBenchmarkReturns()).thenReturn(treeMap);

      doCallRealMethod().when(sut).defineCalculationMethod(req);
      // ACT
      sut.defineCalculationMethod(req);

      // VERIFY
      mockedPeriodCalculationAbstract.verify(() -> PeriodCalculationAbstract.calculateExcessReturn(treeMap, treeMap),
          Mockito.times(2));
    }
  }

}