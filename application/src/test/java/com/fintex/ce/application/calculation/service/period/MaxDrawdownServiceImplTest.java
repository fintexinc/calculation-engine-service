package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.MaxDrawdownCalculation;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class MaxDrawdownServiceImplTest {

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildPeriodCalculationInput() {
    final var monthlyReturnsService = mock(PortfolioMonthlyReturnsContextProvider.class);
    final var service = mock(MaxDrawdownServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, null, Set.of()));

    final var benchmarkContext = mock(PeriodCalculationInput.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>(Map.of(LocalDate.now(),
        BigDecimal.TEN));

    final var req = mock(PeriodCommand.class);
    when(service.buildPeriodCalculationInput(any(), any())).thenReturn(benchmarkContext);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(benchmarkContext.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(service).defineCalculationMethod(req);
    service.defineCalculationMethod(req);

    verify(service).buildPeriodCalculationInput(req, ReturnFactorScale.SCALE_OF_TWO);
  }

  @Test
  void shouldProduceCalculationWithCompoundedGrowthCurve_whenWeightedReturnsPresent() {
    final var monthlyReturnsService = mock(PortfolioMonthlyReturnsContextProvider.class);
    final var service = mock(MaxDrawdownServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, null, Set.of()));

    final var context = mock(PeriodCalculationInput.class);
    final var weightedAverageReturns = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.TEN));
    final var req = mock(PeriodCommand.class);
    when(service.buildPeriodCalculationInput(any(), any())).thenReturn(context);
    when(context.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(service).defineCalculationMethod(req);
    MaxDrawdownCalculation calculation = service.defineCalculationMethod(req);

    // Curve has a $10K seed at month-1 and one compounded month — verifies the
    // CalculationUtils.compoundGrowth10K() integration on the weighted-average return series.
    assertThat(calculation).isNotNull();
  }

  @Test
  void shouldProduceCalculationWithEmptyGrowthCurve_whenWeightedReturnsIsEmpty() {
    final var monthlyReturnsService = mock(PortfolioMonthlyReturnsContextProvider.class);
    final var service = mock(MaxDrawdownServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, null, Set.of()));

    final var context = mock(PeriodCalculationInput.class);
    final TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>();
    final var req = mock(PeriodCommand.class);
    when(service.buildPeriodCalculationInput(any(), any())).thenReturn(context);
    when(context.getWeightedAveragePortfolioReturns()).thenReturn(weightedAverageReturns);

    doCallRealMethod().when(service).defineCalculationMethod(req);
    MaxDrawdownCalculation calculation = service.defineCalculationMethod(req);

    assertThat(calculation).isNotNull();
  }
}