package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.CorrelationCalculation;
import com.fintex.ce.application.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.monthlyreturns.Returns;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.port.input.result.CorrelationResult;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.application.service.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

@Service
public class CorrelationServiceImpl extends PeriodAbstractService<CorrelationResult, PeriodCommand> {

  public CorrelationServiceImpl(
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods,      final MonthlyReturnsService monthlyReturnsService) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public CorrelationResult perform(final PeriodCommand reqDTO) {
    final PeriodCalculationAbstract<CorrelationResult, ?> calculationMethod = defineCalculationMethod(reqDTO);
    return calculationMethod.calculate(reqDTO.getPeriods());
  }

  public CorrelationCalculation defineCalculationMethod(final PeriodCommand reqDTO) {
    reqDTO.setReqCurrencyToCashHolding();
    final Returns monthlyReturns = monthlyReturnsService.getPortfolioMonthlyReturns(
        reqDTO.getHoldings(), reqDTO.getCurrency(), ReturnFactorScale.SCALE_OF_TWO);

    final Map<Holding, Map<LocalDate, BigDecimal>> baseTotalReturns = monthlyReturns
        .validateCped(reqDTO.getCustomPed())
        .cutByCpedIfCpedEmptyCutByPed(reqDTO.getCustomPed())
        .fxRatesApplied()
        .getReturnsMap();

    final NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns = monthlyReturns
        .cutByPsd()
        .getWeightedAverage();

    final var calculationDTO = new CalculationDTO(reqDTO.getCustomIntervalPsd(), weightedAveragePortfolioReturns);
    return new CorrelationCalculation(calculationDTO, baseTotalReturns, defaultPeriods);
  }

}
