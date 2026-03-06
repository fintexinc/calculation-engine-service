package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.port.output.cache.TBillsProvider;
import com.fintex.ce.application.service.calculation.period.SharpeRatioCalculationServiceImpl;
import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.util.ReturnFactorScale;
import org.junit.jupiter.api.Test;

import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SharpeRatioCalculationServiceImplTest {

  @Test
  void defineCalculationMethod_verifyBuildCalculationDto() {
    // SETUP
    final var tBillsCacheStorage = mock(TBillsProvider.class);
    final var sut = mock(SharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(null, tBillsCacheStorage, null));

    final var weightedAverageInputDTO = mock(CalculationDTO.class);
    final PeriodCommand req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(tBillsCacheStorage.loadTBillsFor(any())).thenReturn(new TreeMap<>());
    when(sut.buildCalculationDto(any(), any())).thenReturn(weightedAverageInputDTO);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    // ACT
    sut.defineCalculationMethod(req);

    // VERIFY
    verify(sut).buildCalculationDto(req, ReturnFactorScale.SCALE_OF_ONE);
  }

  @Test
  void defineCalculationMethod_verifyLoadTBillsFor() {
    // SETUP
    final var tBillsCacheStorage = mock(TBillsProvider.class);
    final var sut = mock(SharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(null, tBillsCacheStorage, null));

    final var calculationDTO = mock(CalculationDTO.class);
    final PeriodCommand req = mock(PeriodCommand.class);
    when(tBillsCacheStorage.loadTBillsFor(any())).thenReturn(new TreeMap<>());
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(sut.buildCalculationDto(any(), any())).thenReturn(calculationDTO);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    // ACT
    sut.defineCalculationMethod(req);

    // VERIFY
    verify(tBillsCacheStorage).loadTBillsFor(Currency.CAD);
  }

}
