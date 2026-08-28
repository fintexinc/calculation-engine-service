package ca.tangerine.pce.application.util;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;

import static ca.tangerine.pce.application.util.CalculationUtils.sumProduct;
import static ca.tangerine.pce.application.util.CollectorUtils.toMap;
import static ca.tangerine.pce.application.util.PortfolioUtils.calculateInitialPortfolioWeight;

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
    return calculateNetProductsWithRawHoldingValues(values, holdings, types);
  }

  /**
   * Aggregates per-holding allocation maps into per-type net products using raw holding-value weights. Call this only
   * for metric families whose currency policy intentionally does not normalize values before weighting.
   */
  public static <T> Map<T, BigDecimal> calculateNetProductsWithRawHoldingValues(
      Map<PortfolioHolding, Map<T, BigDecimal>> values,
      List<PortfolioHolding> holdings,
      T[] types) {
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
