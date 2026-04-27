package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.MarRatioCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class MarRatioCalculationServiceImplTest {

  @Test
  void shouldPerform_whenVerifyValidateMarRatio() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(MarRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));
    final var reqDTO = mock(PeriodCommand.class);
    final var holdings = List.of(mock(PortfolioHolding.class));

    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(sut.defineCalculationMethod(any())).thenReturn(mock(MarRatioCalculation.class));

    doCallRealMethod().when(sut).perform(any());
    sut.perform(reqDTO);

    verify(sut).defineCalculationMethod(reqDTO);
  }

  @Test
  void shouldPerform_whenVerifyDefineCalculationMethod() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(MarRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));
    final var reqDTO = mock(PeriodCommand.class);
    final var holdings = List.of(mock(PortfolioHolding.class));

    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(sut.defineCalculationMethod(any())).thenReturn(mock(MarRatioCalculation.class));

    doCallRealMethod().when(sut).perform(any());
    sut.perform(reqDTO);

    verify(sut).defineCalculationMethod(reqDTO);
  }

  @Test
  void shouldPerform_whenVerifyCalculate() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(MarRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));
    final var reqDTO = mock(PeriodCommand.class);
    final var holdings = List.of(mock(PortfolioHolding.class));

    when(reqDTO.getHoldings()).thenReturn(holdings);
    final var calculationMethod = mock(MarRatioCalculation.class);
    when(sut.defineCalculationMethod(any())).thenReturn(calculationMethod);
    when(reqDTO.getPeriods()).thenReturn(Set.of("12"));

    doCallRealMethod().when(sut).perform(any());
    sut.perform(reqDTO);

    verify(calculationMethod).calculate(Set.of("12"));
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildCalculationDto() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(MarRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));
    final var benchmarkCalculationDTO = mock(CalculationDTO.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>(Map.of(LocalDate.now(),
        BigDecimal.TEN));

    final var req = mock(PeriodCommand.class);
    when(sut.buildCalculationDto(any(), any())).thenReturn(benchmarkCalculationDTO);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(benchmarkCalculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(sut).defineCalculationMethod(req);
    sut.defineCalculationMethod(req);

    verify(sut).buildCalculationDto(req, ReturnFactorScale.SCALE_OF_TWO);
  }
}