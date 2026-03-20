package com.fintex.ce.application.service.calculation.period.core;

import com.fintex.ce.monthlyreturns.Returns;
import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.model.result.PeriodResult;
import com.fintex.ce.domain.exception.notification.pattern.Notification;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
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

    Returns portfolioMonthlyReturns = notification.tryCatch(() -> monthlyReturnsService
        .getPortfolioMonthlyReturns(command.getHoldings(), command.getCurrency(), returnFactorScale));
    Returns benchmarkMonthlyReturns = notification.tryCatch(() -> monthlyReturnsService
        .getBenchmarkMonthlyReturns(command.getBenchmarkHoldings(), command.getCurrency(), returnFactorScale));
    notification.ifAnyErrorThrowException();

    portfolioMonthlyReturns.cutArgumentToTheSameEndDate(benchmarkMonthlyReturns);
    benchmarkMonthlyReturns.cutArgumentToTheSameEndDate(portfolioMonthlyReturns);

    NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = notification.tryCatch(() -> monthlyReturnsService
        .getWeightedAverageWithCpedValidation(portfolioMonthlyReturns, command.getCustomPed()));
    NavigableMap<LocalDate, BigDecimal> benchmarkTotalReturns = notification.tryCatch(() -> monthlyReturnsService
        .getWeightedAverageWithCpedValidation(benchmarkMonthlyReturns, command.getCustomPed()));
    notification.ifAnyErrorThrowException();

    var result = new BenchmarkCalculationDTO();
    result.setWeightedAverageBenchmarkReturns(benchmarkTotalReturns);
    result.setWeightedAveragePortfolioReturns(portfolioTotalReturns);
    result.setCipsd(command.getCustomIntervalPsd());
    return result;
  }

}
