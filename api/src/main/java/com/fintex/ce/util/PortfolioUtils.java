package com.fintex.ce.util;

import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.IncomeForecastDto;
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

  public static void setHoldingResponseDetails(final PortfolioHolding holding,
      final IncomeForecastDto incomeForecastDTO) {
    SecurityIdentifier secId = holding.getSecurityIdentifier();
    if (secId == null) {
      return;
    }

    if (secId instanceof EquitySecurityIdentifier eqId) {
      incomeForecastDTO.setTicker(secId.getId());
      incomeForecastDTO.setExchangeCode(eqId.getExchangeId());
    } else if (FilterUtils.CANADA_MUTUAL_PREDICATE.test(holding)) {
      incomeForecastDTO.setFundServeCode(secId.getId());
    } else {
      incomeForecastDTO.setIdentifier(secId.getId());
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
    return map.values().stream().flatMap(e -> e.values().stream()).allMatch(v -> v.compareTo(ZERO) == 0);
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
