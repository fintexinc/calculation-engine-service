package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.sm.TBillsFetcher;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class DownsideDeviationCalculationServiceImplTest {

  @Test
  void shouldDefineCalculationMethod_whenVerifyDefineCalculationMethod() {
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var service = mock(DownsideDeviationCalculationServiceImpl.class, withSettings()
        .useConstructor(null, tBillsFetcher, Set.of()));

    final var benchmarkContext = mock(BenchmarkPeriodCalculationInput.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>(Map.of(LocalDate.now(),
        BigDecimal.TEN));

    final var req = mock(PeriodCommand.class);
    when(service.buildPeriodCalculationInput(any(), any())).thenReturn(benchmarkContext);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(benchmarkContext.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
    when(benchmarkContext.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);
    when(tBillsFetcher.fetch(any())).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(service).defineCalculationMethod(req);
    service.defineCalculationMethod(req);

    verify(service).buildPeriodCalculationInput(req, ReturnFactorScale.SCALE_OF_ONE);
  }
}