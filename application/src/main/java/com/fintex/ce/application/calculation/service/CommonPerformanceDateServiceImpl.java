package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.Returns;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.domain.dto.command.MultiplePortfoliosCommand;
import com.fintex.ce.domain.exception.notification.pattern.Notification;
import com.fintex.ce.domain.model.CommonDates;
import com.fintex.ce.domain.model.HoldingMonthlyReturns;
import com.fintex.ce.domain.model.ValidationError;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.CommonPerformanceDatesResult;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

@Service
public class CommonPerformanceDateServiceImpl implements CalculationService<CommonPerformanceDatesResult, MultiplePortfoliosCommand> {

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
    Returns<HoldingMonthlyReturns> monthlyReturnsForPortfolios = notification.tryCatch(() -> getPortfolioMonthlyReturns(
        portfolioHoldings));
    CommonDates commonPerformanceDateForPortfolios = notification.tryCatch(() -> commonPerformanceDateFor(
        monthlyReturnsForPortfolios));
    Returns<HoldingMonthlyReturns> monthlyReturnsForBenchmark = notification.tryCatch(() -> getPortfolioMonthlyReturns(mReqDTO
        .getBenchmarkHoldings()));
    CommonDates commonPerformanceDatesForBenchmarks = notification.tryCatch(() -> commonPerformanceDateFor(
        monthlyReturnsForBenchmark));
    notification.ifAnyErrorThrowException();

    CommonPerformanceDatesResult res = new CommonPerformanceDatesResult()
        .setCommonPerformanceStartDatePf(commonPerformanceDateForPortfolios.getStart())
        .setCommonPerformanceEndDatePf(commonPerformanceDateForPortfolios.getEnd())
        .setCommonPerformanceStartDateBm(commonPerformanceDatesForBenchmarks.getStart())
        .setCommonPerformanceEndDateBm(commonPerformanceDatesForBenchmarks.getEnd());

    if (!ObjectUtils.isEmpty(monthlyReturnsForPortfolios)) {
      res.setErrors(notification.tryCatch(monthlyReturnsForPortfolios.getErrors().stream().map(
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

  CommonDates commonPerformanceDateFor(Returns<HoldingMonthlyReturns> monthlyReturns) {
    if (ObjectUtils.isEmpty(monthlyReturns)) {
      return new CommonDates();
    }
    return new CommonDates()
        .setStart(monthlyReturns.getPsd())
        .setEnd(monthlyReturns.getPed());
  }

  Returns<HoldingMonthlyReturns> getPortfolioMonthlyReturns(List<Holding> holdings) {
    if (CollectionUtils.isEmpty(holdings)) {
      return new Returns<>();
    }
    return monthlyReturnsService.getMonthlyReturnsOnlyWithMonthlyReturnsDataValidation(holdings, CurrencyType.CAD);
  }

}
