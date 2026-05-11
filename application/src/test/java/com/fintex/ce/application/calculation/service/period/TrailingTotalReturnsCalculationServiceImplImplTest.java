package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.ce.port.webclient.sm.TreasuryBillsFetcher;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class TrailingTotalReturnsCalculationServiceImplImplTest {

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildPeriodCalculationInput() {
    var monthlyReturnsService = mock(MonthlyReturnsService.class);
    var treasuryBillsFetcher = mock(TreasuryBillsFetcher.class);
    var service = mock(TrailingTotalReturnsCalculationServiceImpl.class,
        withSettings().useConstructor(monthlyReturnsService, treasuryBillsFetcher, Set.of()));

    PeriodCommand req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(treasuryBillsFetcher.fetch(Currency.CAD))
        .thenReturn(new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.ONE)));
    when(service.buildPeriodCalculationInput(req, ReturnFactorScale.SCALE_OF_TWO)).thenReturn(
        new PeriodCalculationInput());

    doCallRealMethod().when(service).defineCalculationMethod(req);
    service.defineCalculationMethod(req);

    verify(service).buildPeriodCalculationInput(req, ReturnFactorScale.SCALE_OF_TWO);
  }

  @Test
  void shouldFetchTBills_whenDefiningCalculationMethod() {
    var treasuryBillsFetcher = mock(TreasuryBillsFetcher.class);
    var service = mock(TrailingTotalReturnsCalculationServiceImpl.class,
        withSettings().useConstructor(null, treasuryBillsFetcher, Set.of()));

    PeriodCommand req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(treasuryBillsFetcher.fetch(Currency.CAD))
        .thenReturn(new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.ONE)));
    when(service.buildPeriodCalculationInput(any(), any())).thenReturn(new PeriodCalculationInput());

    doCallRealMethod().when(service).defineCalculationMethod(any());
    service.defineCalculationMethod(req);

    verify(treasuryBillsFetcher).fetch(Currency.CAD);
  }

  @Test
  void shouldThrowTBillSeriesNotAvailable_whenTBillSeriesEmptyForRequestedCurrency() {
    var treasuryBillsFetcher = mock(TreasuryBillsFetcher.class);
    var service = mock(TrailingTotalReturnsCalculationServiceImpl.class,
        withSettings().useConstructor(null, treasuryBillsFetcher, Set.of()));

    PeriodCommand req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.EUR);
    when(service.buildPeriodCalculationInput(any(), any())).thenReturn(new PeriodCalculationInput());
    when(treasuryBillsFetcher.fetch(Currency.EUR)).thenReturn(new TreeMap<>());

    doCallRealMethod().when(service).defineCalculationMethod(any());

    CalculationException ex = assertThrows(CalculationException.class,
        () -> service.defineCalculationMethod(req));
    assertEquals(ErrorCode.TBILL_SERIES_NOT_AVAILABLE_FOR_CURRENCY, ex.getErrorCode());
  }

}