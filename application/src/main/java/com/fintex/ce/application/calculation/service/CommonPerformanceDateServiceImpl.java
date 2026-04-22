package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.CommonPerformanceDatesResult;
import com.fintex.ce.model.dto.command.MultiplePortfoliosCommand;
import com.fintex.ce.model.error.PceExceptionCollector;
import com.fintex.ce.model.error.Warning;
import com.fintex.wm.commons.domain.currency.Currency;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

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
  public CommonPerformanceDatesResult perform(MultiplePortfoliosCommand mReqDTO) {
    List<PortfolioHolding> portfolioHoldings = collectAllPortfolioHoldings(mReqDTO.getPortfolios());

    var collector = new PceExceptionCollector();
    ReturnsAggregate<HoldingMonthlyReturns> monthlyReturnsAggregateForPortfolios = collector.tryCatch(
        () -> getPortfolioMonthlyReturns(portfolioHoldings));
    DateRange commonPerformanceDateForPortfolios = collector.tryCatch(
        () -> commonPerformanceDateFor(monthlyReturnsAggregateForPortfolios));
    ReturnsAggregate<HoldingMonthlyReturns> monthlyReturnsAggregateForBenchmark = collector.tryCatch(
        () -> getPortfolioMonthlyReturns(mReqDTO.getBenchmarkHoldings()));
    DateRange commonPerformanceDatesForBenchmarks = collector.tryCatch(
        () -> commonPerformanceDateFor(monthlyReturnsAggregateForBenchmark));
    collector.throwIfAny();

    List<Warning> warnings = Stream.of(monthlyReturnsAggregateForPortfolios, monthlyReturnsAggregateForBenchmark)
        .filter(Objects::nonNull)
        .flatMap(a -> a.getErrorsAsWarnings().stream())
        .toList();

    CommonPerformanceDatesResult result = new CommonPerformanceDatesResult()
        .setCommonPerformanceStartDatePf(commonPerformanceDateForPortfolios.start())
        .setCommonPerformanceEndDatePf(commonPerformanceDateForPortfolios.end())
        .setCommonPerformanceStartDateBm(commonPerformanceDatesForBenchmarks.start())
        .setCommonPerformanceEndDateBm(commonPerformanceDatesForBenchmarks.end());
    result.setWarnings(warnings);
    return result;
  }

  List<PortfolioHolding> collectAllPortfolioHoldings(Set<MultiplePortfoliosCommand.Portfolio> portfolios) {
    if (CollectionUtils.isEmpty(portfolios)) {
      return List.of();
    }
    return portfolios.stream().flatMap(p -> p.getHoldings().stream()).toList();
  }

  DateRange commonPerformanceDateFor(ReturnsAggregate<HoldingMonthlyReturns> monthlyReturnsAggregate) {
    if (ObjectUtils.isEmpty(monthlyReturnsAggregate)) {
      return DateRange.UNBOUNDED;
    }
    return new DateRange(monthlyReturnsAggregate.getPerformanceStartDate(), monthlyReturnsAggregate
        .getPerformanceEndDate());
  }

  ReturnsAggregate<HoldingMonthlyReturns> getPortfolioMonthlyReturns(List<PortfolioHolding> holdings) {
    if (CollectionUtils.isEmpty(holdings)) {
      return new ReturnsAggregate<>();
    }
    return monthlyReturnsService.getMonthlyReturnsOnlyWithMonthlyReturnsDataValidation(holdings, Currency.CAD);
  }

}
