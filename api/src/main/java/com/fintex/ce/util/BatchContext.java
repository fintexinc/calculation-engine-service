package com.fintex.ce.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Thread-local cache that deduplicates Security Master fetcher calls within a single batch request. Call
 * {@link #begin()} at the start of a batch, {@link #end()} in a finally block, and let
 * {@code AbstractSecurityMasterFetcher} read/write via {@link #get}/{@link #put}.
 */
public final class BatchContext {

  private static final ThreadLocal<Map<String, Object>> CACHE = new ThreadLocal<>();

  private BatchContext() {
  }

  public static void begin() {
    CACHE.set(new HashMap<>());
  }

  public static void end() {
    CACHE.remove();
  }

  public static boolean isActive() {
    return CACHE.get() != null;
  }

  @SuppressWarnings("unchecked")
  public static <T> Optional<T> get(String key) {
    Map<String, Object> cache = CACHE.get();
    if (cache == null) {
      return Optional.empty();
    }
    return Optional.ofNullable((T) cache.get(key));
  }

  public static void put(String key, Object value) {
    Map<String, Object> cache = CACHE.get();
    if (cache != null) {
      cache.put(key, value);
    }
  }
}
