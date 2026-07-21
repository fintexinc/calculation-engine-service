package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.TreynorRatioCalculation;
import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.returns.BenchmarkMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.sm.TreasuryBillsFetcher;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.wm.commons.domain.currency.Currency.CAD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
class TreynorRatioServiceImplTest {

  TreynorRatioServiceImplTest() {
  }

  @Test
  void shouldPerform_whenVerifyBuildPeriodCalculationInput() {
    var portfolioProvider = mock(PortfolioMonthlyReturnsContextProvider.class);
    var benchmarkProvider = mock(BenchmarkMonthlyReturnsContextProvider.class);
    var tBillsFetcher = mock(TreasuryBillsFetcher.class);
    var service = mock(TreynorRatioServiceImpl.class, withSettings()
        .useConstructor(portfolioProvider, benchmarkProvider, null, null, tBillsFetcher, Set.of()));

    var benchmarkContext = mock(BenchmarkPeriodCalculationInput.class);
    TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>(Map.of(LocalDate.now(),
        BigDecimal.ONE));

    var req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(CAD);
    when(service.buildPeriodCalculationInput(any(), any(), any())).thenReturn(benchmarkContext);
    when(benchmarkContext.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
    when(benchmarkContext.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);
    when(tBillsFetcher.fetch(CAD)).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(service).perform(req, PortfolioBenchmarkReturns.EMPTY);
    try (var ignored = mockConstruction(TreynorRatioCalculation.class)) {
      service.perform(req, PortfolioBenchmarkReturns.EMPTY);
    }

    verify(service, times(2)).buildPeriodCalculationInput(eq(req), any(), any());
  }

  @Test
  void shouldPerform_whenVerifyLoadTBillsFor() {
    var portfolioProvider = mock(PortfolioMonthlyReturnsContextProvider.class);
    var benchmarkProvider = mock(BenchmarkMonthlyReturnsContextProvider.class);
    var tBillsFetcher = mock(TreasuryBillsFetcher.class);
    var service = mock(TreynorRatioServiceImpl.class, withSettings()
        .useConstructor(portfolioProvider, benchmarkProvider, null, null, tBillsFetcher, Set.of()));

    var benchmarkContext = mock(BenchmarkPeriodCalculationInput.class);
    TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>(Map.of(LocalDate.now(),
        BigDecimal.ONE));

    var req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(CAD);
    when(service.buildPeriodCalculationInput(any(), any(), any())).thenReturn(benchmarkContext);
    when(benchmarkContext.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
    when(benchmarkContext.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);
    when(tBillsFetcher.fetch(CAD)).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(service).perform(req, PortfolioBenchmarkReturns.EMPTY);
    try (var ignored = mockConstruction(TreynorRatioCalculation.class)) {
      service.perform(req, PortfolioBenchmarkReturns.EMPTY);
    }

    verify(tBillsFetcher).fetch(CAD);
  }

  @Test
  void shouldPerform_whenVerifyCalculateExcessReturn() {
    try (var mockedPeriodCalculationAbstract = Mockito.mockStatic(PeriodCalculationAbstract.class)) {
      var portfolioProvider = mock(PortfolioMonthlyReturnsContextProvider.class);
      var benchmarkProvider = mock(BenchmarkMonthlyReturnsContextProvider.class);
      var tBillsFetcher = mock(TreasuryBillsFetcher.class);
      var service = mock(TreynorRatioServiceImpl.class, withSettings()
          .useConstructor(portfolioProvider, benchmarkProvider, null, null, tBillsFetcher, Set.of()));

      var benchmarkContext = mock(BenchmarkPeriodCalculationInput.class);
      TreeMap<LocalDate, BigDecimal> treeMap = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.ONE));
      var req = mock(PeriodCommand.class);

      when(req.getCurrency()).thenReturn(CAD);
      when(tBillsFetcher.fetch(CAD)).thenReturn(treeMap);
      when(service.buildPeriodCalculationInput(any(), any(), any())).thenReturn(benchmarkContext);
      when(benchmarkContext.getWeightedAveragePortfolioReturns()).thenReturn(treeMap);
      when(benchmarkContext.getWeightedAverageBenchmarkReturns()).thenReturn(treeMap);

      doCallRealMethod().when(service).perform(req, PortfolioBenchmarkReturns.EMPTY);
      try (var ignored = mockConstruction(TreynorRatioCalculation.class)) {
        service.perform(req, PortfolioBenchmarkReturns.EMPTY);
      }

      mockedPeriodCalculationAbstract.verify(() -> PeriodCalculationAbstract.calculateExcessReturn(treeMap, treeMap),
          Mockito.times(2));
    }
  }
}
