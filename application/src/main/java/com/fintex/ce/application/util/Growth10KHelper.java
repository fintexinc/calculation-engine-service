package com.fintex.ce.application.util;

import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import lombok.experimental.UtilityClass;

import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.util.BigDecimalConstants.TEN_THOUSAND;
import static com.fintex.ce.util.DateTimeUtils.minusOneMonth;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;

/**
 * Pure-math helpers for the Growth-of-$10K curve shared across Growth10K, Max-Drawdown, and Mar-Ratio services.
 */
@UtilityClass
public final class Growth10KHelper {

  /**
   * Compounds a monthly return series into a Growth-of-$10K curve. The seed sits at (first-return-month − 1 month) with
   * $10,000 and each subsequent month is {@code previous × factor(entry)}, where the factor is produced by the
   * caller-supplied {@link ReturnFactorScale}. Empty when the input series is null/empty.
   */
  public static NavigableMap<LocalDate, BigDecimal> compoundGrowth10K(final NavigableMap<LocalDate, BigDecimal> returns,
      final ReturnFactorScale scale) {
    final TreeMap<LocalDate, BigDecimal> growth = new TreeMap<>();
    if (CollectionUtils.isEmpty(returns)) {
      return growth;
    }
    final LocalDate seedMonth = toLastDayOfMonth(minusOneMonth(returns.firstKey()));
    growth.put(seedMonth, TEN_THOUSAND);
    BigDecimal previous = TEN_THOUSAND;
    for (final Map.Entry<LocalDate, BigDecimal> entry : returns.entrySet()) {
      final BigDecimal factor = scale.getFormula().apply(entry);
      final BigDecimal next = toUserScale(previous.multiply(factor));
      growth.put(entry.getKey(), next);
      previous = next;
    }
    return growth;
  }
}
