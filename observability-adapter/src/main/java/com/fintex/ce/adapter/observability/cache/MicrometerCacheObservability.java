package com.fintex.ce.adapter.observability.cache;

import com.fintex.ce.port.observability.CacheObservability;
import com.fintex.ce.port.observability.CacheStatistics;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToDoubleFunction;

/**
 * Publishes a cache's counters under the conventional {@code cache.*} meter names, tagged with the cache it describes.
 *
 * <p>
 * Hits and misses share the name {@code cache.gets} and are told apart by {@code result}, because a hit ratio is a
 * division of one by their sum: two unrelated meter names would force every dashboard to join them back together by
 * hand. The names and tags are the ones the Micrometer cache conventions define, so these meters read the same as those
 * of any other instrumented cache.
 *
 * <p>
 * Every meter is a function of the live {@link CacheStatistics} rather than a value pushed at registration time — a
 * cache reports by being read, not by remembering to tell anyone. Registered views are kept in {@link #registered}
 * because the registry holds them only weakly: a statistics view is created to be registered and often held nowhere
 * else, and once collected its meters would report zero for a cache that is working.
 */
@Component
public class MicrometerCacheObservability implements CacheObservability {

  static final String SIZE_METER_NAME = "cache.size";
  static final String GETS_METER_NAME = "cache.gets";
  static final String EVICTIONS_METER_NAME = "cache.evictions";
  static final String EVICTION_WEIGHT_METER_NAME = "cache.eviction.weight";

  static final String CACHE_TAG = "cache";
  static final String RESULT_TAG = "result";

  static final String HIT = "hit";
  static final String MISS = "miss";

  private final MeterRegistry meterRegistry;
  private final Map<String, CacheStatistics> registered = new ConcurrentHashMap<>();

  public MicrometerCacheObservability(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @Override
  public void registerCache(String cacheName, CacheStatistics statistics) {
    registered.put(cacheName, statistics);
    Gauge.builder(SIZE_METER_NAME, statistics, CacheStatistics::estimatedSize)
        .description("Entries currently held by this cache")
        .baseUnit("entries")
        .tag(CACHE_TAG, cacheName)
        .register(meterRegistry);
    countGets(cacheName, statistics, HIT, CacheStatistics::hitCount,
        "Lookups this cache served without going upstream");
    countGets(cacheName, statistics, MISS, CacheStatistics::missCount,
        "Lookups that had to go upstream, which is what this cache exists to avoid");
    count(cacheName, statistics, EVICTIONS_METER_NAME, CacheStatistics::evictionCount,
        "Entries this cache dropped to stay within its bounds");
    count(cacheName, statistics, EVICTION_WEIGHT_METER_NAME, CacheStatistics::evictionWeight,
        "Total weight of the entries this cache dropped");
  }

  private void countGets(
      String cacheName,
      CacheStatistics statistics,
      String result,
      ToDoubleFunction<CacheStatistics> value,
      String description) {
    FunctionCounter.builder(GETS_METER_NAME, statistics, value)
        .description(description)
        .tag(CACHE_TAG, cacheName)
        .tag(RESULT_TAG, result)
        .register(meterRegistry);
  }

  private void count(
      String cacheName,
      CacheStatistics statistics,
      String meterName,
      ToDoubleFunction<CacheStatistics> value,
      String description) {
    FunctionCounter.builder(meterName, statistics, value)
        .description(description)
        .tag(CACHE_TAG, cacheName)
        .register(meterRegistry);
  }
}
