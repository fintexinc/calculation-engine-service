package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.DownsideCaptureCalculation;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@Disabled("metric unsupported")
class DownsideCaptureCalculationServiceImplTest {

  @Test
  void shouldPerform_whenVerifyBuildPeriodCalculationInput() {
    final var service = mock(DownsideCaptureCalculationServiceImpl.class, withSettings()
        .useConstructor(null, null, null, null, Set.of()));

    final var benchmarkContext = mock(BenchmarkPeriodCalculationInput.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>();

    final var req = mock(PeriodCommand.class);
    when(service.buildPeriodCalculationInput(any(), any(), any())).thenReturn(benchmarkContext);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(benchmarkContext.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
    when(benchmarkContext.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(service).perform(req, PortfolioBenchmarkReturns.EMPTY);
    try (var ignored = mockConstruction(DownsideCaptureCalculation.class)) {
      service.perform(req, PortfolioBenchmarkReturns.EMPTY);
    }

    verify(service).buildPeriodCalculationInput(req, ReturnFactorScale.AS_IS, PortfolioBenchmarkReturns.EMPTY);
  }
}
