package ca.tangerine.pce.cache.observability;

import ca.tangerine.pce.port.observability.CacheStatistics;

import com.github.benmanes.caffeine.cache.Cache;

/**
 * Exposes a Caffeine cache as a {@link CacheStatistics}, which is the only place in this module that knows the caching
 * library. Every read goes to the live cache, so the values follow it rather than a snapshot taken when the cache was
 * registered.
 *
 * <p>
 * The cache must have been built with {@code recordStats()}; without it Caffeine returns zeroes and the meters would
 * report a cache that is never hit and never missed.
 */
public final class CaffeineCacheStatistics implements CacheStatistics {

  private final Cache<?, ?> cache;

  public CaffeineCacheStatistics(Cache<?, ?> cache) {
    this.cache = cache;
  }

  @Override
  public long estimatedSize() {
    return cache.estimatedSize();
  }

  @Override
  public long hitCount() {
    return cache.stats().hitCount();
  }

  @Override
  public long missCount() {
    return cache.stats().missCount();
  }

  @Override
  public long evictionCount() {
    return cache.stats().evictionCount();
  }

  @Override
  public long evictionWeight() {
    return cache.stats().evictionWeight();
  }
}
