package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.model.domain.result.KeyValueResult;
import com.fintex.ce.model.domain.result.returns.AnnualReturnResult;
import com.fintex.wm.commons.error.Notification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

import static com.fintex.ce.application.util.CalculationUtils.product;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.math.BigDecimal.ONE;

@Slf4j
public class AnnualReturnCalculation {

  private final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns;
  private final List<Notification> warnings;

  public AnnualReturnCalculation(final NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns,
      final List<Notification> warnings) {
    this.portfolioTotalReturns = weightedAveragePortfolioReturns;
    this.warnings = warnings;
  }

  public AnnualReturnResult<Integer> calculate() {
    final var portfolioReturns = new TreeMap<>(portfolioTotalReturns);
    final Set<Integer> years = portfolioReturns.keySet().stream().map(LocalDate::getYear).collect(Collectors.toSet());
    final NavigableMap<Integer, BigDecimal> annualReturns = calculateAnnualReturns(portfolioReturns, years);
    final AnnualReturnResult<Integer> result = new AnnualReturnResult<>();
    result.setAnnualReturns(annualReturns.entrySet().stream().map(e -> new KeyValueResult<>(e.getKey(), e.getValue()))
        .toList());
    populateBasicDetails(result, portfolioReturns);
    result.setWarnings(warnings);
    return result;
  }

  /**
   * Calculates annual returns of the passing portfolio returns
   *
   * @param portfolioReturns
   *          calculated portfolio returns
   * @param years
   *          set of years
   * @return annual returns map
   */
  public TreeMap<Integer, BigDecimal> calculateAnnualReturns(final TreeMap<LocalDate, BigDecimal> portfolioReturns,
      final Set<Integer> years) {
    final TreeMap<Integer, BigDecimal> map = new TreeMap<>();
    for (final Integer year : years) {
      final LocalDate startDate = toLastDayOfMonth(LocalDate.of(year, Month.JANUARY, 1));
      final LocalDate endDate = toLastDayOfMonth(LocalDate.of(year, Month.DECEMBER, 1));
      if (!portfolioReturns.containsKey(startDate) || !portfolioReturns.containsKey(endDate)) {
        continue;
      }
      final NavigableMap<LocalDate, BigDecimal> subMap = portfolioReturns.subMap(startDate, true, endDate, true);
      if (subMap.size() < 12) {
        log.warn("Portfolio Returns are missing a few months between period: {} - {}", startDate, endDate);
        continue;
      }
      final BigDecimal product = product(subMap).subtract(ONE);
      map.put(year, DecimalUtils.toUserScale(product));
    }
    return map;
  }

  /**
   * Populates PSD and PED for the response
   *
   * @param result
   *          annual-return result to populate
   * @param portfolioReturns
   *          portfolio returns
   */
  public void populateBasicDetails(final AnnualReturnResult<Integer> result,
      final TreeMap<LocalDate, BigDecimal> portfolioReturns) {
    result.setPerformanceStartDate(portfolioReturns.firstKey());
    result.setPerformanceEndDate(portfolioReturns.lastKey());
  }

}
