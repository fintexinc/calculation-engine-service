package ca.tangerine.pce.application.calculation.service;

import ca.tangerine.pce.application.calculation.service.period.core.WeightedAverageWithCpsdAndCpedAbstractService;
import ca.tangerine.pce.application.returns.PortfolioMonthlyReturnsContextProvider;
import ca.tangerine.pce.application.returns.WeightedAverageResult;
import ca.tangerine.pce.application.returns.pipeline.PortfolioWeightedAverageWithCpsdAndCpedPipeline;
import ca.tangerine.pce.application.util.Growth10KHelper;
import ca.tangerine.pce.application.util.ReturnFactorScale;
import ca.tangerine.pce.model.domain.calculation.returns.HoldingMonthlyReturns;
import ca.tangerine.pce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.result.KeyValueResult;
import ca.tangerine.pce.model.domain.result.returns.Growth10KResult;
import ca.tangerine.pce.model.dto.command.ReturnCommand;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

@Service
public class GrowthOf10KCalculationServiceImpl
    extends
      WeightedAverageWithCpsdAndCpedAbstractService<ReturnCommand, Growth10KResult> {

  private final ReturnBenchmarkComparisonService returnBenchmarkComparisonService;

  public GrowthOf10KCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpsdAndCpedPipeline portfolioWeightedAverageWithCpsdAndCped,
      ReturnBenchmarkComparisonService returnBenchmarkComparisonService) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCpsdAndCped);
    this.returnBenchmarkComparisonService = returnBenchmarkComparisonService;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.GROWTH_OF_10K;
  }

  @Override
  public Growth10KResult perform(ReturnCommand command,
      PortfolioBenchmarkReturns returnsData) {
    WeightedAverageResult<HoldingMonthlyReturns> weighted = runWeightedAverage(command, ReturnFactorScale.SCALE_OF_TWO,
        returnsData);
    Growth10KResult result = buildResult(weighted);
    if (CollectionUtils.isEmpty(command.getBenchmarkHoldings())) {
      return result;
    }

    var comparison = returnBenchmarkComparisonService.compare(
        new ReturnBenchmarkComparisonService.ReturnBenchmarkComparisonRequest<>(
            result.getGrowth10k(),
            returnBenchmarkComparisonService.benchmarkWeightedAverage(
                command, returnsData, ReturnFactorScale.SCALE_OF_TWO),
            GrowthOf10KCalculationServiceImpl::buildResult,
            Growth10KResult::getGrowth10k));
    result.setComparison(comparison.comparison());
    result.setWarnings(returnBenchmarkComparisonService.mergeWarnings(result.getWarnings(), comparison.warnings()));
    return result;
  }

  private static Growth10KResult buildResult(WeightedAverageResult<HoldingMonthlyReturns> weighted) {
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
