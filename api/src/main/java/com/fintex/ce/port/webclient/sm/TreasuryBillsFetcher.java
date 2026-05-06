package com.fintex.ce.port.webclient.sm;

import com.fintex.wm.commons.domain.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;

/**
 * Port returning the historical risk-free (Treasury bill) monthly return series for one {@link Currency}. Returns an
 * empty map when the upstream provider has no data for that currency. Caching is layered on top via the
 * {@code CachingTreasuryBillsFetcher} decorator (in {@code cache-adapter}), which keeps one entry per {@code Currency}.
 */
public interface TreasuryBillsFetcher {

  NavigableMap<LocalDate, BigDecimal> fetch(Currency currency);
}
