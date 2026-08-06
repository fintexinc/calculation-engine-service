package com.fintex.ce.adapter.cache.fx;

import com.fintex.ce.adapter.cache.config.CacheDataProperties.FxRatesCacheProperties;
import com.fintex.ce.model.domain.CurrencyExchangePair;
import com.fintex.ce.model.domain.calculation.DateRange;

import org.apache.commons.collections4.MapUtils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;

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
 * Past days returned empty by the upstream provider — weekends, holidays — are remembered with
 * {@link CachedRate#ABSENT} so they are not refetched on every subsequent call. Today's date is intentionally not
 * cached as ABSENT: Bank of Canada publishes the day's rate around 16:30 ET, so a request earlier in the day would
 * otherwise lock today as "no data" for the rest of the session and keep returning yesterday's rate even after BoC
 * publishes.
 */
@Slf4j
public class CaffeineFxRatesCache implements FxRatesCache {

  public static final String CACHE_NAME = "fx-rates";

  private final Cache<RateKey, CachedRate> rates;

  /**
   * Builds a cache capped at {@link FxRatesCacheProperties#getMaxEntries()} {@code (pair, date)} rows. Each row counts
   * toward the cap regardless of whether it holds a rate or {@link CachedRate#ABSENT}.
   */
  public CaffeineFxRatesCache(FxRatesCacheProperties properties) {
    this(properties, null);
  }

  /**
   * Same as {@link #CaffeineFxRatesCache(FxRatesCacheProperties)} but additionally publishes the Caffeine statistics as
   * {@code cache.*} meters tagged {@code cache=fx-rates}. A {@code null} registry disables the binding.
   */
  public CaffeineFxRatesCache(FxRatesCacheProperties properties, MeterRegistry meterRegistry) {
    this.rates = Caffeine.newBuilder()
        .maximumSize(properties.getMaxEntries())
        .recordStats()
        .build();
    if (meterRegistry != null) {
      CaffeineCacheMetrics.monitor(meterRegistry, rates, CACHE_NAME);
    }
  }

  @Override
  public NavigableMap<LocalDate, BigDecimal> getOrLoad(
      CurrencyExchangePair pair,
      DateRange range,
      Function<DateRange, NavigableMap<LocalDate, BigDecimal>> loader) {
    NavigableMap<LocalDate, BigDecimal> result = new TreeMap<>();
    List<LocalDate> missingDates = collectCachedAndMissing(pair, range, result);

    for (DateRange gap : contiguousRanges(missingDates)) {
      NavigableMap<LocalDate, BigDecimal> loaded = loader.apply(gap);
      log.debug("Loaded {} FX rates for {} from delegate - missing range {}", loaded.size(), pair, gap);
      if (MapUtils.isNotEmpty(loaded)) {
        cacheGap(pair, gap, loaded);
      }
      result.putAll(loaded.subMap(gap.start(), true, gap.end(), true));
    }
    return result;
  }

  private List<LocalDate> collectCachedAndMissing(
      CurrencyExchangePair pair,
      DateRange range,
      NavigableMap<LocalDate, BigDecimal> result) {
    List<LocalDate> missing = new ArrayList<>();
    LocalDate cursor = range.start();
    while (!cursor.isAfter(range.end())) {
      CachedRate cached = rates.getIfPresent(new RateKey(pair, cursor));
      if (cached == null) {
        missing.add(cursor);
      } else if (cached.isPresent()) {
        result.put(cursor, cached.value());
      }
      cursor = cursor.plusDays(1);
    }
    return missing;
  }

  private void cacheGap(
      CurrencyExchangePair pair,
      DateRange gap,
      NavigableMap<LocalDate, BigDecimal> loaded) {
    LocalDate today = LocalDate.now();
    LocalDate cursor = gap.start();
    while (!cursor.isAfter(gap.end())) {
      BigDecimal raw = loaded.get(cursor);
      if (raw == null && !cursor.isBefore(today)) {
        cursor = cursor.plusDays(1);
        continue;
      }
      rates.put(
          new RateKey(pair, cursor),
          raw == null ? CachedRate.ABSENT : new CachedRate(raw));
      cursor = cursor.plusDays(1);
    }
  }

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

  private record RateKey(CurrencyExchangePair pair, LocalDate date) {
  }

  private record CachedRate(BigDecimal value) {

    static final CachedRate ABSENT = new CachedRate(null);

    boolean isPresent() {
      return value != null;
    }
  }
}
