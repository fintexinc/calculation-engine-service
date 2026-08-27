package ca.tangerine.pce.application.calculation.service.period;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

import ca.tangerine.pce.application.calculation.metric.TrailingTotalReturnsCalculation;
import ca.tangerine.pce.application.calculation.service.ReturnBenchmarkComparisonService;
import ca.tangerine.pce.application.calculation.service.period.core.WeightedAverageWithCpedAbstractService;
import ca.tangerine.pce.application.config.PeriodProperties;
import ca.tangerine.pce.application.returns.PortfolioMonthlyReturnsContextProvider;
import ca.tangerine.pce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import ca.tangerine.pce.application.util.ReturnFactorScale;
import ca.tangerine.pce.application.util.TBillsValidator;
import ca.tangerine.pce.model.domain.calculation.input.PeriodCalculationInput;
import ca.tangerine.pce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.result.KeyValueResult;
import ca.tangerine.pce.model.domain.result.returns.TrailingTotalReturnsResult;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.port.webclient.mic.TreasuryBillsFetcher;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;

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
