package com.fintex.ce.adapter.cache.fx;

import com.fintex.ce.adapter.cache.config.CacheDataProperties.FxRatesCacheProperties;
import com.fintex.ce.model.domain.CurrencyExchangePair;
import com.fintex.ce.model.domain.calculation.DateRange;

import org.apache.commons.collections4.MapUtils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

/**
 * Caffeine-backed {@link FxRatesCache} keyed by {@code (pair, date)}.
 * <p>
 * Caffeine guarantees atomic per-key reads and writes, so no explicit synchronization is required — concurrent loaders
 * for the same key converge on the same value because rates are append-only. Eviction is size-based (W-TinyLFU,
 * least-recently-used go out first).
 * <p>
 * This class is direction-agnostic: each {@link CurrencyExchangePair} occupies its own rows and the value stored is
 * exactly what the loader returned, with no transformation. Callers that want a pair and its inverse to share storage
 * (e.g. {@code USD→CAD} and {@code CAD→USD}) are responsible for canonicalizing the pair before calling — this avoids
 * double-inversion precision loss when the loader itself produces inverted values. See {@link CachingFxRatesFetcher}
 * for the canonicalization done above this layer.
 * <p>
 * Days returned empty by the upstream provider — weekends, holidays — are remembered with {@link CachedRate#ABSENT} so
 * they are not refetched on every subsequent call.
 * <p>
 * Recency window: dates within {@code recencyCutoffDays} of today are never written to the cache, so the very latest
 * rates (which the upstream provider may still be publishing) are always refetched on subsequent calls.
 */
@Slf4j
public class CaffeineFxRatesCache implements FxRatesCache {

  private final Cache<RateKey, CachedRate> rates;
  private final int recencyCutoffDays;

  /**
   * Builds a cache capped at {@link FxRatesCacheProperties#getMaxEntries()} {@code (pair, date)} rows. Each row counts
   * toward the cap regardless of whether it holds a rate or {@link CachedRate#ABSENT}.
   */
  public CaffeineFxRatesCache(FxRatesCacheProperties properties) {
    this.rates = Caffeine.newBuilder()
        .maximumSize(properties.getMaxEntries())
        .recordStats()
        .build();
    this.recencyCutoffDays = properties.getRecencyCutoffDays();
  }

  /**
   * Returns rates for {@code pair} covering the inclusive {@code range}, calling {@code loader} only for date
   * sub-ranges that have not previously been seen. Cached dates are served without invoking the loader; uncached dates
   * are grouped into contiguous gaps and loaded in one delegate call per gap. Values are stored verbatim from the
   * loader — no transformation is applied.
   */
  @Override
  public NavigableMap<LocalDate, BigDecimal> getOrLoad(
      CurrencyExchangePair pair,
      DateRange range,
      Function<DateRange, NavigableMap<LocalDate, BigDecimal>> loader) {
    LocalDate cacheableUntil = LocalDate.now().minusDays(recencyCutoffDays);
    NavigableMap<LocalDate, BigDecimal> result = new TreeMap<>();
    List<LocalDate> missingDates = collectCachedAndMissing(pair, range, cacheableUntil, result);

    for (DateRange gap : contiguousRanges(missingDates)) {
      NavigableMap<LocalDate, BigDecimal> loaded = loader.apply(gap);
      log.debug("Loaded {} FX rates for {} from delegate - missing range {}", loaded.size(), pair, gap);
      if (MapUtils.isNotEmpty(loaded)) {
        cacheGap(pair, gap, loaded, cacheableUntil);
      }
      result.putAll(loaded.subMap(gap.start(), true, gap.end(), true));
    }
    return result;
  }

  /**
   * Walks every date in {@code range} once. Cached dates are added to {@code result}; uncached dates are collected and
   * returned for the loader to fill. Dates within the recency window are treated as uncached because those rows are
   * never written.
   */
  private List<LocalDate> collectCachedAndMissing(
      CurrencyExchangePair pair,
      DateRange range,
      LocalDate cacheableUntil,
      NavigableMap<LocalDate, BigDecimal> result) {
    List<LocalDate> missing = new ArrayList<>();
    LocalDate cursor = range.start();
    while (!cursor.isAfter(range.end())) {
      CachedRate cached = cursor.isAfter(cacheableUntil) ? null : rates.getIfPresent(new RateKey(pair, cursor));
      if (cached == null) {
        missing.add(cursor);
      } else if (cached.isPresent()) {
        result.put(cursor, cached.value());
      }
      cursor = cursor.plusDays(1);
    }
    return missing;
  }

  /**
   * Persists every date in {@code gap} to the cache. Dates returned by the loader are stored under {@code pair}; dates
   * the loader did not return are stored as {@link CachedRate#ABSENT} so non-trading days are not refetched on
   * subsequent calls. Dates within the recency window ({@code cacheableUntil}) are skipped entirely.
   */
  private void cacheGap(
      CurrencyExchangePair pair,
      DateRange gap,
      NavigableMap<LocalDate, BigDecimal> loaded,
      LocalDate cacheableUntil) {
    LocalDate cursor = gap.start();
    while (!cursor.isAfter(gap.end())) {
      if (!cursor.isAfter(cacheableUntil)) {
        BigDecimal raw = loaded.get(cursor);
        rates.put(
            new RateKey(pair, cursor),
            raw == null ? CachedRate.ABSENT : new CachedRate(raw));
      }
      cursor = cursor.plusDays(1);
    }
  }

  /**
   * Collapses a chronologically ordered list of missing dates into the minimal set of contiguous inclusive
   * {@link DateRange}s. Each maximal run of consecutive days becomes one range so the loader is invoked once per gap
   * instead of once per missing date.
   */
  private static List<DateRange> contiguousRanges(List<LocalDate> dates) {
    if (dates.isEmpty()) {
      return List.of();
    }
    List<DateRange> ranges = new ArrayList<>();
    LocalDate rangeStart = dates.getFirst();
    LocalDate previous = rangeStart;
    for (int i = 1; i < dates.size(); i++) {
      LocalDate current = dates.get(i);
      if (!current.equals(previous.plusDays(1))) {
        ranges.add(new DateRange(rangeStart, previous));
        rangeStart = current;
      }
      previous = current;
    }
    ranges.add(new DateRange(rangeStart, previous));
    return ranges;
  }

  /**
   * Composite cache key. Equality follows record semantics on both fields, so {@code (USD→CAD, 2025-01-01)} and
   * {@code (CAD→USD, 2025-01-01)} occupy distinct rows.
   */
  private record RateKey(CurrencyExchangePair pair, LocalDate date) {
  }

  /**
   * Cache value wrapper that distinguishes "no rate exists for this date" ({@link #ABSENT}) from an actual rate.
   * Required because Caffeine forbids null values, and we need negative caching so non-trading days are not refetched.
   */
  private record CachedRate(BigDecimal value) {

    static final CachedRate ABSENT = new CachedRate(null);

    /**
     * Returns {@code true} if this entry holds a real rate, {@code false} if it is the negative-cache sentinel.
     */
    boolean isPresent() {
      return value != null;
    }
  }
}
