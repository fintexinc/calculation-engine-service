package com.fintex.ce.port.observability;

/**
 * Publishes a cache's effectiveness. A cache is registered once, when it is built, and its counters are read from then
 * on by whatever polls them — so a cache that is never used publishes no misleading zeroes and a cache that is used
 * reports without being asked again.
 *
 * <p>
 * {@link #NO_OP} exists for contexts that deliberately publish nothing, such as a unit test exercising caching
 * behaviour on its own. It is an explicit choice at the point of wiring, never a fallback substituted when the real
 * implementation is missing.
 */
public interface CacheObservability {

  CacheObservability NO_OP = (cacheName, statistics) -> {
  };

  void registerCache(String cacheName, CacheStatistics statistics);
}
