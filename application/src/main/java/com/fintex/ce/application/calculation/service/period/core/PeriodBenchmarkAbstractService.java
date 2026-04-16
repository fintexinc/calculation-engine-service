package com.fintex.ce.application.calculation.service.period.core;

import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.Notification;
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
    var notification = new Notification();

    ReturnsAggregate portfolioMonthlyReturnsAggregate = notification.tryCatch(() -> monthlyReturnsService
        .getPortfolioMonthlyReturns(command.getHoldings(), command.getCurrency(), returnFactorScale));
    ReturnsAggregate benchmarkMonthlyReturnsAggregate = notification.tryCatch(() -> monthlyReturnsService
        .getBenchmarkMonthlyReturns(command.getBenchmarkHoldings(), command.getCurrency(), returnFactorScale));
    notification.ifAnyErrorThrowException();

    portfolioMonthlyReturnsAggregate.cutArgumentToTheSameEndDate(benchmarkMonthlyReturnsAggregate);
    benchmarkMonthlyReturnsAggregate.cutArgumentToTheSameEndDate(portfolioMonthlyReturnsAggregate);

    NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = notification.tryCatch(() -> monthlyReturnsService
        .getWeightedAverageWithCpedValidation(portfolioMonthlyReturnsAggregate, command.getCustomPed()));
    NavigableMap<LocalDate, BigDecimal> benchmarkTotalReturns = notification.tryCatch(() -> monthlyReturnsService
        .getWeightedAverageWithCpedValidation(benchmarkMonthlyReturnsAggregate, command.getCustomPed()));
    notification.ifAnyErrorThrowException();

    var result = new BenchmarkCalculationDTO();
    result.setWeightedAverageBenchmarkReturns(benchmarkTotalReturns);
    result.setWeightedAveragePortfolioReturns(portfolioTotalReturns);
    result.setCipsd(command.getCustomIntervalPsd());
    return result;
  }

}
