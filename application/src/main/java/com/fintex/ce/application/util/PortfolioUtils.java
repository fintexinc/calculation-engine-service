package com.fintex.ce.application.util;

import com.fintex.ce.model.domain.calculation.yield.HoldingIncomeForecast;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.id.EquitySecurityIdentifier;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

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
   * amounts produced by {@code DefaultTargetCurrencyConverter}.
   */
  public static Map<PortfolioHolding, BigDecimal> calculateInitialPortfolioWeightFromValues(
      Map<PortfolioHolding, BigDecimal> convertedValues) {
    BigDecimal sum = convertedValues.values().stream().reduce(ZERO, BigDecimal::add);
    return convertedValues.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> DecimalUtils.divide(e.getValue(), sum)));
  }

  public static void setHoldingResponseDetails(final PortfolioHolding holding,
      final HoldingIncomeForecast holdingIncomeForecast) {
    SecurityIdentifier secId = holding.getSecurityIdentifier();
    if (secId == null) {
      return;
    }

    if (secId instanceof EquitySecurityIdentifier eqId) {
      holdingIncomeForecast.setTicker(secId.getId());
      holdingIncomeForecast.setExchangeCode(eqId.getExchangeId());
    } else if (FilterUtils.CANADA_MUTUAL_PREDICATE.test(holding)) {
      holdingIncomeForecast.setFundServeCode(secId.getId());
    } else {
      holdingIncomeForecast.setIdentifier(secId.getId());
    }
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
