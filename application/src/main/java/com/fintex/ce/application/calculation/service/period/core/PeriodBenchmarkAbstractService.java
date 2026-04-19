package com.fintex.ce.application.calculation.service.period.core;

import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.PceExceptionCollector;
import com.fintex.ce.util.ReturnFactorScale;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

public abstract class PeriodBenchmarkAbstractService<E extends PeriodResult, R extends PeriodCommand>
    extends
      PeriodAbstractService<E, R> {

  protected PeriodBenchmarkAbstractService(final MonthlyReturnsService monthlyReturnsService,
      final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
  }

  @Override
  public BenchmarkCalculationDTO buildCalculationDto(R command, ReturnFactorScale returnFactorScale) {
    var collector = new PceExceptionCollector();

    ReturnsAggregate portfolioMonthlyReturnsAggregate = collector.tryCatch(() -> monthlyReturnsService
        .getPortfolioMonthlyReturns(command.getHoldings(), command.getCurrency(), returnFactorScale));
    ReturnsAggregate benchmarkMonthlyReturnsAggregate = collector.tryCatch(() -> monthlyReturnsService
        .getBenchmarkMonthlyReturns(command.getBenchmarkHoldings(), command.getCurrency(), returnFactorScale));
    collector.throwIfAny();

    portfolioMonthlyReturnsAggregate.cutArgumentToTheSameEndDate(benchmarkMonthlyReturnsAggregate);
    benchmarkMonthlyReturnsAggregate.cutArgumentToTheSameEndDate(portfolioMonthlyReturnsAggregate);

    NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = collector.tryCatch(() -> monthlyReturnsService
        .getWeightedAverageWithCpedValidation(portfolioMonthlyReturnsAggregate, command.getCustomPed()));
    NavigableMap<LocalDate, BigDecimal> benchmarkTotalReturns = collector.tryCatch(() -> monthlyReturnsService
        .getWeightedAverageWithCpedValidation(benchmarkMonthlyReturnsAggregate, command.getCustomPed()));
    collector.throwIfAny();

    var result = new BenchmarkCalculationDTO();
    result.setWeightedAverageBenchmarkReturns(benchmarkTotalReturns);
    result.setWeightedAveragePortfolioReturns(portfolioTotalReturns);
    result.setCipsd(command.getCustomIntervalPsd());
    return result;
  }

}
