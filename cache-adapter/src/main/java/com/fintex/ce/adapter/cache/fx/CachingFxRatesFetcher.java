package com.fintex.ce.adapter.cache.fx;

import com.fintex.ce.model.domain.CurrencyExchangePair;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.port.webclient.boc.FxRatesFetcher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.fintex.ce.model.util.BigDecimalConstants.INVERSE_SCALE;
import static com.fintex.ce.model.util.BigDecimalConstants.ROUNDING_MODE;

/**
 * Caching decorator over {@link FxRatesFetcher}. On each call the cache determines which sub-ranges are already loaded
 * and delegates only the gaps to the wrapped fetcher. Unbounded queries bypass the cache and go straight to the
 * delegate — partial-range reasoning requires both endpoints.
 * <p>
 * For pairs whose canonical direction differs from the requested direction (as reported by
 * {@link FxRatesFetcher#canonicalDirection(CurrencyExchangePair)}), the cache is keyed on the canonical direction and
 * the result is inverted at this boundary. This applies generically to any pair where the upstream provider only
 * publishes one direction — without it, the delegate's own inverse-pair fallback would feed already-inverted values
 * into the cache, and a subsequent canonical-direction request would receive a doubly-inverted (slightly lossy)
 * approximation instead of the exact upstream rate.
 * <p>
 * The boundary inversion uses {@link com.fintex.ce.model.util.BigDecimalConstants#INVERSE_SCALE} with
 * {@link com.fintex.ce.model.util.BigDecimalConstants#ROUNDING_MODE} so cached and un-cached output stay bit-identical
 * with adapters that fall back to inverse pairs internally.
 */
@Slf4j
@RequiredArgsConstructor
public class CachingFxRatesFetcher implements FxRatesFetcher {

  private final FxRatesFetcher delegate;
  private final FxRatesCache cache;

  @Override
  public NavigableMap<LocalDate, BigDecimal> fetch(CurrencyExchangePair currencyPair, DateRange dateRange) {
    if (dateRange == null || !dateRange.isBounded()) {
      log.debug("Cache bypass for {} — unbounded range", currencyPair);
      return delegate.fetch(currencyPair, dateRange);
    }
    if (dateRange.start().isAfter(dateRange.end())) {
      return new TreeMap<>();
    }
    CurrencyExchangePair canonical = delegate.canonicalDirection(currencyPair);
    if (!canonical.equals(currencyPair)) {
      NavigableMap<LocalDate, BigDecimal> canonicalRates = cache.getOrLoad(
          canonical, dateRange, gap -> delegate.fetch(canonical, gap));
      return invertAll(canonicalRates);
    }
    return cache.getOrLoad(currencyPair, dateRange, gap -> delegate.fetch(currencyPair, gap));
  }

  /**
   * Inverts every rate in {@code rates} using the precision adapters apply to their inverse-pair fallback, so the
   * non-canonical-direction output of this caching wrapper matches what the un-cached delegate would have produced.
   */
  private static NavigableMap<LocalDate, BigDecimal> invertAll(NavigableMap<LocalDate, BigDecimal> rates) {
    NavigableMap<LocalDate, BigDecimal> inverted = new TreeMap<>();
    rates.forEach((date, rate) -> inverted.put(date, BigDecimal.ONE.divide(rate, INVERSE_SCALE, ROUNDING_MODE)));
    return inverted;
  }
}
