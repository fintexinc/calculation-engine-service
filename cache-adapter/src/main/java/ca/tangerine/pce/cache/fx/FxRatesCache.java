package ca.tangerine.pce.cache.fx;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.function.Function;

import ca.tangerine.pce.model.domain.CurrencyExchangePair;
import ca.tangerine.pce.model.domain.calculation.DateRange;

/**
 * Range-aware cache over historical FX rates. Implementations track which date ranges have already been loaded per
 * currency pair so that sub-range queries can be served from cache and only the missing gaps delegated to the loader.
 */
public interface FxRatesCache {

  /**
   * Returns rates for {@code pair} covering the inclusive {@code range}. Any sub-ranges not previously loaded are
   * fetched via {@code loader}; already-covered sub-ranges are returned from the cache.
   */
  NavigableMap<LocalDate, BigDecimal> getOrLoad(
      CurrencyExchangePair pair,
      DateRange range,
      Function<DateRange, NavigableMap<LocalDate, BigDecimal>> loader);
}
