package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.ce.model.domain.result.CommonPerformanceDatesResult;
import com.fintex.ce.model.dto.command.MultiplePortfoliosCommand;
import com.fintex.ce.model.error.Notification;
import com.fintex.ce.model.error.ValidationError;
import com.fintex.wm.commons.domain.currency.Currency;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Set;

@Service
public class CommonPerformanceDateServiceImpl
    implements
      CalculationService<CommonPerformanceDatesResult, MultiplePortfoliosCommand> {

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
    List<Holding> portfolioHoldings = collectAllPortfolioHoldings(mReqDTO.getPortfolios());

    var notification = new Notification();
    ReturnsAggregate<HoldingMonthlyReturns> monthlyReturnsAggregateForPortfolios = notification.tryCatch(
        () -> getPortfolioMonthlyReturns(
            portfolioHoldings));
    DateRange commonPerformanceDateForPortfolios = notification.tryCatch(() -> commonPerformanceDateFor(
        monthlyReturnsAggregateForPortfolios));
    ReturnsAggregate<HoldingMonthlyReturns> monthlyReturnsAggregateForBenchmark = notification.tryCatch(
        () -> getPortfolioMonthlyReturns(
            mReqDTO
                .getBenchmarkHoldings()));
    DateRange commonPerformanceDatesForBenchmarks = notification.tryCatch(() -> commonPerformanceDateFor(
        monthlyReturnsAggregateForBenchmark));
    notification.ifAnyErrorThrowException();

    CommonPerformanceDatesResult res = new CommonPerformanceDatesResult()
        .setCommonPerformanceStartDatePf(commonPerformanceDateForPortfolios.start())
        .setCommonPerformanceEndDatePf(commonPerformanceDateForPortfolios.end())
        .setCommonPerformanceStartDateBm(commonPerformanceDatesForBenchmarks.start())
        .setCommonPerformanceEndDateBm(commonPerformanceDatesForBenchmarks.end());

    if (!ObjectUtils.isEmpty(monthlyReturnsAggregateForPortfolios)) {
      res.setErrors(notification.tryCatch(monthlyReturnsAggregateForPortfolios.getErrors().stream().map(
          err -> new ValidationError(err.getId(), err.getCode().name(), err.getMessage()))::toList));
    }

    return res;
  }

  List<Holding> collectAllPortfolioHoldings(Set<MultiplePortfoliosCommand.Portfolio> portfolios) {
    if (CollectionUtils.isEmpty(portfolios)) {
      return List.of();
    }
    return portfolios.stream().flatMap(p -> p.getHoldings().stream()).toList();
  }

  DateRange commonPerformanceDateFor(ReturnsAggregate<HoldingMonthlyReturns> monthlyReturnsAggregate) {
    if (ObjectUtils.isEmpty(monthlyReturnsAggregate)) {
      return DateRange.UNBOUNDED;
    }
    return new DateRange(monthlyReturnsAggregate.getPsd(), monthlyReturnsAggregate.getPed());
  }

  ReturnsAggregate<HoldingMonthlyReturns> getPortfolioMonthlyReturns(List<Holding> holdings) {
    if (CollectionUtils.isEmpty(holdings)) {
      return new ReturnsAggregate<>();
    }
    return monthlyReturnsService.getMonthlyReturnsOnlyWithMonthlyReturnsDataValidation(holdings, Currency.CAD);
  }

}
