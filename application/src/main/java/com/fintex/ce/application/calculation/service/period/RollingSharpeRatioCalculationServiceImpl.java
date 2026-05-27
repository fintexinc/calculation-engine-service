package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.RollingSharpeRatioCalculation;
import com.fintex.ce.application.calculation.metric.SharpeRatioCalculation;
import com.fintex.ce.application.calculation.metric.StandardDeviationCalculation;
import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpsdAndCpedAbstractService;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.application.util.TBillsValidator;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.SharpeRatioResult;
import com.fintex.ce.model.domain.result.rolling.RollingSharpeRatioResult;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.ce.port.webclient.sm.TreasuryBillsFetcher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class RollingSharpeRatioCalculationServiceImpl
    extends
      WeightedAverageWithCpsdAndCpedAbstractService<RollingCalculationCommand, RollingSharpeRatioResult> {

  private final TreasuryBillsFetcher treasuryBillsFetcher;
  private final Set<String> defaultPeriods;

  public RollingSharpeRatioCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpsdAndCpedPipeline portfolioWeightedAverageWithCpsdAndCped,
      TreasuryBillsFetcher treasuryBillsFetcher,
      @Value("#{'${default.periods.rolling-calculations}'.split(',')}") Set<String> defaultPeriods) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCpsdAndCped);
    this.treasuryBillsFetcher = treasuryBillsFetcher;
    this.defaultPeriods = defaultPeriods;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ROLLING_SHARPE_RATIO;
  }

  @Override
  public RollingSharpeRatioResult perform(RollingCalculationCommand command) {
    var tBills = TBillsValidator.requireNonEmpty(
        treasuryBillsFetcher.fetch(command.getCurrency()), command.getCurrency());
    PeriodCalculationInput input = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_ONE);

    StandardDeviationCalculation<SharpeRatioResult> standardDeviationCalculation = new StandardDeviationCalculation<>(
        input, defaultPeriods);
    SharpeRatioCalculation sharpeRatioCalculation = new SharpeRatioCalculation(input, defaultPeriods, tBills,
        standardDeviationCalculation);

    return new RollingSharpeRatioCalculation(input, defaultPeriods, sharpeRatioCalculation)
        .calculate(command.getRollingPeriods());
  }
}