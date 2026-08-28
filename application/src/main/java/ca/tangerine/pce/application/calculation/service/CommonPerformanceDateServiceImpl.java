package ca.tangerine.pce.application.calculation.service;

import ca.tangerine.pce.application.returns.ReturnsSnapshot;
import ca.tangerine.pce.calculation.ReturnsBasedCalculationService;
import ca.tangerine.pce.model.domain.calculation.DateRange;
import ca.tangerine.pce.model.domain.calculation.returns.HoldingMonthlyReturns;
import ca.tangerine.pce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.CommonPerformanceDatesResult;
import ca.tangerine.pce.model.domain.security.SecurityData;
import ca.tangerine.pce.model.dto.command.MultiplePortfoliosCommand;
import ca.tangerine.pce.model.error.PceExceptionCollector;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.error.Notification;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class CommonPerformanceDateServiceImpl
    implements
      ReturnsBasedCalculationService<MultiplePortfoliosCommand, CommonPerformanceDatesResult> {

  private final MonthlyReturnsService monthlyReturnsService;

  public CommonPerformanceDateServiceImpl(MonthlyReturnsService monthlyReturnsService) {
    this.monthlyReturnsService = monthlyReturnsService;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.COMMON_PERFORMANCE_DATES;
  }

  @Override
  public List<CompositeSecurityAttribute> requiredAttributes() {
    return List.of(CompositeSecurityAttribute.MONTHLY_RETURNS);
  }

  @Override
  public PortfolioBenchmarkReturns prepareData(SecurityData securityData) {
    return PortfolioBenchmarkReturns.from(securityData);
  }

  @Override
  public CommonPerformanceDatesResult perform(MultiplePortfoliosCommand command,
      PortfolioBenchmarkReturns returnsData) {
    List<PortfolioHolding> portfolioHoldings = collectAllPortfolioHoldings(command.getPortfolios());

    PceExceptionCollector collector = new PceExceptionCollector();
    ReturnsSnapshot<HoldingMonthlyReturns> portfolioSnapshot = collector.tryCatch(
        () -> getPortfolioMonthlyReturns(portfolioHoldings, returnsData.portfolio()));
    DateRange commonPerformanceDateForPortfolios = collector.tryCatch(
        () -> commonPerformanceDateFor(portfolioSnapshot));
    ReturnsSnapshot<HoldingMonthlyReturns> benchmarkSnapshot = collector.tryCatch(
        () -> getPortfolioMonthlyReturns(command.getBenchmarkHoldings(), returnsData.benchmark()));
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

  ReturnsSnapshot<HoldingMonthlyReturns> getPortfolioMonthlyReturns(List<PortfolioHolding> holdings,
      Map<PortfolioHolding, HoldingMonthlyReturns> monthlyReturns) {
    if (CollectionUtils.isEmpty(holdings)) {
      return ReturnsSnapshot.empty();
    }
    return monthlyReturnsService.getMonthlyReturnsOnlyWithReturnsValidation(holdings, monthlyReturns);
  }
}
