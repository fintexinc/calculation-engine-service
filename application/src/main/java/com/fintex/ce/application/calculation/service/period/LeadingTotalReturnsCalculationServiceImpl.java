package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.LeadingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.returns.LeadingTotalReturnsResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
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
  public LeadingTotalReturnsResult perform(final LeadingTotalReturnCommand reqDTO) {
    final LeadingTotalReturnsCalculation leadingTotalReturnsCalculation = defineCalculationMethod(reqDTO);
    return leadingTotalReturnsCalculation.calculate(reqDTO.getPeriods());
  }

  @Override
  public LeadingTotalReturnsCalculation defineCalculationMethod(final LeadingTotalReturnCommand reqDTO) {
    final CalculationDTO input = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
    return new LeadingTotalReturnsCalculation(input, defaultPeriods);
  }

  @Override
  public CalculationDTO buildCalculationDto(final LeadingTotalReturnCommand reqDTO,
      final ReturnFactorScale returnFactorScale) {
    final ReturnsAggregate portfolioMonthlyReturnsAggregate = monthlyReturnsService.getPortfolioMonthlyReturns(
        reqDTO.getHoldings(), reqDTO.getCurrency(), returnFactorScale);

    final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = portfolioMonthlyReturnsAggregate
        .validateCpsd(reqDTO.getCustomPsd())
        .cutByPed()
        .cutByCpsdIfCpsdEmptyCutByPsd(reqDTO.getCustomPsd())
        .fxRatesApplied()
        .getWeightedAverage();

    return new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioTotalReturns);

  }

}
