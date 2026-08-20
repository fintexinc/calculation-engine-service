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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;

import static com.fintex.ce.model.error.ErrorCode.FX_RATES_UNAVAILABLE;
import static java.math.BigDecimal.ONE;

/**
 * Converts per-holding money values from their source currency into a single target currency using spot FX rates, so
 * that values from different currencies can be summed or weighted. Reusable across calculations — fees, MER, AUM,
 * allocations, top holdings — anything that weights or sums multi-currency holding values.
 *
 * <p>
 * Responsibility is intentionally narrow: this class fetches rates, applies conversion, and reports per-holding
 * outcomes. It does <b>not</b> decide whether a missing currency or unavailable rate is fatal — callers apply their own
 * business rules to the {@link Conversion#missingCurrency()} list and {@link Conversion#warnings()}.
 */
@Service
@RequiredArgsConstructor
public class HoldingCurrencyConverter {

  private final FxRateService fxRateService;
  private final FxProperties fxProperties;

  /**
   * Each input holding's value is converted to the configured {@link FxProperties#getDefaultTargetCurrency()}.
   * Behaviour per holding:
   * <ul>
   * <li>{@code currency} equals the target → value passed through unchanged.</li>
   * <li>{@code currency} non-null, rate available → value multiplied by the spot rate.</li>
   * <li>{@code currency} non-null, rate unavailable → value passed through and one
   * {@link com.fintex.ce.model.error.ErrorCode#FX_RATES_UNAVAILABLE} warning is added.</li>
   * <li>{@code currency} is null → holding is included in {@link Conversion#missingCurrency()} and is <i>not</i>
   * present in {@link Conversion#converted()}. Caller decides whether to error, skip, or substitute.</li>
   * <li>{@code value} is null → skipped entirely (not in {@code converted}, not in {@code missingCurrency}).</li>
   * </ul>
   */
  public Conversion convert(Map<PortfolioHolding, CurrencyValue> input) {
    return convert(input, null);
  }

  /**
   * As {@link #convert(Map)}, but into {@code targetCurrency} — the currency the caller was asked to report in. A
   * {@code null} target falls back to the configured {@link FxProperties#getDefaultTargetCurrency()}, so callers can
   * pass an optional request field straight through.
   */
  public Conversion convert(Map<PortfolioHolding, CurrencyValue> input, Currency targetCurrency) {
    Currency target = resolveTargetCurrency(targetCurrency);

    List<PortfolioHolding> missingCurrency = valued(input)
        .filter(entry -> entry.getValue().currency() == null)
        .map(Map.Entry::getKey)
        .toList();
    Map<PortfolioHolding, Currency> sourceByHolding = valued(input)
        .filter(entry -> entry.getValue().currency() != null)
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().currency()));

    Set<Currency> sourceCurrencies = new HashSet<>(sourceByHolding.values());
    Map<Currency, BigDecimal> rates = fxRateService.spotRates(sourceCurrencies, target,
        LocalDate.now(ZoneOffset.UTC));

    Map<PortfolioHolding, BigDecimal> converted = new HashMap<>();
    List<Notification> warnings = new ArrayList<>();
    for (Map.Entry<PortfolioHolding, Currency> entry : sourceByHolding.entrySet()) {
      PortfolioHolding holding = entry.getKey();
      Currency from = entry.getValue();
      BigDecimal rate = from.equals(target) ? ONE : rates.get(from);
      if (rate == null) {
        warnings.add(FX_RATES_UNAVAILABLE.toNotificationForHolding(holding, from, target));
        rate = ONE;
      }
      converted.put(holding, input.get(holding).value().multiply(rate));
    }
    return new Conversion(converted, warnings, missingCurrency);
  }

  /**
   * The currency a conversion reports in: the requested one, or the configured
   * {@link FxProperties#getDefaultTargetCurrency()} when the request omitted it. Exposed so a caller that has to tell
   * the user which default was applied (CUR-001) names the same currency the conversion used, instead of repeating the
   * fallback and drifting from it.
   */
  public Currency resolveTargetCurrency(Currency requested) {
    return requested != null ? requested : fxProperties.getDefaultTargetCurrency();
  }

  /**
   * The input entries that actually carry a money value. Entries with no {@link CurrencyValue} or a null amount are
   * nothing to convert, and are reported neither as converted nor as missing a currency.
   */
  private static Stream<Map.Entry<PortfolioHolding, CurrencyValue>> valued(
      Map<PortfolioHolding, CurrencyValue> input) {
    return input.entrySet().stream()
        .filter(entry -> entry.getValue() != null && entry.getValue().value() != null);
  }

  public record CurrencyValue(Currency currency, BigDecimal value) {
  }

  public record Conversion(
      Map<PortfolioHolding, BigDecimal> converted,
      List<Notification> warnings,
      List<PortfolioHolding> missingCurrency) {
  }
}
