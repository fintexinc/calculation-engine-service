package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.port.TBillsFetcher;
import com.fintex.ce.util.ReturnFactorScale;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class SortinoRatioCalculationServiceImplTest {

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildCalculationDto() {
    // SETUP
    final var tBillsFetcher = mock(TBillsFetcher.class);
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(SortinoRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, tBillsFetcher, Set.of()));

    final var reqDTO = mock(PeriodCommand.class);
    final var input = mock(BenchmarkCalculationDTO.class);
    final var treeMap = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.TEN));

    when(input.getWeightedAveragePortfolioReturns()).thenReturn(treeMap);
    when(sut.buildCalculationDto(any(), any())).thenReturn(input);
    when(reqDTO.getCurrency()).thenReturn(Currency.CAD);
    when(tBillsFetcher.fetch(any())).thenReturn(treeMap);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    // ACT
    sut.defineCalculationMethod(reqDTO);

    // VERIFY
    verify(sut).buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
  }
}