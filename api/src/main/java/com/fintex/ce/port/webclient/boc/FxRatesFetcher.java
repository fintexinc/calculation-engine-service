package com.fintex.ce.port.webclient.boc;

import com.fintex.ce.model.domain.CurrencyExchangePair;
import com.fintex.ce.model.domain.calculation.DateRange;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;

public interface FxRatesFetcher {

  NavigableMap<LocalDate, BigDecimal> fetch(CurrencyExchangePair currencyPair, DateRange dateRange);

  /**
   * Returns the direction in which the underlying provider publishes {@code pair} natively. The default returns the
   * pair as-is — implementations that internally fall back to fetching the inverse direction and inverting (e.g.
   * because only one direction is configured upstream) should override this so callers can request the canonical
   * direction directly. This lets caching layers store the exact upstream value rather than a doubly-inverted
   * approximation.
   */
  default CurrencyExchangePair canonicalDirection(CurrencyExchangePair pair) {
    return pair;
  }
}
