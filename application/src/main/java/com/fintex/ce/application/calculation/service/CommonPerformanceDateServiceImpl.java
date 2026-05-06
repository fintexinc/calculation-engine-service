package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.CommonPerformanceDatesResult;
import com.fintex.ce.model.dto.command.MultiplePortfoliosCommand;
import com.fintex.ce.model.error.PceExceptionCollector;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class CommonPerformanceDateServiceImpl
    implements
      CalculationService<MultiplePortfoliosCommand, CommonPerformanceDatesResult> {

  private final MonthlyReturnsService monthlyReturnsService;

  public CommonPerformanceDateServiceImpl(MonthlyReturnsService monthlyReturnsService) {
    this.monthlyReturnsService = monthlyReturnsService;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.COMMON_PERFORMANCE_DATES;
  }

  @Override
  public CommonPerformanceDatesResult perform(MultiplePortfoliosCommand command) {
    List<PortfolioHolding> portfolioHoldings = collectAllPortfolioHoldings(command.getPortfolios());

    PceExceptionCollector collector = new PceExceptionCollector();
    ReturnsSnapshot<HoldingMonthlyReturns> portfolioSnapshot = collector.tryCatch(
        () -> getPortfolioMonthlyReturns(portfolioHoldings));
    DateRange commonPerformanceDateForPortfolios = collector.tryCatch(
        () -> commonPerformanceDateFor(portfolioSnapshot));
    ReturnsSnapshot<HoldingMonthlyReturns> benchmarkSnapshot = collector.tryCatch(
        () -> getPortfolioMonthlyReturns(command.getBenchmarkHoldings()));
    DateRange commonPerformanceDatesForBenchmarks = collector.tryCatch(
        () -> commonPerformanceDateFor(benchmarkSnapshot));
    collector.throwIfAny();

    List<Notification> warnings = Stream.of(portfolioSnapshot, benchmarkSnapshot)
        .filter(Objects::nonNull)
        .flatMap(snapshot -> snapshot.getErrorsAsWarnings().stream())
        .toList();

    return CommonPerformanceDatesResult.builder()
        .commonPerformanceStartDatePf(commonPerformanceDateForPortfolios.start())
        .commonPerformanceEndDatePf(commonPerformanceDateForPortfolios.end())
        .commonPerformanceStartDateBm(commonPerformanceDatesForBenchmarks.start())
        .commonPerformanceEndDateBm(commonPerformanceDatesForBenchmarks.end())
        .warnings(warnings)
        .build();
  }

  List<PortfolioHolding> collectAllPortfolioHoldings(Set<MultiplePortfoliosCommand.Portfolio> portfolios) {
    if (CollectionUtils.isEmpty(portfolios)) {
      return List.of();
    }
    return portfolios.stream().flatMap(portfolio -> portfolio.getHoldings().stream()).toList();
  }

  DateRange commonPerformanceDateFor(ReturnsSnapshot<HoldingMonthlyReturns> snapshot) {
    if (snapshot == null
        || (snapshot.performanceStartDate() == null && snapshot.performanceEndDate() == null)) {
      return DateRange.UNBOUNDED;
    }
    return new DateRange(snapshot.performanceStartDate(), snapshot.performanceEndDate());
  }

  ReturnsSnapshot<HoldingMonthlyReturns> getPortfolioMonthlyReturns(List<PortfolioHolding> holdings) {
    if (CollectionUtils.isEmpty(holdings)) {
      return ReturnsSnapshot.empty();
    }
    return monthlyReturnsService.getMonthlyReturnsOnlyWithMonthlyReturnsDataValidation(holdings);
  }
}
