package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.sm.TBillsFetcher;
import com.fintex.wm.commons.domain.currency.Currency;

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
  void shouldDefineCalculationMethod_whenVerifyBuildPeriodCalculationInput() {
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var service = mock(RSquaredCalculationServiceImpl.class, withSettings().useConstructor(null, tBillsFetcher,
        null));

    final var benchmarkContext = mock(BenchmarkPeriodCalculationInput.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>();

    final var req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(tBillsFetcher.fetch(any())).thenReturn(new TreeMap<>());
    when(service.buildPeriodCalculationInput(any(), any())).thenReturn(benchmarkContext);
    when(benchmarkContext.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
    when(benchmarkContext.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(service).defineCalculationMethod(req);
    service.defineCalculationMethod(req);

    verify(service).buildPeriodCalculationInput(req, ReturnFactorScale.SCALE_OF_TWO);
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyLoadTBillsFor() {
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var service = mock(RSquaredCalculationServiceImpl.class, withSettings()
        .useConstructor(null, tBillsFetcher, null));

    final var benchmarkContext = mock(BenchmarkPeriodCalculationInput.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>();

    final var req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(tBillsFetcher.fetch(any())).thenReturn(new TreeMap<>());
    when(service.buildPeriodCalculationInput(any(), any())).thenReturn(benchmarkContext);
    when(benchmarkContext.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
    when(benchmarkContext.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(service).defineCalculationMethod(req);
    service.defineCalculationMethod(req);

    verify(tBillsFetcher).fetch(Currency.CAD);
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyCalculateExcessReturn() {
    try (var mockedPeriodCalculationAbstract = Mockito.mockStatic(PeriodCalculationAbstract.class)) {
      final var tBillsFetcher = mock(TBillsFetcher.class);
      final var service = mock(RSquaredCalculationServiceImpl.class, withSettings().useConstructor(null, tBillsFetcher,
          null));

      final var benchmarkContext = mock(BenchmarkPeriodCalculationInput.class);
      final TreeMap<LocalDate, BigDecimal> treeMap = new TreeMap<>();
      final var req = mock(PeriodCommand.class);

      when(req.getCurrency()).thenReturn(Currency.CAD);
      when(tBillsFetcher.fetch(any())).thenReturn(treeMap);
      when(service.buildPeriodCalculationInput(any(), any())).thenReturn(benchmarkContext);
      when(benchmarkContext.getWeightedAveragePortfolioReturns()).thenReturn(treeMap);
      when(benchmarkContext.getWeightedAverageBenchmarkReturns()).thenReturn(treeMap);

      doCallRealMethod().when(service).defineCalculationMethod(req);
      service.defineCalculationMethod(req);

      mockedPeriodCalculationAbstract.verify(() -> PeriodCalculationAbstract.calculateExcessReturn(treeMap, treeMap),
          Mockito.times(2));
    }
  }
}