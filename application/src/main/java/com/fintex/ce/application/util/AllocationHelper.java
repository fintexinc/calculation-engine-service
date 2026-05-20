package com.fintex.ce.application.util;

import com.fintex.ce.model.domain.holding.PortfolioHolding;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;

import static com.fintex.ce.application.util.CalculationUtils.sumProduct;
import static com.fintex.ce.application.util.CollectorUtils.toMap;
import static com.fintex.ce.application.util.PortfolioUtils.calculateInitialPortfolioWeight;

/**
 * Static helpers for breakdown-style allocation calculations. Aggregates per-holding exposures into per-type net
 * products using initial portfolio weights, or explicit caller-supplied weights when the caller has already normalized
 * (e.g., FX-adjusted CAD weights in asset allocation).
 */
@UtilityClass
public final class AllocationHelper {

  /**
   * Aggregates per-holding allocation maps into per-type net products using initial portfolio weights derived from raw
   * holding values (no currency normalization).
   */
  public static <T> Map<T, BigDecimal> calculateNetProducts(Map<PortfolioHolding, Map<T, BigDecimal>> values,
      List<PortfolioHolding> holdings, T[] types) {
    Map<PortfolioHolding, BigDecimal> weights = calculateInitialPortfolioWeight(holdings);
    return calculateNetProducts(values, weights, types);
  }

  /**
   * Aggregates per-holding allocation maps into per-type net products using the supplied per-holding weights.
   */
  public static <T> Map<T, BigDecimal> calculateNetProducts(Map<PortfolioHolding, Map<T, BigDecimal>> values,
      Map<PortfolioHolding, BigDecimal> weights, T[] types) {
    Map<T, BigDecimal> products = new HashMap<>();
    for (T type : types) {
      products.put(type, calculateNetProduct(type, values, weights));
    }
    return products;
  }

  /**
   * Net product for a single type: sum over holdings of {@code weight × allocation(type)}.
   */
  public static <T> BigDecimal calculateNetProduct(T type, Map<PortfolioHolding, Map<T, BigDecimal>> values,
      Map<PortfolioHolding, BigDecimal> weights) {
    Map<PortfolioHolding, BigDecimal> typeExposures = values.entrySet().stream()
        .filter(e -> e.getValue().containsKey(type))
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().get(type)));
    return sumProduct(typeExposures, weights);
  }
}
