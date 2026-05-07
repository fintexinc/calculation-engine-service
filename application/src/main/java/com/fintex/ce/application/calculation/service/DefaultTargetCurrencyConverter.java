package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;

import static com.fintex.ce.model.error.ErrorCode.FX_RATES_UNAVAILABLE;

/**
 * Converts per-holding money values from their source currency into the configured default target currency (see
 * {@link FxProperties#getDefaultTargetCurrency()}) using spot FX rates. Reusable across calculations — fees, MER, AUM,
 * allocations, top holdings — anything that weights or sums multi-currency holding values.
 *
 * <p>
 * Responsibility is intentionally narrow: this class fetches rates, applies conversion, and reports per-holding
 * outcomes. It does <b>not</b> decide whether a missing currency or unavailable rate is fatal — callers apply their own
 * business rules to the {@link Conversion#missingCurrency()} list and {@link Conversion#warnings()}.
 */
@Service
@RequiredArgsConstructor
public class DefaultTargetCurrencyConverter {

  private final FxRateService fxRateService;
  private final FxProperties fxProperties;

  /**
   * Each input holding's value is converted to the default target currency. Behaviour per holding:
   * <ul>
   * <li>{@code currency} equals default target → value passed through unchanged.</li>
   * <li>{@code currency} non-null, rate available → value multiplied by the spot rate.</li>
   * <li>{@code currency} non-null, rate unavailable → value passed through and one
   * {@link com.fintex.ce.model.error.ErrorCode#FX_RATES_UNAVAILABLE} warning is added.</li>
   * <li>{@code currency} is null → holding is included in {@link Conversion#missingCurrency()} and is <i>not</i>
   * present in {@link Conversion#converted()}. Caller decides whether to error, skip, or substitute.</li>
   * <li>{@code value} is null → skipped entirely (not in {@code converted}, not in {@code missingCurrency}).</li>
   * </ul>
   */
  public Conversion convert(Map<PortfolioHolding, CurrencyValue> input) {
    Currency defaultTargetCurrency = fxProperties.getDefaultTargetCurrency();
    Map<PortfolioHolding, BigDecimal> converted = new HashMap<>();
    List<PortfolioHolding> missingCurrency = new ArrayList<>();
    Map<PortfolioHolding, Currency> sourceByHolding = new HashMap<>();

    for (Map.Entry<PortfolioHolding, CurrencyValue> entry : input.entrySet()) {
      CurrencyValue cv = entry.getValue();
      if (cv == null || cv.value() == null) {
        continue;
      }
      if (cv.currency() == null) {
        missingCurrency.add(entry.getKey());
        continue;
      }
      sourceByHolding.put(entry.getKey(), cv.currency());
    }

    Set<Currency> sourceCurrencies = new HashSet<>(sourceByHolding.values());
    Map<Currency, BigDecimal> rates = fxRateService.spotRates(sourceCurrencies, defaultTargetCurrency,
        LocalDate.now(ZoneOffset.UTC));

    List<Notification> warnings = new ArrayList<>();
    for (Map.Entry<PortfolioHolding, Currency> entry : sourceByHolding.entrySet()) {
      PortfolioHolding holding = entry.getKey();
      Currency from = entry.getValue();
      BigDecimal value = input.get(holding).value();
      if (from.equals(defaultTargetCurrency)) {
        converted.put(holding, value);
        continue;
      }
      BigDecimal rate = rates.get(from);
      if (rate == null) {
        warnings.add(FX_RATES_UNAVAILABLE.toNotificationForHolding(holding, from, defaultTargetCurrency));
        converted.put(holding, value);
        continue;
      }
      converted.put(holding, value.multiply(rate));
    }
    return new Conversion(converted, warnings, missingCurrency);
  }

  public record CurrencyValue(Currency currency, BigDecimal value) {
  }

  public record Conversion(
      Map<PortfolioHolding, BigDecimal> converted,
      List<Notification> warnings,
      List<PortfolioHolding> missingCurrency) {
  }
}
