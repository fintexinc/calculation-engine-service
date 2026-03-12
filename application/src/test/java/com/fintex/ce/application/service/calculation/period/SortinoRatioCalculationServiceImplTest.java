package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.port.output.TBillsPort;
import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.util.ReturnFactorScale;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SortinoRatioCalculationServiceImplTest {

  @Test
  void defineCalculationMethod_verifyBuildCalculationDto() {
    // SETUP
    final var tBillsCacheStorage = mock(TBillsPort.class);
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(SortinoRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, tBillsCacheStorage, Set.of()));

    final var reqDTO = mock(PeriodCommand.class);
    final var input = mock(BenchmarkCalculationDTO.class);
    final var treeMap = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.TEN));

    when(input.getWeightedAveragePortfolioReturns()).thenReturn(treeMap);
    when(sut.buildCalculationDto(any(), any())).thenReturn(input);
    when(reqDTO.getCurrency()).thenReturn(Currency.CAD);
    when(tBillsCacheStorage.loadTBillsFor(any())).thenReturn(treeMap);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    // ACT
    sut.defineCalculationMethod(reqDTO);

    // VERIFY
    verify(sut).buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
  }
}