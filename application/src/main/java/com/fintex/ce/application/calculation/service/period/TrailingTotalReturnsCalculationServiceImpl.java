package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.TrailingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.ReturnBenchmarkComparisonService;
import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpedAbstractService;
import com.fintex.ce.application.config.PeriodProperties;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.application.util.TBillsValidator;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.KeyValueResult;
import com.fintex.ce.model.domain.result.returns.TrailingTotalReturnsResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.mic.TreasuryBillsFetcher;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class TrailingTotalReturnsCalculationServiceImpl
    extends
      WeightedAverageWithCpedAbstractService<PeriodCommand, TrailingTotalReturnsResult> {

  private final TreasuryBillsFetcher treasuryBillsFetcher;
  private final ReturnBenchmarkComparisonService returnBenchmarkComparisonService;

  public TrailingTotalReturnsCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
      TreasuryBillsFetcher treasuryBillsFetcher,
      PeriodProperties periods,
      ReturnBenchmarkComparisonService returnBenchmarkComparisonService) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCped, periods.getTrailingTotalReturns());
    this.treasuryBillsFetcher = treasuryBillsFetcher;
    this.returnBenchmarkComparisonService = returnBenchmarkComparisonService;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.TRAILING_TOTAL_RETURNS;
  }

  @Override
  public TrailingTotalReturnsResult perform(PeriodCommand command,
      PortfolioBenchmarkReturns returnsData) {
    PeriodCalculationInput input = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO,
        returnsData);
    var tBills = TBillsValidator.requireNonEmpty(
        treasuryBillsFetcher.fetch(command.getCurrency()), command.getCurrency());
    TrailingTotalReturnsResult result = TrailingTotalReturnsCalculation.withTBillPrecondition(input,
        defaultPeriods,
        tBills)
        .calculate(command.getPeriods());
    if (CollectionUtils.isEmpty(command.getBenchmarkHoldings())) {
      return result;
    }

    var comparison = returnBenchmarkComparisonService.compare(
        new ReturnBenchmarkComparisonService.ReturnBenchmarkComparisonRequest<>(
            comparisonValues(result),
            returnBenchmarkComparisonService.benchmarkWeightedAverage(
                command, returnsData, ReturnFactorScale.SCALE_OF_TWO),
            benchmarkWeightedAverage -> {
              PeriodCalculationInput benchmarkInput = new PeriodCalculationInput(
                  command.getCustomIntervalPsd(),
                  benchmarkWeightedAverage.weightedAverage(),
                  benchmarkWeightedAverage.getErrorsAsWarnings());
              return TrailingTotalReturnsCalculation.withTBillPrecondition(benchmarkInput, defaultPeriods, tBills)
                  .calculate(command.getPeriods());
            },
            TrailingTotalReturnsCalculationServiceImpl::comparisonValues));
    result.setComparison(comparison.comparison());
    result.setWarnings(returnBenchmarkComparisonService.mergeWarnings(result.getWarnings(), comparison.warnings()));
    return result;
  }

  private static List<KeyValueResult<TimePeriod>> comparisonValues(TrailingTotalReturnsResult result) {
    return result.getTrailingTotalReturn().stream()
        .map(trailingReturn -> new KeyValueResult<>(TimePeriod.valueOf(trailingReturn.period()), trailingReturn
            .value()))
        .toList();
  }
}
