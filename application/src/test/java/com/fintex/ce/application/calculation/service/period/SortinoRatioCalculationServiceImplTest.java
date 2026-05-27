package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.sm.TreasuryBillsFetcher;
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
class SortinoRatioCalculationServiceImplTest {

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildPeriodCalculationInput() {
    final var tBillsFetcher = mock(TreasuryBillsFetcher.class);
    final var monthlyReturnsService = mock(PortfolioMonthlyReturnsContextProvider.class);
    final var service = mock(SortinoRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, null, tBillsFetcher, Set.of()));

    final var command = mock(PeriodCommand.class);
    final var input = mock(BenchmarkPeriodCalculationInput.class);
    final var treeMap = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.TEN));

    when(input.getWeightedAveragePortfolioReturns()).thenReturn(treeMap);
    when(service.buildPeriodCalculationInput(any(), any())).thenReturn(input);
    when(command.getCurrency()).thenReturn(Currency.CAD);
    when(tBillsFetcher.fetch(Currency.CAD)).thenReturn(treeMap);

    doCallRealMethod().when(service).defineCalculationMethod(any());
    service.defineCalculationMethod(command);

    verify(service).buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_ONE);
  }
}