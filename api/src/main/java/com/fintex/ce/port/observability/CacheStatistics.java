package com.fintex.ce.port.observability;

/**
 * Live view of one cache's counters, read on demand by whoever publishes them. Every method is a fresh read rather than
 * a snapshot, because the values are polled long after the cache was registered.
 *
 * <p>
 * The contract is deliberately the vocabulary of caching rather than that of a caching library, so a cache is described
 * by what it did — served, missed, evicted — and the metrics layer never learns which library is behind it.
 */
public interface CacheStatistics {

  long estimatedSize();

  long hitCount();

  long missCount();

  long evictionCount();

  long evictionWeight();
}
