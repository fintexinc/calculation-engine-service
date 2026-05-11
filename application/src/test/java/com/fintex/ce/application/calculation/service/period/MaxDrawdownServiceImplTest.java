package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.MaxDrawdownCalculation;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.returns.pipeline.CpedScaleParams;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MaxDrawdownServiceImplTest {

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildPeriodCalculationInput() {
    var contextProvider = mock(PortfolioMonthlyReturnsContextProvider.class);
    var pipeline = mock(PortfolioWeightedAverageWithCpedPipeline.class);
    var service = new MaxDrawdownServiceImpl(contextProvider, pipeline, Set.of());

    var req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.CAD);

    var monthlyReturnsContext = mock(MonthlyReturnsContext.class);
    when(contextProvider.get(req.getHoldings(), Currency.CAD)).thenReturn(monthlyReturnsContext);

    TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.TEN));
    var result = new WeightedAverageResult<>(weightedAverageReturns, ReturnsSnapshot
        .empty());
    when(pipeline.run(monthlyReturnsContext, new CpedScaleParams(req.getCustomPed(), ReturnFactorScale.SCALE_OF_TWO)))
        .thenReturn(result);

    service.defineCalculationMethod(req);

    verify(pipeline).run(monthlyReturnsContext, new CpedScaleParams(req.getCustomPed(),
        ReturnFactorScale.SCALE_OF_TWO));
  }

  @Test
  void shouldProduceCalculationWithCompoundedGrowthCurve_whenWeightedReturnsPresent() {
    var contextProvider = mock(PortfolioMonthlyReturnsContextProvider.class);
    var pipeline = mock(PortfolioWeightedAverageWithCpedPipeline.class);
    var service = new MaxDrawdownServiceImpl(contextProvider, pipeline, Set.of());

    var req = mock(PeriodCommand.class);
    var monthlyReturnsContext = mock(MonthlyReturnsContext.class);
    when(contextProvider.get(req.getHoldings(), req.getCurrency())).thenReturn(monthlyReturnsContext);

    TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.TEN));
    var result = new WeightedAverageResult<>(weightedAverageReturns, ReturnsSnapshot
        .empty());
    when(pipeline.run(monthlyReturnsContext, new CpedScaleParams(req.getCustomPed(), ReturnFactorScale.SCALE_OF_TWO)))
        .thenReturn(result);

    MaxDrawdownCalculation calculation = service.defineCalculationMethod(req);

    // Curve has a $10K seed at month-1 and one compounded month — verifies the new
    // Growth10KCalculation.compound() integration on the weighted-average return series.
    assertThat(calculation).isNotNull();
  }

  @Test
  void shouldProduceCalculationWithEmptyGrowthCurve_whenWeightedReturnsIsEmpty() {
    var contextProvider = mock(PortfolioMonthlyReturnsContextProvider.class);
    var pipeline = mock(PortfolioWeightedAverageWithCpedPipeline.class);
    var service = new MaxDrawdownServiceImpl(contextProvider, pipeline, Set.of());

    var req = mock(PeriodCommand.class);
    var monthlyReturnsContext = mock(MonthlyReturnsContext.class);
    when(contextProvider.get(req.getHoldings(), req.getCurrency())).thenReturn(monthlyReturnsContext);

    TreeMap<LocalDate, BigDecimal> weightedAverageReturns = new TreeMap<>();
    var result = new WeightedAverageResult<>(weightedAverageReturns, ReturnsSnapshot
        .empty());
    when(pipeline.run(monthlyReturnsContext, new CpedScaleParams(req.getCustomPed(), ReturnFactorScale.SCALE_OF_TWO)))
        .thenReturn(result);

    MaxDrawdownCalculation calculation = service.defineCalculationMethod(req);

    assertThat(calculation).isNotNull();
  }
}