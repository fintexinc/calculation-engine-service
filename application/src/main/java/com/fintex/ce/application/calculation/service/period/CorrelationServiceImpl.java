package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.CorrelationCalculation;
import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodAbstractService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.correlation.CorrelationResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
import com.fintex.ce.model.dto.command.PeriodCommand;

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
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods,
      final MonthlyReturnsService monthlyReturnsService) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.CORRELATION;
  }

  @Override
  public CorrelationResult perform(final PeriodCommand reqDTO) {
    final PeriodCalculationAbstract<CorrelationResult, ?> calculationMethod = defineCalculationMethod(reqDTO);
    return calculationMethod.calculate(reqDTO.getPeriods());
  }

  public CorrelationCalculation defineCalculationMethod(final PeriodCommand reqDTO) {
    reqDTO.setReqCurrencyToCashHolding();
    final ReturnsAggregate monthlyReturnsAggregate = monthlyReturnsService.getPortfolioMonthlyReturns(
        reqDTO.getHoldings(), reqDTO.getCurrency(), ReturnFactorScale.SCALE_OF_TWO);

    final Map<PortfolioHolding, Map<LocalDate, BigDecimal>> baseTotalReturns = monthlyReturnsAggregate
        .validateCped(reqDTO.getCustomPed())
        .cutByCpedIfCpedEmptyCutByPed(reqDTO.getCustomPed())
        .fxRatesApplied()
        .getReturnsMap();

    final NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns = monthlyReturnsAggregate
        .cutByPsd()
        .getWeightedAverage();

    final var calculationDTO = new CalculationDTO(reqDTO.getCustomIntervalPsd(), weightedAveragePortfolioReturns);
    return new CorrelationCalculation(calculationDTO, baseTotalReturns, defaultPeriods);
  }

}
