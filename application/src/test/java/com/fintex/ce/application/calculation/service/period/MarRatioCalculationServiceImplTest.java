package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.MarRatioCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.util.ReturnFactorScale;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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

class MarRatioCalculationServiceImplTest {

  @Test
  void shouldPerform_whenVerifyValidateMarRatio() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(MarRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));
    final var reqDTO = mock(PeriodCommand.class);
    final var holdings = List.of(mock(Holding.class));

    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(sut.defineCalculationMethod(any())).thenReturn(mock(MarRatioCalculation.class));

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(reqDTO);

    // VERIFY
    verify(sut).defineCalculationMethod(reqDTO);
  }

  @Test
  void shouldPerform_whenVerifyDefineCalculationMethod() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(MarRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));
    final var reqDTO = mock(PeriodCommand.class);
    final var holdings = List.of(mock(Holding.class));

    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(sut.defineCalculationMethod(any())).thenReturn(mock(MarRatioCalculation.class));

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(reqDTO);

    // VERIFY
    verify(sut).defineCalculationMethod(reqDTO);
  }

  @Test
  void shouldPerform_whenVerifyCalculate() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(MarRatioCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));
    final var reqDTO = mock(PeriodCommand.class);
    final var holdings = List.of(mock(Holding.class));

    when(reqDTO.getHoldings()).thenReturn(holdings);
    final var calculationMethod = mock(MarRatioCalculation.class);
    when(sut.defineCalculationMethod(any())).thenReturn(calculationMethod);
    when(reqDTO.getPeriods()).thenReturn(Set.of("12"));

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(reqDTO);

    // VERIFY
    verify(calculationMethod).calculate(Set.of("12"));
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildCalculationDto() {
    // SETUP
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
    // ACT
    sut.defineCalculationMethod(req);

    // VERIFY
    verify(sut).buildCalculationDto(req, ReturnFactorScale.SCALE_OF_TWO);
  }
}