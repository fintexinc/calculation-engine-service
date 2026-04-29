package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.sm.TBillsFetcher;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class SharpeRatioCalculationServiceImplTest {

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildPeriodCalculationInput() {
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var sut = mock(SharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(null, tBillsFetcher, null));

    final var weightedAverageInput = mock(PeriodCalculationInput.class);
    final PeriodCommand req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(tBillsFetcher.fetch(any())).thenReturn(new TreeMap<>());
    when(sut.buildPeriodCalculationInput(any(), any())).thenReturn(weightedAverageInput);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    sut.defineCalculationMethod(req);

    verify(sut).buildPeriodCalculationInput(req, ReturnFactorScale.SCALE_OF_ONE);
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyLoadTBillsFor() {
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var sut = mock(SharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(null, tBillsFetcher, null));

    final var context = mock(PeriodCalculationInput.class);
    final PeriodCommand req = mock(PeriodCommand.class);
    when(tBillsFetcher.fetch(any())).thenReturn(new TreeMap<>());
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(sut.buildPeriodCalculationInput(any(), any())).thenReturn(context);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    sut.defineCalculationMethod(req);

    verify(tBillsFetcher).fetch(Currency.CAD);
  }

}
