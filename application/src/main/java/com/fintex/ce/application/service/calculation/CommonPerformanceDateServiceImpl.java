package com.fintex.ce.application.service.calculation;

import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.monthlyreturns.Returns;
import com.fintex.ce.domain.model.CommonDates;
import com.fintex.ce.domain.model.ValidationError;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.MultiplePortfoliosCommand;
import com.fintex.ce.port.input.result.CommonPerformanceDatesResult;
import com.fintex.ce.domain.exception.notification.pattern.Notification;
import com.fintex.ce.domain.model.MonthlyReturns;
import com.fintex.ce.service.calculation.CommonPerformanceDateService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Set;

@Service
public class CommonPerformanceDateServiceImpl implements CommonPerformanceDateService {

  private final MonthlyReturnsService monthlyReturnsService;

  public CommonPerformanceDateServiceImpl(MonthlyReturnsService monthlyReturnsService) {
    this.monthlyReturnsService = monthlyReturnsService;
  }

  @Override
  public CommonPerformanceDatesResult commonPerformanceDate(MultiplePortfoliosCommand mReqDTO) {
    List<Holding> portfolioHoldings = collectAllPortfolioHoldings(mReqDTO.getPortfolios());

    var notification = new Notification();
    Returns<MonthlyReturns> monthlyReturnsForPortfolios = notification.tryCatch(() -> getPortfolioMonthlyReturns(
        portfolioHoldings));
    CommonDates commonPerformanceDateForPortfolios = notification.tryCatch(() -> commonPerformanceDateFor(
        monthlyReturnsForPortfolios));
    Returns<MonthlyReturns> monthlyReturnsForBenchmark = notification.tryCatch(() -> getPortfolioMonthlyReturns(mReqDTO
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

  CommonDates commonPerformanceDateFor(Returns<MonthlyReturns> monthlyReturns) {
    if (ObjectUtils.isEmpty(monthlyReturns)) {
      return new CommonDates();
    }
    return new CommonDates()
        .setStart(monthlyReturns.getPsd())
        .setEnd(monthlyReturns.getPed());
  }

  Returns<MonthlyReturns> getPortfolioMonthlyReturns(List<Holding> holdings) {
    if (CollectionUtils.isEmpty(holdings)) {
      return new Returns<>();
    }
    return monthlyReturnsService.getMonthlyReturnsOnlyWithMonthlyReturnsDataValidation(holdings, Currency.CAD);
  }

}
