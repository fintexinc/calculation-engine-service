package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.model.dto.calculation.CalculationDTO;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.TBillsFetcher;
import com.fintex.ce.util.ReturnFactorScale;
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
  void shouldDefineCalculationMethod_whenVerifyBuildCalculationDto() {
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var sut = mock(SharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(null, tBillsFetcher, null));

    final var weightedAverageInputDTO = mock(CalculationDTO.class);
    final PeriodCommand req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(tBillsFetcher.fetch(any())).thenReturn(new TreeMap<>());
    when(sut.buildCalculationDto(any(), any())).thenReturn(weightedAverageInputDTO);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    sut.defineCalculationMethod(req);

    verify(sut).buildCalculationDto(req, ReturnFactorScale.SCALE_OF_ONE);
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyLoadTBillsFor() {
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var sut = mock(SharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(null, tBillsFetcher, null));

    final var calculationDTO = mock(CalculationDTO.class);
    final PeriodCommand req = mock(PeriodCommand.class);
    when(tBillsFetcher.fetch(any())).thenReturn(new TreeMap<>());
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(sut.buildCalculationDto(any(), any())).thenReturn(calculationDTO);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    sut.defineCalculationMethod(req);

    verify(tBillsFetcher).fetch(Currency.CAD);
  }

}
