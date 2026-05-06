package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.RollingSharpeRatioCalculation;
import com.fintex.ce.application.calculation.metric.SharpeRatioCalculation;
import com.fintex.ce.application.calculation.metric.StandardDeviationCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.application.util.TBillsValidator;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.SharpeRatioResult;
import com.fintex.ce.model.domain.result.rolling.RollingSharpeRatioResult;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.ce.port.webclient.sm.TBillsFetcher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

@Service
public class RollingSharpeRatioCalculationServiceImpl
    extends
      PeriodAbstractService<RollingSharpeRatioResult, RollingCalculationCommand> {

  private final TBillsFetcher tBillsProvider;

  public RollingSharpeRatioCalculationServiceImpl(
      final MonthlyReturnsService monthlyReturnsService,
      final TBillsFetcher tBillsProvider,
      @Value("#{'${default.periods.rolling-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
    this.tBillsProvider = tBillsProvider;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ROLLING_SHARPE_RATIO;
  }

  @Override
  public RollingSharpeRatioResult perform(final RollingCalculationCommand command) {
    final var rollingStandardDeviationCalculation = defineCalculationMethod(command);
    return rollingStandardDeviationCalculation.calculate(command.getRollingPeriods());
  }

  @Override
  public RollingSharpeRatioCalculation defineCalculationMethod(final RollingCalculationCommand command) {
    final PeriodCalculationInput input = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_ONE);
    final var tBills = TBillsValidator.requireNonEmpty(
        tBillsProvider.fetch().get(command.getCurrency()), command.getCurrency());

    final var standardDeviationCalculation = new StandardDeviationCalculation<SharpeRatioResult>(input, defaultPeriods);
    final var sharpeRatioCalculation = new SharpeRatioCalculation(input, defaultPeriods, tBills,
        standardDeviationCalculation);

    return new RollingSharpeRatioCalculation(input, defaultPeriods, sharpeRatioCalculation);
  }

  @Override
  public PeriodCalculationInput buildPeriodCalculationInput(final RollingCalculationCommand command,
      final ReturnFactorScale returnFactorScale) {
    final ReturnsAggregate monthlyReturnsAggregate = monthlyReturnsService.getPortfolioMonthlyReturns(
        command.getHoldings(), command.getCurrency(), returnFactorScale);

    final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(monthlyReturnsAggregate, command.getCustomPsd(), command
            .getCustomPed());

    return new PeriodCalculationInput(portfolioTotalReturns);
  }

}
