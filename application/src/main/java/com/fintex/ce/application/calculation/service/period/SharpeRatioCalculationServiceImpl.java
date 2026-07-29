package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.SharpeRatioCalculation;
import com.fintex.ce.application.calculation.metric.StandardDeviationCalculation;
import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpedAbstractService;
import com.fintex.ce.application.config.PeriodProperties;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.application.util.TBillsValidator;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.SharpeRatioResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.sm.TreasuryBillsFetcher;

import org.springframework.stereotype.Service;

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
