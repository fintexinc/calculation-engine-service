package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.RSquaredCalculation;
import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.sm.TreasuryBillsFetcher;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
@Disabled("metric unsupported")
class RSquaredCalculationServiceImplTest {

  RSquaredCalculationServiceImplTest() {
  }

  @Test
  void shouldPerform_whenVerifyBuildPeriodCalculationInput() {
    final var tBillsFetcher = mock(TreasuryBillsFetcher.class);
    final var service = mock(RSquaredCalculationServiceImpl.class, withSettings().useConstructor(null, null, null, null,
        tBillsFetcher,
        null));

    final var benchmarkContext = mock(BenchmarkPeriodCalculationInput.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>();

    final var req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(tBillsFetcher.fetch(Currency.CAD))
        .thenReturn(new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.ONE)));
    when(service.buildPeriodCalculationInput(any(), any(), any())).thenReturn(benchmarkContext);
    when(benchmarkContext.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
    when(benchmarkContext.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(service).perform(req, PortfolioBenchmarkReturns.EMPTY);
    try (var ignored = mockConstruction(RSquaredCalculation.class)) {
      service.perform(req, PortfolioBenchmarkReturns.EMPTY);
    }

    verify(service).buildPeriodCalculationInput(req, ReturnFactorScale.SCALE_OF_TWO, PortfolioBenchmarkReturns.EMPTY);
  }

  @Test
  void shouldPerform_whenVerifyLoadTBillsFor() {
    final var tBillsFetcher = mock(TreasuryBillsFetcher.class);
    final var service = mock(RSquaredCalculationServiceImpl.class, withSettings()
        .useConstructor(null, null, null, null, tBillsFetcher, null));

    final var benchmarkContext = mock(BenchmarkPeriodCalculationInput.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>();

    final var req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(tBillsFetcher.fetch(Currency.CAD))
        .thenReturn(new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.ONE)));
    when(service.buildPeriodCalculationInput(any(), any(), any())).thenReturn(benchmarkContext);
    when(benchmarkContext.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
    when(benchmarkContext.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(service).perform(req, PortfolioBenchmarkReturns.EMPTY);
    try (var ignored = mockConstruction(RSquaredCalculation.class)) {
      service.perform(req, PortfolioBenchmarkReturns.EMPTY);
    }

    verify(tBillsFetcher).fetch(Currency.CAD);
  }

  @Test
  void shouldPerform_whenVerifyCalculateExcessReturn() {
    try (var mockedPeriodCalculationAbstract = Mockito.mockStatic(PeriodCalculationAbstract.class)) {
      final var tBillsFetcher = mock(TreasuryBillsFetcher.class);
      final var service = mock(RSquaredCalculationServiceImpl.class, withSettings().useConstructor(null, null, null,
          null, tBillsFetcher,
          null));

      final var benchmarkContext = mock(BenchmarkPeriodCalculationInput.class);
      final TreeMap<LocalDate, BigDecimal> treeMap = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.ONE));
      final var req = mock(PeriodCommand.class);

      when(req.getCurrency()).thenReturn(Currency.CAD);
      when(tBillsFetcher.fetch(Currency.CAD)).thenReturn(treeMap);
      when(service.buildPeriodCalculationInput(any(), any(), any())).thenReturn(benchmarkContext);
      when(benchmarkContext.getWeightedAveragePortfolioReturns()).thenReturn(treeMap);
      when(benchmarkContext.getWeightedAverageBenchmarkReturns()).thenReturn(treeMap);

      doCallRealMethod().when(service).perform(req, PortfolioBenchmarkReturns.EMPTY);
      try (var ignored = mockConstruction(RSquaredCalculation.class)) {
        service.perform(req, PortfolioBenchmarkReturns.EMPTY);
      }

      mockedPeriodCalculationAbstract.verify(() -> PeriodCalculationAbstract.calculateExcessReturn(treeMap, treeMap),
          Mockito.times(2));
    }
  }
}
