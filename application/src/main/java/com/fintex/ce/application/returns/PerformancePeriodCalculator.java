package com.fintex.ce.application.returns;

import com.fintex.ce.model.domain.holding.PortfolioHolding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;

import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
/**
 * Stateless utilities for the performance window (PSD / PED) and for trimming a per-holding returns map by date.
 *
 * <p>
 * The performance start date is the latest-starting holding's first return date — every holding has data on or after
 * it. The performance end date is the earliest-ending holding's last return date — every holding has data on or before
 * it. Together they bound the date range over which all holdings simultaneously have returns.
 * </p>
 *
 * <p>
 * The trim helpers produce a fresh outer map and a fresh {@link TreeMap} per holding so callers never share mutable
 * inner maps with the snapshot they were derived from.
 * </p>
 */
@UtilityClass
public final class PerformancePeriodCalculator {

  public static LocalDate findPerformanceStartDate(Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returnsMap) {
    return returnsMap.values()
        .stream()
        .map(series -> series.keySet().stream().min(LocalDate::compareTo))
        .filter(Optional::isPresent).map(Optional::get)
        .max(LocalDate::compareTo).orElse(null);
  }

  public static LocalDate findPerformanceEndDate(Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returnsMap) {
    return returnsMap.values()
        .stream()
        .map(series -> series.keySet().stream().max(LocalDate::compareTo))
        .filter(Optional::isPresent).map(Optional::get)
        .min(LocalDate::compareTo).orElse(null);
  }

  public static Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> trimByEndDate(
      Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> source, LocalDate endDate) {
    if (endDate == null) {
      return source;
    }
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> result = new HashMap<>(source.size());
    source.forEach((holding, series) -> result.put(holding, new TreeMap<>(series.headMap(endDate, true))));
    return result;
  }

  public static Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> trimByStartDate(
      Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> source, LocalDate startDate) {
    if (startDate == null) {
      return source;
    }
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> result = new HashMap<>(source.size());
    source.forEach((holding, series) -> result.put(holding, new TreeMap<>(series.tailMap(startDate, true))));
    return result;
  }

  /**
   * Retains the latest uninterrupted monthly period shared by every holding. This prevents a missing month in one
   * holding from being treated as a shorter, but otherwise valid, period during portfolio aggregation.
   */
  public static Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> trimToTrailingContiguousCommonMonths(
      Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> source) {
    NavigableSet<LocalDate> commonMonths = source.values().stream()
        .map(TreeMap::navigableKeySet)
        .reduce((left, right) -> {
          NavigableSet<LocalDate> intersection = new TreeSet<>(left);
          intersection.retainAll(right);
          return intersection;
        })
        .orElseGet(TreeSet::new);
    NavigableSet<LocalDate> trailingMonths = trailingContiguousMonths(commonMonths);
    return source.entrySet().stream().collect(Collectors.toMap(
        Map.Entry::getKey,
        entry -> entry.getValue().entrySet().stream()
            .filter(value -> trailingMonths.contains(value.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (first, second) -> first,
                TreeMap::new))));
  }

  private static NavigableSet<LocalDate> trailingContiguousMonths(NavigableSet<LocalDate> commonMonths) {
    if (commonMonths.isEmpty()) {
      return new TreeSet<>();
    }
    return Stream.iterate(
        commonMonths.last(),
        commonMonths::contains,
        month -> toLastDayOfMonth(month.minusMonths(1)))
        .collect(Collectors.toCollection(TreeSet::new));
  }
}
