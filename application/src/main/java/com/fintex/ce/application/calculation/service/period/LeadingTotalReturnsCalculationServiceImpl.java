package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.LeadingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.returns.LeadingTotalReturnsResult;
import com.fintex.ce.model.dto.command.LeadingTotalReturnCommand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

@Service
public class LeadingTotalReturnsCalculationServiceImpl
    extends
      PeriodAbstractService<LeadingTotalReturnsResult, LeadingTotalReturnCommand> {

  public LeadingTotalReturnsCalculationServiceImpl(
      @Autowired final MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.leading-total-returns}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.LEADING_TOTAL_RETURNS;
  }

  @Override
  public LeadingTotalReturnsResult perform(final LeadingTotalReturnCommand command) {
    final LeadingTotalReturnsCalculation leadingTotalReturnsCalculation = defineCalculationMethod(command);
    return leadingTotalReturnsCalculation.calculate(command.getPeriods());
  }

  @Override
  public LeadingTotalReturnsCalculation defineCalculationMethod(final LeadingTotalReturnCommand command) {
    final PeriodCalculationInput input = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO);
    return new LeadingTotalReturnsCalculation(input, defaultPeriods);
  }

  @Override
  public PeriodCalculationInput buildPeriodCalculationInput(final LeadingTotalReturnCommand command,
      final ReturnFactorScale returnFactorScale) {
    final ReturnsAggregate portfolioMonthlyReturnsAggregate = monthlyReturnsService.getPortfolioMonthlyReturns(
        command.getHoldings(), command.getCurrency(), returnFactorScale);

    final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = portfolioMonthlyReturnsAggregate
        .validateCpsd(command.getCustomPsd())
        .cutByPed()
        .cutByCpsdIfCpsdEmptyCutByPsd(command.getCustomPsd())
        .fxRatesApplied()
        .getWeightedAverage();

    return new PeriodCalculationInput().setWeightedAveragePortfolioReturns(portfolioTotalReturns);

  }

}
