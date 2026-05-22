package com.fintex.ce.application.calculation.service;

import com.fintex.ce.model.domain.CurrencyExchangePair;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.exceptions.BasePceException;
import com.fintex.ce.port.webclient.boc.FxRatesFetcher;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.fintex.ce.application.util.CollectorUtils.toMap;
import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.model.error.ErrorCode.FX_RATES_UNAVAILABLE;
import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.math.BigDecimal.ONE;

/**
 * Single application-layer entry point for FX rate fetching and conversion. Wraps the {@link FxRatesFetcher} port so
 * that calling code (returns, money values, MER) does not have to coordinate transport calls and conversion separately.
 * <p>
 * Also owns return-conversion: per-holding monthly returns are converted into a common target currency using
 * end-of-month FX rates. Missing rates are non-fatal — any month-end the formula cannot resolve is dropped from that
 * holding's series, and a single {@link com.fintex.ce.model.error.ErrorCode#FX_RATES_UNAVAILABLE} {@link Notification}
 * is appended to the warnings list per affected holding. The caller surfaces those warnings on the response; the
 * calculation continues with the partial converted series.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FxRateService {

  // lookback period to ensure we don't have empty map in case of long holidays or other data misses
  // we will take the most recent rate we have from that range anyway
  private static final int SPOT_LOOKBACK_DAYS = 365;

  private final FxRatesFetcher fxRatesFetcher;

  /**
   * Returns the FX rates needed to convert each distinct source currency in {@code holdingCurrencies} into
   * {@code toCurrency} over {@code range}. Self-currency pairs are skipped. Each pair is fetched independently; a
   * transport failure for any pair is downgraded to an empty rates map for that pair, leaving the rest unaffected.
   */
  public Map<CurrencyExchangePair, NavigableMap<LocalDate, BigDecimal>> rates(
      Map<PortfolioHolding, Currency> holdingCurrencies, Currency toCurrency, DateRange range) {
    return holdingCurrencies.values().stream()
        .distinct()
        .filter(fromCurrency -> !fromCurrency.equals(toCurrency))
        .collect(Collectors.toMap(
            fromCurrency -> new CurrencyExchangePair(fromCurrency, toCurrency),
            fromCurrency -> fetchOrEmpty(new CurrencyExchangePair(fromCurrency, toCurrency), range)));
  }

  /**
   * Converts per-holding monthly returns from each holding's source currency into {@code toCurrency}. Month-ends with
   * no resolvable FX rate are dropped from that holding's series, and a single
   * {@link com.fintex.ce.model.error.ErrorCode#FX_RATES_UNAVAILABLE} {@link Notification} is appended to
   * {@code warnings} per affected holding (regardless of how many month-ends were dropped). Holdings already in
   * {@code toCurrency} pass through unchanged.
   */
  public Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> convertReturns(
      Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns,
      Map<PortfolioHolding, Currency> holdingCurrencies,
      Map<CurrencyExchangePair, NavigableMap<LocalDate, BigDecimal>> fxRates,
      Currency toCurrency,
      List<Notification> warnings) {
    return returns.entrySet().stream().collect(toMap(Map.Entry::getKey, entry -> {
      PortfolioHolding holding = entry.getKey();
      Currency fromCurrency = holdingCurrencies.get(holding);
      if (fromCurrency == null || fromCurrency.equals(toCurrency)) {
        return entry.getValue();
      }
      NavigableMap<LocalDate, BigDecimal> rates = fxRates.get(new CurrencyExchangePair(fromCurrency, toCurrency));
      TreeMap<LocalDate, BigDecimal> converted = convertAvailable(rates, entry.getValue());
      if (converted.size() < entry.getValue().size()) {
        // toNotificationForHolding auto-prepends the holding id as the first format arg (filling the leading %s
        // in "FX rates unavailable for holding %s"); we only supply the trailing currency pair.
        warnings.add(FX_RATES_UNAVAILABLE.toNotificationForHolding(holding, fromCurrency, toCurrency));
      }
      return converted;
    }));
  }

  /**
   * Returns a single FX rate from each {@code sourceCurrencies} entry into {@code targetCurrency} as of {@code asOf},
   * picking the latest published rate at or before {@code asOf}. Used by snapshot money-value conversions like the Fees
   * (annual / monthly) metric. Self-currency pairs are mapped to {@code 1}. If a pair has no rate within
   * {@link #SPOT_LOOKBACK_DAYS} days of {@code asOf} (provider outage, missing pair, or holiday cluster), the value for
   * that source currency is {@code null} — callers are expected to fall back to the original currency and emit an
   * {@link com.fintex.ce.model.error.ErrorCode#FX_RATES_UNAVAILABLE} warning, matching the behaviour of
   * {@link #convertReturns}.
   */
  public Map<Currency, BigDecimal> spotRates(Set<Currency> sourceCurrencies, Currency targetCurrency, LocalDate asOf) {
    Map<Currency, BigDecimal> result = new EnumMap<>(Currency.class);
    DateRange lookback = new DateRange(asOf.minusDays(SPOT_LOOKBACK_DAYS), asOf);
    for (Currency from : sourceCurrencies) {
      if (from == null) {
        continue;
      }
      if (from.equals(targetCurrency)) {
        result.put(from, BigDecimal.ONE);
        continue;
      }
      NavigableMap<LocalDate, BigDecimal> rates = fetchOrEmpty(new CurrencyExchangePair(from, targetCurrency),
          lookback);
      Map.Entry<LocalDate, BigDecimal> latest = rates.isEmpty() ? null : rates.floorEntry(asOf);
      result.put(from, latest != null ? latest.getValue() : null);
    }
    return result;
  }

  private NavigableMap<LocalDate, BigDecimal> fetchOrEmpty(CurrencyExchangePair pair, DateRange range) {
    try {
      NavigableMap<LocalDate, BigDecimal> rates = fxRatesFetcher.fetch(pair, range);
      return rates != null ? rates : new TreeMap<>();
    } catch (BasePceException ex) {
      log.warn("FX rates unavailable for {} over {}: {}", pair, range, ex.getMessage());
      return new TreeMap<>();
    }
  }

  private static TreeMap<LocalDate, BigDecimal> convertAvailable(
      NavigableMap<LocalDate, BigDecimal> fxRates,
      Map<LocalDate, BigDecimal> returns) {
    TreeMap<LocalDate, BigDecimal> converted = new TreeMap<>();
    if (fxRates == null || fxRates.isEmpty()) {
      return converted;
    }
    for (Map.Entry<LocalDate, BigDecimal> entry : returns.entrySet()) {
      BigDecimal value = holdingPortfolioBaseTotalReturnFormula(entry.getKey(), entry.getValue(), fxRates);
      if (value != null) {
        converted.put(entry.getKey(), value);
      }
    }
    return converted;
  }

  private static BigDecimal holdingPortfolioBaseTotalReturnFormula(LocalDate date, BigDecimal value,
      NavigableMap<LocalDate, BigDecimal> fxRates) {
    LocalDate previousDate = toLastDayOfMonth(date.minusMonths(1));
    BigDecimal fxRateValue = lookupRate(fxRates, date);
    BigDecimal previousFxValue = lookupRate(fxRates, previousDate);
    if (fxRateValue == null || previousFxValue == null) {
      return null;
    }
    // value is the source-supplied monthly return in percent form (e.g. 4.988 for 4.988%); convert to a
    // decimal (1 + r) factor, apply the FX growth ratio, then multiply by 100 to return a percent
    // value that matches the unconverted USD path's units.
    return ONE.add(divide(value, HUNDRED)).multiply(divide(fxRateValue, previousFxValue))
        .subtract(ONE).multiply(HUNDRED);
  }

  private static BigDecimal lookupRate(NavigableMap<LocalDate, BigDecimal> rates, LocalDate date) {
    Map.Entry<LocalDate, BigDecimal> entry = rates.floorEntry(date);
    return entry != null ? entry.getValue() : null;
  }
}
