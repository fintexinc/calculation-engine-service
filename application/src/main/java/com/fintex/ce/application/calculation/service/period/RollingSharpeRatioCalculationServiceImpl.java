package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.RollingSharpeRatioCalculation;
import com.fintex.ce.application.calculation.metric.SharpeRatioCalculation;
import com.fintex.ce.application.calculation.metric.StandardDeviationCalculation;
import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpsdAndCpedAbstractService;
import com.fintex.ce.application.config.PeriodProperties;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.application.util.TBillsValidator;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.SharpeRatioResult;
import com.fintex.ce.model.domain.result.rolling.RollingSharpeRatioResult;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.ce.port.webclient.sm.TreasuryBillsFetcher;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import java.util.Set;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class RollingSharpeRatioCalculationServiceImpl
    extends
      WeightedAverageWithCpsdAndCpedAbstractService<RollingCalculationCommand, RollingSharpeRatioResult> {

  private final TreasuryBillsFetcher treasuryBillsFetcher;
  private final Set<TimePeriod> defaultPeriods;

  public RollingSharpeRatioCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpsdAndCpedPipeline portfolioWeightedAverageWithCpsdAndCped,
      TreasuryBillsFetcher treasuryBillsFetcher,
      PeriodProperties periods) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCpsdAndCped);
    this.treasuryBillsFetcher = treasuryBillsFetcher;
    this.defaultPeriods = periods.getRollingCalculations();
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ROLLING_SHARPE_RATIO;
  }

  @Override
  public RollingSharpeRatioResult perform(RollingCalculationCommand command,
      PortfolioBenchmarkReturns returnsData) {
    var tBills = TBillsValidator.requireNonEmpty(
        treasuryBillsFetcher.fetch(command.getCurrency()), command.getCurrency());
    PeriodCalculationInput input = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_ONE,
        returnsData);

    StandardDeviationCalculation<SharpeRatioResult> standardDeviationCalculation = new StandardDeviationCalculation<>(
        input, defaultPeriods);
    SharpeRatioCalculation sharpeRatioCalculation = new SharpeRatioCalculation(input, defaultPeriods, tBills,
        standardDeviationCalculation);

    return new RollingSharpeRatioCalculation(input, defaultPeriods, sharpeRatioCalculation)
        .calculate(command.getRollingPeriods());
  }
}