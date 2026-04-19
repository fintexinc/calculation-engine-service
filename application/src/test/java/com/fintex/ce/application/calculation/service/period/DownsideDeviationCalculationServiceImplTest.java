package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.model.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.sm.TBillsFetcher;
import com.fintex.ce.util.ReturnFactorScale;
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
    final var sut = mock(DownsideDeviationCalculationServiceImpl.class, withSettings()
        .useConstructor(null, tBillsFetcher, Set.of()));

    final var benchmarkCalculationDTO = mock(BenchmarkCalculationDTO.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>(Map.of(LocalDate.now(),
        BigDecimal.TEN));

    final var req = mock(PeriodCommand.class);
    when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);
    when(benchmarkCalculationDTO.getWeightedAverageBenchmarkReturns()).thenReturn(weightedAverageReturns);
    when(tBillsFetcher.fetch(any())).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(sut).defineCalculationMethod(req);
    sut.defineCalculationMethod(req);

    verify(sut).buildCalculationDto(req, ReturnFactorScale.SCALE_OF_ONE);
  }
}