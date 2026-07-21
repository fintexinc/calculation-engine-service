package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.SharpeRatioCalculation;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.ce.port.webclient.sm.TreasuryBillsFetcher;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class SharpeRatioCalculationServiceImplTest {

  @Test
  void shouldPerform_whenVerifyBuildPeriodCalculationInput() {
    final var tBillsFetcher = mock(TreasuryBillsFetcher.class);
    final var service = mock(SharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(null, null, tBillsFetcher, null));

    final var weightedAverageInput = mock(PeriodCalculationInput.class);
    final PeriodCommand req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(tBillsFetcher.fetch(Currency.CAD))
        .thenReturn(new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.ONE)));
    when(weightedAverageInput.getWeightedAveragePortfolioReturns())
        .thenReturn(new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.ONE)));
    when(service.buildPeriodCalculationInput(any(), any(), any())).thenReturn(weightedAverageInput);

    doCallRealMethod().when(service).perform(any(), any());
    try (var ignored = mockConstruction(SharpeRatioCalculation.class)) {
      service.perform(req, PortfolioBenchmarkReturns.EMPTY);
    }

    verify(service).buildPeriodCalculationInput(req, ReturnFactorScale.SCALE_OF_ONE, PortfolioBenchmarkReturns.EMPTY);
  }

  @Test
  void shouldPerform_whenVerifyLoadTBillsFor() {
    final var tBillsFetcher = mock(TreasuryBillsFetcher.class);
    final var service = mock(SharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(null, null, tBillsFetcher, null));

    final var context = mock(PeriodCalculationInput.class);
    final PeriodCommand req = mock(PeriodCommand.class);
    when(tBillsFetcher.fetch(Currency.CAD))
        .thenReturn(new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.ONE)));
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(context.getWeightedAveragePortfolioReturns())
        .thenReturn(new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.ONE)));
    when(service.buildPeriodCalculationInput(any(), any(), any())).thenReturn(context);

    doCallRealMethod().when(service).perform(any(), any());
    try (var ignored = mockConstruction(SharpeRatioCalculation.class)) {
      service.perform(req, PortfolioBenchmarkReturns.EMPTY);
    }

    verify(tBillsFetcher).fetch(Currency.CAD);
  }

  @Test
  void shouldThrowTBillSeriesNotAvailable_whenTBillSeriesEmptyForRequestedCurrency() {
    final var tBillsFetcher = mock(TreasuryBillsFetcher.class);
    final var service = mock(SharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(null, null, tBillsFetcher, null));

    final var context = mock(PeriodCalculationInput.class);
    final PeriodCommand req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.EUR);
    when(service.buildPeriodCalculationInput(any(), any(), any())).thenReturn(context);
    // EUR is not supported by the SMS producer → fetch returns an empty series → TBillsValidator throws.
    when(tBillsFetcher.fetch(Currency.EUR)).thenReturn(new TreeMap<>());

    doCallRealMethod().when(service).perform(any(), any());

    CalculationException ex = assertThrows(CalculationException.class,
        () -> service.perform(req, PortfolioBenchmarkReturns.EMPTY));
    assertEquals(ErrorCode.TBILL_SERIES_NOT_AVAILABLE_FOR_CURRENCY, ex.getErrorCode());
    assertEquals("T-Bill rates are not available for currency " + Currency.EUR, ex.getMessage());
    assertEquals(Map.of("param-1", Currency.EUR), ex.getMetadata());
  }
}
