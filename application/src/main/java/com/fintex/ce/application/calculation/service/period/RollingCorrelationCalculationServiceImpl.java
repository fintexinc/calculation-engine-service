package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.CorrelationCalculation;
import com.fintex.ce.application.calculation.metric.RollingCorrelationCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.application.returns.Returns;
import com.fintex.ce.domain.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.domain.dto.command.RollingCalculationCommand;
import com.fintex.ce.domain.exception.notification.pattern.Notification;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.RollingCorrelationResult;
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
    Map<Holding, Map<LocalDate, BigDecimal>> baseTotalReturn = getBaseTotalReturns(reqDTO);
    var correlationCalculation = new CorrelationCalculation(inputDTO, baseTotalReturn, defaultPeriods);
    return new RollingCorrelationCalculation(inputDTO, defaultPeriods, correlationCalculation, inputDTO
        .getWeightedAverageBenchmarkReturns());
  }

  public Map<Holding, Map<LocalDate, BigDecimal>> getBaseTotalReturns(RollingCalculationCommand reqDTO) {
    Returns monthlyReturns = monthlyReturnsService.getPortfolioMonthlyReturns(reqDTO.getHoldings(), reqDTO
        .getCurrency(), ReturnFactorScale.SCALE_OF_TWO);

    return monthlyReturns
        .validateCped(reqDTO.getCustomPed())
        .cutByCpedIfCpedEmptyCutByPed(reqDTO.getCustomPed())
        .fxRatesApplied()
        .getReturnsMap();
  }

  @Override
  public BenchmarkCalculationDTO buildCalculationDto(RollingCalculationCommand reqDTO,
      ReturnFactorScale returnFactorScale) {
    Notification notification = new Notification();

    Returns portfolioMonthlyReturns = notification.tryCatch(() -> monthlyReturnsService
        .getPortfolioMonthlyReturns(reqDTO.getHoldings(), reqDTO.getCurrency(), returnFactorScale));
    Returns benchmarkMonthlyReturns = notification.tryCatch(() -> monthlyReturnsService
        .getBenchmarkMonthlyReturns(reqDTO.getBenchmarkHoldings(), reqDTO.getCurrency(), returnFactorScale));
    notification.ifAnyErrorThrowException();

    portfolioMonthlyReturns.cutArgumentToTheSameEndDate(benchmarkMonthlyReturns);
    benchmarkMonthlyReturns.cutArgumentToTheSameEndDate(portfolioMonthlyReturns);

    NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = notification.tryCatch(() -> monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(portfolioMonthlyReturns, reqDTO.getCustomPsd(), reqDTO
            .getCustomPed()));
    NavigableMap<LocalDate, BigDecimal> benchmarkTotalReturns = notification.tryCatch(() -> monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(benchmarkMonthlyReturns, reqDTO.getCustomPsd(), reqDTO
            .getCustomPed()));
    notification.ifAnyErrorThrowException();

    var result = new BenchmarkCalculationDTO();
    result.setWeightedAverageBenchmarkReturns(benchmarkTotalReturns);
    result.setWeightedAveragePortfolioReturns(portfolioTotalReturns);
    result.setCipsd(reqDTO.getCustomIntervalPsd());
    return result;
  }

}
