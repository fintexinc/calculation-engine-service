package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.RollingSharpeRatioCalculation;
import com.fintex.ce.application.calculation.metric.SharpeRatioCalculation;
import com.fintex.ce.application.calculation.metric.StandardDeviationCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.application.util.TBillsValidator;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
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
      PeriodAbstractService<RollingSharpeRatioResult, RollingCalculationCommand> {

  private final TreasuryBillsFetcher treasuryBillsFetcher;

  public RollingSharpeRatioCalculationServiceImpl(
      MonthlyReturnsService monthlyReturnsService,
      TreasuryBillsFetcher treasuryBillsFetcher,
      @Value("#{'${default.periods.rolling-calculations}'.split(',')}") Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
    this.treasuryBillsFetcher = treasuryBillsFetcher;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ROLLING_SHARPE_RATIO;
  }

  @Override
  public RollingSharpeRatioResult perform(RollingCalculationCommand command) {
    RollingSharpeRatioCalculation rollingSharpeRatioCalculation = defineCalculationMethod(command);
    return rollingSharpeRatioCalculation.calculate(command.getRollingPeriods());
  }

  @Override
  public RollingSharpeRatioCalculation defineCalculationMethod(RollingCalculationCommand command) {
    var tBills = TBillsValidator.requireNonEmpty(
        treasuryBillsFetcher.fetch(command.getCurrency()), command.getCurrency());
    PeriodCalculationInput input = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_ONE);

    StandardDeviationCalculation<SharpeRatioResult> standardDeviationCalculation = new StandardDeviationCalculation<>(
        input, defaultPeriods);
    SharpeRatioCalculation sharpeRatioCalculation = new SharpeRatioCalculation(input, defaultPeriods, tBills,
        standardDeviationCalculation);

    return new RollingSharpeRatioCalculation(input, defaultPeriods, sharpeRatioCalculation);
  }

  @Override
  public PeriodCalculationInput buildPeriodCalculationInput(RollingCalculationCommand command,
      ReturnFactorScale returnFactorScale) {
    MonthlyReturnsContext<HoldingMonthlyReturns> portfolioContext = monthlyReturnsService.getPortfolioMonthlyReturns(
        command.getHoldings(), command.getCurrency());
    WeightedAverageResult<HoldingMonthlyReturns> result = monthlyReturnsService
        .calculateWeightedAverageWithCpsdAndCped(portfolioContext, command.getCustomPsd(), command.getCustomPed(),
            returnFactorScale);
    return new PeriodCalculationInput(result.weightedAverage());
  }
}
