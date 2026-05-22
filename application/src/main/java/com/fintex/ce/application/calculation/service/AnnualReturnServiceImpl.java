package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.KeyValueResult;
import com.fintex.ce.model.domain.result.returns.AnnualReturnResult;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.error.Notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class AnnualReturnServiceImpl implements CalculationService<ReturnCommand, AnnualReturnResult<Integer>> {

  private final MonthlyReturnsService monthlyReturnsService;

  @Autowired
  public AnnualReturnServiceImpl(MonthlyReturnsService monthlyReturnsService) {
    this.monthlyReturnsService = monthlyReturnsService;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ANNUAL_RETURNS;
  }

  @Override
  public AnnualReturnResult<Integer> perform(ReturnCommand command) {
    MonthlyReturnsContext<HoldingMonthlyReturns> context = monthlyReturnsService
        .getPortfolioMonthlyReturns(command.getHoldings(), command.getCurrency());
    WeightedAverageResult<HoldingMonthlyReturns> weightedAverage = monthlyReturnsService
        .calculateWeightedAverageWithCpsdAndCped(context, command.getCustomPsd(), command.getCustomPed(), SCALE_OF_TWO);
    return buildAnnualReturnResult(weightedAverage.weightedAverage(), weightedAverage.getErrorsAsWarnings());
  }

  static AnnualReturnResult<Integer> buildAnnualReturnResult(NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      List<Notification> warnings) {
    TreeMap<LocalDate, BigDecimal> sortedReturns = new TreeMap<>(portfolioReturns);
    Set<Integer> years = sortedReturns.keySet().stream().map(LocalDate::getYear).collect(Collectors.toSet());
    TreeMap<Integer, BigDecimal> annualReturns = calculateAnnualReturns(sortedReturns, years);
    if (annualReturns.isEmpty() && !sortedReturns.isEmpty()) {
      throw ErrorCode.NO_COMPLETE_CALENDAR_YEAR.toException(sortedReturns.firstKey(), sortedReturns.lastKey());
    }
    AnnualReturnResult<Integer> result = new AnnualReturnResult<>();
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
