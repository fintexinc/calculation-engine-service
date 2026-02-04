package com.fintex.ce.application.calculation;

import com.fintex.ce.domain.model.CommonDates;
import com.fintex.ce.application.result.Growth10KResult;
import com.fintex.ce.application.result.core.KeyValueResult;
import com.fintex.ce.domain.model.core.Warning;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import static com.fintex.ce.domain.constant.BigDecimalConstants.TEN_THOUSAND;
import static com.fintex.ce.util.DateTimeUtils.addOneMonth;
import static com.fintex.ce.util.DateTimeUtils.minusOneMonth;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static com.fintex.ce.util.DecimalUtils.toUserScale;

public class Growth10KCalculation {

  private final NavigableMap<LocalDate, BigDecimal> portfolioReturns;
  private final CommonDates commonDates;
  private final boolean calculateForNAV;
  private final List<Warning> warnings;

  public Growth10KCalculation(final NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      final CommonDates commonDates,
      final boolean calculateForNAV) {
    this.portfolioReturns = portfolioReturns;
    this.commonDates = commonDates;
    this.calculateForNAV = calculateForNAV;
    this.warnings = List.of();
  }

  public Growth10KCalculation(final NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      final CommonDates commonDates,
      final boolean calculateForNAV,
      final List<Warning> warnings) {
    this.portfolioReturns = portfolioReturns;
    this.commonDates = commonDates;
    this.calculateForNAV = calculateForNAV;
    this.warnings = warnings;
  }

  public Growth10KResult calculate() {
    final List<KeyValueResult> growth10KMap = calculateGrowth10K(portfolioReturns);
    Growth10KResult growth10KResult = new Growth10KResult();
    growth10KResult.setPed(getPortfolioEndDate(portfolioReturns));
    growth10KResult.setPsd(getPortfolioStartDate(portfolioReturns));
    growth10KResult.setGrowth10k(growth10KMap);
    growth10KResult.setWarnings(warnings);
    return growth10KResult;
  }

  public List<KeyValueResult> calculateGrowth10K(final NavigableMap<LocalDate, BigDecimal> portfolioReturns) {
    final TreeMap<LocalDate, BigDecimal> growth10K = new TreeMap<>();
    if (!CollectionUtils.isEmpty(portfolioReturns)) {
      setFirstGrowth10KValue(portfolioReturns, growth10K);
      populateGrowth10KValuesAfterLastDate(portfolioReturns, growth10K);
      if (calculateForNAV) {
        portfolioReturns.forEach((key, value) -> growth10K.put(key, toUserScale(value)));
      } else {
        calculateGrowth10K(portfolioReturns, growth10K);
      }
    }
    return growth10K.entrySet().stream()
        .map(e -> new KeyValueResult(e.getKey(), e.getValue()))
        .toList();
  }

  public void calculateGrowth10K(final NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      final NavigableMap<LocalDate, BigDecimal> growth10K) {
    portfolioReturns.entrySet().forEach(p -> growth10K.put(p.getKey(), getGrowth10KValue(growth10K, p)));
  }

  public void setFirstGrowth10KValue(final NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      final NavigableMap<LocalDate, BigDecimal> growth10K) {
    growth10K.put(toLastDayOfMonth(minusOneMonth(portfolioReturns.firstKey())), TEN_THOUSAND);
  }

  public BigDecimal getGrowth10KValue(final NavigableMap<LocalDate, BigDecimal> growth10K,
      final Map.Entry<LocalDate, BigDecimal> entry) {
    return toUserScale(growth10K.lastEntry().getValue().multiply(entry.getValue()));
  }

  /**
   * Populates growth10KValues with null when portfolioReturns last date is before portfolioEndDate
   *
   * @param portfolioReturns
   * @param growth10K
   */
  public void populateGrowth10KValuesAfterLastDate(final NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      final NavigableMap<LocalDate, BigDecimal> growth10K) {
    if (portfolioReturns.lastKey().isBefore(toLastDayOfMonth(getPortfolioEndDate(portfolioReturns)))) {
      LocalDate nextPortfolioReturnsMonth = getNextPortfolioReturnsMonth(growth10K);
      while (!nextPortfolioReturnsMonth.isAfter(getPortfolioEndDate(portfolioReturns))) {
        nextPortfolioReturnsMonth = putDefaultGrowth10KValueAndGetNextPortfolioReturnsMonth(growth10K,
            nextPortfolioReturnsMonth);
      }
    }
  }

  public LocalDate putDefaultGrowth10KValueAndGetNextPortfolioReturnsMonth(
      final NavigableMap<LocalDate, BigDecimal> growth10K,
      final LocalDate nextPortfolioReturnsMonth) {
    growth10K.put(nextPortfolioReturnsMonth, null);
    return getNextPortfolioReturnsMonth(growth10K);
  }

  public LocalDate getNextPortfolioReturnsMonth(final NavigableMap<LocalDate, BigDecimal> growth10K) {
    return toLastDayOfMonth(addOneMonth(growth10K.lastKey()));
  }

  public LocalDate getPortfolioEndDate(final NavigableMap<LocalDate, BigDecimal> portfolioReturns) {
    return Objects.nonNull(commonDates) && Objects.nonNull(commonDates.getEnd())
        ? commonDates.getEnd()
        : portfolioReturns.lastKey();
  }

  public LocalDate getPortfolioStartDate(final NavigableMap<LocalDate, BigDecimal> portfolioReturns) {
    return Objects.nonNull(commonDates) && Objects.nonNull(commonDates.getStart())
        ? commonDates.getStart()
        : portfolioReturns.firstKey();
  }

}
