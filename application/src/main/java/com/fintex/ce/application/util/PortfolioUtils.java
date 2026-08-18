package com.fintex.ce.application.util;

import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.id.EquitySecurityIdentifier;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;

public class PortfolioUtils {

  private PortfolioUtils() {
  }

  public static Map<PortfolioHolding, BigDecimal> calculateInitialPortfolioWeight(
      final Collection<PortfolioHolding> holdings) {
    final BigDecimal sum = holdings.stream().map(PortfolioHolding::getValue).reduce(ZERO, BigDecimal::add);
    return holdings.stream().collect(toMap(e -> e, e -> DecimalUtils.divide(e.getValue(), sum)));
  }

  /**
   * Variant for pipelines that have already normalised each holding's value into a single common currency. The map's
   * values are summed directly without re-reading {@code PortfolioHolding.value}, so callers can pass FX-converted
   * amounts produced by {@code HoldingCurrencyConverter}.
   */
  public static Map<PortfolioHolding, BigDecimal> calculateInitialPortfolioWeightFromValues(
      Map<PortfolioHolding, BigDecimal> convertedValues) {
    BigDecimal sum = convertedValues.values().stream().reduce(ZERO, BigDecimal::add);
    return convertedValues.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> DecimalUtils.divide(e.getValue(), sum)));
  }

  /**
   * Sums the given holdings' weights out of a full portfolio weight map. Used to size the portion of a breakdown
   * attributable to holdings excluded from every classification bucket (e.g. unresolved by the data source).
   */
  public static BigDecimal sumWeights(final Collection<PortfolioHolding> holdings,
      final Map<PortfolioHolding, BigDecimal> weights) {
    return holdings.stream()
        .map(weights::get)
        .filter(Objects::nonNull)
        .reduce(ZERO, BigDecimal::add);
  }

  public static <T> boolean areAllValuesInMapEmpty(final Map<PortfolioHolding, Map<T, BigDecimal>> map) {
    for (final Map.Entry<PortfolioHolding, Map<T, BigDecimal>> entry : map.entrySet()) {
      if (entry.getValue() != null && !entry.getValue().isEmpty()) {
        return false;
      }
    }
    return true;
  }

  public static <T> boolean areAllValuesZerosInMap(final Map<PortfolioHolding, Map<T, BigDecimal>> map) {
    return map.values().stream().flatMap(e -> e.values().stream())
        .allMatch(v -> v == null || v.compareTo(ZERO) == 0);
  }

  /**
   * Currency for a cash or GIC holding, read directly off the typed holding rather than a fetched MIC allocation, since
   * cash/GIC values never come from an MIC security response. Empty for any other holding type.
   */
  public static Optional<Currency> cashOrGicCurrency(final PortfolioHolding holding) {
    if (FilterUtils.CASH_PREDICATE.test(holding)) {
      return Optional.ofNullable(((CashHolding) holding).getCurrency());
    }
    if (FilterUtils.GIC_PREDICATE.test(holding)) {
      return Optional.ofNullable(((GicHolding) holding).getCurrency());
    }
    return Optional.empty();
  }

  /**
   * Currency for a holding used in a weight computation: cash/GIC holdings resolve directly off the typed holding (see
   * {@link #cashOrGicCurrency}), everything else falls back to {@code currencyOf} applied to its fetched MIC data.
   */
  public static <D> Currency currencyFor(final PortfolioHolding holding, final Map<PortfolioHolding, D> rawData,
      final Function<D, Currency> currencyOf) {
    return cashOrGicCurrency(holding)
        .or(() -> Optional.ofNullable(rawData.get(holding)).map(currencyOf))
        .orElse(null);
  }

  public static String createKey(final PortfolioHolding holding) {
    String result;
    SecurityIdentifier secId = holding.getSecurityIdentifier();

    if (FilterUtils.CASH_PREDICATE.test(holding)) {
      CashHolding cashHolding = (CashHolding) holding;
      result = cashHolding.getCurrency() != null ? cashHolding.getCurrency().name() : "";
    } else if (secId instanceof EquitySecurityIdentifier eqId) {
      result = secId.getId() + "_" + eqId.getExchangeId();
    } else {
      result = secId != null ? secId.getId() : "";
    }
    return holding.getHoldingType() + "_" + result;
  }

}
