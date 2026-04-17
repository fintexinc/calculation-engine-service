package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.CorrelationCalculation;
import com.fintex.ce.application.calculation.metric.RollingCorrelationCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.rolling.RollingCorrelationResult;
import com.fintex.ce.model.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.ce.model.error.Notification;
import com.fintex.ce.util.ReturnFactorScale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

@Service
public class RollingCorrelationCalculationServiceImpl
    extends
      PeriodBenchmarkAbstractService<RollingCorrelationResult, RollingCalculationCommand> {

  public RollingCorrelationCalculationServiceImpl(
      MonthlyReturnsService monthlyReturnsService,
      @Value("#{'${default.periods.rolling-calculations}'.split(',')}") Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ROLLING_CORRELATION;
  }

  @Override
  public RollingCorrelationResult perform(RollingCalculationCommand reqDTO) {
    RollingCorrelationCalculation rollingCorrelationCalculation = defineCalculationMethod(reqDTO);
    return rollingCorrelationCalculation.calculate(reqDTO.getRollingPeriods());
  }

  @Override
  public RollingCorrelationCalculation defineCalculationMethod(RollingCalculationCommand reqDTO) {
    BenchmarkCalculationDTO inputDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
    Map<PortfolioHolding, Map<LocalDate, BigDecimal>> baseTotalReturn = getBaseTotalReturns(reqDTO);
    var correlationCalculation = new CorrelationCalculation(inputDTO, baseTotalReturn, defaultPeriods);
    return new RollingCorrelationCalculation(inputDTO, defaultPeriods, correlationCalculation, inputDTO
        .getWeightedAverageBenchmarkReturns());
  }

  public Map<PortfolioHolding, Map<LocalDate, BigDecimal>> getBaseTotalReturns(RollingCalculationCommand reqDTO) {
    ReturnsAggregate monthlyReturnsAggregate = monthlyReturnsService.getPortfolioMonthlyReturns(reqDTO.getHoldings(),
        reqDTO
            .getCurrency(), ReturnFactorScale.SCALE_OF_TWO);

    return monthlyReturnsAggregate
        .validateCped(reqDTO.getCustomPed())
        .cutByCpedIfCpedEmptyCutByPed(reqDTO.getCustomPed())
        .fxRatesApplied()
        .getReturnsMap();
  }

  @Override
  public BenchmarkCalculationDTO buildCalculationDto(RollingCalculationCommand reqDTO,
      ReturnFactorScale returnFactorScale) {
    Notification notification = new Notification();

    ReturnsAggregate portfolioMonthlyReturnsAggregate = notification.tryCatch(() -> monthlyReturnsService
        .getPortfolioMonthlyReturns(reqDTO.getHoldings(), reqDTO.getCurrency(), returnFactorScale));
    ReturnsAggregate benchmarkMonthlyReturnsAggregate = notification.tryCatch(() -> monthlyReturnsService
        .getBenchmarkMonthlyReturns(reqDTO.getBenchmarkHoldings(), reqDTO.getCurrency(), returnFactorScale));
    notification.ifAnyErrorThrowException();

    portfolioMonthlyReturnsAggregate.cutArgumentToTheSameEndDate(benchmarkMonthlyReturnsAggregate);
    benchmarkMonthlyReturnsAggregate.cutArgumentToTheSameEndDate(portfolioMonthlyReturnsAggregate);

    NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = notification.tryCatch(() -> monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(portfolioMonthlyReturnsAggregate, reqDTO.getCustomPsd(), reqDTO
            .getCustomPed()));
    NavigableMap<LocalDate, BigDecimal> benchmarkTotalReturns = notification.tryCatch(() -> monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(benchmarkMonthlyReturnsAggregate, reqDTO.getCustomPsd(), reqDTO
            .getCustomPed()));
    notification.ifAnyErrorThrowException();

    var result = new BenchmarkCalculationDTO();
    result.setWeightedAverageBenchmarkReturns(benchmarkTotalReturns);
    result.setWeightedAveragePortfolioReturns(portfolioTotalReturns);
    result.setCipsd(reqDTO.getCustomIntervalPsd());
    return result;
  }

}
