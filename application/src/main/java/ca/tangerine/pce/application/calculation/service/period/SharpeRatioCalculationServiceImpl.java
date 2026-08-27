package ca.tangerine.pce.application.calculation.service.period;

import org.springframework.stereotype.Service;

import ca.tangerine.pce.application.calculation.metric.SharpeRatioCalculation;
import ca.tangerine.pce.application.calculation.metric.StandardDeviationCalculation;
import ca.tangerine.pce.application.calculation.service.period.core.WeightedAverageWithCpedAbstractService;
import ca.tangerine.pce.application.config.PeriodProperties;
import ca.tangerine.pce.application.returns.PortfolioMonthlyReturnsContextProvider;
import ca.tangerine.pce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import ca.tangerine.pce.application.util.ReturnFactorScale;
import ca.tangerine.pce.application.util.TBillsValidator;
import ca.tangerine.pce.model.domain.calculation.input.PeriodCalculationInput;
import ca.tangerine.pce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.result.risk.SharpeRatioResult;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.port.webclient.mic.TreasuryBillsFetcher;

@Service
public class SharpeRatioCalculationServiceImpl
    extends
      WeightedAverageWithCpedAbstractService<PeriodCommand, SharpeRatioResult> {

  private final TreasuryBillsFetcher treasuryBillsFetcher;

  public SharpeRatioCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
      final TreasuryBillsFetcher treasuryBillsFetcher,
      PeriodProperties periods) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCped, periods.getRiskCalculations());
    this.treasuryBillsFetcher = treasuryBillsFetcher;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.SHARPE_RATIO;
  }

  @Override
  public SharpeRatioResult perform(final PeriodCommand command,
      final PortfolioBenchmarkReturns returnsData) {
    final var tBills = TBillsValidator.requireNonEmpty(
        treasuryBillsFetcher.fetch(command.getCurrency()), command.getCurrency());
    final PeriodCalculationInput input = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_ONE,
        returnsData);
    final var standardDeviationCalculation = new StandardDeviationCalculation<SharpeRatioResult>(input, defaultPeriods);
    return new SharpeRatioCalculation(input, defaultPeriods, tBills, standardDeviationCalculation)
        .calculate(command.getPeriods());
  }

}
