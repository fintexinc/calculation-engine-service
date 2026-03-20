package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.port.TBillsFetcher;
import com.fintex.ce.util.ReturnFactorScale;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class SharpeRatioCalculationServiceImplTest {

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildCalculationDto() {
    // SETUP
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var sut = mock(SharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(null, tBillsFetcher, null));

    final var weightedAverageInputDTO = mock(CalculationDTO.class);
    final PeriodCommand req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(tBillsFetcher.fetch(any())).thenReturn(new TreeMap<>());
    when(sut.buildCalculationDto(any(), any())).thenReturn(weightedAverageInputDTO);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    // ACT
    sut.defineCalculationMethod(req);

    // VERIFY
    verify(sut).buildCalculationDto(req, ReturnFactorScale.SCALE_OF_ONE);
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyLoadTBillsFor() {
    // SETUP
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var sut = mock(SharpeRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(null, tBillsFetcher, null));

    final var calculationDTO = mock(CalculationDTO.class);
    final PeriodCommand req = mock(PeriodCommand.class);
    when(tBillsFetcher.fetch(any())).thenReturn(new TreeMap<>());
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(sut.buildCalculationDto(any(), any())).thenReturn(calculationDTO);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    // ACT
    sut.defineCalculationMethod(req);

    // VERIFY
    verify(tBillsFetcher).fetch(Currency.CAD);
  }

}
