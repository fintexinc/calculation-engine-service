package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.port.output.TBillsPort;
import com.fintex.ce.application.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.util.ReturnFactorScale;
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
  void defineCalculationMethod_verifyBuildCalculationDto() {
    // SETUP
    final var tBillsCacheStorage = mock(TBillsPort.class);
    final var sut = mock(RSquaredCalculationServiceImpl.class, withSettings().useConstructor(null, tBillsCacheStorage,
        null));

    final var benchmarkCalculationDTO = mock(BenchmarkCalculationDTO.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>();

    final var req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(tBillsCacheStorage.loadTBillsFor(any())).thenReturn(new TreeMap<>());
    when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
    when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
    when(benchmarkCalculationDTO.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(sut).defineCalculationMethod(req);
    // ACT
    sut.defineCalculationMethod(req);

    // VERIFY
    verify(sut).buildCalculationDto(req, ReturnFactorScale.SCALE_OF_TWO);
  }

  @Test
  void defineCalculationMethod_verifyLoadTBillsFor() {
    // SETUP
    final var tBillsCacheStorage = mock(TBillsPort.class);
    final var sut = mock(RSquaredCalculationServiceImpl.class, withSettings()
        .useConstructor(null, tBillsCacheStorage, null));

    final var benchmarkCalculationDTO = mock(BenchmarkCalculationDTO.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>();

    final var req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(tBillsCacheStorage.loadTBillsFor(any())).thenReturn(new TreeMap<>());
    when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
    when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
    when(benchmarkCalculationDTO.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(sut).defineCalculationMethod(req);
    // ACT
    sut.defineCalculationMethod(req);

    // VERIFY
    verify(tBillsCacheStorage).loadTBillsFor(Currency.CAD);
  }

  @Test
  void defineCalculationMethod_verifyCalculateExcessReturn() {
    try (var mockedPeriodCalculationAbstract = Mockito.mockStatic(PeriodCalculationAbstract.class)) {
      // SETUP
      final var tBillsCacheStorage = mock(TBillsPort.class);
      final var sut = mock(RSquaredCalculationServiceImpl.class, withSettings().useConstructor(null, tBillsCacheStorage,
          null));

      final var benchmarkCalculationDTO = mock(BenchmarkCalculationDTO.class);
      final TreeMap<LocalDate, BigDecimal> treeMap = new TreeMap<>();
      final var req = mock(PeriodCommand.class);

      when(req.getCurrency()).thenReturn(Currency.CAD);
      when(tBillsCacheStorage.loadTBillsFor(any())).thenReturn(treeMap);
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