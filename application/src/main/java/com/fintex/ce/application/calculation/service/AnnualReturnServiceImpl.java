package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpsdAndCpedAbstractService;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.KeyValueResult;
import com.fintex.ce.model.domain.result.returns.AnnualReturnResult;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.fintex.ce.application.util.CalculationUtils.product;
import static com.fintex.ce.application.util.ReturnFactorScale.SCALE_OF_TWO;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.math.BigDecimal.ONE;

@Service
public class AnnualReturnServiceImpl
    extends
      WeightedAverageWithCpsdAndCpedAbstractService<ReturnCommand, AnnualReturnResult> {

  private final ReturnBenchmarkComparisonService returnBenchmarkComparisonService;

  public AnnualReturnServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpsdAndCpedPipeline portfolioWeightedAverageWithCpsdAndCped,
      ReturnBenchmarkComparisonService returnBenchmarkComparisonService) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCpsdAndCped);
    this.returnBenchmarkComparisonService = returnBenchmarkComparisonService;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ANNUAL_RETURNS;
  }

  @Override
  public AnnualReturnResult perform(ReturnCommand command,
      PortfolioBenchmarkReturns returnsData) {
    PeriodCalculationInput context = buildPeriodCalculationInput(command, SCALE_OF_TWO, returnsData);
    AnnualReturnResult result = buildAnnualReturnResult(context.getWeightedAveragePortfolioReturns(),
        context.getWarnings());
    if (CollectionUtils.isEmpty(command.getBenchmarkHoldings())) {
      return result;
    }

    var comparison = returnBenchmarkComparisonService.compare(
        new ReturnBenchmarkComparisonService.ReturnBenchmarkComparisonRequest<>(
            result.getAnnualReturns(),
            returnBenchmarkComparisonService.benchmarkWeightedAverage(command, returnsData, SCALE_OF_TWO),
            benchmarkWeightedAverage -> buildAnnualReturnResult(
                benchmarkWeightedAverage.weightedAverage(),
                benchmarkWeightedAverage.getErrorsAsWarnings()),
            AnnualReturnResult::getAnnualReturns));
    result.setComparison(comparison.comparison());
    result.setWarnings(returnBenchmarkComparisonService.mergeWarnings(result.getWarnings(), comparison.warnings()));
    return result;
  }

  static AnnualReturnResult buildAnnualReturnResult(NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      List<Notification> warnings) {
    TreeMap<LocalDate, BigDecimal> sortedReturns = new TreeMap<>(portfolioReturns);
    Set<Integer> years = sortedReturns.keySet().stream().map(LocalDate::getYear).collect(Collectors.toSet());
    TreeMap<Integer, BigDecimal> annualReturns = calculateAnnualReturns(sortedReturns, years);
    if (annualReturns.isEmpty() && !sortedReturns.isEmpty()) {
      throw ErrorCode.NO_COMPLETE_CALENDAR_YEAR.toException(sortedReturns.firstKey(), sortedReturns.lastKey());
    }
    AnnualReturnResult result = new AnnualReturnResult();
    result.setAnnualReturns(annualReturns.entrySet().stream()
        .map(entry -> new KeyValueResult<>(entry.getKey(), entry.getValue())).toList());
    result.setPerformanceStartDate(sortedReturns.firstKey());
    result.setPerformanceEndDate(sortedReturns.lastKey());
    result.setWarnings(warnings);
    return result;
  }

  /**
   * Throws {@link ErrorCode#INCOMPLETE_YEAR_SKIPPED} when a year has both January and December present but at least one
   * month in between is missing — annual returns are an all-or-nothing contract, so partial coverage of a bracketed
   * year aborts the calculation rather than silently omitting that year.
   */
  static TreeMap<Integer, BigDecimal> calculateAnnualReturns(TreeMap<LocalDate, BigDecimal> portfolioReturns,
      Set<Integer> years) {
    TreeMap<Integer, BigDecimal> map = new TreeMap<>();
    for (Integer year : years) {
      LocalDate startDate = toLastDayOfMonth(LocalDate.of(year, Month.JANUARY, 1));
      LocalDate endDate = toLastDayOfMonth(LocalDate.of(year, Month.DECEMBER, 1));
      if (!portfolioReturns.containsKey(startDate) || !portfolioReturns.containsKey(endDate)) {
        continue;
      }
      NavigableMap<LocalDate, BigDecimal> subMap = portfolioReturns.subMap(startDate, true, endDate, true);
      if (subMap.size() < 12) {
        throw ErrorCode.INCOMPLETE_YEAR_SKIPPED.toException(year, subMap.size());
      }
      BigDecimal product = product(subMap).subtract(ONE);
      map.put(year, DecimalUtils.toUserScale(product));
    }
    return map;
  }
}
