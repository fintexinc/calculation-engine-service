package com.fintex.ce.adapter.cache.tbills;

import com.fintex.ce.adapter.cache.observability.CaffeineCacheStatistics;
import com.fintex.ce.port.observability.CacheObservability;
import com.fintex.ce.port.webclient.mic.TreasuryBillsFetcher;
import com.fintex.wm.commons.domain.currency.Currency;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.NavigableMap;
import lombok.extern.slf4j.Slf4j;

/**
 * Caching decorator over {@link TreasuryBillsFetcher}. Holds one entry per {@link Currency}; each entry is the full
 * historical rate series for that currency, refreshed after the configured TTL elapses. Concurrent callers that miss
 * the cache for the same currency converge on a single delegate call thanks to Caffeine's per-key load coalescing.
 *
 * <p>
 * The cache size is bounded to the number of supported {@link Currency} values, so the cache is effectively permanent
 * once populated; only {@code expireAfterWrite} drives evictions.
 * </p>
 *
 * <p>
 * The cache registers itself with {@link CacheObservability} so its effectiveness is reportable under
 * {@code cache=t-bills}. The dependency is required rather than optional: pass {@link CacheObservability#NO_OP} to
 * publish nothing, which is a decision made at the point of wiring instead of a consequence of a missing bean.
 * </p>
 */
@Slf4j
public class CachingTreasuryBillsFetcher implements TreasuryBillsFetcher {

  public static final String CACHE_NAME = "t-bills";

  private final TreasuryBillsFetcher delegate;
  private final Cache<Currency, NavigableMap<LocalDate, BigDecimal>> cache;

  public CachingTreasuryBillsFetcher(
      TreasuryBillsFetcher delegate,
      Duration refreshAfter,
      CacheObservability cacheObservability) {
    this.delegate = delegate;
    this.cache = Caffeine.newBuilder()
        .maximumSize(Currency.values().length)
        .expireAfterWrite(refreshAfter)
        .recordStats()
        .build();
    cacheObservability.registerCache(CACHE_NAME, new CaffeineCacheStatistics(cache));
  }

  @Override
  public NavigableMap<LocalDate, BigDecimal> fetch(Currency currency) {
    return cache.get(currency, key -> {
      log.debug("T-Bills cache miss for {} - delegating to upstream fetcher", key);
      return delegate.fetch(key);
    });
  }
}