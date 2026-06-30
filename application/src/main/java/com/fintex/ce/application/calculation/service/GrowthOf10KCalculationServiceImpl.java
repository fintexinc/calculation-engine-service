package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpsdAndCpedAbstractService;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.application.util.Growth10KHelper;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.KeyValueResult;
import com.fintex.ce.model.domain.result.returns.Growth10KResult;
import com.fintex.ce.model.dto.command.ReturnCommand;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

@Service
public class GrowthOf10KCalculationServiceImpl
    extends
      WeightedAverageWithCpsdAndCpedAbstractService<ReturnCommand, Growth10KResult> {

  public GrowthOf10KCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpsdAndCpedPipeline portfolioWeightedAverageWithCpsdAndCped) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCpsdAndCped);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.GROWTH_OF_10K;
  }

  @Override
  public Growth10KResult perform(ReturnCommand command) {
    // Weighted-average pipeline applies SCALE_OF_TWO upstream, so the returned series is already in factor form
    // (e.g. 1.05 for a 5% month). compoundGrowth10K therefore uses AS_IS to avoid re-scaling.
    WeightedAverageResult<HoldingMonthlyReturns> weighted = runWeightedAverage(command, ReturnFactorScale.SCALE_OF_TWO);

    NavigableMap<LocalDate, BigDecimal> growth = Growth10KHelper.compoundGrowth10K(weighted.weightedAverage(),
        ReturnFactorScale.AS_IS);
    List<KeyValueResult<LocalDate>> points = growth.entrySet().stream()
        .map(e -> new KeyValueResult<>(e.getKey(), e.getValue()))
        .toList();

    return Growth10KResult.builder()
        .growth10k(points)
        .performanceStartDate(performanceStart(growth))
        .performanceEndDate(performanceEnd(growth))
        .warnings(weighted.getErrorsAsWarnings())
        .build();
  }

  private static LocalDate performanceStart(Map<LocalDate, BigDecimal> growth) {
    return growth.isEmpty() ? null : growth.keySet().iterator().next();
  }

  private static LocalDate performanceEnd(NavigableMap<LocalDate, BigDecimal> growth) {
    return growth.isEmpty() ? null : growth.lastKey();
  }
}
