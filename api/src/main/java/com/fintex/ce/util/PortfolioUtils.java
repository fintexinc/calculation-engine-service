package com.fintex.ce.util;

import com.fintex.ce.domain.dto.IncomeForecastDto;
import com.fintex.ce.domain.exception.SystemException;
import com.fintex.ce.domain.exception.code.ErrorCode;
import com.fintex.ce.domain.model.FxRates;
import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.EquitySecurityIdentifier;
import com.fintex.sm.model.domain.SecurityIdentifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;

public class PortfolioUtils {

  private PortfolioUtils() {
  }

  public static Map<Holding, BigDecimal> calculateInitialPortfolioWeight(final Collection<Holding> holdings) {
    final BigDecimal sum = holdings.stream().map(Holding::getValue).reduce(ZERO, BigDecimal::add);
    return holdings.stream().collect(toMap(e -> e, e -> DecimalUtils.divide(e.getValue(), sum)));
  }

  public static Map<Holding, Map<LocalDate, BigDecimal>> fxRatesForHoldings(
      final Map<Holding, Currency> holdings, final Currency toCurrency, final Map<LocalDate, FxRates.FxRate> fxRates) {
    return holdings.entrySet().stream().collect(toMap(Map.Entry::getKey, entry -> fxRatesForHolding(fxRates, entry
        .getValue(), toCurrency)));
  }

  public static Map<LocalDate, BigDecimal> fxRatesForHolding(final Map<LocalDate, FxRates.FxRate> fxRates,
      final Currency from, final Currency to) {
    return fxRates.entrySet().stream().collect(CollectorUtils.toTreeMap(Map.Entry::getKey, mapFxRateBasedOnCurrency(
        from, to)));
  }

  private static Function<Map.Entry<LocalDate, FxRates.FxRate>, BigDecimal> mapFxRateBasedOnCurrency(final Currency from,
      final Currency to) {
    return entry -> {
      if (Currency.USD.equals(from) && Currency.CAD.equals(to)) {
        return entry.getValue().getUsdCad();
      } else if (Currency.CAD.equals(from) && Currency.USD.equals(to)) {
        return entry.getValue().getCadUsd();
      } else
        if (Currency.CAD.equals(from) && Currency.CAD.equals(to) || Currency.USD.equals(from) && Currency.USD.equals(
            to)) {
              return ONE;
            }
      final String message = String.format("Currency exchange %s->%s not supported", from, to);
      throw new SystemException(message, ErrorCode.INTERNAL_SERVER_ERROR);
    };
  }

  public static void setHoldingResponseDetails(final Holding holding,
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

  /**
   * return true if no holdings in the portfolio contain any value
   */
  public static <T> boolean areAllValuesInMapEmpty(final Map<Holding, Map<T, BigDecimal>> map) {
    for (final Map.Entry<Holding, Map<T, BigDecimal>> entry : map.entrySet()) {
      if (entry.getValue() != null && !entry.getValue().isEmpty()) {
        return false;
      }
    }
    return true;
  }

  /**
   * return true if all holdings in the portfolio contain BigDecimal.ZERO values
   *
   * @param map
   *          contains Map with values for each Holding type
   * @param <T>
   *          generic key
   */
  public static <T> boolean areAllValuesZerosInMap(final Map<Holding, Map<T, BigDecimal>> map) {
    return map.values().stream().flatMap(e -> e.values().stream()).allMatch(v -> v.compareTo(ZERO) == 0);
  }

  public static String createKey(final Holding holding) {
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
